package com.christianokeke.scroll.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.christianokeke.scroll.data.BibleBooks
import com.christianokeke.scroll.data.BibleTranslations
import com.christianokeke.scroll.data.BollsClient
import com.christianokeke.scroll.data.ChapterVerse
import com.christianokeke.scroll.data.SavedStore
import com.christianokeke.scroll.data.VerseShare
import com.christianokeke.scroll.ui.theme.ScrollTheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChapterUiState(
    val translation: String,
    val book: Int,
    val chapter: Int,
    /** The verse to highlight, while the chapter on screen is the one it belongs to. */
    val highlightedVerse: Int? = null,
    val verses: List<ChapterVerse> = emptyList(),
    val isLoading: Boolean = true,
    val failed: Boolean = false,
) {
    val title: String get() = "${BibleBooks.name(book)} $chapter"
    val canGoPrevious: Boolean get() = BibleBooks.previous(book, chapter) != null
    val canGoNext: Boolean get() = BibleBooks.next(book, chapter) != null
}

class ChapterViewModel(private val handle: SavedStateHandle) : ViewModel() {
    private val translation = BibleTranslations.resolve(handle.get<String>(ARG_TRANSLATION))
    private val initialBook = (handle.get<Int>(ARG_BOOK) ?: 1).coerceIn(1, BibleBooks.count)
    private val initialChapter = (handle.get<Int>(ARG_CHAPTER) ?: 1).coerceIn(1, BibleBooks.chapters(initialBook))
    private val targetVerse = handle.get<Int>(ARG_VERSE)?.takeIf { it > 0 }

    private val _state = MutableStateFlow(
        stateFor(handle[KEY_BOOK] ?: initialBook, handle[KEY_CHAPTER] ?: initialChapter)
    )
    val state: StateFlow<ChapterUiState> = _state.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    fun goNext() {
        val current = _state.value
        BibleBooks.next(current.book, current.chapter)?.let { open(it.book, it.chapter) }
    }

    fun goPrevious() {
        val current = _state.value
        BibleBooks.previous(current.book, current.chapter)?.let { open(it.book, it.chapter) }
    }

    fun refresh() = load()

    private fun open(book: Int, chapter: Int) {
        // Saved so that a restored process reopens the chapter the reader walked to.
        handle[KEY_BOOK] = book
        handle[KEY_CHAPTER] = chapter
        _state.value = stateFor(book, chapter)
        load()
    }

    private fun stateFor(book: Int, chapter: Int) = ChapterUiState(
        translation = translation,
        book = book,
        chapter = chapter,
        highlightedVerse = targetVerse.takeIf { book == initialBook && chapter == initialChapter },
    )

    private fun load() {
        loadJob?.cancel()
        val requested = _state.value
        _state.update { it.copy(verses = emptyList(), isLoading = true, failed = false) }
        loadJob = viewModelScope.launch {
            val loaded = try {
                BollsClient.fetchChapter(requested.translation, requested.book, requested.chapter)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                null
            }
            _state.update { it.copy(verses = loaded.orEmpty(), isLoading = false, failed = loaded == null) }
        }
    }

    companion object {
        const val ARG_TRANSLATION = "translation"
        const val ARG_BOOK = "book"
        const val ARG_CHAPTER = "chapter"
        const val ARG_VERSE = "verse"
        private const val KEY_BOOK = "currentBook"
        private const val KEY_CHAPTER = "currentChapter"
    }
}

@Composable
fun ChapterScreen(viewModel: ChapterViewModel, store: SavedStore, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val savedChapters by store.chapters.collectAsStateWithLifecycle()
    val isSaved = savedChapters.any {
        it.translation == state.translation && it.book == state.book && it.chapter == state.chapter
    }
    val toast = rememberToastState()
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current

    ResetScrollOnChange(listState, state.book to state.chapter)

    // Bring the verse the reader came from into view, once. Saved, so a rotation
    // or a restored process keeps wherever the reader has scrolled to since.
    var revealedTarget by rememberSaveable { mutableStateOf(false) }
    val revealOffset = with(LocalDensity.current) { 96.dp.roundToPx() }
    LaunchedEffect(state.verses, state.highlightedVerse) {
        val target = state.highlightedVerse
        if (!revealedTarget && target != null && state.verses.isNotEmpty()) {
            val index = state.verses.indexOfFirst { it.verse == target }
            if (index > 0) listState.scrollToItem(index, -revealOffset)
            revealedTarget = true
        }
    }

    fun shareTextFor(verse: ChapterVerse) =
        VerseShare.text(verse.text, state.book, state.chapter, verse.verse, state.translation)

    ScreenFrame(toast) { side ->
        Column(Modifier.fillMaxSize()) {
            ChapterToolbar(
                state = state,
                isSaved = isSaved,
                sidePadding = side,
                onBack = onBack,
                onToggleSaved = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    store.toggleChapter(state.translation, state.book, state.chapter)
                },
                onRefresh = viewModel::refresh,
                onPrevious = viewModel::goPrevious,
                onNext = viewModel::goNext,
            )

            when {
                state.verses.isNotEmpty() -> LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = side,
                        end = side,
                        top = 8.dp,
                        bottom = navigationBarBottom() + 24.dp,
                    ),
                ) {
                    items(state.verses, key = { it.id }) { verse ->
                        VerseRow(
                            verse = verse,
                            isHighlighted = verse.verse == state.highlightedVerse,
                            onShare = { shareText(context, shareTextFor(verse)) },
                            onCopy = {
                                copyText(context, "Verse", shareTextFor(verse))
                                toast.show("Verse Copied")
                            },
                        )
                    }
                }

                state.isLoading -> LoadingState("Loading chapter...")

                state.failed -> RetryState(
                    title = "Couldn’t load ${state.title}",
                    hint = "Check your connection and try again.",
                    onRetry = viewModel::refresh,
                )

                else -> RetryState(
                    title = "No verses found",
                    hint = "${state.title} came back empty in the ${state.translation}.",
                    onRetry = viewModel::refresh,
                    icon = Icons.Filled.SearchOff,
                )
            }
        }
    }
}

