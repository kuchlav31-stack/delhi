package com.dark.delhi

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@Composable
fun DilliDatingSplashScreen(navController: NavController) {
    // --- 1. Animation States ---
    val scale = remember { Animatable(0.7f) }
    val alpha = remember { Animatable(0f) }

    // --- 2. Logic & Navigation ---
    LaunchedEffect(Unit) {
        // Logo Entrance: Bouncy Scale and Fade-in
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        alpha.animateTo(1f, tween(800))

        delay(500) // 2 Seconds hold time

        // Firebase Auth Check
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // User logged in hai -> Home par jao
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        } else {
            // No user -> Signup par jao
            navController.navigate(Screen.Signup.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        }
    }

    // --- 3. UI Layout ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VibeTheme.DarkBg), // Consistent theme background
        contentAlignment = Alignment.Center
    ) {
        // --- Aesthetic Glow behind the Logo ---
        Box(
            modifier = Modifier
                .size(280.dp)
                .blur(100.dp)
                .background(
                    brush = Brush.radialGradient(
                        listOf(VibeTheme.NeonPink.copy(0.2f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
        ) {
            // --- India Gate / App Logo ---
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Dill-E Logo",
                modifier = Modifier.size(240.dp), // Adjust size as needed
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- App Name ---
//            Text(
//                text = "DilliDate",
//                color = Color.White,
//                fontSize = 42.sp,
//                fontWeight = FontWeight.Black,
//                letterSpacing = (-1).sp
//            )
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            // --- Tagline ---
//            Text(
//                text = "दिल से दिल्ली तक",
//                color = Color.White.copy(alpha = 0.6f),
//                fontSize = 16.sp,
//                fontWeight = FontWeight.Normal,
//                letterSpacing = 2.sp
//            )
        }

        // --- Bottom Version Text ---
        Text(
            text = "v1.0 • Made for Dilli",
            color = Color.White.copy(alpha = 0.2f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp),
            letterSpacing = 1.sp
        )
    }
}