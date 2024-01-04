package com.univ.quizouille.services

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import com.univ.quizouille.database.AppDao

class AppDownloadManager(private var context: Context,private var dao: AppDao) {
    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    fun enqueueDownload(url: String): Long {
        val adr = "https://www.collection-appareils.fr/fed/images/FED22.jpg"
        val uri = Uri.parse(adr)
        val request = DownloadManager.Request(uri).setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE).setDestinationInExternalFilesDir(context, Environment.DIRECTORY_PICTURES, "oui")
        return downloadManager.enqueue(request)
    }
}