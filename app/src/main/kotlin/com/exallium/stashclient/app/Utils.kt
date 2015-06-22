package com.exallium.stashclient.app

import android.accounts.Account
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.support.v7.widget.Toolbar
import com.exallium.rxrecyclerview.lib.GroupComparator
import com.exallium.rxrecyclerview.lib.element.EventElement
import com.exallium.rxrecyclerview.lib.event.Event
import com.exallium.rxrecyclerview.lib.operators.ElementGenerationOperator
import com.exallium.stashclient.app.controller.logging.Logger
import com.exallium.stashclient.app.model.stash.Page
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import rx.Observable
import rx.Subscriber

public abstract class AbstractLoggingSubscriber<T>(val tag: String): Subscriber<T>() {

    override fun onError(e: Throwable?) {
        Logger.emit(tag, "Something bad happened", e)
    }

    override fun onCompleted() {
        unsubscribe()
    }

}

public object Constants {

    // Object Manipulation
    val CREATE = "com.exallium.stashclient.CREATE"

    val PREFERENCES = "com.exallium.stashclient.PREFERENCES"

    // Routing Keys
    val NEXT_PAGE = "com.exallium.stashclient.NEXT_PAGE"
    val NEXT_BUNDLE = "com.exallium.stashclient.NEXT_BUNDLE"

    // Stash Account Information
    val ACCOUNT_KEY = "com.exallium.stashclient.ACCOUNT"

    // Stash API Information
    val PROJECT_KEY = "com.exallium.stashclient.PROJECT"
    val PROJECT_NAME = "com.exallium.stashclient.PROJECT_NAME"
    val REPOSITORY_SLUG = "com.exallium.stashclient.REPO_SLUG"

    // Actions
    val LOGIN_ACTION = "com.exallium.stashclient.LOGIN"
}

public fun Account.getApiUrl(): String {
    return this.name.substring(this.name.indexOf('@') + 1)
}

public fun Account.getUsername(): String {
    return this.name.substring(0, this.name.indexOf('@'))
}

public class ToolbarTarget(val toolbar: Toolbar) : Target {
    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
        toolbar.setLogo(BitmapDrawable(toolbar.getResources(), bitmap))
    }

    override fun onBitmapFailed(errorDrawable: Drawable?) {
    }

    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
    }

}

public abstract class GenericComparator<T : Comparable<T>> : GroupComparator<String, T> {
    override fun getEmptyEvent(p0: Event.TYPE?): Event<String, T>? {
        return Event<String, T>(p0, "", null)
    }

    override fun compare(lhs: Event<String, T>?, rhs: Event<String, T>?): Int {
        if (lhs?.getValue() == null)
            return -1
        if (rhs?.getValue() == null)
            return 1

        return lhs?.getValue()?.compareTo(rhs?.getValue()!!) ?: 0
    }

}

public class RetroFitElementTransformer<K, V>(val groupComparator: GroupComparator<K, V>,
                                              val hasHeader: Boolean = false,
                                              val hasFooter: Boolean = false,
                                              val hasEmpty: Boolean = false,
                                              val getKey: (V) -> K) : Observable.Transformer<V, EventElement<K, V>> {

    override fun call(t1: Observable<V>?): Observable<EventElement<K, V>>? {
        return t1?.map {
            Event(Event.TYPE.ADD, getKey(it), it)
        }?.lift(ElementGenerationOperator.Builder(groupComparator)
            .hasHeader(hasHeader).hasFooter(hasFooter).hasEmpty(hasEmpty).build())
        ?.onBackpressureBuffer()
    }
}

public class RetroFitPageTransformer<V> : Observable.Transformer<Page<V>, V> {
    override fun call(t1: Observable<Page<V>>?): Observable<V>? {
        return t1?.flatMap {
            it -> Observable.from(it.values)
        }
    }
}

public class EmptySubscriber<T> : Subscriber<T>() {
    override fun onError(e: Throwable?) {
        unsubscribe()
    }

    override fun onNext(t: T) {
    }

    override fun onCompleted() {
        unsubscribe()
    }

}

public class SubscriberDelegate<T : Subscriber<*>>(val instance: () -> T?) {

    private var internal: T? = null

    fun get(thisRef: Any?, prop: PropertyMetadata) : T? {
        if (internal == null)
            internal = instance()
        return internal!!
    }

    fun set(thisRef: Any?, prop: PropertyMetadata, value: T?) {
        internal?.unsubscribe()
        internal = value
    }
}