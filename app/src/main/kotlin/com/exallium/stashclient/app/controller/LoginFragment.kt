package com.exallium.stashclient.app.controller

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.app.Fragment
import android.content.Context
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.widget.DrawerLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.exallium.stashclient.app.AbstractLoggingSubscriber
import com.exallium.stashclient.app.Constants
import com.exallium.stashclient.app.R
import com.exallium.stashclient.app.controller.logging.Logger
import com.exallium.stashclient.app.model.stash.Core
import com.exallium.stashclient.app.model.stash.Page
import com.exallium.stashclient.app.model.stash.Repository
import com.exallium.stashclient.app.model.stash.StashApiManager
import kotlinx.android.synthetic.fragment_login.*
import rx.Subscriber
import rx.android.view.OnClickEvent
import rx.android.view.ViewObservable

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
                            Constants.ACCOUNT_KEY)
                    val accountManager = AccountManager.get(getActivity())
                    val accountCreated = accountManager.addAccountExplicitly(account,
                            password.getText().toString(), null)

                    // Try a simple request
                    if (accountCreated) {
                        val apiManager = StashApiManager.Factory.get(getActivity())
                        val adapter = apiManager.getAdapter(javaClass<Core.Profile.Repos>())
                        StashAccountManager.Factory.get(getActivity()).setDefaultAccountName(account.name)
                        adapter.retrieveRecent().subscribe(LoginTestSubscriber(account))
                    } else {
                        Logger.emit(TAG, "Login Failure. Account Exists.")
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

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        val view = activity?.findViewById(R.id.toolbar)
        view?.setVisibility(View.GONE)
        val drawer = activity?.findViewById(R.id.drawer) as DrawerLayout?
        drawer?.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    private inner class LoginTestSubscriber(val account: Account): Subscriber<Page<Repository>>() {
        override fun onError(e: Throwable?) {
            Logger.emit(TAG, "Login Failure", e)
            StashAccountManager.Factory.get(getActivity()).logOut(getActivity())
        }

        override fun onCompleted() {
            unsubscribe()
        }

        override fun onNext(t: Page<Repository>?) {
            Logger.emit(TAG, "Login Success")
            getActivity().onBackPressed()
        }

    }

}
