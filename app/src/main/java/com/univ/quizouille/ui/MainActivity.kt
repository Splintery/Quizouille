@file:OptIn(ExperimentalMaterial3Api::class)

package com.univ.quizouille.ui

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.univ.quizouille.services.AppNotificationManager
import com.univ.quizouille.utilities.navigateToRoute
import com.univ.quizouille.viewmodel.GameViewModel
import com.univ.quizouille.viewmodel.SettingsViewModel


class MainActivity : ComponentActivity() {
    private lateinit var notificationManager: AppNotificationManager

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // initialisation du notification manager
        notificationManager = AppNotificationManager(this)
        notificationManager.createChannel()

        setContent {
            Main(notificationManager = notificationManager)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Main(
    gameViewModel: GameViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel(),
    notificationManager: AppNotificationManager
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        Log.d("permissions", if(it) "granted" else "denied")
    }

    Scaffold(snackbarHost = { SnackbarHost (snackbarHostState) },
        bottomBar = {
            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
            if (currentRoute != "question/{questionId}") {
                BottomBar(navController = navController) }
        }) { paddingValues ->
        NavHost(navController = navController, startDestination = "game", modifier = Modifier.padding(paddingValues)) {
            composable("game") {
                gameViewModel.resetSnackbarMessage()
                GameScreen(gameViewModel = gameViewModel, settingsViewModel = settingsViewModel, navController = navController)
            }
            composable("edit") {
                gameViewModel.resetSnackbarMessage()
                EditScreen(gameViewModel = gameViewModel, settingsViewModel = settingsViewModel,
                    snackbarHostState = snackbarHostState)
            }
            composable("settings") {
                gameViewModel.resetSnackbarMessage()
                SettingsScreen(settingsViewModel = settingsViewModel, notificationManager = notificationManager, permissionLauncher = permissionLauncher)
            }
            composable("question/{questionId}") {navBackStackEntry ->
                gameViewModel.resetSnackbarMessage()
                val questionId = navBackStackEntry.arguments?.getString("questionId") ?: "0"
                QuestionScreen(questionId = questionId.toInt(), gameViewModel = gameViewModel,
                    settingsViewModel = settingsViewModel, navController = navController,
                    snackbarHostState = snackbarHostState)
            }
            composable("gameEnded") {
                gameViewModel.resetSnackbarMessage()
                GameEnded(settingsViewModel = settingsViewModel, navController = navController)
            }
            composable("statistics") {
                gameViewModel.resetSnackbarMessage()
                StatisticsScreen(gameViewModel = gameViewModel, settingsViewModel = settingsViewModel,
                    navController = navController)
            }
            composable("statistics/all") {
                gameViewModel.resetSnackbarMessage()
                ShowAllStatisticsScreen(gameViewModel = gameViewModel, settingsViewModel = settingsViewModel)
            }
            composable("statistics/{setId}") { navBackStackEntry ->
                gameViewModel.resetSnackbarMessage()
                val setId = navBackStackEntry.arguments?.getString("setId") ?: "1"
                ShowStatisticsScreen(setId = setId.toInt(), gameViewModel = gameViewModel,
                    settingsViewModel = settingsViewModel)
            }
            composable("download") {
                gameViewModel.resetSnackbarMessage()
                DownloadSetsScreen(gameViewModel = gameViewModel, settingsViewModel = settingsViewModel,
                    snackbarHostState = snackbarHostState)
            }
        }
    }
}

@Composable
fun BottomBar(navController: NavHostController) = BottomNavigation {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val gameRoute = "game"
    val editRoute = "edit"
    val settingsRoute = "settings"
    val statisticsRoute = "statistics"
    val downloadSetsRoute = "download"

    BottomNavigationItem(
        selected = currentRoute == gameRoute,
        onClick = {
            if (currentRoute != gameRoute) {
                navigateToRoute(route = gameRoute, navController = navController)
            }
        },
        icon = { Icon(Icons.Outlined.PlayArrow, contentDescription = "Play menu")}
    )
    BottomNavigationItem(
        selected = currentRoute == editRoute,
        onClick = {
            if (currentRoute != editRoute) {
                navigateToRoute(route = editRoute, navController = navController)
            }
        },
        icon = { Icon(Icons.Outlined.Edit, contentDescription = "Edit menu")}
    )
    BottomNavigationItem(
        selected = currentRoute == settingsRoute,
        onClick = {
            if (currentRoute != settingsRoute) {
                navigateToRoute(route = settingsRoute, navController = navController)
            }
        },
        icon = { Icon(Icons.Outlined.Settings, contentDescription = "Settings menu")}
    )
    BottomNavigationItem(
        selected = currentRoute == statisticsRoute,
        onClick = {
            if (currentRoute != statisticsRoute) {
                navigateToRoute(route = statisticsRoute, navController = navController)
            }
        },
        icon = { Icon(Icons.Outlined.List, contentDescription = "Statistics menu")}
    )
    BottomNavigationItem(
        selected = currentRoute == downloadSetsRoute,
        onClick = {
            if (currentRoute != downloadSetsRoute) {
                navigateToRoute(route = downloadSetsRoute, navController = navController)
            }
        },
        icon = { Icon(Icons.Outlined.List, contentDescription = "Download sets menu")}
    )
}