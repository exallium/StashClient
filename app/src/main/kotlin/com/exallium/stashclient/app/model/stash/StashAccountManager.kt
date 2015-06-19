package com.exallium.stashclient.app.controller

import android.accounts.Account
import android.accounts.AccountManager
import android.accounts.AccountManagerCallback
import android.accounts.AccountManagerFuture
import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
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
import rx.subjects.BehaviorSubject
import java.util.*

public class StashAccountManager private constructor(val context: Context) : Interceptor, RequestInterceptor {

    private val accountSubject: BehaviorSubject<String> = BehaviorSubject.create()

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


    private object logoutCallback : AccountManagerCallback<Bundle> {
        override fun run(future: AccountManagerFuture<Bundle>?) {
            Router.flow.replaceTo(Router.Request(Router.Route.PROJECTS))
        }
    }

    private fun getDefaultAccountName(): String? {
        val preferences = context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE)
        return preferences.getString(Constants.ACCOUNT_KEY, null)
    }

    public fun setDefaultAccountName(accountName: String?) {
        val preferences = context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE)
        preferences.edit().putString(Constants.ACCOUNT_KEY, accountName).apply()
    }

    object Factory {
        private var manager: StashAccountManager? = null
        public fun get(context: Context): StashAccountManager {
            if (manager == null)
                manager = StashAccountManager(context)
            return manager!!
        }
    }


    private var _account: Account?  = null
    private var account: Account?
        get() {
            if (_account == null) {
                _account = getDefaultAccount()
                accountSubject.onNext(_account?.name)
            }
            return _account
        }
        private set(value: Account?) {
            _account = value
            accountSubject.onNext(account?.name)
        }

    private val accountManager = AccountManager.get(context)
    public val okHttpClient: OkHttpClient = OkHttpClient()

    init {
        okHttpClient.interceptors().add(this)
        Picasso.setSingletonInstance(Picasso.Builder(context)
                .downloader(OkHttpDownloader(okHttpClient)).build())
    }

    private fun getDefaultAccount(): Account? {
        val defaultAccountName = getDefaultAccountName()

        val accountList = accountManager.getAccountsByType(Constants.ACCOUNT_KEY)
        val defaultAccountList = accountList.filter { it.name.equals(defaultAccountName) }

        if (defaultAccountList.isEmpty()) {
            val backupAccountList = accountList.filterNot { it.name.equals(defaultAccountName) }
            val backupAccount = if (backupAccountList.size() != 0) backupAccountList.get(0) else null

            backupAccount?.let {
                setDefaultAccountName(accountName = backupAccount?.name)
            }

            return backupAccount
        } else {
            return defaultAccountList.get(0)
        }
    }

    public fun getAccountDetails(): String? {
        return account?.name
    }

    public fun getAccountUsername(): String? {
        return account?.getUsername()
    }

    public fun isLoggedIn(): Boolean {
        return account != null
    }

    public fun logOut(activity: Activity) {
        if (_account != null) {
            if (_account?.equals(getDefaultAccount())?:false)
                setDefaultAccountName(null)
            accountManager.removeAccount(_account, activity, logoutCallback, null)
            account = null
        }
    }

    public fun getApiUrl(): String? {
        return account?.getApiUrl()
    }

    public fun getAccountPrettyUrl(): String? {
        return if (account != null) "@ %s".format(Uri.parse(account?.getApiUrl()).getAuthority()) else "Logged Out"
    }

    public fun accountChangeObservable(): rx.Observable<String> {
        return accountSubject
    }
}

