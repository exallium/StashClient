package com.exallium.stashclient.app

import android.accounts.Account
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.support.v7.widget.Toolbar
import com.exallium.rxrecyclerview.lib.GroupComparator
import com.exallium.rxrecyclerview.lib.event.Event
import com.exallium.stashclient.app.controller.logging.Logger
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
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
    val PREFERENCES = "com.exallium.stashclient.PREFERENCES"
    val ACCOUNT_KEY = "com.exallium.stashclient.ACCOUNT"
    val PROJECT_KEY = "com.exallium.stashclient.PROJECT"

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