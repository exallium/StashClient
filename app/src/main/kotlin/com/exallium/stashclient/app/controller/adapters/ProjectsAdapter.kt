package com.exallium.stashclient.app.controller.adapters

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.exallium.rxrecyclerview.lib.RxRecyclerViewAdapter
import com.exallium.rxrecyclerview.lib.element.EventElement
import com.exallium.stashclient.app.Constants
import com.exallium.stashclient.app.R
import com.exallium.stashclient.app.controller.Router
import com.exallium.stashclient.app.controller.RouterActivity
import com.exallium.stashclient.app.controller.StashAccountManager
import com.exallium.stashclient.app.getApiUrl
import com.exallium.stashclient.app.model.stash.Core
import com.exallium.stashclient.app.model.stash.Project
import com.squareup.picasso.Picasso
import rx.Observable
import rx.android.view.ViewObservable

public class ProjectsAdapter(observable: Observable<EventElement<String, Project>>) : RxRecyclerViewAdapter<String, Project, ProjectsAdapter.ViewHolder>(observable) {

    override fun onBindViewHolder(holder: ViewHolder?, event: EventElement<String, Project>?) {
        if (holder != null && event != null)
            holder.onBind(event)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder? {
        val inflater = LayoutInflater.from(parent!!.getContext())
        return when(viewType.ushr(EventElement.MASK_SHIFT)) {
            EventElement.HEADER_MASK -> HeaderViewHolder(inflater.inflate(R.layout.item_header, parent, false) as TextView)
            else -> DataViewHolder(inflater.inflate(R.layout.item_project, parent, false))
        }
    }

    abstract class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun onBind(event: EventElement<String, Project>)
    }

    class DataViewHolder(itemView: View) : ViewHolder(itemView) {

        val name: TextView
        val avatar: ImageView
        val description: TextView
        var project: Project? = null

        init {
            name = itemView.findViewById(R.id.project_name) as TextView
            avatar = itemView.findViewById(R.id.project_avatar) as ImageView
            description = itemView.findViewById(R.id.project_description) as TextView
            ViewObservable.clicks(itemView).forEach {
                val project = project
                project?.let {
                    val bundle = Bundle()
                    bundle.putString(Constants.PROJECT_KEY, project?.key)
                    bundle.putString(Constants.PROJECT_NAME, project?.name)
                    Router.get(Router.Request(Router.Route.PROJECT, bundle))
                }
            }
        }


        override fun onBind(event: EventElement<String, Project>) {
            val project = event.getData().getValue()
            this.project = project
            name.setText(project?.name)
            description.setText(project?.description)
            val loggedIn = StashAccountManager.Factory.get(name.getContext()).isLoggedIn()
            if (loggedIn && project != null)
                Core.Projects.Avatar.load(avatar.getContext(), avatar, project.key)
        }
    }

    class HeaderViewHolder(itemView: TextView) : ViewHolder(itemView) {
        override fun onBind(event: EventElement<String, Project>) {
            (itemView as TextView).setText(event.getGroup())
        }
    }

}
