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

        // General
        LOGIN,
        LOGOUT,
        SETTINGS,

        // Projects
        PROJECTS,
        PROJECT,

        // Repositories
        DOWNLOAD,
        BRANCH,
        PULL_REQUEST,
        FORK,
        SOURCES,
        COMMITS,
        BRANCHES,
        PULL_REQUESTS
    }

    private val backstack = Backstack.single(Request(Route.PROJECTS))
    private val flow: Flow = Flow(backstack, this)

    public fun goTo(request: Request, isAction: Boolean = false) {
        if (isAction) {
            requestPublisher.onNext(request)
        } else {
            flow.goTo(request)
        }
    }

    public fun replaceTo(request: Request) {
        flow.replaceTo(request)
    }

    fun getLastRequest(): Request {
        return flow.getBackstack().current().getScreen() as Request
    }

    fun goBack(): Boolean {
        return flow.goBack()
    }

}
