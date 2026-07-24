package com.dark.delhi

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

// --- 1. DATA MODEL (FIXED: Added 'type' field) ---


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(navController: NavController, otherUserId: String, otherUserName: String) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val myId = auth.currentUser?.uid ?: ""

    // --- 1. STATES FOR PROFILE DETAIL ---
    var showProfileDetail by remember { mutableStateOf(false) }
    var otherUserData by remember { mutableStateOf<DatingUser?>(null) }

    var messageText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var otherUserImg by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val chatId = if (myId < otherUserId) "${myId}_$otherUserId" else "${otherUserId}_$myId"

    // --- 2. FETCH FULL USER DATA (For BottomSheet) ---
    LaunchedEffect(otherUserId) {
        db.collection("users").document(otherUserId).get().addOnSuccessListener { doc ->
            val user = doc.toObject(DatingUser::class.java)?.copy(id = doc.id)
            otherUserData = user // Pura data save kar liya

            // Profile Pic for top bar
            val images = doc.get("images") as? List<*>
            otherUserImg = images?.get(0).toString() ?: ""
        }

        // Chat messages listener
        db.collection("chats").document(chatId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, _ ->
                messages = snap?.toObjects(ChatMessage::class.java) ?: emptyList()
            }
    }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // Navigation ki jagah state change karein
                                if (otherUserData != null) {
                                    showProfileDetail = true
                                }
                            }
                    ) {
                        AsyncImage(
                            model = otherUserImg,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(otherUserName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Online", fontSize = 11.sp, color = Color.Green)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CardBg)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {

            Column(modifier = Modifier.fillMaxSize().imePadding()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    items(messages) { msg ->
                        ChatBubble(msg, isMe = msg.senderId == myId)
                    }
                }

                if (isUploading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = NeonPink)

                ChatInputBar(
                    value = messageText,
                    onValueChange = { messageText = it },
                    onImageClick = { /* image picker code */ },
                    onSend = {
                        if (messageText.isNotBlank()) {
                            sendMessage(db, myId, otherUserId, chatId, messageText, "text", otherUserName, otherUserImg)
                            messageText = ""
                        }
                    }
                )
            }

            // --- 3. SHOW PROFILE DETAIL BOTTOM SHEET ---
            if (showProfileDetail && otherUserData != null) {
                ProfileDetailBottomSheet(
                    user = otherUserData!!,
                    onDismiss = { showProfileDetail = false }
                )
            }
        }
    }
}
@Composable
fun ChatBubble(msg: ChatMessage, isMe: Boolean) {
    val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(msg.timestamp.toDate())

    // FIXED Error: Alignment.Horizontal cast explicitly
    val alignment: Alignment.Horizontal = if (isMe) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalAlignment = alignment
    ) {
        Surface(
            color = if (isMe) NeonPink else CardBg,
            shape = RoundedCornerShape(
                topStart = 20.dp, topEnd = 20.dp,
                bottomStart = if (isMe) 20.dp else 4.dp,
                bottomEnd = if (isMe) 4.dp else 20.dp
            ),
            border = if (!isMe) BorderStroke(1.dp, Color.White.copy(0.1f)) else null
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                // Check Type: Text or Image
                if (msg.type == "image") {
                    AsyncImage(
                        model = msg.message,
                        contentDescription = null,
                        modifier = Modifier
                            .sizeIn(maxWidth = 240.dp, maxHeight = 320.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(msg.message, color = Color.White, fontSize = 15.sp)
                }

                Text(
                    text = time,
                    color = Color.White.copy(0.6f),
                    fontSize = 9.sp,
                    modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun ChatInputBar(value: String, onValueChange: (String) -> Unit, onImageClick: () -> Unit, onSend: () -> Unit) {
    Row(
        modifier = Modifier.padding(12.dp).fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onImageClick) {
            Icon(Icons.Default.AddPhotoAlternate, "Send Image", tint = NeonPink)
        }

        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text("Dilli wali baatein...", color = Color.Gray) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(28.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = CardBg,
                unfocusedContainerColor = CardBg,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier.size(48.dp).clip(CircleShape)
                .background(Brush.linearGradient(listOf(NeonPink, NeonPurple)))
                .clickable { onSend() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Send, null, tint = Color.White, modifier = Modifier.size(22.dp))
        }
    }
}
fun sendMessage(
    db: FirebaseFirestore,
    myId: String,
    otherId: String,
    chatId: String,
    text: String,
    type: String,
    otherName: String,
    otherImg: String
) {
    val message = ChatMessage(senderId = myId, message = text, timestamp = Timestamp.now(), type = type)

    // 1. Save to Chat messages
    db.collection("chats").document(chatId).collection("messages").add(message)

    // 2. Fetch My Info for Other User's Recent Chats
    db.collection("users").document(myId).get().addOnSuccessListener { snap ->
        val myName = snap.getString("name") ?: "Dilliwala"
        val images = snap.get("images") as? List<*>
        val myImg = images?.get(0).toString() ?: ""

        val displayMsg = if (type == "image") "📷 Photo" else text

        // 3. Update My Recent Chats
        val mySummary = mapOf(
            "chatId" to chatId,
            "lastMessage" to displayMsg,
            "timestamp" to Timestamp.now(),
            "otherUserId" to otherId,
            "otherUserName" to otherName,
            "otherUserImg" to otherImg
        )
        db.collection("users").document(myId).collection("recent_chats").document(otherId).set(mySummary)

        // 4. Update Other User's Recent Chats (Receiver will now see the message)
        val otherSummary = mapOf(
            "chatId" to chatId,
            "lastMessage" to displayMsg,
            "timestamp" to Timestamp.now(),
            "otherUserId" to myId,
            "otherUserName" to myName,
            "otherUserImg" to myImg
        )
        db.collection("users").document(otherId).collection("recent_chats").document(myId).set(otherSummary)
    }
}

// Logic: Image Upload to Firebase Storage
fun uploadChatImage(uri: Uri, chatId: String, myId: String, otherId: String, otherName: String, otherImg: String, onComplete: () -> Unit) {
    val storageRef = FirebaseStorage.getInstance().reference.child("chats/$chatId/${UUID.randomUUID()}.jpg")

    storageRef.putFile(uri).addOnSuccessListener {
        storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
            sendMessage(FirebaseFirestore.getInstance(), myId, otherId, chatId, downloadUrl.toString(), "image", otherName, otherImg)
            onComplete()
        }
    }.addOnFailureListener { onComplete() }
}