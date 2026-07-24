package com.dark.delhi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen(navController: NavController) {
    // --- States for Filters ---
    var distanceValue by remember { mutableStateOf(25f) } // Default 25km
    var ageRange by remember { mutableStateOf(18f..35f) } // Default 18-35 age
    var isVerifiedOnly by remember { mutableStateOf(false) }
    var selectedArea by remember { mutableStateOf("All Delhi") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Discovery Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8F8F8))
                .padding(20.dp)
        ) {

            // --- Distance Filter ---
            FilterSectionTitle(title = "Maximum Distance", value = "${distanceValue.toInt()} km")
            Slider(
                value = distanceValue,
                onValueChange = { distanceValue = it },
                valueRange = 1f..50f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFFFF4B2B),
                    activeTrackColor = Color(0xFFFF4B2B)
                )
            )
            Text(
                "Delhi-NCR is big! Choose distance carefully.",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- Age Range Filter ---
            FilterSectionTitle(
                title = "Age Range",
                value = "${ageRange.start.toInt()} - ${ageRange.endInclusive.toInt()}"
            )
            RangeSlider(
                value = ageRange,
                onValueChange = { ageRange = it },
                valueRange = 18f..60f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFFFF4B2B),
                    activeTrackColor = Color(0xFFFF4B2B)
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- Verification Filter ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, "Verified", tint = Color(0xFF1DA1F2), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Verified Profiles Only", fontWeight = FontWeight.Bold)
                        }
                        Text("Dilli ke asli log dikhayenge", fontSize = 12.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = isVerifiedOnly,
                        onCheckedChange = { isVerifiedOnly = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFFF4B2B))
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- Area Selection (Delhi Specific) ---
            Text("Preferred Region", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(12.dp))
            val delhiAreas = listOf("All Delhi", "South Delhi", "North Delhi", "West Delhi", "East Delhi", "Noida", "Gurgaon")

            // FlowRow for area chips
            OptInFlowRow {
                delhiAreas.forEach { area ->
                    FilterChip(
                        selected = selectedArea == area,
                        onClick = { selectedArea = area },
                        label = { Text(area) },
                        modifier = Modifier.padding(4.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFF4B2B).copy(alpha = 0.1f),
                            selectedLabelColor = Color(0xFFFF4B2B)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // --- Apply Button ---
            Button(
                onClick = {
                    // Logic to save filters and refresh Home Screen
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(listOf(Color(0xFFFF416C), Color(0xFFFF4B2B))),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("APPLY FILTERS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun FilterSectionTitle(title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(text = value, fontSize = 16.sp, color = Color(0xFFFF4B2B), fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OptInFlowRow(content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.FlowRow {
        content()
    }
}