package com.exallium.stashclient.app.model.stash

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

public class DownloadReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        StashDownloadManager.Factory.get(context).handleResponse(intent)
    }
}
