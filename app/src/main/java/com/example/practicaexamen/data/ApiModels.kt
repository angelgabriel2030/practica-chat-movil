package com.example.practicaexamen.data
data class UserDTO(
    val id: Long,
    val username: String,
    val email: String?
)

data class MessageDTO(
    val id: Long,
    val user_id: Long,
    val username: String,
    val content: String,
    val created_at: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val user: UserDTO?
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class MessageRequest(
    val user_id: Long,
    val content: String
)