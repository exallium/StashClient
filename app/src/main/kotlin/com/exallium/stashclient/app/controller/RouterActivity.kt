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

        val accountManager = StashAccountManager.Factory.getInstance(this)

        toolbar.setTitle(R.string.app_name)
        toolbar.setTitleTextColor(Color.WHITE)

        if (Constants.LOGIN_ACTION.equals(getIntent().getAction())) {
            requestFragment(Router.Request(Router.Route.LOGIN))
        } else {
            val defaultAccount = accountManager.account
            requestFragment(if (defaultAccount == null) Router.Request(Router.Route.LOGIN) else Router.Request(Router.Route.PROJECTS))
        }
    }

    private fun requestFragment(request: Router.Request) {
        if (currentRequest != request) {
            var transaction = getFragmentManager().beginTransaction()
            transaction.replace(R.id.fragment_container, createFragment(request))
            if (currentRequest != null)
                transaction.addToBackStack(null)
            transaction.commit()
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
