package com.christianokeke.scroll

import android.app.Application
import com.christianokeke.scroll.data.SavedStore

class ScrollApplication : Application() {
    /** One store for the whole app, so every screen sees the same saved lists. */
    val store: SavedStore by lazy { SavedStore(this) }
}