@Composable
private fun ChapterToolbar(
    state: ChapterUiState,
    isSaved: Boolean,
    sidePadding: Dp,
    onBack: () -> Unit,
    onToggleSaved: () -> Unit,
    onRefresh: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    val colors = ScrollTheme.colors
    val actions: @Composable () -> Unit = {
        Row(horizontalArrangement = Arrangement.spacedBy(ToolbarGap)) {
            CircleIconButton(
                icon = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                contentDescription = if (isSaved) "Remove chapter from saved" else "Save chapter",
                onClick = onToggleSaved,
                size = Dimens.smallIconButton,
                foreground = if (isSaved) colors.onAccent else colors.accent,
                background = if (isSaved) colors.accent else colors.accentLight,
            )
            CircleIconButton(Icons.Filled.Refresh, "Refresh chapter", onRefresh, size = Dimens.smallIconButton)
            CircleIconButton(
                Icons.Filled.ChevronLeft, "Previous chapter", onPrevious,
                size = Dimens.smallIconButton, enabled = state.canGoPrevious, iconSize = 26.dp,
            )
            CircleIconButton(
                Icons.Filled.ChevronRight, "Next chapter", onNext,
                size = Dimens.smallIconButton, enabled = state.canGoNext, iconSize = 26.dp,
            )
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = sidePadding + Dimens.horizontalPadding)
            .padding(top = 8.dp, bottom = 8.dp),
    ) {
        // The title shares the row with five buttons. When even its smallest size
        // would not fit (long book names on narrow phones, large font settings),
        // it moves to a line of its own instead of being cut off.
        val measurer = rememberTextMeasurer()
        val density = LocalDensity.current
        val smallestTitle = remember(state.title, density) {
            val style = TextStyle(fontSize = TitleMinSize, fontWeight = FontWeight.SemiBold)
            with(density) { measurer.measure(state.title, style, maxLines = 1).size.width.toDp() }
        }
        val roomForTitle = maxWidth - Dimens.iconButton - ToolbarActionsWidth - TitlePadding * 2
        val inline = smallestTitle <= roomForTitle

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BackButton(onBack)
                if (inline) {
                    FittedText(
                        text = state.title,
                        color = colors.textPrimary,
                        maxSize = 20.sp,
                        minSize = TitleMinSize,
                        weight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f).padding(horizontal = TitlePadding).semantics { heading() },
                    )
                } else {
                    Spacer(Modifier.weight(1f))
                }
                actions()
            }
            if (!inline) {
                Text(
                    text = state.title,
                    color = colors.textPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 4.dp).semantics { heading() },
                )
            }
        }
    }
}

private val ToolbarGap = 6.dp
private val ToolbarActionsWidth = Dimens.smallIconButton * 4 + ToolbarGap * 3
private val TitlePadding = 12.dp
private val TitleMinSize = 15.sp

@Composable
private fun VerseRow(verse: ChapterVerse, isHighlighted: Boolean, onShare: () -> Unit, onCopy: () -> Unit) {
    val colors = ScrollTheme.colors
    val haptics = LocalHapticFeedback.current
    var menuOpen by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    when {
                        isHighlighted -> colors.highlight
                        menuOpen -> colors.card // marks the verse the open menu belongs to
                        else -> Color.Transparent
                    }
                )
                .pointerInput(Unit) {
                    detectTapGestures(onLongPress = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        menuOpen = true
                    })
                }
                .semantics {
                    onLongClick("Share or copy verse") { menuOpen = true; true }
                    customActions = listOf(
                        CustomAccessibilityAction("Share verse") { onShare(); true },
                        CustomAccessibilityAction("Copy verse") { onCopy(); true },
                    )
                }
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "${verse.verse}",
                color = if (isHighlighted) colors.accent else colors.textSecondary,
                fontSize = 12.sp,
                lineHeight = 25.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                modifier = Modifier.width(28.dp),
            )
            Text(verse.text, color = colors.textPrimary, style = VerseTextStyle, modifier = Modifier.weight(1f))
        }

        DropdownMenu(
            expanded = menuOpen,
            onDismissRequest = { menuOpen = false },
            offset = DpOffset(x = 52.dp, y = 0.dp),
            shape = RoundedCornerShape(14.dp),
        ) {
            DropdownMenuItem(
                text = { Text("Share verse") },
                leadingIcon = { Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(20.dp)) },
                onClick = { menuOpen = false; onShare() },
            )
            DropdownMenuItem(
                text = { Text("Copy verse") },
                leadingIcon = { Icon(Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(20.dp)) },
                onClick = { menuOpen = false; onCopy() },
            )
        }
    }
}
