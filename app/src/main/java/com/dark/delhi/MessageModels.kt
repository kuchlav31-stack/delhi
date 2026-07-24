package com.dark.delhi

import com.google.firebase.Timestamp

data class ChatMessage(
    val senderId: String = "",
    val message: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val isRead: Boolean = false,
    val type: String = "text" ,

    val replyTo: String? = null // For Swipe-to-Reply feature
)

data class Match(
    val id: String = "",
    val name: String = "",
    val imageUrl: String = "",
    val timestamp: Timestamp = Timestamp.now()
)

data class ChatSummary(
    val chatId: String = "",
    val otherUserId: String = "",
    val otherUserName: String = "",
    val otherUserImg: String = "",
    val lastMessage: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val unreadCount: Int = 0
)