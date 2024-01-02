package com.univ.quizouille.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import com.univ.quizouille.model.Question
import com.univ.quizouille.model.QuestionSet
import com.univ.quizouille.model.QuestionSetStatistics

@Database(entities = [QuestionSet::class, Question::class, QuestionSetStatistics::class], version = 9, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getDataBase(c: Context): AppDatabase {
            if (instance != null) return instance!!
            val db = databaseBuilder(c.applicationContext, AppDatabase::class.java, "QuizouilleDB")
                .fallbackToDestructiveMigration().build()
            instance = db
            return instance!!
        }
    }
}