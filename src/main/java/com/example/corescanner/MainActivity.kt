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
import androidx.lifecycle.viewmodel.compose.viewModel

import com.example.corescanner.data.ChatDb
import com.example.corescanner.repository.ChatRepository
import com.example.corescanner.ui.ChatScreen
import com.example.corescanner.ui.ContinueChatScreen
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

                val vm: ChatViewModel = viewModel()

                NavHost(navController = nav, startDestination = "home") {
                    composable("home") {
                        HomeScreen(
                            repository = repo,
                            vm = vm,   // ✅ se lo pasamos al Home
                            onGoHistory = { nav.navigate("history") },
                            onGoNewChat = { id ->
                                vm.resetLastImage()
                                nav.navigate("chat/$id")
                            }
                        )
                    }
                    composable("history") {
                        HistoryScreen(
                            repository = repo,
                            vm = vm,
                            onOpen = { id -> nav.navigate("retomar/$id") },
                            onNew = { id ->
                                vm.resetLastImage()
                                nav.navigate("chat/$id")
                            }
                        )
                    }
                    composable(
                        route = "retomar/{id}",
                        arguments = listOf(navArgument("id") { type = NavType.LongType })
                    ) { back ->
                        val id = back.arguments!!.getLong("id")
                        ContinueChatScreen(
                            sessionId = id,
                            repository = repo,
                            onContinue = { nav.navigate("chat/$id") },
                            onBack = { nav.popBackStack() }
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
                            onBack = { nav.popBackStack() },
                            vm = vm
                        )
                    }
                }
            }
        }
    }
}