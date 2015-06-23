package com.exallium.stashclient.app.controller

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.MenuItem
import com.exallium.stashclient.app.*
import com.exallium.stashclient.app.controller.logging.Logger
import com.exallium.stashclient.app.model.stash.Core
import com.exallium.stashclient.app.model.stash.DownloadRequest
import com.exallium.stashclient.app.model.stash.StashApiManager
import com.exallium.stashclient.app.model.stash.StashDownloadManager
import com.squareup.picasso.OkHttpDownloader
import com.squareup.picasso.Picasso
import rx.subjects.PublishSubject

import kotlinx.android.synthetic.activity_router.*
import kotlinx.android.synthetic.activity_fragment_container.*
import kotlinx.android.synthetic.drawer_header.*
import rx.Subscriber
import rx.android.lifecycle.LifecycleObservable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

public class RouterActivity : Activity() {

    companion object {
        private val TAG = RouterActivity.javaClass.getSimpleName()
    }

    var currentRequest: Router.Request? = null
    var currentSubscriber: RouteRequestSubscriber? = null
    var accountChangeSubscriber: AccountChangeSubscriber? by SubscriberDelegate { AccountChangeSubscriber() }

    private inner class RouteRequestSubscriber : AbstractLoggingSubscriber<Router.Request>(TAG) {
        override fun onNext(t: Router.Request?) {
            if (t != null) {
                when (t.route) {
                    Router.Route.DOWNLOAD -> onDownload(t.bundle)
                    Router.Route.SNACKBAR -> onSnackbar(t.bundle)
                    Router.Route.LOGOUT -> StashAccountManager.Factory
                            .get(this@RouterActivity)
                            .logOut(this@RouterActivity);
                    else -> requestFragment(t)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_router)

        toolbar.setTitle(R.string.app_name)
        toolbar.setTitleTextColor(Color.WHITE)


        if (Constants.LOGIN_ACTION.equals(getIntent().getAction())) {
            requestFragment(Router.Request(Router.Route.LOGIN))
        } else {
            val request = Router.getLastRequest()
            requestFragment(request)
        }
    }

    private fun requestFragment(request: Router.Request) {
        val accountManager = StashAccountManager.Factory.get(this)
        if (!accountManager.isLoggedIn() && request.route != Router.Route.LOGIN) {
            Router.get(Router.Request(Router.Route.LOGIN))
            return
        }

        if (currentRequest != request) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, createFragment(request))
                    .commit()
            currentRequest = request
        }

    }

    override fun onResume() {
        super.onResume()
        currentSubscriber = RouteRequestSubscriber()
        Router.requestObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(currentSubscriber)
        StashAccountManager.Factory.get(this)
                .accountChangeObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(accountChangeSubscriber)
    }

    override fun onPause() {
        super.onPause()
        currentSubscriber?.unsubscribe()
        currentSubscriber = null
        accountChangeSubscriber = null
    }

    override fun onBackPressed() {
        if (!Router.goBack())
            super.onBackPressed()
    }

    private inner class AccountChangeSubscriber : Subscriber<String?>() {
        override fun onCompleted() {
        }

        override fun onNext(t: String?) {
            if (t != null) {
                val userAdapter = StashApiManager.Factory.get(this@RouterActivity).getAdapter(javaClass<Core.Users>())
                val account = Account(t, Constants.ACCOUNT_KEY)
                drawer_header_server.setText(StashAccountManager.Factory.get(this@RouterActivity).getAccountPrettyUrl())
                userAdapter.single(account.getUsername())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .forEach({
                            drawer_header_username.setText("%s (%s)".format(it.displayName, it.name))
                        })

                // setup drawer
            }
        }

        override fun onError(e: Throwable?) {
            Logger.emit(TAG, "Something bad happened", e)
        }

    }

    private fun onDownload(bundle: Bundle?) {

        if (bundle == null)
            return

        // Send a download request to the download manager
        StashDownloadManager.Factory.get(this)
                .queueRequest(DownloadRequest.fromBundle(bundle))
                .subscribe({
                    val view = findViewById(android.R.id.content)
                    if (view != null) {
                        if (it.cursor.getCount() == 0) {
                            Snackbar.make(view, R.string.download_canceled, Snackbar.LENGTH_SHORT).show()
                        } else {
                            it.cursor.moveToFirst()
                            val status = it.cursor.getInt(it.cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))

                            val out = when (status) {
                                DownloadManager.STATUS_FAILED -> R.string.download_failed
                                DownloadManager.STATUS_SUCCESSFUL -> R.string.download_success
                                else -> R.string.error
                            }

                            Snackbar.make(view, out, Snackbar.LENGTH_SHORT).show()
                        }
                    }
                })
    }

    private fun onSnackbar(bundle: Bundle?) {
        if (bundle == null) return
        Snackbar.make(findViewById(android.R.id.content),
                bundle.getString(Constants.MESSAGE), bundle.getInt(Constants.MESSAGE_LENGTH)).show()
    }

}
