package com.univ.quizouille.services

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class NotifyWorker(private val context: Context, workerParameters: WorkerParameters) : Worker(context, workerParameters) {
    override fun doWork(): Result {
        return Result.success()
    }
}