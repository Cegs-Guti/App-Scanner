package com.example.corescanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.corescanner.data.ChatDb
import com.example.corescanner.repository.ChatRepository
import com.example.corescanner.ui.ChatScreen
import com.example.corescanner.ui.HistoryScreen
import com.example.corescanner.ui.HomeScreen
import com.example.corescanner.ui.theme.CoreScannerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CoreScannerTheme {
                val nav = rememberNavController()
                val repo = remember {
                    ChatRepository(
                        applicationContext,
                        ChatDb.get(applicationContext).chatDao()
                    )
                }

                NavHost(navController = nav, startDestination = "home") {

                    composable("home") {
                        HomeScreen(
                            repository = repo,
                            onGoHistory = { nav.navigate("history") },
                            onGoNewChat = { id ->
                                nav.navigate("chat/$id")
                            }
                        )
                    }

                    composable("history") {
                        HistoryScreen(
                            repository = repo,
                            onOpen = { id -> nav.navigate("chat/$id") },
                            onNew = { id -> nav.navigate("chat/$id") },
                            onBack = { nav.popBackStack() }   // ✅ aquí mandamos el back
                        )
                    }

                    composable(
                        route = "chat/{id}",
                        arguments = listOf(navArgument("id") { type = NavType.LongType })
                    ) { back ->
                        val id = back.arguments!!.getLong("id")
                        ChatScreen(
                            sessionId = id,
                            repository = repo,
                            onBack = { nav.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}