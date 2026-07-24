package com.dark.delhi

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.Locale

// --- Theme Colors ---
val DarkBg = Color(0xFF000000)
val CardBg = Color(0xFF151517)
val NeonPink = Color(0xFFFF2D55)
val NeonPurple = Color(0xFFBC00FF)
val TextSub = Color(0xFF8E8E93)
val TextMain = Color(0xFFFFFFFF)

// --- Data Structure ---
data class UserProfileData(
    val name: String = "",
    val age: String = "",
    val gender: String = "",
    val interests: List<String> = emptyList(),
    val metroLine: String = "",
    val area: String = "",
    val images: List<Uri> = emptyList()
)

@Composable
fun OnboardingScreen(navController: NavController) {
    val context = LocalContext.current
    var currentStep by remember { mutableIntStateOf(1) }
    var userData by remember { mutableStateOf(UserProfileData()) }
    var isUploading by remember { mutableStateOf(false) }
    val totalSteps = 5

    Box(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        // Aesthetic Glows
        Box(modifier = Modifier.size(300.dp).offset(x = (-150).dp).blur(120.dp).background(NeonPink.copy(0.15f), CircleShape))
        Box(modifier = Modifier.align(Alignment.BottomEnd).size(300.dp).offset(x = 150.dp).blur(120.dp).background(NeonPurple.copy(0.15f), CircleShape))

        Column(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 24.dp)) {
            Spacer(modifier = Modifier.height(20.dp))

            // Progress Bar
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(totalSteps) { index ->
                    val isActive = index + 1 <= currentStep
                    val barWidth by animateFloatAsState(if (isActive) 1f else 0.3f, label = "")
                    Box(modifier = Modifier.weight(barWidth).height(6.dp).clip(CircleShape).background(if (isActive) NeonPink else Color.White.copy(0.1f)))
                }
            }

            // Step Content
            Box(modifier = Modifier.weight(1f).padding(vertical = 20.dp)) {
                when (currentStep) {
                    1 -> BasicInfoStep(userData) { userData = it }
                    2 -> MediaUploadStep(userData) { userData = it }
                    3 -> LocationStep(userData, { userData = it }) { currentStep++ }
                    4 -> InterestsStep(userData) { userData = it }
                    5 -> DelhiSpecificStep(userData) { userData = it }
                }
            }

            // Navigation Row
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 30.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep > 1 && !isUploading) {
                    TextButton(onClick = { currentStep-- }) {
                        Text("Back", color = TextSub, fontSize = 16.sp)
                    }
                } else {
                    Spacer(modifier = Modifier.width(10.dp))
                }

                Button(
                    onClick = {
                        if (currentStep < totalSteps) {
                            currentStep++
                        } else {
                            if (userData.images.isEmpty()) {
                                Toast.makeText(context, "Please upload at least one photo", Toast.LENGTH_SHORT).show()
                            } else {
                                isUploading = true
                                uploadData(context, userData, navController) { isUploading = false }
                            }
                        }
                    },
                    modifier = Modifier.height(56.dp).width(160.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isUploading
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(NeonPink, NeonPurple))),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isUploading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(if (currentStep == totalSteps) "Finish" else "Next", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// --- STEP 1: BASIC INFO ---
@Composable
fun BasicInfoStep(data: UserProfileData, onDataChange: (UserProfileData) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        StepHeader("About You", "Dilli ke launde aur kudiyan, apni details btao!")

        OutlinedTextField(
            value = data.name, onValueChange = { onDataChange(data.copy(name = it)) },
            label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = CardBg,
                unfocusedContainerColor = CardBg,
                focusedBorderColor = NeonPink,
                unfocusedBorderColor = Color.Gray,
                cursorColor = Color.White
            )         )

        OutlinedTextField(
            value = data.age, onValueChange = { onDataChange(data.copy(age = it)) },
            label = { Text("Age") }, modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = CardBg,
                unfocusedContainerColor = CardBg,
                focusedBorderColor = NeonPink,
                unfocusedBorderColor = Color.Gray,
                cursorColor = Color.White
            )         )

        Text("Gender", color = Color.White, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf("Male", "Female", "Other").forEach { g ->
                SelectableChip(selected = data.gender == g, text = g) { onDataChange(data.copy(gender = g)) }
            }
        }
    }
}

