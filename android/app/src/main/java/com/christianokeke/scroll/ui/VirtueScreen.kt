package com.christianokeke.scroll.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.christianokeke.scroll.data.BibleTranslations
import com.christianokeke.scroll.data.BibleVerse
import com.christianokeke.scroll.data.BollsClient
import com.christianokeke.scroll.data.SavedStore
import com.christianokeke.scroll.data.VerseShare
import com.christianokeke.scroll.ui.theme.ScrollTheme
import kotlin.random.Random
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VirtueUiState(
    val title: String,
    val translation: String,
    /** Search results in their shuffled order. */
    val verses: List<BibleVerse> = emptyList(),
    /** Identifies the current shuffle; changes whenever the order does. */
    val order: Long = 0,
    val isLoading: Boolean = true,
    /** The last load failed and there is nothing to show. */
    val failed: Boolean = false,
)

class VirtueViewModel(
    private val handle: SavedStateHandle,
    private val store: SavedStore,
) : ViewModel() {
    private val virtue = Virtues.all[(handle.get<Int>(ARG_INDEX) ?: 0).coerceIn(Virtues.all.indices)]

    /** Results as the API returned them; the screen shows them shuffled by [seed]. */
    private var results: List<BibleVerse> = emptyList()

    // The seed is saved so that a restored process shows the same order again.
    private var seed: Long
        get() = handle[KEY_SEED] ?: 0L
        set(value) { handle[KEY_SEED] = value }

    private val _state = MutableStateFlow(
        VirtueUiState(
            title = virtue.name,
            translation = BibleTranslations.resolve(handle[KEY_TRANSLATION] ?: store.translation),
        )
    )
    val state: StateFlow<VirtueUiState> = _state.asStateFlow()

    private val _toasts = Channel<String>(Channel.BUFFERED)
    val toasts: Flow<String> = _toasts.receiveAsFlow()

    private var loadJob: Job? = null

    init {
        load(reshuffle = !handle.contains(KEY_SEED))
    }

    fun selectTranslation(code: String) {
        val current = _state.value
        if (code == current.translation && !current.failed) return
        store.translation = code
        handle[KEY_TRANSLATION] = code
        results = emptyList()
        _state.update { it.copy(translation = code, verses = emptyList()) }
        load(reshuffle = true)
    }

    fun shuffle() {
        if (results.isEmpty()) return
        seed = Random.nextLong()
        publish()
    }

    /** The refresh button: same search again, new order, and a toast when it lands. */
    fun refresh() = load(reshuffle = true, announce = true)

    fun retry() = load(reshuffle = true)

    private fun load(reshuffle: Boolean, announce: Boolean = false) {
        loadJob?.cancel()
        val translation = _state.value.translation
        _state.update { it.copy(isLoading = true, failed = false) }
        loadJob = viewModelScope.launch {
            val loaded = try {
                BollsClient.searchVerses(translation, virtue.searchTerm)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                null
            }
            if (loaded == null) {
                val nothingToShow = results.isEmpty()
                _state.update { it.copy(isLoading = false, failed = nothingToShow) }
                // A failed refresh keeps the verses already on screen.
                if (!nothingToShow) _toasts.send("Couldn’t refresh")
            } else {
                results = loaded
                if (reshuffle) seed = Random.nextLong()
                publish()
                if (announce) _toasts.send("Refreshed")
            }
        }
    }

    private fun publish() {
        val seed = seed
        _state.update {
            it.copy(verses = results.shuffled(Random(seed)), order = seed, isLoading = false, failed = false)
        }
    }

    companion object {
        const val ARG_INDEX = "index"
        private const val KEY_SEED = "seed"
        private const val KEY_TRANSLATION = "selectedTranslation"
    }
}

@Composable
fun VirtueScreen(
    viewModel: VirtueViewModel,
    store: SavedStore,
    onBack: () -> Unit,
    onOpenVerse: (BibleVerse) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val saved by store.verses.collectAsStateWithLifecycle()
    val savedIds = saved.mapTo(HashSet()) { it.id }
    val toast = rememberToastState()
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current

    LaunchedEffect(viewModel) {
        viewModel.toasts.collect { toast.show(it) }
    }
    ResetScrollOnChange(listState, state.order)

    ScreenFrame(toast) { side ->
        Column(Modifier.fillMaxSize()) {
            VirtueHeader(state, side, onBack, viewModel::shuffle, viewModel::refresh, viewModel::selectTranslation)

            when {
                state.verses.isNotEmpty() -> LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = listPadding(side),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.verses, key = { it.id }) { verse ->
                        val isSaved = verse.id in savedIds
                        VerseCard(
                            verse = verse,
                            action = if (isSaved) VerseCardAction.Saved else VerseCardAction.Save,
                            onOpen = { onOpenVerse(verse) },
                            onShare = { shareText(context, VerseShare.text(verse)) },
                            onAction = {
                                // Matches iOS: saving twice is a no-op, the toast still confirms it.
                                store.saveVerse(verse)
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                toast.show("Verse Saved")
                            },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }

                state.isLoading -> LoadingState("Loading verses...")

                state.failed -> RetryState(
                    title = "Couldn’t load verses",
                    hint = "Check your connection and try again.",
                    onRetry = viewModel::retry,
                )

                else -> RetryState(
                    title = "No verses found",
                    hint = "Nothing came back for this search in the ${state.translation}.",
                    onRetry = viewModel::retry,
                    icon = Icons.Filled.SearchOff,
                )
            }
        }
    }
}

@Composable
private fun VirtueHeader(
    state: VirtueUiState,
    sidePadding: Dp,
    onBack: () -> Unit,
    onShuffle: () -> Unit,
    onRefresh: () -> Unit,
    onSelectTranslation: (String) -> Unit,
) {
    val colors = ScrollTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = sidePadding + Dimens.horizontalPadding)
            .padding(top = 8.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BackButton(onBack)
            FittedText(
                text = state.title,
                color = colors.textPrimary,
                maxSize = 28.sp,
                minSize = 18.sp,
                weight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f).padding(start = 4.dp).semantics { heading() },
            )
            CircleIconButton(
                icon = Icons.Filled.Shuffle,
                contentDescription = "Shuffle verses",
                onClick = onShuffle,
                enabled = state.verses.isNotEmpty(),
            )
            if (state.isLoading) {
                Box(
                    modifier = Modifier.size(Dimens.iconButton).clip(CircleShape).background(colors.card),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = colors.accent, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                }
            } else {
                CircleIconButton(icon = Icons.Filled.Refresh, contentDescription = "Refresh verses", onClick = onRefresh)
            }
        }

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            for (code in BibleTranslations.available) {
                val isSelected = code == state.translation
                Box(
                    modifier = Modifier
                        .heightIn(min = 34.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) colors.accent else colors.card)
                        .clickable(role = Role.Tab) { onSelectTranslation(code) }
                        .semantics { selected = isSelected }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = code,
                        color = if (isSelected) colors.onAccent else colors.textSecondary,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}
