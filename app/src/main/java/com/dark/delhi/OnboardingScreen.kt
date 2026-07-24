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
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.*

// --- Updated Data Class for Richer Profiles ---
data class OnboardingData(
    var name: String = "",
    var age: String = "",
    var gender: String = "",
    var bio: String = "",
    var favSpot: String = "",
    var metroLine: String = "",
    var area: String = "",
    var goal: String = "", // Relationship goals
    var interests: List<String> = emptyList(),
    var images: List<Uri> = emptyList()
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(navController: NavController) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var currentStep by remember { mutableIntStateOf(1) }
    var userData by remember { mutableStateOf(OnboardingData()) }
    var isUploading by remember { mutableStateOf(false) }
    val totalSteps = 6

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VibeTheme.DarkBg)
            .navigationBarsPadding() // FIX: Prevents phone home/back overlap
            .statusBarsPadding()    // FIX: Prevents clock/notch overlap
    ) {
        // --- High-End Aesthetic Glows ---
        Box(modifier = Modifier.size(350.dp).offset(x = (-150).dp, y = (-100).dp).blur(120.dp).background(VibeTheme.NeonPink.copy(0.12f), CircleShape))
        Box(modifier = Modifier.align(Alignment.BottomEnd).size(300.dp).offset(x = 100.dp, y = 100.dp).blur(100.dp).background(VibeTheme.NeonPurple.copy(0.1f), CircleShape))

        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
            Spacer(modifier = Modifier.height(20.dp))

            // --- Custom Pager Progress Bar ---
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(totalSteps) { index ->
                    val isActive = index + 1 <= currentStep
                    val barWidth by animateFloatAsState(if (isActive) 1f else 0.3f, label = "")
                    Box(modifier = Modifier.weight(barWidth).height(6.dp).clip(CircleShape).background(if (isActive) VibeTheme.NeonPink else Color.White.copy(0.1f)))
                }
            }

            // --- Animated Step Container ---
            Box(modifier = Modifier.weight(1f).padding(vertical = 20.dp)) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            slideInHorizontally { it } + fadeIn() with slideOutHorizontally { -it } + fadeOut()
                        } else {
                            slideInHorizontally { -it } + fadeIn() with slideOutHorizontally { it } + fadeOut()
                        }.using(SizeTransform(clip = false))
                    }, label = ""
                ) { step ->
                    when (step) {
                        1 -> BasicBioStep(userData, { userData = it }, focusManager)
                        2 -> GoalsStep(userData) { userData = it }
                        3 -> DetailedMediaStep(userData) { userData = it }
                        4 -> LiveLocationStep(userData, { userData = it }) { currentStep++ }
                        5 -> PremiumInterestsStep(userData) { userData = it }
                        6 -> DelhiSpecificStep(userData) { userData = it }
                    }
                }
            }

            // --- Navigation Bar ---
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 30.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep > 1 && !isUploading) {
                    IconButton(onClick = { currentStep-- }, modifier = Modifier.size(56.dp).background(VibeTheme.CardBg, CircleShape)) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                } else {
                    Spacer(modifier = Modifier.width(56.dp))
                }

                Button(
                    onClick = {
                        if (currentStep < totalSteps) {
                            if (validateStep(currentStep, userData, context)) currentStep++
                        } else {
                            if (userData.images.size < 2) {
                                Toast.makeText(context, "Kam se kam 2 photos toh banti hain!", Toast.LENGTH_SHORT).show()
                            } else {
                                isUploading = true
                                performFinalUpload(context, userData, navController) { isUploading = false }
                            }
                        }
                    },
                    modifier = Modifier.height(58.dp).width(180.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(18.dp),
                    enabled = !isUploading
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(VibeTheme.PrimaryGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isUploading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Text(if (currentStep == totalSteps) "FINISH" else "NEXT", color = Color.White, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                        }
                    }
                }
            }
        }
    }
}

