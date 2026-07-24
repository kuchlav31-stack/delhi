package com.dark.delhi

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid ?: ""
    val context = LocalContext.current

    val usersList = remember { mutableStateListOf<DatingUser>() }
    var isLoading by remember { mutableStateOf(true) }
    var selectedUserForDetail by remember { mutableStateOf<DatingUser?>(null) }
    var hasNewMessages by remember { mutableStateOf(false) }

    // --- Data Fetching ---
    LaunchedEffect(Unit) {
        db.collection("users").limit(20).get().addOnSuccessListener { snapshot ->
            val fetched = snapshot.documents.mapNotNull { it.toObject(DatingUser::class.java)?.copy(id = it.id) }
            usersList.clear()
            usersList.addAll(fetched.filter { it.id != currentUserId })
            isLoading = false
        }

        db.collection("users").document(currentUserId).collection("recent_chats")
            .whereGreaterThan("unreadCount", 0)
            .addSnapshotListener { snap, _ ->
                hasNewMessages = !(snap?.isEmpty ?: true)
            }
    }

    Scaffold(
        containerColor = VibeTheme.DarkBg,
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(VibeTheme.DarkBg)
        ) {
            // Background Aesthetic Glows
            Box(modifier = Modifier.size(400.dp).offset(x = (-150).dp, y = (-100).dp).blur(120.dp).background(VibeTheme.NeonPink.copy(0.12f), CircleShape))
            Box(modifier = Modifier.align(Alignment.BottomEnd).size(300.dp).offset(x = 100.dp).blur(100.dp).background(VibeTheme.NeonPurple.copy(0.1f), CircleShape))

            Column(modifier = Modifier.fillMaxSize()) {
                // 1. Header
                HomeHeader(navController, hasNewMessages)

                // 2. Card Stack
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    if (isLoading) {
                        CircularProgressIndicator(color = VibeTheme.NeonPink)
                    } else if (usersList.isEmpty()) {
                        EmptyDiscoveryView {
                            // Refresh Logic
                            isLoading = true
                            db.collection("users").limit(20).get().addOnSuccessListener { /* re-fetch */ }
                        }
                    } else {
                        val visibleCards = usersList.takeLast(3)
                        visibleCards.forEachIndexed { index, user ->
                            val isTopCard = index == visibleCards.size - 1
                            key(user.id) {
                                SwipeCard(
                                    user = user,
                                    isDraggable = isTopCard,
                                    onSwipeLeft = { usersList.remove(user) },
                                    onSwipeRight = {
                                        handleMatchLogic(db, currentUserId, user.id)
                                        usersList.remove(user)
                                        Toast.makeText(context, "Liked ${user.name}", Toast.LENGTH_SHORT).show()
                                    },
                                    onClick = { selectedUserForDetail = user }
                                )
                            }
                        }
                    }
                }

                // 3. Action Buttons
                ActionButtonsRow(
                    onPass = { if (usersList.isNotEmpty()) usersList.remove(usersList.last()) },
                    onDirectMsg = {
                        usersList.lastOrNull()?.let {
                            navController.navigate("chat_room/${it.id}/${it.name}")
                        }
                    }
                )

                Spacer(modifier = Modifier.navigationBarsPadding().height(24.dp))
            }

            // Detail BottomSheet
            if (selectedUserForDetail != null) {
                ProfileDetailBottomSheet(
                    user = selectedUserForDetail!!,
                    onDismiss = { selectedUserForDetail = null }
                )
            }
        }
    }
}

@Composable
fun SwipeCard(
    user: DatingUser,
    isDraggable: Boolean,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onClick: () -> Unit
) {
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val scale by animateFloatAsState(if (isDraggable) 1f else 0.88f, label = "scale")

    Box(
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .aspectRatio(0.72f)
            .graphicsLayer {
                translationX = offsetX.value
                rotationZ = offsetX.value / 25f
                scaleX = scale
                scaleY = scale
            }
            .pointerInput(isDraggable) {
                if (!isDraggable) return@pointerInput
                detectDragGestures(
                    onDragEnd = {
                        scope.launch {
                            if (offsetX.value > 450f) {
                                offsetX.animateTo(1200f, tween(400))
                                onSwipeRight()
                            } else if (offsetX.value < -450f) {
                                offsetX.animateTo(-1200f, tween(400))
                                onSwipeLeft()
                            } else {
                                offsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                            }
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        scope.launch { offsetX.snapTo(offsetX.value + dragAmount.x) }
                    }
                )
            }
            .clip(RoundedCornerShape(32.dp))
            .border(1.dp, Color.White.copy(0.08f), RoundedCornerShape(32.dp))
            .background(VibeTheme.CardBg)
            .clickable(enabled = isDraggable) { onClick() }
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(user.images.firstOrNull())
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = if (isDraggable) 1f else 0.5f
        )

        // Stamps
        if (offsetX.value > 150f) Stamp("LIKE", Color.Green, Alignment.TopStart, -15f)
        if (offsetX.value < -150f) Stamp("NOPE", Color.Red, Alignment.TopEnd, 15f)

        Box(modifier = Modifier.fillMaxSize().background(VibeTheme.GlassOverlay))

        Column(modifier = Modifier.align(Alignment.BottomStart).padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${user.name}, ${user.age}", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.Verified, null, tint = VibeTheme.VerifiedBlue, modifier = Modifier.size(24.dp))
            }

            Row(modifier = Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).background(getMetroColor(user.metroLine), CircleShape))
                Text(user.metroLine + " Line", color = Color.White.copy(0.8f), fontSize = 14.sp, modifier = Modifier.padding(start = 6.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Icon(Icons.Default.Place, null, tint = VibeTheme.NeonPink, modifier = Modifier.size(16.dp))
                Text(user.favSpot, color = Color.White.copy(0.8f), fontSize = 14.sp, modifier = Modifier.padding(start = 4.dp))
            }
        }
    }
}

