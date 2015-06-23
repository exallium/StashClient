package com.exallium.stashclient.app.model.stash

import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import com.exallium.stashclient.app.Constants

public data class DownloadRequest(val projectKey: String, val repoSlug: String, val at: String) {
    companion object {
        public fun fromBundle(bundle: Bundle): DownloadRequest {
            return DownloadRequest(
                    projectKey = bundle.getString(Constants.PROJECT_KEY),
                    repoSlug = bundle.getString(Constants.REPOSITORY_SLUG),
                    at = ""
            )
        }
    }
}

public data class DownloadResponse(val request: DownloadRequest,
                                   val size: Long,
                                   val cursor: Cursor)
