package com.exallium.stashclient.app.controller

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Fragment
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.exallium.stashclient.app.AbstractLoggingSubscriber
import com.exallium.stashclient.app.R
import com.exallium.stashclient.app.controller.logging.Logger
import com.exallium.stashclient.app.model.stash.Core
import com.exallium.stashclient.app.model.stash.Page
import com.exallium.stashclient.app.model.stash.Repository
import com.exallium.stashclient.app.model.stash.StashApiManager
import rx.android.view.ViewObservable

import kotlinx.android.synthetic.fragment_login.*
import rx.Subscriber
import rx.android.view.OnClickEvent

public class LoginFragment: Fragment() {

    companion object {
        val TAG = LoginFragment.javaClass.getSimpleName()
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        if (view != null) {
            ViewObservable.clicks(submit).subscribe(object : AbstractLoggingSubscriber<OnClickEvent>(TAG) {
                override fun onNext(t: OnClickEvent?) {
                    // Perform Login w. Basic auth
                    val serverInfo = "https://%s".format(
                            if (serverToggle.isChecked()) server.getText().toString()
                            else "bitbucket.com")

                    // Register the auth info using AbstractAccountAuthenticator
                    val account = Account("%s@%s".format(username.getText().toString(), serverInfo),
                            "com.exallium.stashclient.ACCOUNT")
                    val accountManager = AccountManager.get(getActivity())
                    val accountCreated = accountManager.addAccountExplicitly(account,
                            password.getText().toString(), null)

                    // Try a simple request
                    if (accountCreated) {
                        val apiManager = StashApiManager.Factory.getOrCreate(getActivity(), account)
                        val adapter = apiManager.getAdapter(javaClass<Core.Profile.Repos>())

                        adapter.retrieveRecent().subscribe(LoginTestSubscriber(account.name))
                    } else {
                        Logger.emit(TAG, "Login Failure")
                    }
                }
            })
            ViewObservable.clicks(serverToggle).subscribe(object : AbstractLoggingSubscriber<OnClickEvent>(TAG) {
                override fun onNext(t: OnClickEvent?) {
                    server.setVisibility(if (serverToggle.isChecked()) View.VISIBLE else View.GONE)
                }
            })
        }
    }

    private inner class LoginTestSubscriber(val accountName: String): Subscriber<Page<Repository>>() {
        override fun onError(e: Throwable?) {
            Logger.emit(TAG, "Login Failure", e)
        }

        override fun onCompleted() {
            unsubscribe()
        }

        override fun onNext(t: Page<Repository>?) {
            // Do Login
            Logger.emit(TAG, "Login Success")

            getActivity().getSharedPreferences("com.exallium.stashclient.PREFERENCES", Context.MODE_PRIVATE)
                .edit().putString("com.exallium.stashclient.ACCOUNT", accountName)

            if ("com.exallium.stashclient.LOGIN".equals(getActivity().getIntent().getAction())) {
                getActivity().finish()
            } else {
                RouterActivity.routeRequestHandler.onNext(RouterActivity
                        .RouteRequest(RouterActivity.Route.RECENT_EVENTS))
            }
        }

    }

}
