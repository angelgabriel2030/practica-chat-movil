package com.example.practicaexamen

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.practicaexamen.data.UserDTO
import com.example.practicaexamen.network.RetrofitClient
import com.example.practicaexamen.ui.screens.ChatScreen
import com.example.practicaexamen.ui.screens.LoginScreen
import com.example.practicaexamen.ui.theme.PracticaexamenTheme
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : ComponentActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("ChatPrefs", Context.MODE_PRIVATE)

        setContent {
            PracticaexamenTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChatApp()
                }
            }
        }
    }

    @Composable
    fun ChatApp() {
        var currentUser by remember { mutableStateOf<UserDTO?>(loadUser()) }
        if (currentUser == null) {
            LoginScreen(
                onLoginSuccess = { user ->
                    saveUser(user)
                    currentUser = user
                }
            )
        } else {
            ChatScreen(
                currentUser = currentUser!!,
                onLogout = {
                    RetrofitClient.api.logout(mapOf("user_id" to currentUser!!.id))
                        .enqueue(object : Callback<Map<String, String>> {
                            override fun onResponse(
                                call: Call<Map<String, String>>,
                                response: Response<Map<String, String>>
                            ) {
                                clearUser()
                                currentUser = null
                            }
                            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                                clearUser()
                                currentUser = null
                            }
                        })
                }
            )
        }
    }

    private fun saveUser(user: UserDTO) {
        val userJson = gson.toJson(user)
        sharedPreferences.edit()
            .putString("current_user", userJson)
            .apply()
    }

    private fun loadUser(): UserDTO? {
        val userJson = sharedPreferences.getString("current_user", null)
        return if (userJson != null) {
            gson.fromJson(userJson, UserDTO::class.java)
        } else {
            null
        }
    }

    private fun clearUser() {
        sharedPreferences.edit()
            .remove("current_user")
            .apply()
    }
}