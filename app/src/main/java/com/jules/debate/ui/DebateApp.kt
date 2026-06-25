package com.jules.debate.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jules.debate.api.GeminiApiService
import com.jules.debate.data.AppDatabase
import com.jules.debate.repository.DebateRepository
import com.jules.debate.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonFactory

@Composable
fun DebateApp() {
    val navController = rememberNavController()
    val context = LocalContext.current

    val repository = remember {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val apiService = Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(client)
            .addConverterFactory(com.squareup.retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
            .create(GeminiApiService::class.java)

        val database = AppDatabase.getDatabase(context)
        DebateRepository(apiService, database.debateDao(), BuildConfig.GEMINI_API_KEY)
    }

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onNavigateToBrief = { timestamp ->
                    navController.navigate("brief/$timestamp")
                },
                onNavigateToHistory = {
                    navController.navigate("history")
                },
                repository = repository
            )
        }
        composable("history") {
            HistoryScreen(
                onNavigateToBrief = { timestamp ->
                    navController.navigate("brief/$timestamp")
                },
                onBack = { navController.popBackStack() },
                repository = repository
            )
        }
        composable(
            "brief/{timestamp}",
            arguments = listOf(navArgument("timestamp") { type = NavType.LongType })
        ) { backStackEntry ->
            val timestamp = backStackEntry.arguments?.getLong("timestamp") ?: 0L
            BriefScreen(
                timestamp = timestamp,
                onBack = { navController.popBackStack() },
                repository = repository
            )
        }
    }
}