@Composable
fun HomeHeader(navController: NavController, hasNewMessages: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Dill-E",
                style = TextStyle(
                    brush = Brush.horizontalGradient(listOf(VibeTheme.NeonPink, VibeTheme.NeonPurple)),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black
                )
            )
            Text("Discover Delhi Singles", color = Color.Gray, fontSize = 12.sp)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.TopEnd) {
                // HomeScreen.kt ke andar HomeHeader function mein:
                IconButton(
                    onClick = {
                        // CRASH FIX: Ensure this route "messages_list" is defined in NavHost
                        navController.navigate("messages_list")
                    },
                    modifier = Modifier.size(48.dp).background(VibeTheme.CardBg, CircleShape)
                ) {
                    Icon(Icons.Outlined.ChatBubbleOutline, null, tint = Color.White)
                }
                if (hasNewMessages) {
                    Box(modifier = Modifier.size(12.dp).background(VibeTheme.NeonPink, CircleShape).border(2.dp, VibeTheme.DarkBg, CircleShape))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            IconButton(onClick = { navController.navigate("profile_screen") }, modifier = Modifier.size(48.dp).background(VibeTheme.CardBg, CircleShape).border(1.dp, Color.White.copy(0.1f), CircleShape)) {
                Icon(Icons.Default.Person, null, tint = Color.White)
            }
        }
    }
}

@Composable
fun ActionButtonsRow(onPass: () -> Unit, onDirectMsg: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 45.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPass, modifier = Modifier.size(64.dp).background(VibeTheme.CardBg, CircleShape).border(1.dp, Color.White.copy(0.1f), CircleShape)) {
            Icon(Icons.Default.Close, null, tint = Color.White.copy(0.7f), modifier = Modifier.size(32.dp))
        }

        Button(
            onClick = onDirectMsg,
            modifier = Modifier.size(85.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize().background(VibeTheme.PrimaryGradient), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.ChatBubble, null, tint = Color.White, modifier = Modifier.size(34.dp))
            }
        }

        IconButton(onClick = { }, modifier = Modifier.size(64.dp).background(VibeTheme.CardBg, CircleShape).border(1.dp, Color.White.copy(0.1f), CircleShape)) {
            Icon(Icons.Default.Favorite, null, tint = VibeTheme.NeonPink, modifier = Modifier.size(30.dp))
        }
    }
}

@Composable
fun Stamp(text: String, color: Color, alignment: Alignment, rotation: Float) {
    Box(modifier = Modifier.fillMaxSize().padding(30.dp), contentAlignment = alignment) {
        Text(
            text = text,
            color = color,
            fontSize = 36.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier
                .border(4.dp, color, RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .graphicsLayer { rotationZ = rotation }
        )
    }
}

@Composable
fun EmptyDiscoveryView(onRefresh: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.Radar, null, modifier = Modifier.size(100.dp), tint = VibeTheme.NeonPink.copy(0.3f))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Dilli is sleeping...", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        TextButton(onClick = onRefresh) { Text("Search Again", color = VibeTheme.NeonPink) }
    }
}

// --- Logic Helpers ---

fun handleMatchLogic(db: FirebaseFirestore, myId: String, targetId: String) {
    db.collection("users").document(myId).collection("likes").document(targetId)
        .set(mapOf("timestamp" to com.google.firebase.Timestamp.now()))
}
//
//fun getMetroColor(line: String): Color {
//    return when {
//        line.contains("Yellow", true) -> Color(0xFFFFD700)
//        line.contains("Blue", true) -> Color(0xFF0072BB)
//        line.contains("Pink", true) -> Color(0xFFFF91AF)
//        line.contains("Red", true) -> Color(0xFFFF0000)
//        line.contains("Violet", true) -> Color(0xFF8A2BE2)
//        else -> Color.Gray
//    }
//}