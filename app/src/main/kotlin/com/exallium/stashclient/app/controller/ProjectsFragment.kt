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
import com.exallium.stashclient.app.controller.logging.Logger
import com.exallium.stashclient.app.model.stash.Core
import com.exallium.stashclient.app.model.stash.Page
import com.exallium.stashclient.app.model.stash.Project
import com.exallium.stashclient.app.model.stash.StashApiManager
import kotlinx.android.synthetic.fragment_projects.*
import rx.Observable
import rx.Subscriber
import rx.subjects.PublishSubject

public class ProjectsFragment : Fragment() {

    var layoutManager: RecyclerView.LayoutManager? = null
    var pageSubject: PublishSubject<Page<Project>> = PublishSubject.create()
    var restAdapter: Core.Projects? = null

    companion object {
        val TAG = ProjectsFragment.javaClass.getSimpleName()
    }

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
            return p0?.getValue()?.name?.charAt(0)?.toUpperCase().toString()
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_projects, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        layoutManager = LinearLayoutManager(getActivity())
        recyclerView.setLayoutManager(layoutManager)

        val account = getArguments().getParcelable<Account>("com.exallium.stashclient.ACCOUNT")
        restAdapter = StashApiManager.Factory.getOrCreate(getActivity(), account)
                .getAdapter(javaClass<Core.Projects>())
        val projectsObservable = pageSubject.flatMap {
            it -> Observable.from(it.values)
        }.map {
            Event(Event.TYPE.ADD, it.key, it)
        }.lift(ElementGenerationOperator.Builder<String, Project>(groupComparator).hasHeader(true).build())
                .onBackpressureBuffer()

        val viewAdapter = ProjectsAdapter(projectsObservable)
        restAdapter?.retrieve()?.subscribe(RestPageSubscriber())

        recyclerView.setAdapter(viewAdapter)
    }

    private inner class RestPageSubscriber : Subscriber<Page<Project>>() {
        override fun onError(e: Throwable?) {
            Logger.emit(TAG, "RestPageProblem", e)
        }

        override fun onCompleted() {
            unsubscribe()
        }

        override fun onNext(t: Page<Project>?) {
            if (t != null) {
                pageSubject.onNext(t)
                if (!t.isLastPage) {
                    restAdapter?.retrieve(start = t.nextPageStart)?.subscribe(RestPageSubscriber())
                }
            }
        }

    }

}
