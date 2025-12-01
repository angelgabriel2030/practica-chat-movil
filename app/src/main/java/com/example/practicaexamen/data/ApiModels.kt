package com.example.practicaexamen.data
data class UserDTO(
    val id: Long,
    val name: String,
    val email: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val user: UserDTO?
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class MessageRequest(
    val user_id: Long,
    val content: String
)

data class MessageDTO(
    val id: Long,
    val user_id: Long,
    val name: String,
    val content: String,
    val created_at: String
)
