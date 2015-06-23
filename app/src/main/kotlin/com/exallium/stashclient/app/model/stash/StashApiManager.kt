package com.exallium.stashclient.app.model.stash

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.util.Base64
import com.exallium.stashclient.app.controller.StashAccountManager
import com.exallium.stashclient.app.controller.logging.Logger
import com.exallium.stashclient.app.getApiUrl
import com.exallium.stashclient.app.getUsername
import com.squareup.okhttp.Interceptor
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Response
import retrofit.Profiler
import retrofit.RequestInterceptor
import retrofit.RestAdapter
import retrofit.http.*
import rx.Observable
import java.util.*

/**
 * StashApiManager is an adapter generation tool tied to a single Stash instance.
 * Multiple Stash instances would thus need multiple instances of this object.
 *
 * Adapters are cached, so we only create each once, but we do it lazily to keep object
 * creation to a minimum.
 */
public class StashApiManager(val context: Context) {

    private val restAdapter: RestAdapter
    private val adapterMap: MutableMap<Class<*>, Any> = HashMap()

    init {
        // Break account name into user and api
        val apiUrl = StashAccountManager.Factory.get(context).getApiUrl()
        restAdapter = RestAdapter.Builder().setEndpoint(apiUrl)
                .setProfiler(object : Profiler<Long> {
                    override fun beforeCall(): Long? {
                        return null
                    }

                    override fun afterCall(requestInfo: Profiler.RequestInformation?, elapsedTime: Long, statusCode: Int, beforeCallData: Long?) {
                        Logger.emit(TAG, "HTTPS %d  %s %s (%dms)".format(statusCode, requestInfo?.getMethod(), requestInfo?.getRelativePath(), elapsedTime))
                    }
                })
                .setRequestInterceptor(StashAccountManager.Factory.get(context))
                .build()
    }

    public fun <T> getAdapter(adapterClass: Class<T>): T {
        val cachedAdapter = adapterMap.get(adapterClass)
        if (cachedAdapter != null) {
            return cachedAdapter as T
        } else {
            val newAdapter = restAdapter.create(adapterClass)
            adapterMap.put(adapterClass, newAdapter)
            return newAdapter
        }
    }

    companion object {
        val TAG = StashApiManager.javaClass.getSimpleName()
    }

    object Factory {

        private val managers: MutableMap<String, StashApiManager> = HashMap()

        private fun get(context: Context, accountName: String): StashApiManager {
            var manager = managers.get(accountName)
            if (manager == null) {
                manager = StashApiManager(context)
                managers.put(accountName, manager)
            }
            return manager
        }

        public fun get(context: Context): StashApiManager {
            val account = StashAccountManager.Factory.get(context).getAccountDetails()
            return get(context, account!!)
        }
    }

}

/** TODO: This all gets moved to their own files as we implement these apis */

public interface Audit {

}

public interface BranchPermissions {

}

public interface BranchUtils {

}

public interface BuildIntegration {

}

public interface CommentLikes {

}

public interface JiraIntegration {

}

public interface Ssh {

}

public interface Git {

}
