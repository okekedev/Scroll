package com.christianokeke.scroll.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.BackHand
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.christianokeke.scroll.ui.theme.ScrollTheme

/** One fruit of the Spirit: what the tile says, what it searches for, and how it looks. */
data class Virtue(val name: String, val searchTerm: String, val icon: ImageVector)

object Virtues {
    /** Home-grid order; colours come from `ScrollColors.virtues` by the same index. */
    val all: List<Virtue> = listOf(
        Virtue("Love", "love", Icons.Filled.Favorite),
        Virtue("Joy", "joy", Icons.Filled.EmojiEmotions),
        Virtue("Peace", "peace", Icons.Filled.Eco),
        Virtue("Patience", "patience", Icons.Filled.WatchLater),
        Virtue("Kindness", "kindness", Icons.Filled.BackHand),
        Virtue("Goodness", "goodness", Icons.Filled.AutoAwesome),
        Virtue("Faith", "faith", Icons.Filled.Star),
        Virtue("Gentleness", "gentleness", Icons.Filled.Cloud),
        Virtue("Self-Control", "self-control", Icons.Filled.Shield),
    )
}

@Composable
fun HomeScreen(
    savedVerseCount: Int,
    savedChapterCount: Int,
    onOpenVirtue: (index: Int) -> Unit,
    onOpenSavedVerses: () -> Unit,
    onOpenSavedChapters: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val colors = ScrollTheme.colors
    ScreenFrame { side ->
        // Wide windows (tablets) get the roomier tiles the iPad layout uses.
        val roomy = isRoomyWindow()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = side + Dimens.horizontalPadding)
                .padding(bottom = navigationBarBottom() + 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.End) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable(role = Role.Button, onClick = onOpenSettings),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = colors.textSecondary, modifier = Modifier.size(24.dp))
                }
            }

            Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = colors.accent, modifier = Modifier.size(40.dp))
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Scroll",
                color = colors.textPrimary,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { heading() },
            )
            Spacer(Modifier.height(4.dp))
            Text("Fruits of the Spirit", color = colors.textSecondary, fontSize = 15.sp)
            Spacer(Modifier.height(32.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Virtues.all.chunked(3).forEachIndexed { rowIndex, row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        row.forEachIndexed { column, virtue ->
                            val index = rowIndex * 3 + column
                            VirtueTile(
                                virtue = virtue,
                                color = colors.virtues[index],
                                roomy = roomy,
                                onClick = { onOpenVirtue(index) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SavedTile(Icons.Filled.Bookmark, "Saved Verses", savedVerseCount, onOpenSavedVerses, Modifier.weight(1f))
                SavedTile(Icons.Filled.AutoStories, "Saved Chapters", savedChapterCount, onOpenSavedChapters, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun VirtueTile(virtue: Virtue, color: Color, roomy: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ScrollCard(modifier = modifier, onClick = onClick) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = if (roomy) 32.dp else 16.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(if (roomy) 16.dp else 10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(if (roomy) 88.dp else 56.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(virtue.icon, contentDescription = null, tint = color, modifier = Modifier.size(if (roomy) 40.dp else 26.dp))
            }
            FittedText(
                text = virtue.name,
                color = ScrollTheme.colors.textPrimary,
                maxSize = if (roomy) 18.sp else 13.sp,
                minSize = 10.sp,
                weight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SavedTile(icon: ImageVector, title: String, count: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = ScrollTheme.colors
    ScrollCard(modifier = modifier, radius = 14.dp, color = colors.accent, onClick = onClick) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, contentDescription = null, tint = colors.onAccent, modifier = Modifier.size(18.dp))
                FittedText(title, color = colors.onAccent, maxSize = 15.sp, minSize = 11.sp, weight = FontWeight.SemiBold)
            }
            Text("$count", color = colors.onAccent.copy(alpha = 0.92f), fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}