// --- STEP 2: MEDIA UPLOAD ---
@Composable
fun MediaUploadStep(data: UserProfileData, onDataChange: (UserProfileData) -> Unit) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { onDataChange(data.copy(images = (data.images + it).take(6))) }
    }

    Column {
        StepHeader("Dilli Wali Smile", "Upload up to 6 of your best photos.")
        Spacer(modifier = Modifier.height(20.dp))

        LazyVerticalGrid(columns = GridCells.Fixed(3), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(6) { index ->
                Box(
                    modifier = Modifier.aspectRatio(0.8f).clip(RoundedCornerShape(12.dp)).background(CardBg)
                        .clickable { if (index >= data.images.size) launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (index < data.images.size) {
                        AsyncImage(model = data.images[index], contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        IconButton(
                            onClick = { onDataChange(data.copy(images = data.images.filterIndexed { i, _ -> i != index })) },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(16.dp).background(Color.Black.copy(0.5f), CircleShape))
                        }
                    } else {
                        Icon(Icons.Default.AddCircle, null, tint = NeonPink, modifier = Modifier.size(32.dp))
                    }
                }
            }
        }
    }
}


@SuppressLint("MissingPermission")
@Composable
fun LocationStep(
    userData: UserProfileData,
    onDataChange: (UserProfileData) -> Unit,
    onNext: () -> Unit // Callback to move to next step automatically
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var locationStatus by remember { mutableStateOf("Hum aapko wahi log dikhayenge jo aapke as-pas hain.") }
    var isFetching by remember { mutableStateOf(false) }

    // Launcher to ask for permission
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            isFetching = true
            locationStatus = "Locating... Dilli mein kaha ho?"

            // Get actual coordinates
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    // Convert coordinates to Area Name (Optional but better for UI)
                    val areaName = getAreaName(context, location.latitude, location.longitude)
                    onDataChange(userData.copy(area = areaName))
                    locationStatus = "Found: $areaName"
                    isFetching = false
                    // Optionally auto-move to next step after small delay
                    onNext()
                } else {
                    locationStatus = "GPS band hai bhai! Settings se on karo."
                    isFetching = false
                }
            }.addOnFailureListener {
                locationStatus = "Error fetching location."
                isFetching = false
            }
        } else {
            locationStatus = "Permission denied. Aap search karke bhi area daal sakte ho."
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Icon(
            Icons.Default.LocationOn,
            null,
            modifier = Modifier.size(100.dp),
            tint = if (isFetching) NeonPurple else NeonPink
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            "Nearby Explorers",
            fontSize = 24.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        Text(
            locationStatus,
            color = TextSub,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // --- Action Buttons ---
        Button(
            onClick = {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            enabled = !isFetching
        ) {
            if (isFetching) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
            } else {
                Text("Allow Location Access", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Skip Button ---
        TextButton(
            onClick = {
                onDataChange(userData.copy(area = "Delhi (Not Shared)"))
                onNext()
            }
        ) {
            Text("Skip for now", color = TextSub, fontSize = 14.sp)
        }
    }
}

// Helper to get City Name from Latitude/Longitude
fun getAreaName(context: Context, lat: Double, lng: Double): String {
    return try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(lat, lng, 1)
        if (addresses?.isNotEmpty() == true) {
            val subLocality = addresses[0].subLocality // e.g. "Hauz Khas"
            val locality = addresses[0].locality // e.g. "New Delhi"
            subLocality ?: locality ?: "Delhi"
        } else {
            "Delhi"
        }
    } catch (e: Exception) {
        "Delhi"
    }
}

// --- STEP 4: INTERESTS ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InterestsStep(data: UserProfileData, onDataChange: (UserProfileData) -> Unit) {
    val interests = listOf("Momos", "CP Night Life", "Gym", "Sufi Nights", "Metro Vibes", "Gaming", "Photography", "Street Food", "Clubbing")
    Column {
        StepHeader("What's your vibe?", "Select what you enjoy doing in Delhi.")
        Spacer(modifier = Modifier.height(20.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            interests.forEach { interest ->
                val isSelected = data.interests.contains(interest)
                SelectableChip(selected = isSelected, text = interest) {
                    val newList = if (isSelected) data.interests - interest else data.interests + interest
                    onDataChange(data.copy(interests = newList))
                }
            }
        }
    }
}

// --- STEP 5: DELHI SPECIFIC ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DelhiSpecificStep(data: UserProfileData, onDataChange: (UserProfileData) -> Unit) {
    val metroLines = listOf("Yellow" to Color(0xFFFFD700), "Blue" to Color(0xFF0072BB), "Pink" to Color(0xFFFF91AF), "Red" to Color(0xFFFF0000))
    val areas = listOf("South Delhi", "CP", "Hauz Khas", "Noida", "Gurgaon", "Rohini")

    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        StepHeader("Delhi Filters", "Dilli se hai toh Dilli ki details btao!")
        Text("Nearest Metro Line", color = Color.White, fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            metroLines.forEach { (name, color) ->
                MetroPill(selected = data.metroLine == name, text = name, color = color) { onDataChange(data.copy(metroLine = name)) }
            }
        }
        Text("Hangout Area", color = Color.White, fontWeight = FontWeight.Bold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            areas.forEach { area ->
                SelectableChip(selected = data.area == area, text = area) { onDataChange(data.copy(area = area)) }
            }
        }
    }
}

// --- REUSABLE COMPONENTS ---
@Composable
fun StepHeader(title: String, subtitle: String) {
    Column {
        Text(title, fontSize = 32.sp, color = TextMain, fontWeight = FontWeight.ExtraBold)
        Text(subtitle, fontSize = 16.sp, color = TextSub)
    }
}

@Composable
fun SelectableChip(selected: Boolean, text: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() }, shape = RoundedCornerShape(12.dp),
        color = if (selected) NeonPink else CardBg,
        border = if (selected) null else BorderStroke(1.dp, Color.White.copy(0.1f))
    ) {
        Text(text, color = Color.White, modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp))
    }
}

