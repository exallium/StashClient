package com.exallium.stashclient.app.controller

import android.app.Activity
import android.app.Fragment
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.exallium.rxrecyclerview.lib.element.EmptyElement
import com.exallium.rxrecyclerview.lib.event.Event
import com.exallium.stashclient.app.*
import com.exallium.stashclient.app.controller.adapters.EmptyAdapter
import com.exallium.stashclient.app.controller.adapters.RepositoryAdapter
import com.exallium.stashclient.app.controller.logging.Logger
import com.exallium.stashclient.app.model.stash.Core
import com.exallium.stashclient.app.model.stash.Page
import com.exallium.stashclient.app.model.stash.StashApiManager
import com.exallium.stashclient.app.model.stash.StashFile
import com.exallium.stashclient.app.view.RepositorySVGMenuIconProcessor

import kotlinx.android.synthetic.fragment_projects.*
import rx.Observable
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

public class RepositoryFragment : Fragment() {

    companion object {
        val TAG: String = RepositoryFragment.javaClass.getSimpleName()
    }

    private val groupComparator = object : GenericComparator<StashFile>() {

        override fun getGroupKey(p0: Event<String, StashFile>?): String? {
            return "0"
        }

    }

    private val pageSubject: PublishSubject<Page<String>> = PublishSubject.create()
    private val baseStashFile: StashFile = StashFile("ROOT", true)
    private var restAdapter: Core.Projects.Repos? = null
    private var clickSubject: BehaviorSubject<StashFile> = BehaviorSubject.create(baseStashFile)

    // Subscribers
    private var currentAdapterSubscriber: AdapterSubscriber? by SubscriberDelegate({ AdapterSubscriber() })
    private var currentPageSubjectSubscriber: EmptySubscriber<StashFile>? by SubscriberDelegate({ EmptySubscriber<StashFile>() })

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_projects, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        recyclerView.setLayoutManager(LinearLayoutManager(getActivity()))

        restAdapter = StashApiManager.Factory.get(getActivity()).restAdapter.create(javaClass<Core.Projects.Repos>())

        pageSubject.compose(RetroFitPageTransformer<String>())
                ?.map { baseStashFile.getOrCreate(it) }
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.computation())
                ?.subscribe(currentPageSubjectSubscriber)

        restAdapter?.files(
                projectKey = getArguments().getString(Constants.PROJECT_KEY),
                repositorySlug = getArguments().getString(Constants.REPOSITORY_SLUG))
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(Schedulers.io())
                ?.subscribe(RestPageSubscriber())

        recyclerView.setAdapter(EmptyAdapter<StashFile>(
                Observable.just(EmptyElement(groupComparator.getEmptyEvent(Event.TYPE.ADD), groupComparator)),
                "Loading File Data..."))

        clickSubject.subscribe(currentAdapterSubscriber)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        currentAdapterSubscriber = null
        currentPageSubjectSubscriber = null
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        val drawer = activity.findViewById(R.id.drawer) as DrawerLayout?
        drawer?.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        val nav = activity.findViewById(R.id.nav) as NavigationView?
        nav?.getMenu()?.clear()
        nav?.inflateMenu(R.menu.menu_repository)
        RepositorySVGMenuIconProcessor(nav?.getMenu()!!, getResources()).process()
    }

    private inner class AdapterSubscriber : Subscriber<StashFile>() {
        override fun onNext(t: StashFile?) {
            if (t != null) {
                recyclerView.swapAdapter(RepositoryAdapter(t.getObservable()
                        .compose(RetroFitElementTransformer(groupComparator, getKey = { it.name }))
                        .onBackpressureBuffer(),
                        clickSubject), true)
            }
        }

        override fun onError(e: Throwable?) {
            Logger.emit(TAG, "AdapterSubscriberProblem", e)
        }

        override fun onCompleted() {
            unsubscribe()
        }

    }

    private inner class RestPageSubscriber : Subscriber<Page<String>>() {
        override fun onError(e: Throwable?) {
            Logger.emit(TAG, "RestPageProblem", e)
        }

        override fun onCompleted() {
            unsubscribe()
        }

        override fun onNext(t: Page<String>?) {
            if (t != null && pageSubject.hasObservers()) {
                pageSubject.onNext(t)
                if (!t.isLastPage) {
                    restAdapter?.files(
                            projectKey = getArguments().getString(Constants.PROJECT_KEY),
                            repositorySlug = getArguments().getString(Constants.REPOSITORY_SLUG),
                            start = t.nextPageStart)
                            ?.subscribeOn(Schedulers.io())
                            ?.observeOn(Schedulers.io())
                            ?.subscribe(RestPageSubscriber())
                } else {
                    pageSubject.onCompleted()
                }
            }
        }

    }

}
