package com.exallium.stashclient.app.controller.logging

import android.util.Log
import rx.Subscriber
import rx.subjects.PublishSubject

public object Logger {

    private val publisher: PublishSubject<Event> = PublishSubject.create()

    public class AndroidLogger: Subscriber<Event>() {
        override fun onNext(event: Event?) {
            if (event != null) {
                Log.d(event.tag, event.message, event.throwable)
            } else {
                Log.d("OnEvent", "Null Event Received")
            }

        }

        override fun onCompleted() {
            unsubscribe()
        }

        override fun onError(e: Throwable?) {
            Log.d("OnEvent", "Something bad happened", e)
        }
    }

    private data class Event(val tag: String, val message: String, val throwable: Throwable? = null)

    public fun emit(tag: String, message: String, throwable: Throwable? = null) {
        if (!publisher.hasObservers())
            publisher.subscribe(AndroidLogger())
        publisher.onNext(Event(tag, message, throwable))
    }
}
