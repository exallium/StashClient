package com.exallium.stashclient.app.controller

import android.os.Bundle
import flow.Backstack
import flow.Flow
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

public object Router : Flow.Listener {

    override fun go(nextBackstack: Backstack?, direction: Flow.Direction?, callback: Flow.Callback?) {
        val request = nextBackstack?.current()?.getScreen() as Request
        requestPublisher.onNext(request)
        callback?.onComplete()
    }

    public data class Request(val route: Route, val bundle: Bundle? = null)
    private val requestPublisher: BehaviorSubject<Request> = BehaviorSubject.create()
    public val requestObservable: Observable<Request> = requestPublisher

    public enum class Route {
        LOGIN,
        LOGOUT,
        PROJECTS,
        PROJECT,
        REPOSITORY,
        SETTINGS
    }

    private val backstack = Backstack.single(Request(Route.PROJECTS))
    public val flow: Flow = Flow(backstack, this)
}
