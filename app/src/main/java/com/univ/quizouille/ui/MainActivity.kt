@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class,
    ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class,
    ExperimentalMaterial3Api::class
)

package com.univ.quizouille.ui

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.univ.quizouille.ui.theme.EditScreen
import com.univ.quizouille.utilities.navigateToRoute
import com.univ.quizouille.viewmodel.GameViewModel
import com.univ.quizouille.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Main()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Main(gameViewModel: GameViewModel = viewModel(), settingsViewModel: SettingsViewModel = viewModel()) {
    val navController = rememberNavController()
    gameViewModel.insertSampleData()

    Scaffold(bottomBar = {
        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
        if (currentRoute != "question/{questionId}")
            BottomBar(navController = navController) }) { paddingValues ->
        NavHost(navController = navController, startDestination = "game", modifier = Modifier.padding(paddingValues)) {
            composable("game") {
                GameScreen(gameViewModel, settingsViewModel, navController)
            }
            composable("edit") {
                EditScreen(gameViewModel, settingsViewModel)
            }
            composable("settings") {
                SettingsScreen(settingsViewModel)
            }
            composable("question/{questionId}") {navBackStackEntry ->
                val questionId = navBackStackEntry.arguments?.getString("questionId") ?: "0"
                QuestionScreen(questionId = questionId.toInt(), gameViewModel = gameViewModel, settingsViewModel = settingsViewModel, navController = navController)
            }
            composable("gameEnded") {
                GameEnded(settingsViewModel = settingsViewModel, navController = navController)
            }
            composable("statistics") {
                StatisticsScreen(gameViewModel = gameViewModel, settingsViewModel = settingsViewModel, navController = navController)
            }
            composable("statistics/{setId}") { navBackStackEntry ->
                val setId = navBackStackEntry.arguments?.getString("setId") ?: "1"
                ShowStatisticsScreen(setId = setId.toInt(), gameViewModel = gameViewModel, settingsViewModel = settingsViewModel)
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
}
