package com.exallium.stashclient.app.controller

import android.app.Fragment


public fun createFragment(request: RouterActivity.RouteRequest): Fragment {
    val fragment = when(request.route) {
        RouterActivity.Route.LOGIN -> LoginFragment()
        RouterActivity.Route.PROJECTS -> ProjectsFragment()
    }
    fragment.setArguments(request.bundle)
    return fragment
}




