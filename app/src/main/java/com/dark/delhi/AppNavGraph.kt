package com.dark.delhi

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth

/**
 * 1. Screen Routes Definition
 * Standard approach for navigation with arguments
 */
sealed class Screen(val route: String) {
    object Splash : Screen("splash_screen")
    object Signup : Screen("signup_screen")
    object Login : Screen("login_screen")
    object Onboarding : Screen("onboarding_screen")
    object Home : Screen("home_screen")
    object Messages : Screen("messages_list")
    object Profile : Screen("profile_screen")
    object Filter : Screen("filter_screen")

    // Screens with Arguments
    object Chat : Screen("chat_room/{userId}/{userName}") {
        fun createRoute(userId: String, userName: String) = "chat_room/$userId/$userName"
    }

    object EditProfile : Screen("edit_profile_screen/{userId}") {
        fun createRoute(userId: String) = "edit_profile_screen/$userId"
    }
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    // We always start with Splash to handle auto-login logic
    val startDestination = Screen.Splash.route

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // --- 1. Splash Screen ---
        composable(route = Screen.Splash.route) {
            DilliDatingSplashScreen(navController = navController)
        }

        // --- 2. Signup Screen ---
        composable(route = Screen.Signup.route) {
            SignupScreen(navController = navController)
        }

        // --- 3. Login Screen ---
        composable(route = Screen.Login.route) {
            LoginScreen(navController = navController)
        }

        // --- 4. Onboarding (Profile Setup) ---
        composable(route = Screen.Onboarding.route) {
            OnboardingScreen(navController = navController)
        }

        // --- 5. Main Home (Swipe Deck) ---
        composable(route = Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        // --- 6. Messages List ---
        composable(route = Screen.Messages.route) {
            MessagesListScreen(navController = navController)
        }

        // --- 7. Chat Room (Receives other user's ID and Name) ---
        composable(
            route = Screen.Chat.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("userName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val userName = backStackEntry.arguments?.getString("userName") ?: "User"
            ChatScreen(
                navController = navController,
                otherUserId = userId,
                otherUserName = userName
            )
        }

        // --- 8. My Profile ---
        composable(route = Screen.Profile.route) {
            MyProfileScreen(navController = navController)
        }

        // --- 9. Edit Profile (Receives Current User's ID) ---
        composable(
            route = Screen.EditProfile.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            EditProfileScreen(
                navController = navController,
                userId = userId
            )
        }

        // --- 10. Filter Screen (Optional) ---
        composable(route = Screen.Filter.route) {
            // FilterScreen(navController = navController)
        }
    }
}