// --- STEP 1: IDENTITY & BIO ---
@Composable
fun BasicBioStep(data: OnboardingData, onDataChange: (OnboardingData) -> Unit, focusManager: androidx.compose.ui.focus.FocusManager) {
    Column(verticalArrangement = Arrangement.spacedBy(18.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
        StepHeader("The Basics", "Dilli ke launde aur kudiyan, apni details btao!")

        CustomInput(value = data.name, label = "Full Name", icon = Icons.Default.Person, onValueChange = { onDataChange(data.copy(name = it)) }, focusManager = focusManager)
        CustomInput(value = data.age, label = "Age", icon = Icons.Default.Cake, onValueChange = { onDataChange(data.copy(age = it)) }, keyboardType = KeyboardType.Number, focusManager = focusManager)

        Text("Gender", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf("Male", "Female", "Other").forEach { g ->
                SelectableChip(selected = data.gender == g, text = g) { onDataChange(data.copy(gender = g)) }
            }
        }

        Text("Your Bio", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        OutlinedTextField(
            value = data.bio, onValueChange = { if(it.length <= 150) onDataChange(data.copy(bio = it)) },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            placeholder = { Text("Write something catchy...", color = Color.Gray) },
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,

                focusedContainerColor = VibeTheme.CardBg,
                unfocusedContainerColor = VibeTheme.CardBg,
                disabledContainerColor = VibeTheme.CardBg,

                focusedBorderColor = VibeTheme.NeonPink,
                unfocusedBorderColor = Color.Transparent,
                disabledBorderColor = Color.Transparent,

                cursorColor = VibeTheme.NeonPink
            ),supportingText = { Text("${data.bio.length}/150", color = Color.Gray, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End) }
        )
    }
}

// --- STEP 2: RELATIONSHIP GOALS ---
@Composable
fun GoalsStep(data: OnboardingData, onDataChange: (OnboardingData) -> Unit) {
    val goals = listOf(
        "Long-term partner" to Icons.Default.Favorite,
        "Short-term fun" to Icons.Default.Bolt,
        "New friends" to Icons.Default.People,
        "Still figuring it out" to Icons.Default.QuestionMark
    )
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        StepHeader("Your Goal", "Relationship goals kya hain?")
        goals.forEach { (goal, icon) ->
            val isSelected = data.goal == goal
            Surface(
                modifier = Modifier.fillMaxWidth().clickable { onDataChange(data.copy(goal = goal)) },
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) VibeTheme.NeonPink.copy(0.15f) else VibeTheme.CardBg,
                border = BorderStroke(1.dp, if (isSelected) VibeTheme.NeonPink else Color.White.copy(0.05f))
            ) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, null, tint = if (isSelected) VibeTheme.NeonPink else Color.Gray)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(goal, color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// --- STEP 3: MEDIA (Enhanced) ---
@Composable
fun DetailedMediaStep(data: OnboardingData, onDataChange: (OnboardingData) -> Unit) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { onDataChange(data.copy(images = (data.images + it).take(6))) }
    }
    Column {
        StepHeader("Gallery", "Choose photos where your vibe shines.")
        Spacer(modifier = Modifier.height(20.dp))
        LazyVerticalGrid(columns = GridCells.Fixed(3), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(6) { index ->
                Box(modifier = Modifier.aspectRatio(0.8f).clip(RoundedCornerShape(16.dp)).background(VibeTheme.CardBg).clickable { if (index >= data.images.size) launcher.launch("image/*") }, contentAlignment = Alignment.Center) {
                    if (index < data.images.size) {
                        AsyncImage(model = data.images[index], contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        IconButton(onClick = { onDataChange(data.copy(images = data.images.filterIndexed { i, _ -> i != index })) }, modifier = Modifier.align(Alignment.TopEnd)) {
                            Icon(Icons.Default.Cancel, null, tint = Color.White, modifier = Modifier.background(Color.Black.copy(0.5f), CircleShape))
                        }
                    } else {
                        Icon(Icons.Default.AddAPhoto, null, tint = VibeTheme.NeonPink)
                    }
                }
            }
        }
    }
}

// --- STEP 4: LOCATION (GPS Logic) ---
@SuppressLint("MissingPermission")
@Composable
fun LiveLocationStep(userData: OnboardingData, onDataChange: (OnboardingData) -> Unit, onNext: () -> Unit) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var status by remember { mutableStateOf("Hum aapko wahi log dikhayenge jo aapke as-pas hain.") }
    var isFetching by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
        if (map.values.all { it }) {
            isFetching = true
            status = "Locating your Dilli vibe..."
            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    val area = getAreaName(context, loc.latitude, loc.longitude)
                    onDataChange(userData.copy(area = area))
                    status = "Found: $area"
                    isFetching = false
                    onNext()
                } else { status = "GPS band hai bhai!"; isFetching = false }
            }
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize()) {
        Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(100.dp), tint = VibeTheme.NeonPink)
        Spacer(modifier = Modifier.height(20.dp))
        Text("Nearby Explorers", fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold)
        Text(status, color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.padding(20.dp))
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = { launcher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.White)) {
            if (isFetching) CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
            else Text("Allow Location", color = Color.Black, fontWeight = FontWeight.Bold)
        }
        TextButton(onClick = { onDataChange(userData.copy(area = "Delhi")); onNext() }) { Text("Skip for now", color = Color.Gray) }
    }
}

