package com.exallium.stashclient.app.controller

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import com.exallium.stashclient.app.AbstractLoggingSubscriber
import com.exallium.stashclient.app.R
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

        toolbar.setTitle(R.string.app_name)
        toolbar.setTitleTextColor(Color.WHITE)

        if ("com.exallium.stashclient.LOGIN".equals(getIntent().getAction())) {
            requestFragment(RouteRequest(Route.LOGIN))
        } else {
            // First thing's first, figure out where we're going
            val defaultAccountName = getSharedPreferences("com.exallium.stashclient.PREFERENCES", Context.MODE_PRIVATE).getString("com.exallium.stashclient.DEFAULT_ACCOUNT", null)
            val accountManager = AccountManager.get(this)

            val defaultAccountList = accountManager.getAccountsByType("com.exallium.stashclient.ACCOUNT").filter {
                it.name.equals(defaultAccountName)
            }

            if (defaultAccountList.isEmpty()) {
                // Go to login
                requestFragment(RouteRequest(Route.LOGIN))
            } else {
                // Go to home page
                account = defaultAccountList.get(0)
                val bundle = Bundle()
                bundle.putParcelable("com.exallium.stashclient.ACCOUNT", account)
                requestFragment(RouteRequest(Route.PROJECTS, bundle))
            }
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
