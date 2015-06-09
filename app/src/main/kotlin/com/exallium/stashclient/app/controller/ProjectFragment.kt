package com.exallium.stashclient.app.controller

import android.accounts.Account
import android.app.Activity
import android.app.Fragment
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.exallium.rxrecyclerview.lib.GroupComparator
import com.exallium.rxrecyclerview.lib.event.Event
import com.exallium.stashclient.app.*
import com.exallium.stashclient.app.controller.logging.Logger
import com.exallium.stashclient.app.model.stash.*
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject

public class ProjectFragment : Fragment() {

    companion object {
        val TAG = ProjectFragment.javaClass.getSimpleName()
    }

    private var toolbarTarget: ToolbarTarget? = null
    var pageSubject: PublishSubject<Page<Repository>> = PublishSubject.create()
    var restAdapter: Core.Projects.Repos? = null

    object groupComparator : GenericComparator<Repository>() {
        override fun getGroupKey(p0: Event<String, Repository>?): String? {
            return p0?.getValue()?.name?.charAt(0)?.toUpperCase().toString()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_project, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        restAdapter = StashApiManager.Factory.getOrCreate(getActivity(), getAccount()!!)
                .getAdapter(javaClass<Core.Projects.Repos>())

    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        val account = getAccount()

        if (account != null) {
            val restAdapter = StashApiManager.Factory.getOrCreate(activity, account)
                    .getAdapter(javaClass<Core.Projects>())

            restAdapter.retrieve(projectKey = getArguments().getString(Constants.PROJECT_KEY))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                val toolbar = activity.findViewById(R.id.toolbar) as Toolbar
                toolbarTarget = ToolbarTarget(toolbar)
                toolbar.setTitle(it.name)
                Picasso.with(activity)
                        .load(Core.Projects.Avatar.getUri(account.getApiUrl(), it))
                        .into(toolbarTarget)
            }

        } else {
            Router.requestPublisher.onNext(Router.Request(Router.Route.LOGIN))
        }

    }

    private fun getAccount(): Account? {
        return StashAccountManager.Factory.getInstance(getActivity()).account
    }

    private inner class RestPageSubscriber : Subscriber<Page<Repository>>() {
        override fun onError(e: Throwable?) {
            Logger.emit(TAG, "RestPageProblem", e)
        }

        override fun onCompleted() {
            unsubscribe()
        }

        override fun onNext(t: Page<Repository>?) {
            if (t != null) {
                pageSubject.onNext(t)
                if (!t.isLastPage) {
                    restAdapter?.retrieve(
                            projectKey = getArguments().getString(Constants.PROJECT_KEY),
                            start = t.nextPageStart)?.subscribe(RestPageSubscriber())
                }
            }
        }

    }

}
