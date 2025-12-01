package com.example.practicaexamen.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.practicaexamen.data.MessageDTO
import com.example.practicaexamen.data.MessageRequest
import com.example.practicaexamen.data.UserDTO
import com.example.practicaexamen.network.RetrofitClient
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(currentUser: UserDTO, onLogout: () -> Unit) {
    var messages by remember { mutableStateOf<List<MessageDTO>>(emptyList()) }
    var messageText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isSending by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    fun CargarMensajes() {
        isLoading = true
        errorMessage = null

        RetrofitClient.api.getMessages().enqueue(object : Callback<List<MessageDTO>> {
            override fun onResponse(call: Call<List<MessageDTO>>, response: Response<List<MessageDTO>>) {
                isLoading = false
                if (response.isSuccessful) {
                    messages = response.body() ?: emptyList()
                    scope.launch {
                        if (messages.isNotEmpty()) {
                            listState.animateScrollToItem(messages.size - 1)
                        }
                    }
                } else {
                    errorMessage = "Error al cargar mensajes: ${response.code()}"
                }
            }

            override fun onFailure(call: Call<List<MessageDTO>>, t: Throwable) {
                isLoading = false
                errorMessage = "Error de conexion: ${t.message}"
            }
        })
    }
    LaunchedEffect(Unit) {
        CargarMensajes()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Chat - ${currentUser.name}",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6200EE),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Cerrar Sesion",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { CargarMensajes() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Recargar Mensajes",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5F5))
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (isLoading && messages.isEmpty()) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF6200EE)
                    )
                } else if (messages.isEmpty()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "📭",
                            fontSize = 48.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            "No hay mensajes",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF666666)
                        )
                        Text(
                            "Escribe algo",
                            fontSize = 14.sp,
                            color = Color(0xFF999999),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        items(messages) { message ->
                            Mensaje(
                                message = message,
                                isCurrentUser = message.user_id == currentUser.id
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
            if (errorMessage != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFFFEBEE)
                ) {
                    Text(
                        text = "⚠️ $errorMessage",
                        color = Color(0xFFC62828),
                        modifier = Modifier.padding(12.dp),
                        fontSize = 14.sp
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(
                                "Escribe un mensaje.........",
                                color = Color(0xFF999999)
                            )
                        },
                        enabled = !isSending,
                        maxLines = 3,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6200EE),
                            unfocusedBorderColor = Color(0xFFCCCCCC)
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    FloatingActionButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                isSending = true
                                errorMessage = null

                                val request = MessageRequest(
                                    user_id = currentUser.id,
                                    content = messageText.trim()
                                )

                                RetrofitClient.api.sendMessage(request).enqueue(object : Callback<MessageDTO> {
                                    override fun onResponse(call: Call<MessageDTO>, response: Response<MessageDTO>) {
                                        isSending = false
                                        if (response.isSuccessful) {
                                            messageText = ""
                                            CargarMensajes()
                                        } else {
                                            errorMessage = "Error al enviar mensaje"
                                        }
                                    }

                                    override fun onFailure(call: Call<MessageDTO>, t: Throwable) {
                                        isSending = false
                                        errorMessage = "Error de conexion"
                                    }
                                })
                            }
                        },
                        containerColor = Color(0xFF6200EE),
                        modifier = Modifier.size(56.dp)
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Enviar",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Mensaje(message: MessageDTO, isCurrentUser: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 280.dp),
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (isCurrentUser) 20.dp else 4.dp,
                bottomEnd = if (isCurrentUser) 4.dp else 20.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isCurrentUser)
                    Color(0xFF6200EE)
                else
                    Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp)
            ) {
                if (!isCurrentUser) {
                    Text(
                        text = message.name ?: "Usuario",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6200EE),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                Text(
                    text = message.content,
                    fontSize = 16.sp,
                    color = if (isCurrentUser) Color.White else Color(0xFF333333),
                    lineHeight = 22.sp
                )
                Text(
                    text = message.created_at,
                    fontSize = 11.sp,
                    color = if (isCurrentUser) Color(0xFFE1BEE7) else Color(0xFF999999),
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}