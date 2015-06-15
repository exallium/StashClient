package com.exallium.stashclient.app.controller.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.exallium.rxrecyclerview.lib.RxRecyclerViewAdapter
import com.exallium.rxrecyclerview.lib.element.EmptyElement
import com.exallium.rxrecyclerview.lib.element.EventElement
import com.exallium.stashclient.app.R
import rx.Observable


public class EmptyAdapter<T>(observable: Observable<EventElement<String, T>>, val emptyText: String) : RxRecyclerViewAdapter<String, T, EmptyAdapter.EmptyViewHolder>(observable) {
    override fun onBindViewHolder(p0: EmptyViewHolder?, p1: EventElement<String, T>?) {
        p0?.bind(emptyText)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): EmptyViewHolder? {
        return EmptyViewHolder(LayoutInflater.from(parent!!.getContext()).inflate(R.layout.item_header, parent, false))
    }


    private class EmptyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        public fun bind(emptyText: String) {
            (itemView as TextView).setText(emptyText)
        }
    }

}
