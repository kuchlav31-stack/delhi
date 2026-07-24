package com.dark.delhi

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun MainContainer(navController: NavController) {
    var selectedItem by remember { mutableIntStateOf(0) }
    val items = listOf("Explore", "Messages", "Profile")
    val icons = listOf(Icons.Default.Favorite, Icons.Default.ChatBubble, Icons.Default.Person)

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White
            ) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFFF416C),
                            unselectedIconColor = Color.Gray,
                            indicatorColor = Color(0xFFFF416C).copy(alpha = 0.1f)
                        )
                    )
                }
            }        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedItem) {
                0 -> HomeScreen(navController)
                1 -> MessagesListScreen(navController)
                2 -> MyProfileScreen(navController)
            }
        }
    }
}