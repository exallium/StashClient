package com.exallium.stashclient.app.view

import android.content.res.Resources
import android.view.Menu
import android.view.MenuItem
import com.exallium.stashclient.app.R
import com.larvalabs.svgandroid.SVG
import com.larvalabs.svgandroid.SVGParser
import java.util.*

public open class SVGMenuIconProcessor(val menu: Menu, val resources: Resources, val icons: List<Int>) {

    private fun getMenuItems(menu: Menu): List<MenuItem> {
        val list = ArrayList<MenuItem>()
        val size = menu.size()
        for (i in 0..(size - 1)) {
            val item = menu.getItem(i)
            if (item.hasSubMenu())
                list.addAll(getMenuItems(item.getSubMenu()))
            else
                list.add(item)
        }
        return list
    }

    public fun process() {

        // collect all the menu items into a list
        val menuItems = getMenuItems(menu)

        val size = menuItems.size()
        for (i in 0..(size - 1)) {
            val svg = SVGParser.getSVGFromResource(resources, icons.get(i))
            menuItems.get(i).setIcon(svg.createPictureDrawable())
        }
    }
}

public class RepositorySVGMenuIconProcessor(menu: Menu, resources: Resources)
    : SVGMenuIconProcessor(menu, resources, arrayListOf(
        R.raw.icon_downloads,
        R.raw.icon_clone,
        R.raw.icon_create_branch,
        R.raw.icon_create_pull_request,
        R.raw.icon_fork,
        R.raw.icon_source,
        R.raw.icon_commits,
        R.raw.icon_branches,
        R.raw.icon_pull_requests,
        R.raw.icon_settings
))
