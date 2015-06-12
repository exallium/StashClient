package com.exallium.stashclient.app.controller

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import com.exallium.stashclient.app.AbstractLoggingSubscriber
import com.exallium.stashclient.app.Constants
import com.exallium.stashclient.app.R
import com.squareup.picasso.OkHttpDownloader
import com.squareup.picasso.Picasso
import rx.subjects.PublishSubject

import kotlinx.android.synthetic.activity_router.*
import rx.Subscriber
import rx.android.lifecycle.LifecycleObservable

public class RouterActivity : Activity() {

    companion object {
        private val TAG = RouterActivity.javaClass.getSimpleName()
    }

    var account: Account? = null
    var currentRequest: Router.Request? = null
    var currentSubscriber: RouteRequestSubscriber? = null

    private inner class RouteRequestSubscriber : AbstractLoggingSubscriber<Router.Request>(TAG) {
        override fun onNext(t: Router.Request?) {
            if (t != null)
                requestFragment(t)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_router)

        toolbar.setTitle(R.string.app_name)
        toolbar.setTitleTextColor(Color.WHITE)

        if (Constants.LOGIN_ACTION.equals(getIntent().getAction())) {
            val loginBundle = Bundle()
            loginBundle.putString(Constants.NEXT_PAGE, Router.Route.PROJECTS.name())
            requestFragment(Router.Request(Router.Route.LOGIN))
        } else {
            requestFragment(Router.Request(Router.Route.PROJECTS))
        }
    }

    private fun requestFragment(request: Router.Request) {
        val accountManager = StashAccountManager.Factory.getInstance(this)
        val account = accountManager.account
        if (account == null && request.route != Router.Route.LOGIN) {
            val bundle = Bundle()
            bundle.putString(Constants.NEXT_PAGE, request.route.name())
            bundle.putBundle(Constants.NEXT_BUNDLE, request.bundle)
            requestFragment(Router.Request(Router.Route.LOGIN))
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
        Router.requestPublisher.subscribe(currentSubscriber)
    }

    override fun onPause() {
        super.onPause()
        currentSubscriber?.unsubscribe()
        currentSubscriber = null
    }

}
