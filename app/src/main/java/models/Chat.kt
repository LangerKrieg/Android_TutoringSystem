package models

data class Chat(
    val id: String,
    val senderName: String,
    val lastMessage: String,
    val hasNewMessages: Boolean
)