@Composable
fun MetroPill(selected: Boolean, text: String, color: Color, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() }, shape = RoundedCornerShape(12.dp),
        color = if (selected) color else CardBg,
        border = BorderStroke(2.dp, color.copy(0.5f))
    ) {
        Text(text, color = if (selected) Color.Black else Color.White, modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), fontWeight = FontWeight.Bold)
    }
}

// --- FINAL LOGIC: UPLOAD & SAVE ---
fun uploadData(context: android.content.Context, data: UserProfileData, navController: NavController, onFail: () -> Unit) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val storage = FirebaseStorage.getInstance().reference
    val db = FirebaseFirestore.getInstance()

    // Order maintain karne ke liye Array use karenge
    val imageUrls = arrayOfNulls<String>(data.images.size)
    var uploadCount = 0

    data.images.forEachIndexed { index, uri ->
        val ref = storage.child("users/$uid/images/img_$index.jpg")

        try {
            // 1. Compression Apply karein
            val compressedBytes = uriToCompressedByteArray(context, uri)

            // 2. putBytes use karein (putFile se fast hai compressed data ke liye)
            ref.putBytes(compressedBytes).addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { downloadUri ->

                    // 3. Sahi index par URL save karein taaki order na bigde
                    imageUrls[index] = downloadUri.toString()
                    uploadCount++

                    if (uploadCount == data.images.size) {
                        // 4. Sab upload hone ke baad Firestore mein save karein
                        val profile = mapOf(
                            "name" to data.name,
                            "age" to (data.age.toIntOrNull() ?: 0),
                            "gender" to data.gender,
                            "interests" to data.interests,
                            "metroLine" to data.metroLine,
                            "area" to data.area,
                            "images" to imageUrls.filterNotNull(), // Null values hata dein
                            "profileCompleted" to true,
                            "createdAt" to com.google.firebase.Timestamp.now()
                        )

                        db.collection("users").document(uid).set(profile, com.google.firebase.firestore.SetOptions.merge())
                            .addOnSuccessListener {
                                navController.navigate("home_screen") {
                                    popUpTo("onboarding") { inclusive = true }
                                }
                            }
                            .addOnFailureListener { onFail() }
                    }
                }
            }.addOnFailureListener { onFail() }

        } catch (e: Exception) {
            onFail()
        }
    }
}

private fun uriToCompressedByteArray(context: android.content.Context, uri: android.net.Uri): ByteArray {
    val inputStream = context.contentResolver.openInputStream(uri)
    val originalBitmap = BitmapFactory.decodeStream(inputStream)
    val outputStream = ByteArrayOutputStream()

    // Quality 75 dating apps ke liye perfect hai (Size chota, Quality mast)
    originalBitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
    return outputStream.toByteArray()
}