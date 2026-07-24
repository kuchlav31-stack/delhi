package com.dark.delhi

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.dark.delhi.VibeTheme.CardBg
import com.dark.delhi.VibeTheme.DarkBg
import com.dark.delhi.VibeTheme.NeonPink
import com.dark.delhi.VibeTheme.NeonPurple
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MessagesListScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val myId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // States for data
    var matches by remember { mutableStateOf<List<Match>>(emptyList()) }
    var chatSummaries by remember { mutableStateOf<List<ChatSummary>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch Data from Firebase
    LaunchedEffect(myId) {
        if (myId.isEmpty()) return@LaunchedEffect

        // 1. Fetch Mutual Matches (New matches without chat history)
        db.collection("users").document(myId).collection("matches")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                matches = snap?.toObjects(Match::class.java) ?: emptyList()
            }

        // 2. Fetch Recent Chats (People you are already talking to)
        db.collection("users").document(myId).collection("recent_chats")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                chatSummaries = snap?.toObjects(ChatSummary::class.java) ?: emptyList()
                isLoading = false
            }
    }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            Column(modifier = Modifier.statusBarsPadding()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Messages",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    IconButton(
                        onClick = { /* Search logic */ },
                        modifier = Modifier.background(CardBg, CircleShape)
                    ) {
                        Icon(Icons.Default.Search, null, tint = Color.White)
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // --- SECTION 1: NEW MATCHES (Horizontal) ---
            if (matches.isNotEmpty()) {
                Text(
                    "New Matches",
                    color = NeonPink,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp),
                    fontSize = 14.sp
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(matches) { match ->
                        NewMatchCircle(match) {
                            navController.navigate("chat_room/${match.id}/${match.name}")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // --- SECTION 2: CHATS (Vertical) ---
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = CardBg,
                shape = RoundedCornerShape(topStart = 35.dp, topEnd = 35.dp)
            ) {
                if (isLoading) {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        CircularProgressIndicator(color = NeonPink)
                    }
                } else if (chatSummaries.isEmpty()) {
                    EmptyMessagesView()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(top = 15.dp),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        items(chatSummaries) { chat ->
                            ChatRowItem(chat) {
                                navController.navigate("chat_room/${chat.otherUserId}/${chat.otherUserName}")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NewMatchCircle(match: Match, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(75.dp)
                .clip(CircleShape)
                .border(2.dp, Brush.linearGradient(listOf(NeonPink, NeonPurple)), CircleShape)
                .padding(3.dp)
        ) {
            AsyncImage(
                model = match.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(CircleShape)
            )
        }
        Text(
            match.name.split(" ")[0],
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

@Composable
fun ChatRowItem(chat: ChatSummary, onClick: () -> Unit) {
    val time = try {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(chat.timestamp.toDate())
    } catch (e: Exception) {
        ""
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // User Profile Pic
        AsyncImage(
            model = chat.otherUserImg,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(65.dp).clip(CircleShape)
        )

        // Text Content
        Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
            Text(
                chat.otherUserName,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                chat.lastMessage,
                color = Color.Gray,
                fontSize = 14.sp,
                maxLines = 1,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // Time and Unread Badge
        Column(horizontalAlignment = Alignment.End) {
            Text(time, color = Color.Gray, fontSize = 11.sp)
            if (chat.unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .size(22.dp)
                        .background(NeonPink, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        chat.unreadCount.toString(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyMessagesView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.ChatBubbleOutline,
            contentDescription = null,
            tint = Color.White.copy(0.1f),
            modifier = Modifier.size(100.dp)
        )
        Text(
            "No conversations yet",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Text(
            "Dilli hai, dil bda rakho aur swipe karo!",
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}