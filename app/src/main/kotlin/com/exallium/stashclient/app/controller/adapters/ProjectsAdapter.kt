package com.exallium.stashclient.app.controller.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.exallium.rxrecyclerview.lib.RxRecyclerViewAdapter
import com.exallium.rxrecyclerview.lib.element.EventElement
import com.exallium.stashclient.app.R
import com.exallium.stashclient.app.model.stash.Project
import rx.Observable

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

        init {
            name = itemView.findViewById(R.id.project_name) as TextView
        }


        override fun onBind(event: EventElement<String, Project>) {
            name.setText(event.getData().getValue().name)
        }
    }

    class HeaderViewHolder(itemView: TextView) : ViewHolder(itemView) {
        override fun onBind(event: EventElement<String, Project>) {
            (itemView as TextView).setText(event.getGroup())
        }
    }

}