package com.exallium.stashclient.app

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
