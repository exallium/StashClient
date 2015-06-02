package com.exallium.stashclient.app.controller

import android.accounts.Account
import android.app.Fragment
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.exallium.rxrecyclerview.lib.GroupComparator
import com.exallium.rxrecyclerview.lib.event.Event
import com.exallium.rxrecyclerview.lib.operators.ElementGenerationOperator
import com.exallium.stashclient.app.R
import com.exallium.stashclient.app.controller.adapters.ProjectsAdapter
import com.exallium.stashclient.app.model.stash.Core
import com.exallium.stashclient.app.model.stash.Project
import com.exallium.stashclient.app.model.stash.StashApiManager
import kotlinx.android.synthetic.fragment_projects.*
import rx.Observable

public class ProjectsFragment : Fragment() {

    var layoutManager: RecyclerView.LayoutManager? = null

    object groupComparator : GroupComparator<String, Project> {
        override fun getEmptyEvent(p0: Event.TYPE?): Event<String, Project>? {
            return Event<String, Project>(p0, "", null)
        }

        override fun compare(lhs: Event<String, Project>?, rhs: Event<String, Project>?): Int {
            if (lhs?.getValue() == null)
                return -1
            if (rhs?.getValue() == null)
                return 1

            val lname = lhs?.getValue()?.name
            val rname = rhs?.getValue()?.name
            if (lname != null && rname != null)
                return lname.compareTo(rname)

            return 0
        }

        override fun getGroupKey(p0: Event<String, Project>?): String? {
            throw UnsupportedOperationException()
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_projects, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        layoutManager = LinearLayoutManager(getActivity())
        recyclerView.setLayoutManager(layoutManager)

        val account = getArguments().getParcelable<Account>("com.exallium.stashclient.ACCOUNT")
        val restAdapter = StashApiManager.Factory.getOrCreate(getActivity(), account)
                .getAdapter(javaClass<Core.Projects>())
        val projectsObservable = restAdapter.retrieve().flatMap {
            it -> Observable.from(it.values)
        }.map {
            Event(Event.TYPE.ADD, it.key, it)
        }.lift(ElementGenerationOperator.Builder<String, Project>(groupComparator).hasHeader(true).build())

        val viewAdapter = ProjectsAdapter(projectsObservable)
        recyclerView.setAdapter(viewAdapter)
    }

}
