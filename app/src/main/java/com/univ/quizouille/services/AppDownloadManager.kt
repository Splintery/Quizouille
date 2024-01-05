package com.univ.quizouille.services

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.univ.quizouille.database.AppDao

class AppDownloadManager(private var context: Context, private var dao: AppDao) {
    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    fun enqueueDownload(url: String): Long {
        val uri = Uri.parse(url)
        val request = DownloadManager.Request(uri)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "data.json")
        return downloadManager.enqueue(request)
    }
}