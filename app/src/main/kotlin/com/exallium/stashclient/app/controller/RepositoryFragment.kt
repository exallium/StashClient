package com.exallium.stashclient.app.controller

import android.app.Fragment
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.exallium.rxrecyclerview.lib.event.Event
import com.exallium.stashclient.app.*
import com.exallium.stashclient.app.controller.adapters.RepositoryAdapter
import com.exallium.stashclient.app.controller.logging.Logger
import com.exallium.stashclient.app.model.stash.Core
import com.exallium.stashclient.app.model.stash.Page
import com.exallium.stashclient.app.model.stash.StashApiManager
import com.exallium.stashclient.app.model.stash.StashFile

import kotlinx.android.synthetic.fragment_projects.*
import rx.Observable
import rx.Subscriber
import rx.subjects.PublishSubject

public class RepositoryFragment : Fragment() {

    companion object {
        val TAG: String = RepositoryFragment.javaClass.getSimpleName()
    }

    private val groupComparator = object : GenericComparator<StashFile>() {

        override fun getGroupKey(p0: Event<String, StashFile>?): String? {
            return if (p0?.getValue()?.isDirectory()?:false) "a" else "b"
        }

    }

    private val pageSubject: PublishSubject<Page<String>> = PublishSubject.create()
    private val baseStashFile: StashFile = StashFile("ROOT")
    private var restAdapter: Core.Projects.Repos? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_projects, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        recyclerView.setLayoutManager(LinearLayoutManager(getActivity()))

        restAdapter = StashApiManager.Factory.getOrCreate(getActivity()).restAdapter.create(javaClass<Core.Projects.Repos>())

        pageSubject.compose(RetroFitPageTransformer<String>())
                ?.map {
                    baseStashFile.getOrCreate(it)
                }?.subscribe()

        recyclerView.setAdapter(RepositoryAdapter(baseStashFile.getObservable()
            .compose(RetroFitElementTransformer(groupComparator, getKey = { it.name + it.isDirectory() }))
            .onBackpressureBuffer()
        ))

        restAdapter?.files(
                projectKey = getArguments().getString(Constants.PROJECT_KEY),
                repositorySlug = getArguments().getString(Constants.REPOSITORY_SLUG))
                ?.subscribe(RestPageSubscriber())
    }

    private inner class RestPageSubscriber : Subscriber<Page<String>>() {
        override fun onError(e: Throwable?) {
            Logger.emit(TAG, "RestPageProblem", e)
        }

        override fun onCompleted() {
            unsubscribe()
        }

        override fun onNext(t: Page<String>?) {
            if (t != null) {

                pageSubject.onNext(t)
                if (!t.isLastPage) {
                    restAdapter?.files(
                            projectKey = getArguments().getString(Constants.PROJECT_KEY),
                            repositorySlug = getArguments().getString(Constants.REPOSITORY_SLUG))
                            ?.subscribe(RestPageSubscriber())
                }
            }
        }

    }
}
