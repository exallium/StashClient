package com.exallium.stashclient.app.controller

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import com.exallium.stashclient.app.R
import rx.subjects.PublishSubject

import kotlinx.android.synthetic.activity_router.*

public class RouterActivity : Activity() {

    companion object {
        public val routeRequestHandler: PublishSubject<RouteRequest> = PublishSubject.create()
    }

    public data class RouteRequest(val route: Route, val bundle: Bundle? = null)

    public enum class Route {
        LOGIN,
        RECENT_EVENTS
    }

    var account: Account? = null
    var currentRequest: RouteRequest? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_router)

        toolbar.setTitle(R.string.app_name)

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
                requestFragment(RouteRequest(Route.RECENT_EVENTS, bundle))
            }
        }
    }

    private fun requestFragment(routeRequest: RouteRequest) {
        if (currentRequest != routeRequest) {
            var transaction = getFragmentManager().beginTransaction()
            when (routeRequest.route) {
                Route.LOGIN -> transaction.add(R.id.fragment_container, LoginFragment())
                Route.RECENT_EVENTS -> transaction.add(R.id.fragment_container, LoginFragment())
            }
            if (currentRequest != null)
                transaction.addToBackStack(null)
            transaction.commit()
            currentRequest = routeRequest
        }
    }
}
