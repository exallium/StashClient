package com.exallium.stashclient.app.controller

import android.app.Fragment
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
                    val authInfo = username.getText().toString() + ':' + password.getText().toString()
                    val serverInfo = if (serverToggle.isChecked()) server.getText().toString() else "bitbucket.com"

                    // Register the auth info using AbstractAccountAuthenticator

                    // Try a simple request
                    val apiManager = StashApiManager.Factory.getOrCreate(serverInfo)
                    val adapter = apiManager.getAdapter(javaClass<Core.Profile.Repos>())

                    adapter.retrieveRecent().subscribe(LoginTestSubscriber())
                }
            })
        }
    }

    private class LoginTestSubscriber: Subscriber<Page<Repository>>() {
        override fun onError(e: Throwable?) {
            Logger.emit(TAG, "Login Failure", e)
        }

        override fun onCompleted() {
            unsubscribe()
        }

        override fun onNext(t: Page<Repository>?) {
            // Do Login
            Logger.emit(TAG, "Login Success")
        }

    }

}
