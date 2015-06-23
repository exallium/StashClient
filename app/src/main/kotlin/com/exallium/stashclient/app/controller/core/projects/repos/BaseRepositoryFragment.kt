package com.exallium.stashclient.app.controller.core.projects.repos

import android.app.Activity
import android.app.DownloadManager
import android.app.Fragment
import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.widget.DrawerLayout
import android.view.MenuItem
import com.exallium.stashclient.app.Constants
import com.exallium.stashclient.app.R
import com.exallium.stashclient.app.controller.Router
import com.exallium.stashclient.app.controller.logging.Logger
import com.exallium.stashclient.app.model.stash.Core
import com.exallium.stashclient.app.model.stash.DownloadRequest
import com.exallium.stashclient.app.model.stash.StashApiManager
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
        Router.post(Router.Request(Router.Route.DOWNLOAD, getArguments()))
    }

    private fun handleActionClone() {
        val repoAdapter = StashApiManager.Factory.get(getActivity()).getAdapter(javaClass<Core.Projects.Repos>())
        repoAdapter.retrieve(getArguments()
                .getString(Constants.PROJECT_KEY), getArguments()
                .getString(Constants.REPOSITORY_SLUG)).subscribe {
            val clipManager = getActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            clipManager.setPrimaryClip(ClipData.newPlainText(
                    getResources().getString(R.string.clone_clip_description), it.cloneUrl))

            val bundle = Bundle()
            bundle.putString(Constants.MESSAGE, getResources().getString(R.string.clone_copy_success))
            bundle.putInt(Constants.MESSAGE_LENGTH, Snackbar.LENGTH_SHORT)
            Router.get(Router.Request(Router.Route.SNACKBAR, bundle))
        }
    }

    private fun handleActionCreateBranch() {
        getArguments().putBoolean(Constants.CREATE, true)
        Router.get(Router.Request(Router.Route.BRANCH, getArguments()))
    }

    private fun handleActionCreatePullRequest() {
        getArguments().putBoolean(Constants.CREATE, true)
        Router.get(Router.Request(Router.Route.PULL_REQUEST, getArguments()))
    }

    private fun handleActionFork() {
        getArguments().putBoolean(Constants.CREATE, true)
        Router.get(Router.Request(Router.Route.FORK, getArguments()))
    }

    private fun handleNavigationBranches() {
        Router.get(Router.Request(Router.Route.BRANCHES, getArguments()))
    }

    private fun handleNavigationCommits() {
        Router.get(Router.Request(Router.Route.COMMITS, getArguments()))
    }

    private fun handleNavigationSource() {
        Router.get(Router.Request(Router.Route.SOURCES, getArguments()))
    }

    private fun handleNavigationPullRequests() {
        Router.get(Router.Request(Router.Route.PULL_REQUESTS, getArguments()))
    }


}
