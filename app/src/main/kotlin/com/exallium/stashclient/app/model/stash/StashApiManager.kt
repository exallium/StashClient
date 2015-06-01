package com.exallium.stashclient.app.model.stash

import android.util.Base64
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
public class StashApiManager(val apiUrl: String) {

    val restAdapter: RestAdapter

    val adapterMap: MutableMap<Class<*>, Any> = HashMap()

    init {
        restAdapter = RestAdapter.Builder().setEndpoint(apiUrl).build()
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

    object Factory {

        private val managers: MutableMap<String, StashApiManager> = HashMap()

        public fun getOrCreate(apiUrl: String): StashApiManager {
            var manager = managers.get(apiUrl)
            if (manager == null) {
                manager = StashApiManager(apiUrl)
                managers.put(apiUrl, manager)
            }
            return manager
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
