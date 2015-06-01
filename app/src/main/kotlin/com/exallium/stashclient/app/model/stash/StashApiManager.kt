package com.exallium.stashclient.app.model.stash

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.util.Base64
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
public class StashApiManager(val context: Context, val account: Account) {

    val restAdapter: RestAdapter

    val adapterMap: MutableMap<Class<*>, Any> = HashMap()

    init {
        // Break account name into user and api
        val apiUrl = getApiUrl(account)
        restAdapter = RestAdapter.Builder().setEndpoint(apiUrl)
                .setRequestInterceptor(AuthenticationInterceptor(context, account)).build()
    }

    private class AuthenticationInterceptor(val context: Context, val account: Account) : RequestInterceptor {

        val authManager = AccountManager.get(context)

        override fun intercept(request: RequestInterceptor.RequestFacade?) {
            val b64Creds = Base64.encode("%s:%s".format(getUsername(account),
                    authManager.getPassword(account)).toByteArray(), Base64.DEFAULT)
            request?.addHeader("Authorization", "Basic " + b64Creds)
        }
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
        private fun getApiUrl(account: Account): String {
            return account.name.substring(account.name.indexOf('@') + 1)
        }

        private fun getUsername(account: Account): String {
            return account.name.substring(0, account.name.indexOf('@'))
        }
    }

    object Factory {

        private val managers: MutableMap<String, StashApiManager> = HashMap()

        public fun getOrCreate(context: Context, account: Account): StashApiManager {
            var manager = managers.get(account.name)
            if (manager == null) {
                manager = StashApiManager(context, account)
                managers.put(account.name, manager)
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
