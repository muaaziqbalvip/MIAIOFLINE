package com.miai.offline

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.miai.offline.ui.Routes
import com.miai.offline.ui.screens.chat.ChatScreen
import com.miai.offline.ui.screens.home.HomeScreen
import com.miai.offline.ui.screens.models.ModelsScreen
import com.miai.offline.ui.theme.MiAiOfflineTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MiAiOfflineTheme {
                MiAiNavHost()
            }
        }
    }
}

@Composable
fun MiAiNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.HOME) {

        composable(Routes.HOME) {
            HomeScreen(onBrowseModels = { navController.navigate(Routes.MODELS) })
        }

        composable(Routes.MODELS) {
            ModelsScreen(
                onBack = { navController.popBackStack() },
                onModelSelected = { modelId ->
                    navController.navigate(Routes.chatRoute(modelId))
                }
            )
        }

        composable(
            route = Routes.CHAT,
            arguments = listOf(navArgument("modelId") { type = NavType.StringType })
        ) { backStackEntry ->
            val modelId = backStackEntry.arguments?.getString("modelId") ?: ""
            ChatScreen(modelId = modelId, onBack = { navController.popBackStack() })
        }
    }
}
