package com.univ.quizouille.services

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AppBroadcastReceiver(private val downloadManager: AppDownloadManager) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val downloadId = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: -1
    }
}