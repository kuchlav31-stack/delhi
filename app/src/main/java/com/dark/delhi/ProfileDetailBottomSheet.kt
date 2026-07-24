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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileDetailBottomSheet(
    user: DatingUser,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val pagerState = rememberPagerState(pageCount = { user.images.size })

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = VibeTheme.DarkBg,
        scrimColor = Color.Black.copy(alpha = 0.8f),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(0.2f)) },
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // --- 1. IMAGE CAROUSEL ---
                Box(modifier = Modifier.fillMaxWidth().height(480.dp)) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxSize()
                            .then(if (user.isPrivate) Modifier.blur(25.dp) else Modifier)
                    ) { page ->
                        AsyncImage(
                            model = user.images[page],
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Top Indicators
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, start = 20.dp, end = 20.dp)
                            .align(Alignment.TopCenter),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        repeat(user.images.size) { index ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .clip(CircleShape)
                                    .background(if (pagerState.currentPage == index) Color.White else Color.White.copy(0.3f))
                            )
                        }
                    }

                    Box(modifier = Modifier.fillMaxSize().background(
                        Brush.verticalGradient(listOf(Color.Transparent, VibeTheme.DarkBg))
                    ))

                    if (user.isPrivate) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Lock, null, tint = Color.White, modifier = Modifier.size(48.dp))
                            Text("Photos are Private", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                    }
                }

                // --- 2. INFO SECTION ---
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${user.name}, ${user.age}", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.Verified, null, tint = VibeTheme.VerifiedBlue, modifier = Modifier.size(24.dp))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Using renamed helper function to avoid conflict
                        ProfileBadge(user.metroLine + " Line", Icons.Default.DirectionsSubway, VibeTheme.NeonPink)
                        ProfileBadge(user.favSpot, Icons.Default.Place, VibeTheme.NeonPurple)
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    DetailHeaderLabel("The Bio")
                    Text(
                        text = if (user.isPrivate) "Bio is hidden for private accounts." else user.bio,
                        color = Color.White.copy(0.7f),
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        modifier = Modifier.then(if (user.isPrivate) Modifier.blur(8.dp) else Modifier)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    DetailHeaderLabel("My Vibes")
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .then(if (user.isPrivate) Modifier.blur(10.dp) else Modifier),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        user.interests.forEach { interest ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(VibeTheme.CardBg)
                                    .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(interest, color = Color.White, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(150.dp))
                }
            }

            // --- 3. BOTTOM ACTION BAR ---
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
                    .padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IconButton(
                    onClick = { onDismiss() },
                    modifier = Modifier.size(56.dp).background(VibeTheme.CardBg, CircleShape)
                ) {
                    Icon(Icons.Default.Close, null, tint = Color.White)
                }

                Button(
                    onClick = { /* Handle Interaction */ },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(
                            Brush.horizontalGradient(listOf(VibeTheme.NeonPink, VibeTheme.NeonPurple))
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Favorite, null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("SEND LOVE", fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

// RENAMED Helpers to prevent "Conflicting Overloads" errors
@Composable
private fun ProfileBadge(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = CircleShape,
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DetailHeaderLabel(text: String) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}