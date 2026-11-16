package com.christianokeke.scroll

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.christianokeke.scroll.ui.ScrollNavHost
import com.christianokeke.scroll.ui.theme.ScrollTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Draws behind the system bars; each screen pads itself with the insets.
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val store = (application as ScrollApplication).store
        setContent {
            ScrollTheme {
                ScrollNavHost(store)
            }
        }
    }
}
