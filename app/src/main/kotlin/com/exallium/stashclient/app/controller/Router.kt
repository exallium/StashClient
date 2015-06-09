package com.exallium.stashclient.app.controller

import android.os.Bundle
import rx.subjects.PublishSubject

public object Router {
    public data class Request(val route: Route, val bundle: Bundle? = null)
    public val requestPublisher: PublishSubject<Request> = PublishSubject.create()
    public enum class Route {
        LOGIN,
        PROJECTS,
        PROJECT
    }

}