// --- STEP 5: INTERESTS (Advanced) ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PremiumInterestsStep(data: OnboardingData, onDataChange: (OnboardingData) -> Unit) {
    val options = listOf("Momos", "CP Night Life", "Gym", "Sufi Nights", "Gaming", "Photography", "Music", "Majnu ka Tilla", "HKV", "Tech", "Netflix")
    Column {
        StepHeader("My Vibez", "Choose what you enjoy doing in Delhi.")
        Spacer(modifier = Modifier.height(20.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            options.forEach { interest ->
                val isSelected = data.interests.contains(interest)
                SelectableChip(selected = isSelected, text = interest) {
                    val newList = if (isSelected) data.interests - interest else data.interests + interest
                    onDataChange(data.copy(interests = newList))
                }
            }
        }
    }
}

// --- STEP 6: DELHI DNA ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DelhiSpecificStep(data: OnboardingData, onDataChange: (OnboardingData) -> Unit) {
    val metroLines = listOf("Yellow", "Blue", "Pink", "Violet", "Red", "Magenta")
    val spots = listOf("Hauz Khas Village", "Majnu ka Tilla", "Khan Market", "CP", "India Gate", "CyberHub")

    Column(verticalArrangement = Arrangement.spacedBy(20.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
        StepHeader("Delhi DNA", "Dilli se hai toh Dilli ki details btao!")

        Text("Nearest Metro Line", color = Color.White, fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            metroLines.forEach { line ->
                MetroPill(selected = data.metroLine == line, text = line, color = getMetroColor(line)) {
                    onDataChange(data.copy(metroLine = line))
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
        }

        Text("Frequent Hangout Area", color = Color.White, fontWeight = FontWeight.Bold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            spots.forEach { spot ->
                SelectableChip(selected = data.favSpot == spot, text = spot) {
                    onDataChange(data.copy(favSpot = spot))
                }
            }
        }
    }
}

// --- LOGIC: COMPRESSION & UPLOAD ---
fun performFinalUpload(context: Context, data: OnboardingData, navController: NavController, onFail: () -> Unit) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val storage = FirebaseStorage.getInstance().reference
    val db = FirebaseFirestore.getInstance()

    val imageUrls = arrayOfNulls<String>(data.images.size)
    var uploadCount = 0

    data.images.forEachIndexed { index, uri ->
        val ref = storage.child("users/$uid/images/img_$index.jpg")
        try {
            val compressedBytes = uriToCompressedByteArray(context, uri)
            ref.putBytes(compressedBytes).addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { downloadUri ->
                    imageUrls[index] = downloadUri.toString()
                    uploadCount++
                    if (uploadCount == data.images.size) {
                        val profile = mapOf(
                            "name" to data.name,
                            "age" to (data.age.toIntOrNull() ?: 0),
                            "gender" to data.gender,
                            "bio" to data.bio,
                            "goal" to data.goal,
                            "interests" to data.interests,
                            "metroLine" to data.metroLine,
                            "favSpot" to data.favSpot,
                            "area" to data.area,
                            "images" to imageUrls.filterNotNull(),
                            "profileCompleted" to true,
                            "isPrivate" to false,
                            "createdAt" to com.google.firebase.Timestamp.now()
                        )
                        db.collection("users").document(uid).set(profile, SetOptions.merge())
                            .addOnSuccessListener {
                                navController.navigate(Screen.Home.route) { popUpTo(Screen.Onboarding.route) { inclusive = true } }
                            }
                            .addOnFailureListener { onFail() }
                    }
                }
            }.addOnFailureListener { onFail() }
        } catch (e: Exception) { onFail() }
    }
}

