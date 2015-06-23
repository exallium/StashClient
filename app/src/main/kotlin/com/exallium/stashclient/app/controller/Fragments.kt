package com.exallium.stashclient.app.controller

import android.app.Fragment
import com.exallium.stashclient.app.controller.core.projects.ProjectFragment
import com.exallium.stashclient.app.controller.core.projects.ProjectsFragment
import com.exallium.stashclient.app.controller.core.projects.repos.*


public fun createFragment(request: Router.Request): Fragment {
    val fragment = when(request.route) {
        // Projects
        Router.Route.PROJECTS -> ProjectsFragment()
        Router.Route.PROJECT -> ProjectFragment()

        // Repositories
        Router.Route.BRANCH -> BranchFragment()
        Router.Route.FORK -> ForkFragment()
        Router.Route.PULL_REQUEST -> PullRequestFragment()
        Router.Route.SOURCES -> SourcesFragment()
        Router.Route.COMMITS -> CommitsFragment()
        Router.Route.BRANCHES -> BranchesFragment()
        Router.Route.PULL_REQUESTS -> PullRequestsFragment()

        // General Stuff
        Router.Route.SETTINGS -> SettingsFragment()
        else -> LoginFragment()
    }
    fragment.setArguments(request.bundle)
    return fragment
}




