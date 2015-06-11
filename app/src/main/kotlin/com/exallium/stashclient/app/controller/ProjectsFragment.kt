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
import com.exallium.stashclient.app.GenericComparator
import com.exallium.stashclient.app.R
import com.exallium.stashclient.app.RetroFitElementTransformer
import com.exallium.stashclient.app.RetroFitPageTransformer
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

    object groupComparator : GenericComparator<Project>() {
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

        // Boot them to the login screen
        val account = StashAccountManager.Factory.getInstance(getActivity()).account ?: return

        restAdapter = StashApiManager.Factory.getOrCreate(getActivity(), account)
                .getAdapter(javaClass<Core.Projects>())

        val projectsObservable = pageSubject
                .compose(RetroFitPageTransformer<Project>())
                .compose(RetroFitElementTransformer(
                        groupComparator = groupComparator,
                        hasHeader = true,
                        getKey = { it.key }
                ))

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
