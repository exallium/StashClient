package com.exallium.stashclient.app.controller.core.projects.repos

import android.app.Activity
import android.app.DownloadManager
import android.app.Fragment
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.widget.DrawerLayout
import android.view.MenuItem
import com.exallium.stashclient.app.Constants
import com.exallium.stashclient.app.R
import com.exallium.stashclient.app.controller.Router
import com.exallium.stashclient.app.controller.logging.Logger
import com.exallium.stashclient.app.model.stash.DownloadRequest
import com.exallium.stashclient.app.model.stash.StashDownloadManager

public open class BaseRepositoryFragment : Fragment() {

    companion object {
        val TAG = BaseRepositoryFragment.javaClass.getSimpleName()
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        val drawer = activity.findViewById(R.id.drawer) as DrawerLayout?
        drawer?.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        val nav = activity.findViewById(R.id.nav) as NavigationView?
        nav?.getMenu()?.clear()
        nav?.inflateMenu(R.menu.menu_repository)
        nav?.setNavigationItemSelectedListener(object : NavigationView.OnNavigationItemSelectedListener {
            override fun onNavigationItemSelected(p0: MenuItem?): Boolean {
                when (p0?.getItemId()) {
                    R.id.menu_repository_action_download -> handleActionDownload()
                    R.id.menu_repository_action_clone -> handleActionClone()
                    R.id.menu_repository_action_create_branch -> handleActionCreateBranch()
                    R.id.menu_repository_action_create_pull_request -> handleActionCreatePullRequest()
                    R.id.menu_repository_action_fork -> handleActionFork()
                    R.id.menu_repository_nav_branches -> handleNavigationBranches()
                    R.id.menu_repository_nav_commits -> handleNavigationCommits()
                    R.id.menu_repository_nav_source -> handleNavigationSource()
                    R.id.menu_repository_nav_pull_requests -> handleNavigationPullRequests()
                    else -> return false
                }
                return true
            }

        })
    }

    private fun handleActionDownload() {
        Router.goTo(Router.Request(Router.Route.DOWNLOAD, getArguments()), true)
    }

    private fun handleActionClone() {
        // Write the git clone URL to the clipboard
    }

    private fun handleActionCreateBranch() {
        getArguments().putBoolean(Constants.CREATE, true)
        Router.goTo(Router.Request(Router.Route.BRANCH, getArguments()))
    }

    private fun handleActionCreatePullRequest() {
        getArguments().putBoolean(Constants.CREATE, true)
        Router.goTo(Router.Request(Router.Route.PULL_REQUEST, getArguments()))
    }

    private fun handleActionFork() {
        getArguments().putBoolean(Constants.CREATE, true)
        Router.goTo(Router.Request(Router.Route.FORK, getArguments()))
    }

    private fun handleNavigationBranches() {
        Router.goTo(Router.Request(Router.Route.BRANCHES, getArguments()))
    }

    private fun handleNavigationCommits() {
        Router.goTo(Router.Request(Router.Route.COMMITS, getArguments()))
    }

    private fun handleNavigationSource() {
        Router.goTo(Router.Request(Router.Route.SOURCES, getArguments()))
    }

    private fun handleNavigationPullRequests() {
        Router.goTo(Router.Request(Router.Route.PULL_REQUESTS, getArguments()))
    }


}
