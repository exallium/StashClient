package com.exallium.stashclient.app

import android.accounts.Account
import com.exallium.stashclient.app.controller.logging.Logger
import rx.Subscriber

public abstract class AbstractLoggingSubscriber<T>(val tag: String): Subscriber<T>() {

    override fun onError(e: Throwable?) {
        Logger.emit(tag, "Something bad happened", e)
    }

    override fun onCompleted() {
        unsubscribe()
    }

}

public object Constants {
    val PREFERENCES = "com.exallium.stashclient.PREFERENCES"
    val ACCOUNT_KEY = "com.exallium.stashclient.ACCOUNT"

    val LOGIN_ACTION = "com.exallium.stashclient.LOGIN"
}

public fun Account.getApiUrl(): String {
    return this.name.substring(this.name.indexOf('@') + 1)
}

public fun Account.getUsername(): String {
    return this.name.substring(0, this.name.indexOf('@'))
}
