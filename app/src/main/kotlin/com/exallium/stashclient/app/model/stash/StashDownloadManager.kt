package com.exallium.stashclient.app.model.stash

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.LongSparseArray
import com.exallium.stashclient.app.controller.StashAccountManager
import com.exallium.stashclient.app.controller.logging.Logger
import rx.Observable
import rx.subjects.PublishSubject
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Manager class for downloading repositories
 */
public class StashDownloadManager private constructor(val context: Context) {

    companion object {
        val TAG = StashDownloadManager.javaClass.getSimpleName()
    }

    private val requestSubject: PublishSubject<DownloadRequest> = PublishSubject.create()
    private val responseSubject: PublishSubject<DownloadResponse> = PublishSubject.create()
    private val idToRequestMapping: LongSparseArray<DownloadRequest> = LongSparseArray()
    private val downloadService: DownloadManager

    public fun queueRequest(request: DownloadRequest): Observable<DownloadResponse> {
        requestSubject.onNext(request)
        return responseSubject.filter { it.request == request }
    }

    fun handleResponse(intent: Intent) {
        when (intent.getAction()) {
            DownloadManager.ACTION_DOWNLOAD_COMPLETE -> handleDownloadComplete(intent)
            DownloadManager.ACTION_NOTIFICATION_CLICKED -> handleNotificationClicked()
        }
    }

    private fun handleDownloadComplete(intent: Intent) {
        val extraId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
        val downloadRequest = idToRequestMapping.get(extraId)
        idToRequestMapping.remove(extraId)
        downloadRequest?.let {
            val cursor = downloadService.query(DownloadManager.Query().setFilterById(extraId))
            responseSubject.onNext(DownloadResponse(downloadRequest, 0, cursor))
        }
    }

    private fun handleNotificationClicked() {
        val intent = Intent()
        intent.setAction(DownloadManager.ACTION_VIEW_DOWNLOADS)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    public object Factory {
        private var stashDownloadManager: StashDownloadManager? = null

        public fun get(context: Context): StashDownloadManager {
            if (stashDownloadManager == null)
                stashDownloadManager = StashDownloadManager(context)
            return stashDownloadManager!!
        }
    }

    init {
        downloadService = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        requestSubject.subscribe({
            // Send the request to the download manager
            val apiUrl = StashAccountManager.Factory.get(context).getApiUrl()

            val uri = Uri.parse(apiUrl).buildUpon()
                .appendEncodedPath("plugins/servlet/archive/projects")
                .appendPath(it.projectKey)
                .appendPath("repos")
                .appendPath(it.repoSlug)
                .appendQueryParameter("at", it.at)

            val request = DownloadManager.Request(uri.build())
            request.setTitle("%s-%s-%s.zip".format(it.projectKey, it.repoSlug, it.at))

            Logger.emit(TAG, uri.build().toString())
            StashAccountManager.Factory.get(context).addHeaders(request)

            val res = downloadService.query(DownloadManager.Query()
                .setFilterByStatus(DownloadManager.STATUS_PAUSED
                        or DownloadManager.STATUS_PENDING
                        or DownloadManager.STATUS_RUNNING
                        or DownloadManager.STATUS_SUCCESSFUL))

            if (res.getCount() != 0) {
                responseSubject.onNext(DownloadResponse(it, -1, res))
            } else {
                val id = downloadService.enqueue(request)
                idToRequestMapping.put(id, it)
            }
        })
    }

}
