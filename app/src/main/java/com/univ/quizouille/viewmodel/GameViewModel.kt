package com.univ.quizouille.viewmodel

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.univ.quizouille.database.AppApplication
import com.univ.quizouille.model.Answer
import com.univ.quizouille.model.Question
import com.univ.quizouille.model.QuestionSet
import com.univ.quizouille.model.QuestionSetStatistics
import com.univ.quizouille.services.AppBroadcastReceiver
import com.univ.quizouille.services.AppDownloadManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.random.Random
import com.univ.quizouille.utilities.stringToLocalDate
import kotlin.Exception
import android.content.IntentFilter
import android.os.Environment
import android.util.Log
import androidx.core.text.HtmlCompat
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.lang.IllegalArgumentException

@RequiresApi(Build.VERSION_CODES.O)
class GameViewModel(private var application: Application) : AndroidViewModel(application) {
    private val dao = (application as AppApplication).database.appDao()

    var downloadManager: AppDownloadManager
    private var broadcastReceiver: AppBroadcastReceiver
    private val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)

    init {
        downloadManager = AppDownloadManager(application, (application as AppApplication).database.appDao())
        broadcastReceiver = AppBroadcastReceiver(gameViewModel = this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            application.registerReceiver(broadcastReceiver, filter, Context.RECEIVER_EXPORTED)
        }
        insertSampleData()
    }

    var snackBarMessage by mutableStateOf("")
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
                snackBarMessage = "Duplicate data: set allready exist with name $setName"
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
                snackBarMessage = "Duplicate data: Failed to insert"
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun insertAnswer(questionId: Int, answer: String, correct: Boolean) {
        viewModelScope.launch {
            try {
                dao.insertAnswer(Answer(questionId = questionId, answer = answer, correct = correct))
            } catch (e: Exception) {
                snackBarMessage = "Answer allready exist for question $questionId"
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

    fun downloadData(url: String): Long {
        if(url.isEmpty()) {
            snackBarMessage = "Le lien ne doit pas être vide"
            return -1L
        }
        return try {
            downloadManager.enqueueDownload(url)
        } catch (e: IllegalArgumentException) {
            snackBarMessage = "Le lien n'a pas un format valide"
            -1L
        }
    }

    override fun onCleared() {
        super.onCleared()
        application.unregisterReceiver(broadcastReceiver)
    }

    fun parseFile() {
        val file = File(application.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "data.json")
        try {
            val inputStream = FileInputStream(file)
            val reader = BufferedReader(InputStreamReader(inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                line?.let {
                    parseJsonFile(it)
                }
            }
            reader.close()
            inputStream.close()

            deleteFile(file = file)
        }
        catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun decodeHTMLEncoding(s: String): String {
        return HtmlCompat.fromHtml(s, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
    }

    /**
     * Notre parsing de fichier JSON est basée sur l'API suivante:
     *            https://opentdb.com/api_config.php
     */
    private fun parseJsonFile(line: String) = viewModelScope.launch {
        val jsonObject = JSONObject(line)
        // on récupère le tableau "results" du JSON
        val resultsArray = jsonObject.getJSONArray("results")
        if (resultsArray.length() == 0) {
            snackBarMessage = "Le fichier n'a pas pu ếtre interprété"
            return@launch
        }
        var firstCategory = resultsArray.getJSONObject(0).getString("category")
        var multipleCategories = false

        // on génère un nom de jeu de questions aléatoire
        val questionSetName = "${System.currentTimeMillis()}"
        val set = QuestionSet(name = questionSetName)
        val setId = dao.insertQuestionSet(set)

        // on créé les statistiques liées au jeu de questions
        val questionSetStats = QuestionSetStatistics(questionSetId = setId.toInt())
        dao.insertQuestionSetStats(questionSetStats)
        for (i in 0 until resultsArray.length()) {
            // question courante
            val questionItem = resultsArray.getJSONObject(i)

            val category = questionItem.getString("category")
            if (category != firstCategory)
                multipleCategories = true
            var questionText = questionItem.getString("question")
            questionText = decodeHTMLEncoding(questionText)
            var correctAnswer = questionItem.getString("correct_answer")
            correctAnswer = decodeHTMLEncoding(correctAnswer)
            val incorrectAnswers = questionItem.getJSONArray("incorrect_answers")

            val question = Question(questionSetId = setId.toInt(), content = questionText)
            val questionId = dao.insertQuestion(question)

            // on insère la question valide
            val correctAnswerEntity = Answer(questionId = questionId.toInt(), answer = correctAnswer, correct = true)
            dao.insertAnswer(correctAnswerEntity)

            // puis toutes les mauvaises questions
            for (j in 0 until incorrectAnswers.length()) {
                var incorrectAnswer = incorrectAnswers.getString(j)
                incorrectAnswer = decodeHTMLEncoding(incorrectAnswer)
                val incorrectAnswerEntity = Answer(questionId = questionId.toInt(), answer = incorrectAnswer, correct = false)
                dao.insertAnswer(incorrectAnswerEntity)
            }
        }
        // on renomme le jeu de question s'il correspond à une catégorie spécifique
        if (!multipleCategories) {
            firstCategory = decodeHTMLEncoding(firstCategory)
            set.name = "$firstCategory $setId"
        }
        else
            set.name = "Jeu multi thèmes $setId"
        set.setId = setId.toInt()
        dao.updateQuestionSet(set)
        snackBarMessage = "Le jeu de question a été importé, bon apprentissage !"
    }

    private fun deleteFile(file: File) {
        if (file.exists()) {
            val isDeleted = file.delete()
            if (isDeleted) {
                Log.d("BroadcastReceiver", "File deleted successfully")
            }
            else {
                Log.d("BroadcastReceiver", "Failed to delete file")
            }
        }
    }

    fun resetSnackbarMessage() {
        snackBarMessage = ""
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
            snackBarMessage = "Failed to insert sample data: ${e.message}"
        }
    }
}