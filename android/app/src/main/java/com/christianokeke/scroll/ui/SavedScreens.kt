package com.christianokeke.scroll.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.BookmarkRemove
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.christianokeke.scroll.data.BibleVerse
import com.christianokeke.scroll.data.SavedChapter
import com.christianokeke.scroll.data.SavedStore
import com.christianokeke.scroll.data.VerseShare
import com.christianokeke.scroll.ui.theme.ScrollTheme

/** "1 verse", "2 verses". */
private fun countLabel(count: Int, noun: String): String = if (count == 1) "1 $noun" else "$count ${noun}s"

@Composable
fun SavedVersesScreen(store: SavedStore, onBack: () -> Unit, onOpenVerse: (BibleVerse) -> Unit) {
    val verses by store.verses.collectAsStateWithLifecycle()
    val toast = rememberToastState()
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current

    ScreenFrame(toast) { side ->
        Column(Modifier.fillMaxSize()) {
            CenteredHeader("Saved Verses", countLabel(verses.size, "verse"), onBack, side)

            if (verses.isEmpty()) {
                EmptyState(Icons.Outlined.BookmarkBorder, "No saved verses yet", "Tap Save on a verse to add it here")
            } else {
                LazyColumn(
                    state = rememberLazyListState(),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = listPadding(side),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(verses, key = { it.id }) { verse ->
                        VerseCard(
                            verse = verse,
                            action = VerseCardAction.Remove,
                            onOpen = { onOpenVerse(verse) },
                            onShare = { shareText(context, VerseShare.text(verse)) },
                            onAction = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                store.removeVerse(verse)
                                toast.show("Verse Removed")
                            },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SavedChaptersScreen(store: SavedStore, onBack: () -> Unit, onOpenChapter: (SavedChapter) -> Unit) {
    val chapters by store.chapters.collectAsStateWithLifecycle()
    val haptics = LocalHapticFeedback.current

    ScreenFrame { side ->
        Column(Modifier.fillMaxSize()) {
            CenteredHeader("Saved Chapters", countLabel(chapters.size, "chapter"), onBack, side)

            if (chapters.isEmpty()) {
                EmptyState(Icons.Outlined.AutoStories, "No saved chapters yet", "Tap the bookmark in a chapter to save it")
            } else {
                LazyColumn(
                    state = rememberLazyListState(),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = listPadding(side),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(chapters, key = { it.id }) { chapter ->
                        SavedChapterRow(
                            chapter = chapter,
                            onOpen = { onOpenChapter(chapter) },
                            onRemove = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                store.removeChapter(chapter)
                            },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SavedChapterRow(chapter: SavedChapter, onOpen: () -> Unit, onRemove: () -> Unit, modifier: Modifier = Modifier) {
    val colors = ScrollTheme.colors
    ScrollCard(modifier = modifier.fillMaxWidth(), onClick = onOpen, onClickLabel = "Open chapter") {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape).background(colors.accentLight),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.AutoStories, contentDescription = null, tint = colors.accent, modifier = Modifier.size(22.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = chapter.reference,
                    color = colors.textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(chapter.translation, color = colors.textSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colors.destructiveLight)
                    .clickable(role = Role.Button, onClick = onRemove),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Outlined.BookmarkRemove,
                    contentDescription = "Remove ${chapter.reference}",
                    tint = colors.destructive,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}