// --- HELPER COMPONENTS ---

@Composable
fun CustomInput(value: String, label: String, icon: ImageVector, onValueChange: (String) -> Unit, keyboardType: KeyboardType = KeyboardType.Text, focusManager: androidx.compose.ui.focus.FocusManager) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label) }, modifier = Modifier.fillMaxWidth(),
        leadingIcon = { Icon(icon, null, tint = VibeTheme.NeonPink) },
        shape = RoundedCornerShape(18.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,

            focusedContainerColor = VibeTheme.CardBg,
            unfocusedContainerColor = VibeTheme.CardBg,
            disabledContainerColor = VibeTheme.CardBg,

            focusedBorderColor = VibeTheme.NeonPink,
            unfocusedBorderColor = Color.White.copy(alpha = 0.05f),
            disabledBorderColor = Color.White.copy(alpha = 0.05f),

            focusedLabelColor = VibeTheme.NeonPink,
            unfocusedLabelColor = Color.White.copy(alpha = 0.7f),

            cursorColor = VibeTheme.NeonPink
        )    )
}

@Composable
fun StepHeader(title: String, subtitle: String) {
    Column {
        Text(title, fontSize = 34.sp, color = Color.White, fontWeight = FontWeight.Black, letterSpacing = (-1).sp)
        Text(subtitle, fontSize = 15.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
fun SelectableChip(selected: Boolean, text: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() }, shape = RoundedCornerShape(14.dp),
        color = if (selected) VibeTheme.NeonPink else VibeTheme.CardBg,
        border = if (selected) null else BorderStroke(1.dp, Color.White.copy(0.05f))
    ) {
        Text(text, color = Color.White, modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp), fontWeight = FontWeight.Bold)
    }
}

@Composable
fun MetroPill(selected: Boolean, text: String, color: Color, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() }, shape = RoundedCornerShape(12.dp),
        color = if (selected) color else VibeTheme.CardBg,
        border = BorderStroke(1.dp, color.copy(0.4f))
    ) {
        Text(text, color = if (selected) Color.Black else Color.White, modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), fontWeight = FontWeight.Bold)
    }
}

// --- UTILS ---

fun validateStep(step: Int, data: OnboardingData, context: Context): Boolean {
    return when(step) {
        1 -> if(data.name.isNotBlank() && data.age.isNotBlank() && data.gender.isNotBlank()) true else { Toast.makeText(context, "Details bharo bhai!", Toast.LENGTH_SHORT).show(); false }
        else -> true
    }
}

private fun uriToCompressedByteArray(context: Context, uri: Uri): ByteArray {
    val inputStream = context.contentResolver.openInputStream(uri)
    val bitmap = BitmapFactory.decodeStream(inputStream)
    val out = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out)
    return out.toByteArray()
}

fun getAreaName(context: Context, lat: Double, lng: Double): String {
    return try {
        val geo = Geocoder(context, Locale.getDefault())
        val add = geo.getFromLocation(lat, lng, 1)
        add?.get(0)?.subLocality ?: add?.get(0)?.locality ?: "Delhi"
    } catch (e: Exception) { "Delhi" }
}