package com.exallium.stashclient.app.view

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Typeface
import kotlin.properties.Delegates

public object IconManager {

    private var _typeface: Typeface? = null

    public fun getTypeface(context: Context): Typeface {
        if (_typeface == null)
            _typeface = Typeface.createFromAsset(context.getAssets(), "Atlassian-icons.ttf")
        return _typeface?:Typeface.DEFAULT
    }

    public val iconDirectory: Char = '\uf131'
    public val iconFile: Char = '\uf12e'
}
