package com.univ.quizouille.viewmodel

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.univ.quizouille.database.AppApplication
import com.univ.quizouille.model.Answer
import com.univ.quizouille.model.Question
import com.univ.quizouille.model.QuestionSet
import com.univ.quizouille.model.QuestionSetStatistics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.random.Random
import com.univ.quizouille.utilities.stringToLocalDate
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlin.Exception

class GameViewModel(private val application: Application) : AndroidViewModel(application) {
    private val dao = (application as AppApplication).database.appDao()

    var errorMessage by mutableStateOf("")
    var questionSetsFlow = dao.getAllQuestionSets()
    var questionFlow = dao.getQuestionById(0)
    var answersFlow = dao.getAllAnswerForQuestion(0)
    var questionSetFlow = dao.getQuestionSetById(0)

    // Statistiques
    var setStatisticsFlow = dao.getSetStatisticsById(0)
    var totalCorrectCount by mutableIntStateOf(0)
    var totalAskedCount by mutableIntStateOf(0)
    var daysSinceTraining by mutableIntStateOf(0)

    // Edit Screen
    var lastSetInsertedIdFlow = dao.getLatestSetId()
    var lastQuestionInsertedIdFlow = dao.getLatestQuestionId()

    @RequiresApi(Build.VERSION_CODES.O)
    private fun shouldShowQuestion(question: Question, currentDate: LocalDate) : Boolean {
        if (question.lastShownDate == "")
            return true
        val daysSinceLastShown = ChronoUnit.DAYS.between(stringToLocalDate(question.lastShownDate), currentDate)
        return daysSinceLastShown >= question.status
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getFilteredQuestionsForSet(setId: Int): Flow<List<Question>> {
        val currentDate = LocalDate.now()
        return dao.getQuestionsForSet(setId).map { questions ->
            questions.filter { question ->
                shouldShowQuestion(question, currentDate)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getQuestionSetsForToday(): Flow<List<QuestionSet>> = questionSetsFlow.map{ questionSets ->
        questionSets.filter { set ->
            // on récupère toutes les questions du set dont le status
            // est inférieur au nombre de jours de la dernière apparition
            val questions = getFilteredQuestionsForSet(set.setId).first()
            // on garde seulement les jeux de questions dont au moins une question est à répondre
            questions.isNotEmpty()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getRandomQuestionFromSet(setId: Int): Flow<Question?> {
        return getFilteredQuestionsForSet(setId).map { questions ->
            if (questions.isNotEmpty())
                questions[Random.nextInt(questions.size)]
            else
                null
        }
    }

    fun fetchQuestionById(questionId: Int) {
        questionFlow = dao.getQuestionById(questionId = questionId)
        answersFlow = dao.getAllAnswerForQuestion(questionId = questionId)
    }

    fun fetchSetStatisticsById(setId: Int) {
        setStatisticsFlow = dao.getSetStatisticsById(setId = setId)
    }

    fun fetchQuestionSet(setId: Int) {
        questionSetFlow = dao.getQuestionSetById(setId = setId)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchAllSetsStatistics() {
        viewModelScope.launch {
            dao.getSetsStatistics().collect { statisticsList ->
                totalCorrectCount = statisticsList.sumOf { it.correctCount }
                totalAskedCount = statisticsList.sumOf { it.totalAsked }

                var minDaysDiff: Long = Long.MAX_VALUE
                val currentDate = LocalDate.now()
                for (stats in statisticsList) {
                    val statDate = stats.lastTrainedDate.takeIf { it.isNotBlank() }?.let { stringToLocalDate(it) }
                    statDate?.let {
                        val daysDiff = ChronoUnit.DAYS.between(it, currentDate)
                        if (daysDiff < minDaysDiff)
                            minDaysDiff = daysDiff
                    }
                }
                daysSinceTraining = minDaysDiff.toInt()
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun insertQuestionSet(setName: String) {
        viewModelScope.launch {
            try {
                dao.insertQuestionSet(QuestionSet(name = setName))
                questionSetsFlow = dao.getAllQuestionSets()
                lastSetInsertedIdFlow = dao.getLatestSetId()
            } catch (e: Exception) {
                errorMessage = "Duplicate data: set allready exist with name $setName"
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun insertQuestion(setId: Int, question: String) {
        viewModelScope.launch {
            try {
                val currentDate = LocalDate.now().toString()
                dao.insertQuestion(Question(questionSetId = setId, content = question, lastShownDate = currentDate))
                lastQuestionInsertedIdFlow = dao.getLatestQuestionId()
            }
            catch (e: Exception) {
                errorMessage = "Duplicate data: Failed to insert"
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun insertAnswer(questionId: Int, answer: String, correct: Boolean) {
        viewModelScope.launch {
            try {
                dao.insertAnswer(Answer(questionId = questionId, answer = answer, correct = correct))
            } catch (e: Exception) {
                errorMessage = "Answer allready exist for question $questionId"
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun successQuestion(question: Question) {
        viewModelScope.launch {
            val currentDate = LocalDate.now().toString()
            question.status += 1
            question.lastShownDate = currentDate

            val setStatistics = dao.getSetStatisticsById(question.questionSetId).first()
            setStatistics.correctCount += 1
            setStatistics.totalAsked += 1
            setStatistics.lastTrainedDate = currentDate
            dao.updateQuestion(question)
            dao.updateQuestionSetStats(setStatistics)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun failQuestion(question: Question) {
        viewModelScope.launch {
            val currentDate = LocalDate.now().toString()
            question.status = 1
            question.lastShownDate = currentDate

            val setStatistics = dao.getSetStatisticsById(question.questionSetId).first()
            setStatistics.totalAsked += 1
            setStatistics.lastTrainedDate = currentDate
            dao.updateQuestion(question)
            dao.updateQuestionSetStats(setStatistics)
        }
    }

    fun insertSampleData() = viewModelScope.launch {
        try {
            val set1 = QuestionSet(name = "Math Formulas")
            val set2 = QuestionSet(name = "French Vocabulary")

            dao.insertQuestionSet(set1)
            dao.insertQuestionSetStats(QuestionSetStatistics(questionSetId = 1))
            dao.insertQuestionSet(set2)
            dao.insertQuestionSetStats(QuestionSetStatistics(questionSetId = 2))

            dao.insertQuestion(
                Question(
                    questionSetId = 1,
                    content = "What is Pi?",
                )
            )
            dao.insertAnswer(
                Answer(
                    questionId = 1,
                    answer = "3.14",
                    correct = true
                )
            )
            dao.insertAnswer(
                Answer(
                    questionId = 1,
                    answer = "trois-point-quatorze",
                    correct = true
                )
            )
            dao.insertAnswer(
                Answer(
                    questionId = 1,
                    answer = "3.14159265359",
                    correct = true
                )
            )
            dao.insertAnswer(
                Answer(
                    questionId = 1,
                    answer = "4.13",
                    correct = false
                )
            )
            dao.insertQuestion(
                Question(
                    questionSetId = 1,
                    content = "What is 1 + 1",
                )
            )
            dao.insertAnswer(
                Answer(
                    questionId = 2,
                    answer = "2",
                    correct = true
                )
            )
            dao.insertQuestion(
                Question(
                    questionSetId = 2,
                    content = "What is 'apple' in French?",
                )
            )
            dao.insertAnswer(
                Answer(
                    questionId = 3,
                    answer = "pomme",
                    correct = true
                )
            )
            dao.insertQuestion(
                Question(
                    questionSetId = 2,
                    content = "What is 'Strawberry' in French?"
                )
            )
            dao.insertAnswer(
                Answer(
                    questionId = 4,
                    answer = "Cerise",
                    correct = false
                )
            )
            dao.insertAnswer(
                Answer(
                    questionId = 4,
                    answer = "Fraise",
                    correct = true
                )
            )
            dao.insertAnswer(
                Answer(
                    questionId = 4,
                    answer = "Framboise",
                    correct = false
                )
            )
        }
        catch (e: Exception) {
            errorMessage = "Failed to insert sample data: ${e.message}"
        }
    }

}