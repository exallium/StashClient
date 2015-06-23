package com.exallium.stashclient.app.controller.core.projects

import android.app.Activity
import android.app.Fragment
import android.support.design.widget.NavigationView
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import com.exallium.stashclient.app.R
import com.exallium.stashclient.app.controller.Router

public open class BaseProjectFragment : Fragment() {

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        val toolbar = activity.findViewById(R.id.toolbar) as Toolbar
        toolbar.setVisibility(View.VISIBLE)
        toolbar.setLogo(null)
        toolbar.setTitle(R.string.app_name)
        val navigationView = activity.findViewById(R.id.nav) as NavigationView
        navigationView.getMenu().clear()
        navigationView.inflateMenu(R.menu.menu_projects)
        navigationView.setNavigationItemSelectedListener(menuItemSelectedListener)
        val drawer = activity.findViewById(R.id.drawer) as DrawerLayout?
        drawer?.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
    }

    private object menuItemSelectedListener : NavigationView.OnNavigationItemSelectedListener {
        override fun onNavigationItemSelected(p0: MenuItem?): Boolean {
            when (p0?.getItemId()) {
                R.id.settings -> Router.get(Router.Request(Router.Route.SETTINGS))
                else -> return false
            }
            return true
        }
    }
}
