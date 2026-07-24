package com.dark.delhi

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MyProfileScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid ?: ""

    // --- Data States ---
    var userData by remember { mutableStateOf<DatingUser?>(null) }
    var likesCount by remember { mutableIntStateOf(0) }
    var matchesCount by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }

    // --- Real-time Data Fetching ---
    LaunchedEffect(userId) {
        if (userId.isEmpty()) return@LaunchedEffect

        // 1. Fetch User Profile
        db.collection("users").document(userId).get().addOnSuccessListener { doc ->
            userData = doc.toObject(DatingUser::class.java)?.copy(id = doc.id)
            isLoading = false
        }

        // 2. Fetch Real Likes Count
        db.collection("likes").document(userId).collection("myLikes").get().addOnSuccessListener {
            likesCount = it.size()
        }

        // 3. Fetch Real Matches Count
        db.collection("users").document(userId).collection("matches").get().addOnSuccessListener {
            matchesCount = it.size()
        }
    }

    Scaffold(
        containerColor = VibeTheme.DarkBg
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = VibeTheme.NeonPink)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
            ) {
                // --- 1. REAL IMAGE CAROUSEL ---
                val images = userData?.images ?: emptyList()
                val pagerState = rememberPagerState(pageCount = { images.size.coerceAtLeast(1) })

                Box(modifier = Modifier.fillMaxWidth().height(460.dp)) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        AsyncImage(
                            model = if (images.isNotEmpty()) images[page] else R.drawable.logo, // App logo if no image
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Bottom Fade Gradient
                    Box(modifier = Modifier.fillMaxSize().background(
                        Brush.verticalGradient(listOf(Color.Transparent, VibeTheme.DarkBg))
                    ))

                    // Dash Indicators (Tinder Style)
                    if (images.size > 1) {
                        Row(
                            modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp).fillMaxWidth().padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            repeat(images.size) { index ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(4.dp)
                                        .clip(CircleShape)
                                        .background(if (pagerState.currentPage == index) Color.White else Color.White.copy(0.3f))
                                )
                            }
                        }
                    }

                    // Name, Age & Badges
                    Column(
                        modifier = Modifier.align(Alignment.BottomStart).padding(24.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${userData?.name}, ${userData?.age}",
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.Verified, null, tint = VibeTheme.VerifiedBlue, modifier = Modifier.size(26.dp))
                        }
                        Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ProfileBadgeItem(userData?.favSpot ?: "Delhi", Icons.Default.LocationOn)
                            ProfileBadgeItem("${userData?.metroLine} Line", Icons.Default.DirectionsSubway, getMetroColor(userData?.metroLine ?: ""))
                        }
                    }
                }

                // --- 2. REAL STATS (Matches & Likes) ---
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    RealStatBox(likesCount.toString(), "Likes Received")
                    RealStatBox(matchesCount.toString(), "Mutual Matches")
                }

                // --- 3. THE BIO ---
                if (!userData?.bio.isNullOrBlank()) {
                    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                        SectionHeader("The Bio")
                        Text(
                            text = userData?.bio!!,
                            color = Color.White.copy(0.7f),
                            fontSize = 16.sp,
                            lineHeight = 24.sp
                        )
                    }
                }

                // --- 4. INTERESTS ---
                if (!userData?.interests.isNullOrEmpty()) {
                    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
                        SectionHeader("My Vibes")
                        FlowRow(
                            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            userData?.interests?.forEach { interest ->
                                InterestTag(interest)
                            }
                        }
                    }
                }

                // --- 5. BENTO ACTION GRID ---
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    SectionHeader("Profile Actions")
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        ProfileSquareAction(
                            "Edit Profile", Icons.Default.AutoFixHigh, VibeTheme.NeonPink, Modifier.weight(1f)
                        ) {
                            navController.navigate(Screen.EditProfile.createRoute(userId))
                        }
                        ProfileSquareAction(
                            "Logout", Icons.Default.Logout, Color.White.copy(0.5f), Modifier.weight(1f)
                        ) {
                            auth.signOut()
                            navController.navigate(Screen.Signup.route) { popUpTo(0) }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

// --- PRODUCTION COMPONENTS ---

@Composable
fun RealStatBox(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
        Text(label, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ProfileSquareAction(title: String, icon: ImageVector, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.height(100.dp).clickable { onClick() },
        color = VibeTheme.CardBg,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color.White.copy(0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
fun InterestTag(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(VibeTheme.CardBg)
            .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}

@Composable
fun ProfileBadgeItem(text: String, icon: ImageVector, color: Color = Color.White.copy(0.1f)) {
    Surface(
        color = if(color == Color.White.copy(0.1f)) Color.White.copy(0.1f) else color.copy(0.15f),
        shape = CircleShape,
        border = BorderStroke(1.dp, if(color == Color.White.copy(0.1f)) Color.White.copy(0.1f) else color.copy(0.3f))
    ) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = if(color == Color.White.copy(0.1f)) Color.White else color, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SectionHeader(text: String) {
    Text(
        text = text.uppercase(),
        color = Color.White.copy(0.4f),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp
    )
}