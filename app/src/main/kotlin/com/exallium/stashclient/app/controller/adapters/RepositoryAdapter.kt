package com.exallium.stashclient.app.controller.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.exallium.rxrecyclerview.lib.RxRecyclerViewAdapter
import com.exallium.rxrecyclerview.lib.element.EventElement
import com.exallium.stashclient.app.R
import com.exallium.stashclient.app.model.stash.StashFile
import com.exallium.stashclient.app.view.IconManager
import rx.Observable
import rx.android.view.ViewObservable
import rx.subjects.BehaviorSubject

public class RepositoryAdapter(observable: Observable<EventElement<String, StashFile>>, val clickSubject: BehaviorSubject<StashFile>) : RxRecyclerViewAdapter<String, StashFile, RepositoryAdapter.ViewHolder>(observable) {

    override fun onBindViewHolder(p0: ViewHolder?, p1: EventElement<String, StashFile>?) {
        p0?.bind(p1)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder? {
        return ViewHolder(LayoutInflater.from(parent!!.getContext()).inflate(R.layout.item_repository, parent, false), clickSubject)
    }

    private class ViewHolder(itemView: View, val clickSubject: BehaviorSubject<StashFile>) : RecyclerView.ViewHolder(itemView) {

        var file: StashFile? = null
        val icon: TextView
        val name: TextView

        init {
            icon = itemView.findViewById(R.id.repository_icon) as TextView
            icon.setTypeface(IconManager.getTypeface(icon.getContext()))
            name = itemView.findViewById(R.id.repository_name) as TextView

            ViewObservable.clicks(itemView).forEach {
                file?.let { clickSubject.onNext(file) }
            }

        }

        public fun bind(element: EventElement<String, StashFile>?) {
            file = element?.getData()?.getValue()
            icon.setTextColor(getColor(file?.isDirectory?:false))
            icon.setText(if (file?.isDirectory?:false) IconManager.iconDirectory.toString() else IconManager.iconFile.toString())
            name.setText(file?.name)
        }

        private fun getColor(isDirectory: Boolean): Int {
            return if (isDirectory)
                icon.getResources().getColor(R.color.repositoryDirectory)
            else
                icon.getResources().getColor(R.color.repositoryFile)
        }
    }

}
