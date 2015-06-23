package com.exallium.stashclient.app.controller.adapters

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.exallium.rxrecyclerview.lib.RxRecyclerViewAdapter
import com.exallium.rxrecyclerview.lib.element.EventElement
import com.exallium.stashclient.app.Constants
import com.exallium.stashclient.app.R
import com.exallium.stashclient.app.controller.Router
import com.exallium.stashclient.app.model.stash.Repository
import com.exallium.stashclient.app.view.IconManager
import rx.Observable
import rx.android.view.ViewObservable

public class ProjectAdapter(observable: Observable<EventElement<String, Repository>>) : RxRecyclerViewAdapter<String, Repository, ProjectAdapter.ViewHolder>(observable) {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder? {
        return ViewHolder(LayoutInflater.from(parent!!.getContext()).inflate(R.layout.item_repository, parent, false))
    }

    override fun onBindViewHolder(p0: ViewHolder?, p1: EventElement<String, Repository>?) {
        p0?.bind(p1)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var repository: Repository? = null
        var name: TextView? = null

        init {
            val icon = itemView.findViewById(R.id.repository_icon) as TextView
            icon.setTypeface(IconManager.getTypeface(icon.getContext()))
            name = itemView.findViewById(R.id.repository_name) as TextView
            ViewObservable.clicks(itemView).forEach {
                val repository = repository
                repository?.let {
                    val bundle = Bundle()
                    bundle.putString(Constants.PROJECT_KEY, repository?.project?.key)
                    bundle.putString(Constants.REPOSITORY_SLUG, repository?.slug)
                    Router.goTo(Router.Request(Router.Route.SOURCES, bundle))
                }
            }
        }

        fun bind(element: EventElement<String, Repository>?) {
            if (element != null) {
                repository = element.getData().getValue()
                name?.setText(repository?.name)
            }
        }

    }


}
