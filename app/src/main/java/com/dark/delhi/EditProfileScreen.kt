package com.dark.delhi

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditProfileScreen(navController: NavController, userId: String) {
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // --- Form States ---
    var bio by remember { mutableStateOf("") }
    var favSpot by remember { mutableStateOf("") }
    var selectedMetro by remember { mutableStateOf("") }
    var selectedInterests by remember { mutableStateOf<List<String>>(emptyList()) }
    var imageList by remember { mutableStateOf<List<Any>>(emptyList()) } // Can be String (URL) or Uri (New)

    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    // --- Image Picker ---
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { if (imageList.size < 6) imageList = imageList + it }
    }

    // --- Fetch Initial Data ---
    LaunchedEffect(Unit) {
        db.collection("users").document(userId).get().addOnSuccessListener { doc ->
            bio = doc.getString("bio") ?: ""
            favSpot = doc.getString("favSpot") ?: ""
            selectedMetro = doc.getString("metroLine") ?: ""
            selectedInterests = doc.get("interests") as? List<String> ?: emptyList()
            imageList = doc.get("images") as? List<String> ?: emptyList()
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(VibeTheme.DarkBg)) {
        // Glow Background
        Box(modifier = Modifier.size(300.dp).offset(x = 200.dp).blur(120.dp).background(VibeTheme.NeonPink.copy(0.1f), CircleShape))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = VibeTheme.NeonPink)
        } else {
            Column(
                modifier = Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState())
            ) {
                // --- Custom Header ---
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, null, tint = Color.White)
                    }
                    Text("Edit Profile", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    TextButton(
                        onClick = {
                            isSaving = true
                            saveUpdatedProfile(context, userId, bio, favSpot, selectedMetro, selectedInterests, imageList, db, storage) {
                                isSaving = false
                                Toast.makeText(context, "Vibe Updated!", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            }
                        },
                        enabled = !isSaving
                    ) {
                        if (isSaving)
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)

                        else Text("SAVE", color = VibeTheme.NeonPink, fontWeight = FontWeight.Black)
                    }
                }

                // --- 1. MEDIA SECTION (6 Photos Grid) ---
                Text("Photos (Max 6)", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
                FlowRow(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(6) { index ->
                        val isPhotoExist = index < imageList.size
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(VibeTheme.CardBg)
                                .clickable { if (!isPhotoExist) launcher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isPhotoExist) {
                                AsyncImage(
                                    model = imageList[index],
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                // Remove Photo Button
                                Icon(
                                    Icons.Default.Cancel, null,
                                    tint = Color.White,
                                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).clickable {
                                        imageList = imageList.filterIndexed { i, _ -> i != index }
                                    }
                                )
                            } else {
                                Icon(Icons.Default.AddAPhoto, null, tint = Color.Gray)
                            }
                        }
                    }
                }

                // --- 2. ABOUT ME ---
                Spacer(modifier = Modifier.height(24.dp))
                EditFieldHeader("The Vibe (Bio)", Icons.Default.EditNote)
                CustomEditTextField(bio, "Describe yourself...") { bio = it }

                // --- 3. FAVORITE SPOT ---
                Spacer(modifier = Modifier.height(24.dp))
                EditFieldHeader("Favorite Dilli Spot", Icons.Default.Place)
                CustomEditTextField(favSpot, "CP, HKV, or Majnu ka Tilla?") { favSpot = it }

                // --- 4. METRO LINE ---
                Spacer(modifier = Modifier.height(24.dp))
                EditFieldHeader("Delhi DNA (Metro)", Icons.Default.DirectionsSubway)
                val lines = listOf("Yellow", "Blue", "Pink", "Violet", "Red", "Magenta")
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
                    lines.forEach { line ->
                        MetroPill(selected = selectedMetro == line, text = line, color = getMetroColor(line)) {
                            selectedMetro = line
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }

                // --- 5. INTERESTS (VIBES) ---
                Spacer(modifier = Modifier.height(24.dp))
                EditFieldHeader("My Interests", Icons.Default.Tag)
                val allInterests = listOf("Momos", "Night Life", "Gym", "Sufi", "Gaming", "Shopping", "Tech", "Music")
                FlowRow(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    allInterests.forEach { interest ->
                        val isSelected = selectedInterests.contains(interest)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedInterests = if (isSelected) selectedInterests - interest else selectedInterests + interest
                            },
                            label = { Text(interest) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VibeTheme.NeonPink,
                                selectedLabelColor = Color.White,
                                containerColor = VibeTheme.CardBg,
                                labelColor = Color.Gray
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

// --- LOGIC: SAVE EVERYTHING (INCLUDING NEW PHOTOS) ---
private fun saveUpdatedProfile(
    context: android.content.Context,
    uid: String,
    bio: String,
    favSpot: String,
    metro: String,
    interests: List<String>,
    imageList: List<Any>,
    db: FirebaseFirestore,
    storage: FirebaseStorage,
    onComplete: () -> Unit
) {
    val finalUrls = mutableListOf<String>()
    val newImages = imageList.filterIsInstance<Uri>()
    val existingUrls = imageList.filterIsInstance<String>()

    finalUrls.addAll(existingUrls)

    if (newImages.isEmpty()) {
        // Sirf data update karna hai
        val data = mapOf("bio" to bio, "favSpot" to favSpot, "metroLine" to metro, "interests" to interests, "images" to finalUrls)
        db.collection("users").document(uid).update(data).addOnSuccessListener { onComplete() }
    } else {
        // Nayi photos upload karni hain
        var uploadedCount = 0
        newImages.forEach { uri ->
            val ref = storage.reference.child("users/$uid/images/${UUID.randomUUID()}.jpg")
            ref.putFile(uri).addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { downloadUri ->
                    finalUrls.add(downloadUri.toString())
                    uploadedCount++
                    if (uploadedCount == newImages.size) {
                        val data = mapOf("bio" to bio, "favSpot" to favSpot, "metroLine" to metro, "interests" to interests, "images" to finalUrls)
                        db.collection("users").document(uid).update(data).addOnSuccessListener { onComplete() }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomEditTextField(value: String, placeholder: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color.Gray) },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedContainerColor = VibeTheme.CardBg,
            unfocusedContainerColor = VibeTheme.CardBg,
            focusedBorderColor = VibeTheme.NeonPink,
            unfocusedBorderColor = Color.White.copy(0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun EditFieldHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = VibeTheme.NeonPink, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}
