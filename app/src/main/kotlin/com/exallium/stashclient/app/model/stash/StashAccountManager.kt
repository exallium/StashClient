package com.exallium.stashclient.app.controller

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.net.Uri
import android.util.Base64
import com.exallium.stashclient.app.Constants
import com.exallium.stashclient.app.getApiUrl
import com.exallium.stashclient.app.getUsername
import com.squareup.okhttp.Interceptor
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Response
import com.squareup.picasso.OkHttpDownloader
import com.squareup.picasso.Picasso
import com.squareup.picasso.Request
import retrofit.RequestInterceptor

public class StashAccountManager private constructor(val context: Context) : Interceptor, RequestInterceptor {

    // Retrofit Interceptor
    override fun intercept(request: RequestInterceptor.RequestFacade?) {
        request?.addHeader("Authorization", "Basic " + getCredentials())
    }

    // Picasso OkHttp Interceptor
    override fun intercept(chain: Interceptor.Chain?): Response? {
        val request = chain?.request()
        val newRequest = request?.newBuilder()
                ?.addHeader("Authorization", "Basic " + getCredentials())
                ?.build()
        return chain?.proceed(newRequest)
    }

    private fun getCredentials(): String {
         return Base64.encodeToString("%s:%s".format(account?.getUsername(),
                accountManager.getPassword(account)).toByteArray(), Base64.DEFAULT)
    }

    object Factory {
        private var manager: StashAccountManager? = null
        public fun getInstance(context: Context): StashAccountManager {
            if (manager == null)
                manager = StashAccountManager(context)
            return manager!!
        }
    }

    private var _account: Account?  = null
    public var account: Account? = null
        get() {
            if (_account == null)
                _account = getDefaultAccount()
            return _account
        }
        private set

    private val accountManager = AccountManager.get(context)
    public val okHttpClient: OkHttpClient = OkHttpClient()

    init {
        okHttpClient.interceptors().add(this)
        Picasso.setSingletonInstance(Picasso.Builder(context)
                .downloader(OkHttpDownloader(okHttpClient)).build())
    }

    public fun getDefaultAccount(): Account? {
        val preferences = context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE)
        val defaultAccountName = preferences.getString(Constants.ACCOUNT_KEY, null)

        val accountList = accountManager.getAccountsByType(Constants.ACCOUNT_KEY)
        val defaultAccountList = accountList.filter { it.name.equals(defaultAccountName) }

        if (defaultAccountList.isEmpty()) {
            val backupAccountList = accountList.filterNot { it.name.equals(defaultAccountName) }
            val backupAccount = if (backupAccountList.size() != 0) backupAccountList.get(0) else null

            backupAccount?.let {
                preferences.edit().putString(Constants.ACCOUNT_KEY, backupAccount?.name).apply()
            }

            return backupAccount
        } else {
            return defaultAccountList.get(0)
        }
    }

    public fun setDefaultAccount(account: Account) {
        val defaultAccountName = account.name
        context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE)
            .edit().putString(Constants.ACCOUNT_KEY, defaultAccountName).apply()
    }

}

