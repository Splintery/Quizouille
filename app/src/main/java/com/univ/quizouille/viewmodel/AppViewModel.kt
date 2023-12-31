package com.univ.quizouille.viewmodel

import android.app.Application
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.univ.quizouille.database.AppApplication
import com.univ.quizouille.model.Question
import com.univ.quizouille.model.QuestionSet
import kotlinx.coroutines.launch
import java.lang.Exception

class AppViewModel(private val application: Application) : AndroidViewModel(application) {
    private val dao = (application as AppApplication).database.appDao()
    val questionSetsFlow = dao.getAllQuestionSets()
    var errorMessage by mutableStateOf("")

    fun insertQuestion(setId: Int, question: String, answer: String) = viewModelScope.launch {
        try {
            dao.insertQuestion(Question(questionSetId = setId, content = question, answer = answer))
        }
        catch (e: Exception) {
            errorMessage = "Duplicate data: Failed to insert"
        }
    }

    fun insertSampleData() = viewModelScope.launch {
        try {
            val set1 = QuestionSet(name = "Math Formulas")
            val set2 = QuestionSet(name = "French Vocabulary")

            dao.insertQuestionSet(set1)
            dao.insertQuestionSet(set2)

            dao.insertQuestion(
                Question(
                    questionSetId = 1,
                    content = "What is Pi?",
                    answer = "3.14"
                )
            )
            dao.insertQuestion(
                Question(
                    questionSetId = 2,
                    content = "What is 'apple' in French?",
                    answer = "pomme"
                )
            )
        }
        catch (e: Exception) {
            errorMessage = "Failed to insert sample data: ${e.message}"
        }
    }
}