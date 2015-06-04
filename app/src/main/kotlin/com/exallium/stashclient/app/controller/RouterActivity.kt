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
        public val routeRequestHandler: PublishSubject<RouteRequest> = PublishSubject.create()
    }

    public data class RouteRequest(val route: Route, val bundle: Bundle? = null)

    public enum class Route {
        LOGIN,
        PROJECTS
    }

    var account: Account? = null
    var currentRequest: RouteRequest? = null
    var currentSubscriber: RouteRequestSubscriber? = null

    private inner class RouteRequestSubscriber : AbstractLoggingSubscriber<RouteRequest>(TAG) {
        override fun onNext(t: RouteRequest?) {
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
            requestFragment(RouteRequest(Route.LOGIN))
        } else {
            val defaultAccount = accountManager.account
            requestFragment(if (defaultAccount == null) RouteRequest(Route.LOGIN) else RouteRequest(Route.PROJECTS))
        }
    }

    private fun requestFragment(routeRequest: RouteRequest) {
        if (currentRequest != routeRequest) {
            var transaction = getFragmentManager().beginTransaction()
            transaction.add(R.id.fragment_container, createFragment(routeRequest))
            if (currentRequest != null)
                transaction.addToBackStack(null)
            transaction.commit()
            currentRequest = routeRequest
        }
    }

    override fun onResume() {
        super.onResume()
        currentSubscriber = RouteRequestSubscriber()
        routeRequestHandler.subscribe(currentSubscriber)
    }

    override fun onPause() {
        super.onPause()
        currentSubscriber?.unsubscribe()
        currentSubscriber = null
    }

}
