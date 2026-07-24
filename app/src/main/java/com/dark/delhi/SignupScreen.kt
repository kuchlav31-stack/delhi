package com.dark.delhi

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SignupScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(VibeTheme.DarkBg)) {

        // --- Animated Aesthetic Glows ---
        Box(modifier = Modifier.size(400.dp).offset(y = (-200).dp, x = (-100).dp).blur(150.dp).background(VibeTheme.NeonPink.copy(0.15f), CircleShape))
        Box(modifier = Modifier.align(Alignment.BottomEnd).size(400.dp).offset(y = 200.dp, x = 100.dp).blur(150.dp).background(VibeTheme.NeonPurple.copy(0.15f), CircleShape))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 30.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // --- Header ---
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                Text(
                    text = "Create Account",
                    fontSize = 42.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1.5).sp
                )
                Text(
                    text = "Join the community and find your vibe in Delhi.",
                    fontSize = 15.sp,
                    color = Color.White.copy(0.6f),
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(50.dp))

            // --- Fields with Keyboard Actions ---
            SignupInputField(
                value = name,
                onValueChange = { name = it },
                label = "Full Name",
                icon = Icons.Default.Person,
                imeAction = ImeAction.Next,
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )

            Spacer(modifier = Modifier.height(16.dp))

            SignupInputField(
                value = email,
                onValueChange = { email = it },
                label = "Email Address",
                icon = Icons.Default.Email,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )

            Spacer(modifier = Modifier.height(16.dp))

            SignupInputField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                icon = Icons.Default.Lock,
                isPassword = true,
                passwordVisible = passwordVisible,
                onPasswordToggle = { passwordVisible = !passwordVisible },
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
            )

            Spacer(modifier = Modifier.height(40.dp))

            // --- Signup Button ---
            Button(
                onClick = {
                    if (name.isBlank() || email.isBlank() || password.length < 6) {
                        Toast.makeText(context, "Please enter valid details (Password: Min 6 chars)", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isLoading = true
                    auth.createUserWithEmailAndPassword(email.trim(), password.trim())
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val userId = auth.currentUser?.uid ?: ""
                                val userMap = hashMapOf(
                                    "uid" to userId,
                                    "name" to name.trim(),
                                    "email" to email.trim(),
                                    "city" to "Delhi",
                                    "profileCompleted" to false, // Crucial for onboarding check
                                    "images" to emptyList<String>(), // Avoids crash in HomeScreen
                                    "bio" to "",
                                    "interests" to emptyList<String>(),
                                    "createdAt" to System.currentTimeMillis()
                                )

                                db.collection("users").document(userId).set(userMap)
                                    .addOnSuccessListener {
                                        isLoading = false
                                        navController.navigate(Screen.Onboarding.route) {
                                            popUpTo(Screen.Signup.route) { inclusive = true }
                                        }
                                    }
                            } else {
                                isLoading = false
                                Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(),
                enabled = !isLoading
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().background(VibeTheme.PrimaryGradient),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text("CONTINUE", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // --- Footer Navigation ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Already a member?", color = Color.White.copy(0.6f))
                TextButton(onClick = { navController.navigate(Screen.Login.route) }) {
                    Text("Login Now", color = VibeTheme.NeonPink, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SignupInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordToggle: (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    var isFocused by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = if (isFocused) VibeTheme.NeonPink else Color.White.copy(0.4f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(VibeTheme.CardBg.copy(0.8f))
                .onFocusChanged { isFocused = it.isFocused },
            leadingIcon = { Icon(icon, null, tint = if (isFocused) VibeTheme.NeonPink else Color.White.copy(0.3f)) },
            trailingIcon = {
                if (isPassword && onPasswordToggle != null) {
                    IconButton(onClick = onPasswordToggle) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = Color.White.copy(0.3f)
                        )
                    }
                }
            },
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            keyboardActions = keyboardActions,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = VibeTheme.NeonPink,
                unfocusedBorderColor = Color.White.copy(0.05f),
                cursorColor = VibeTheme.NeonPink
            ),
            shape = RoundedCornerShape(16.dp)
        )
    }
}