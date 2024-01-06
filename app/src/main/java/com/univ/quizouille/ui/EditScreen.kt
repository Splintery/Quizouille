@file:OptIn(ExperimentalMaterial3Api::class)

package com.univ.quizouille.ui

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Switch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposableOpenTarget
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.univ.quizouille.model.Question
import com.univ.quizouille.model.QuestionSet
import com.univ.quizouille.viewmodel.GameViewModel
import com.univ.quizouille.viewmodel.SettingsViewModel


fun getFontWeight(setId: Int, itemId: Int): FontWeight {
    if (setId == itemId) {
        return FontWeight.Bold
    } else {
        return FontWeight.Normal
    }
}
fun areAnswersValid(answer: List<String>, answerCorrect: List<Boolean>): Boolean {
    for (i in 0..<4) {
        if (!answer[i].equals("") && answerCorrect[i]) {
            return true
        }
    }
    return false
}
@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "RememberReturnType")
@Composable
fun EditScreen(
    gameViewModel: GameViewModel,
    settingsViewModel: SettingsViewModel,
    snackbarHostState: SnackbarHostState)
{
    // Used to chose edit action
    var addNewQuestion by remember { mutableStateOf(false) }
    var deleteSet by remember { mutableStateOf(false) }
    var deleteQuestion by remember { mutableStateOf(false) }
    var modifyQuestion by remember { mutableStateOf(false)}

    // Used to add new question
    var setId by remember { mutableIntStateOf(-1) }
    var newSetName by remember { mutableStateOf("") }
    var question by remember { mutableStateOf("") }
    var answer = remember { mutableStateListOf("", "", "", "") }
    var answerCorrect = remember { mutableStateListOf(true, false, false, false) }
    var newSet by remember { mutableStateOf(false) }

    val lastQuestionSet by gameViewModel.lastSetInsertedIdFlow.collectAsState(initial = -1)
    val lastQuestion by gameViewModel.lastQuestionInsertedIdFlow.collectAsState(initial = -1)

    val policeTitleSize by settingsViewModel.policeTitleSizeFlow.collectAsState(initial = 20)
    val policeSize by settingsViewModel.policeSizeFlow.collectAsState(initial = 16)
    val allQuestionSets: List<QuestionSet> by gameViewModel.questionSetsFlow.collectAsState(initial = mutableListOf())
    val allQuestions: List<Question> by gameViewModel.allQuestionsFlow.collectAsState(initial = mutableListOf())

    @Composable
    fun addNewQuestionScreen() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.padding(vertical = 25.dp)
            ) {
                Button(onClick = {addNewQuestion = false}) {
                    Text(text = "return", fontSize = policeSize.sp, fontWeight = FontWeight.Bold)
                }
            }
            Row(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxHeight(0.3f)
            ) {
                if (newSet) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 5.dp)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        OutlinedTextField(
                            value = newSetName,
                            onValueChange = { newSetName = it },
                            label = {
                                Text(text = "new set")
                            }
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 5.dp)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            items(allQuestionSets) {
                                Text(
                                    text = it.name,
                                    fontSize = policeSize.sp,
                                    fontWeight = getFontWeight(setId, it.setId),
                                    modifier = Modifier
                                        .clickable {
                                            if (setId != it.setId) {
                                                setId = it.setId
                                            } else {
                                                setId = -1
                                            }
                                        }
                                        .border(
                                            width = 1.dp,
                                            color = Color.Black,
                                            shape = RoundedCornerShape(50)
                                        )
                                        .padding(vertical = 10.dp, horizontal = 15.dp)
                                )
                            }
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Button(onClick = {
                        newSet = !newSet
                        newSetName = ""
                        setId = -1
                    }) {
                        if (newSet) {
                            Image(Icons.Outlined.Clear, contentDescription = "cancel create set")
                        } else {
                            Image(Icons.Outlined.Add, contentDescription = "create set")
                        }
                    }
                }
            }
            Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    label = { Text(text = "question") }
                )
            }
            Row(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 10.dp)
            ) {
                Text(
                    text = "Toggle the switch if the answer is correct",
                    fontSize = policeTitleSize.sp,
                    fontWeight = FontWeight.Bold
                )
            }


            for (index in 0..<4) {
                Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    OutlinedTextField(
                        value = answer[index],
                        onValueChange = { answer[index] = it },
                        label = {
                            if (index > 0) {
                                Text(text = "answer (Optinal)")
                            } else {
                                Text(text = "answer")
                            }
                        }
                    )
                    Switch(
                        checked = answerCorrect[index],
                        onCheckedChange = {
                            if (!it) {
                                for (i in 0..<4) {
                                    if (i != index && answerCorrect[i]) {
                                        answerCorrect[index] = false
                                        break;
                                    }
                                }
                            } else {
                                answerCorrect[index] = true
                            }
                        }
                    )
                }
            }
            Row(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 10.dp)
            ) {
                Button(onClick = {
                    if ((setId != -1 || !newSetName.equals("")) && (areAnswersValid(
                            answer,
                            answerCorrect
                        ) && !question.equals(""))
                    ) {
                        if (setId == -1) {
                            gameViewModel.insertQuestionSet(newSetName)
                            if (lastQuestionSet != -1) {
                                setId = lastQuestionSet
                            }
                        }
                        Log.d("test", "setId: $setId && question = $question")
                        gameViewModel.insertQuestion(setId = setId, question = question)
                        var questionId = lastQuestion + 1
                        for (i in 0..<4) {
                            if (!answer[i].equals("")) {
                                gameViewModel.insertAnswer(
                                    questionId = questionId,
                                    answer = answer[i],
                                    correct = answerCorrect[i]
                                )
                            }
                        }
                        question = ""
                        setId = -1
                        newSetName = ""
                        newSet = false
                        for (i in 0..<4) {
                            answer[i] = ""
                            answerCorrect[i] = false
                        }
                        answerCorrect[0] = true
                    }
                }) {
                    Text(text = "insérer", Modifier.padding(1.dp))
                }
            }
        }
    }
    @Composable
    fun deleteSetScreen() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.padding(vertical = 25.dp)
            ) {
                Button(onClick = { deleteSet = false }) {
                    Text(text = "return", fontSize = policeSize.sp, fontWeight = FontWeight.Bold)
                }
            }
            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxHeight(0.8f)
            ) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    items(allQuestionSets) {
                        Text(
                            text = it.name,
                            fontSize = policeSize.sp,
                            fontWeight = getFontWeight(setId, it.setId),
                            modifier = Modifier
                                .clickable {
                                    if (setId != it.setId) {
                                        setId = it.setId
                                    } else {
                                        setId = -1
                                    }
                                }
                                .border(
                                    width = 1.dp,
                                    color = Color.Black,
                                    shape = RoundedCornerShape(50)
                                )
                                .padding(vertical = 10.dp, horizontal = 15.dp)
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 25.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = {
                    if (setId != -1) {
                        gameViewModel.deleteQuestionSet(setId = setId)
                        setId = -1
                    }
                }) {
                    Text(text = "delete set", fontSize = policeSize.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
    @Composable
    fun deleteQuestionScreen() {
        var questionId = -1
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.padding(vertical = 25.dp)
            ) {
                Button(onClick = { deleteQuestion = false }) {
                    Text(text = "return", fontSize = policeSize.sp, fontWeight = FontWeight.Bold)
                }
            }
            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxHeight(0.8f)
            ) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    items(allQuestions) {
                        Text(
                            text = it.content,
                            fontSize = policeSize.sp,
                            fontWeight = getFontWeight(questionId, it.questionId),
                            modifier = Modifier
                                .clickable {
                                    if (questionId != it.questionId) {
                                        questionId = it.questionId
                                    } else {
                                        questionId = -1
                                    }
                                }
                                .border(
                                    width = 1.dp,
                                    color = Color.Black,
                                    shape = RoundedCornerShape(50)
                                )
                                .padding(vertical = 10.dp, horizontal = 15.dp)
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 25.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = {
                    if (questionId != -1) {
                        gameViewModel.deleteQuestion(questionId = questionId)
                        questionId = -1
                    }
                }) {
                    Text(text = "delete question", fontSize = policeSize.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) {
        if (addNewQuestion || deleteSet || deleteQuestion || modifyQuestion) {
            if (addNewQuestion) {
                addNewQuestionScreen()
            } else if (deleteSet) {
                deleteSetScreen()
            } else if (deleteQuestion) {
                deleteQuestionScreen()
            } else {

            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            )
            {
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 25.dp)
                )
                {
                    Button(onClick = {addNewQuestion = true}) {
                        Text(text = "Add new question", fontSize = policeSize.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 25.dp)
                )
                {
                    Button(onClick = {deleteSet = true}) {
                        Text(text = "Delete set", fontSize = policeSize.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 25.dp)
                )
                {
                    Button(onClick = {deleteQuestion = true}) {
                        Text(text = "Delete question", fontSize = policeSize.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 25.dp)
                )
                {
                    Button(onClick = {modifyQuestion = true}) {
                        Text(text = "Modify question", fontSize = policeSize.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        // envoie des snackbars quand une entrée dans la BD n'a pas marché
        LaunchedEffect(gameViewModel.errorMessage) {
            if (gameViewModel.errorMessage.isNotEmpty()) {
                snackbarHostState.showSnackbar(gameViewModel.errorMessage)
                gameViewModel.errorMessage = ""
            }
        }
    }
}

