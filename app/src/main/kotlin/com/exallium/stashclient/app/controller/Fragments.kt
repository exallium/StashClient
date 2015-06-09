package com.exallium.stashclient.app.controller

import android.app.Fragment


public fun createFragment(request: Router.Request): Fragment {
    val fragment = when(request.route) {
        Router.Route.LOGIN -> LoginFragment()
        Router.Route.PROJECTS -> ProjectsFragment()
        Router.Route.PROJECT -> ProjectFragment()
    }
    fragment.setArguments(request.bundle)
    return fragment
}




