@file:OptIn(ExperimentalMaterial3Api::class)

package com.univ.quizouille

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Main()
        }
    }
}

@Composable
fun Test1() {
    Text(text = "page 1")
}

@Composable
fun Test2() {
    Text(text = "page 2")
}

@Composable
fun Settings() {
    // TODO Ajouter VAR globales dans viewmodel
    var notificationsChecked by remember { mutableStateOf(true) }
    var questionDelay by remember { mutableStateOf("10")}
    var policeSize by remember { mutableStateOf("10")}
    Log.d("aled", questionDelay)

    Column {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Paramètres", fontSize = 20.sp, modifier = Modifier.weight(1f))
        }
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Notifications", fontSize = 16.sp, modifier = Modifier.weight(1f))
            Switch(checked = notificationsChecked, onCheckedChange = { notificationsChecked = it } )
        }
        Row(modifier = Modifier
            .padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Temps de réponse aux questions", fontSize = 16.sp, modifier = Modifier.weight(1f))
            OutlinedTextField(value = questionDelay,
                onValueChange = {
                if (it.all { char -> char.isDigit() }) {
                    questionDelay = it
                }}
                , keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
            )
        }
        Row(modifier = Modifier
            .padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Taille de la police", fontSize = 16.sp, modifier = Modifier.weight(1f))
            OutlinedTextField(value = policeSize,
                onValueChange = {
                    if (it.all { char -> char.isDigit() }) {
                        policeSize = it
                    }}
                , keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
            )
        }
    }
    
}

@Composable
fun Main() {
    val navController = rememberNavController()
    Scaffold(bottomBar = { BottomBar(navController = navController)}) { paddingValues ->
        NavHost(navController = navController, startDestination = "game", modifier = Modifier.padding(paddingValues)) {
            composable("game") { Test1() }
            composable("edit") { Test2() }
            composable("settings") { Settings() }
        }
    }
}

@Composable
fun BottomBar(navController: NavHostController) = BottomNavigation {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    BottomNavigationItem(
        selected = currentRoute == "game",
        onClick = { navController.navigate("game") { launchSingleTop = true } },
        icon = { Icon(Icons.Outlined.PlayArrow, contentDescription = "Play menu")}
    )
    BottomNavigationItem(
        selected = currentRoute == "edit",
        onClick = { navController.navigate("edit") { popUpTo("game") } },
        icon = { Icon(Icons.Outlined.Edit, contentDescription = "Edit menu")}
    )
    BottomNavigationItem(
        selected = currentRoute == "settings",
        onClick = { navController.navigate("settings") { popUpTo("game") } },
        icon = { Icon(Icons.Outlined.Settings, contentDescription = "Settings menu")}
    )
}