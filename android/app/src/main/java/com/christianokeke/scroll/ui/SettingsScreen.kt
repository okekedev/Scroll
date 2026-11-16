package com.christianokeke.scroll.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.christianokeke.scroll.data.VerseShare
import com.christianokeke.scroll.ui.theme.ScrollTheme

private const val WEB_URL = VerseShare.WEB_BASE
private const val PRIVACY_URL = "${VerseShare.WEB_BASE}/privacy"

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val colors = ScrollTheme.colors
    val context = LocalContext.current
    val toast = rememberToastState()
    val version = remember { appVersionName(context) }

    fun open(url: String) {
        if (!openLink(context, url)) toast.show("Couldn’t open the link")
    }

    ScreenFrame(toast) { side ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = navigationBarBottom() + 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CenteredHeader("Settings", null, onBack, side)

            Column(
                modifier = Modifier.padding(horizontal = side + Dimens.horizontalPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ScrollCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.Info, contentDescription = null, tint = colors.textPrimary, modifier = Modifier.size(20.dp))
                            Text(
                                text = "About",
                                color = colors.textPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.semantics { heading() },
                            )
                        }
                        Text(
                            text = "Scroll helps you discover Bible verses about the Fruits of the Spirit. " +
                                "Save your favorite verses, save full chapters, and walk book by book " +
                                "with the prev / next arrows.",
                            color = colors.textSecondary,
                            fontSize = 15.sp,
                            lineHeight = 23.sp,
                        )
                    }
                }

                ScrollCard(Modifier.fillMaxWidth()) {
                    Column {
                        LinkRow(Icons.Filled.Public, "Read on the web", "scroll.okeke.us") { open(WEB_URL) }
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 52.dp),
                            color = colors.textSecondary.copy(alpha = 0.2f),
                        )
                        LinkRow(Icons.Filled.PrivacyTip, "Privacy Policy", null) { open(PRIVACY_URL) }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
            Text(
                text = "Scroll · v$version",
                color = colors.textSecondary.copy(alpha = 0.7f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun LinkRow(icon: ImageVector, title: String, detail: String?, onClick: () -> Unit) {
    val colors = ScrollTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(role = Role.Button, onClickLabel = "Open in browser", onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Icon(icon, contentDescription = null, tint = colors.accent, modifier = Modifier.size(22.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = colors.textPrimary, fontSize = 16.sp)
            if (detail != null) Text(detail, color = colors.textSecondary, fontSize = 13.sp)
        }
        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(18.dp))
    }
}

/** versionName as built into the package, so the footer can never drift from the release. */
private fun appVersionName(context: Context): String = try {
    context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()
} catch (_: PackageManager.NameNotFoundException) {
    ""
}

private fun openLink(context: Context, url: String): Boolean = try {
    context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    true
} catch (_: ActivityNotFoundException) {
    false
}
