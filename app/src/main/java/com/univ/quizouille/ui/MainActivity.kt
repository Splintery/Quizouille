@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.univ.quizouille.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.TextField
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.univ.quizouille.model.QuestionSet
import com.univ.quizouille.viewmodel.AppViewModel
import com.univ.quizouille.viewmodel.SettingsViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.forEach

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Main()
        }
    }
}

@Composable
fun showQuestionsSet(set: List<QuestionSet>) {
    LazyColumn {
        items(set) {
            Row {
                Text(text = it.name)
                Text(text = it.setId.toString())
            }
        }
    }
}

@SuppressLint("RememberReturnType", "UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Test1(appViewModel: AppViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }
    val questionsSet by appViewModel.questionSetsFlow.collectAsState(listOf())
    var setId by remember { mutableIntStateOf(0) }
    var question by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("") }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState)} ) {
        Column {
            Row {
                OutlinedTextField(value = setId.toString(), onValueChange = { setId = it.toInt()} , label = { Text(text = "setId")})
            }
            Row {
                OutlinedTextField(value = question, onValueChange = { question = it } , label = { Text(text = "question")})
            }
            Row {
                OutlinedTextField(value = answer, onValueChange = { answer = it } , label = { Text(text = "answer")})
            }
            Row {
                Button(onClick = {
                    appViewModel.insertQuestion(1, "oui", "non")
                }) {
                    Text(text = "insérer", Modifier.padding(1.dp))
                }
            }
            Row {
                showQuestionsSet(questionsSet)
            }
        }

    }
    

    LaunchedEffect(appViewModel.errorMessage) {
        if (appViewModel.errorMessage.isNotEmpty()) {
            snackbarHostState.showSnackbar(appViewModel.errorMessage)
            appViewModel.errorMessage = ""  // Clear the error message after showing it
        }
    }
}

@Composable
fun Test2() {
    Text(text = "page 2")
}

@Composable
fun Main(model: SettingsViewModel = viewModel(), appViewModel: AppViewModel = viewModel()) {
    val navController = rememberNavController()
    appViewModel.insertSampleData()

    Scaffold(bottomBar = { BottomBar(navController = navController) }) { paddingValues ->
        NavHost(navController = navController, startDestination = "game", modifier = Modifier.padding(paddingValues)) {
            composable("game") { Test1(appViewModel) }
            composable("edit") { Test2() }
            composable("settings") { SettingsScreen(settingsViewModel = model) }
        }
    }
}

@Composable
fun BottomBar(navController: NavHostController) = BottomNavigation {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    BottomNavigationItem(
        selected = currentRoute == "game",
        onClick = {
            if (currentRoute != "game") {
                navController.navigate("game")
            }
        },
        icon = { Icon(Icons.Outlined.PlayArrow, contentDescription = "Play menu")}
    )
    BottomNavigationItem(
        selected = currentRoute == "edit",
        onClick = {
            if (currentRoute != "edit") {
                navController.navigate("edit")
            }
        },
        icon = { Icon(Icons.Outlined.Edit, contentDescription = "Edit menu")}
    )
    BottomNavigationItem(
        selected = currentRoute == "settings",
        onClick = {
            if (currentRoute != "settings") {
                navController.navigate("settings")
            }
        },
        icon = { Icon(Icons.Outlined.Settings, contentDescription = "Settings menu")}
    )
}