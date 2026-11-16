package com.christianokeke.scroll.ui

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.BookmarkRemove
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.christianokeke.scroll.data.BibleVerse
import com.christianokeke.scroll.ui.theme.ScrollTheme
import kotlinx.coroutines.delay

/** Layout numbers shared by every screen (the iOS app's `Theme.Layout`). */
object Dimens {
    val horizontalPadding = 16.dp
    val cardPadding = 16.dp
    val cardRadius = 16.dp
    val iconButton = 40.dp
    val smallIconButton = 36.dp

    /** Content never grows wider than this; on tablets and in landscape it sits centred. */
    val readingColumn = 720.dp
}

val VerseTextStyle = TextStyle(fontSize = 16.sp, lineHeight = 25.sp)

// ---------------------------------------------------------------------------
// Screen frame
// ---------------------------------------------------------------------------

/**
 * Background, system-bar insets and the toast layer for one screen.
 * [content] gets the side padding that centres a 720dp reading column, to add
 * to its own horizontal padding. Full-width scrolling stays possible because
 * the padding goes inside the scrolling container, not around it.
 */
@Composable
fun ScreenFrame(
    toast: ToastState? = null,
    content: @Composable (sidePadding: Dp) -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(ScrollTheme.colors.background)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)),
    ) {
        val side = ((maxWidth - Dimens.readingColumn) / 2).coerceAtLeast(0.dp)
        content(side)
        if (toast != null) {
            ToastHost(toast, Modifier.align(Alignment.TopCenter).padding(top = 60.dp))
        }
    }
}

/** True on tablet-sized windows, where the home tiles get the roomier iPad sizing. */
@Composable
fun isRoomyWindow(): Boolean {
    val size = LocalWindowInfo.current.containerSize
    return with(LocalDensity.current) { size.width.toDp() >= 600.dp && size.height.toDp() >= 480.dp }
}

/** Space to leave under the last item so it can scroll clear of the navigation bar. */
@Composable
fun navigationBarBottom(): Dp =
    WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

@Composable
fun listPadding(sidePadding: Dp): PaddingValues = PaddingValues(
    start = sidePadding + Dimens.horizontalPadding,
    end = sidePadding + Dimens.horizontalPadding,
    top = 8.dp,
    bottom = navigationBarBottom() + 24.dp,
)

/** Jumps a list back to the top whenever [token] changes (new shuffle, new chapter). */
@Composable
fun ResetScrollOnChange(listState: LazyListState, token: Any) {
    // Starts at the current token, so a rotation or a restored process keeps its place.
    val last = remember { arrayOf(token) }
    if (last[0] != token) {
        last[0] = token
        Snapshot.withoutReadObservation { listState.requestScrollToItem(0) }
    }
}

// ---------------------------------------------------------------------------
// Buttons, cards, headers
// ---------------------------------------------------------------------------

@Composable
fun CircleIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = Dimens.iconButton,
    enabled: Boolean = true,
    foreground: Color = ScrollTheme.colors.accent,
    background: Color = ScrollTheme.colors.card,
    iconSize: Dp = if (size < Dimens.iconButton) 20.dp else 22.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(background)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) foreground else ScrollTheme.colors.disabled,
            modifier = Modifier.size(iconSize),
        )
    }
}

@Composable
fun BackButton(onBack: () -> Unit, modifier: Modifier = Modifier) {
    CircleIconButton(
        icon = Icons.AutoMirrored.Filled.ArrowBack,
        contentDescription = "Back",
        onClick = onBack,
        modifier = modifier,
        foreground = ScrollTheme.colors.textPrimary,
    )
}

/** A card on the grey "secondary background", 16dp corners. */
@Composable
fun ScrollCard(
    modifier: Modifier = Modifier,
    radius: Dp = Dimens.cardRadius,
    color: Color = ScrollTheme.colors.card,
    onClick: (() -> Unit)? = null,
    onClickLabel: String? = null,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(radius)
    Box(
        modifier = modifier
            .clip(shape)
            .background(color)
            .then(if (onClick != null) Modifier.clickable(onClickLabel = onClickLabel, onClick = onClick) else Modifier),
    ) {
        content()
    }
}

/** One line of text that shrinks before it truncates. */
@Composable
fun FittedText(
    text: String,
    color: Color,
    maxSize: TextUnit,
    minSize: TextUnit,
    weight: FontWeight,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
) {
    BasicText(
        text = text,
        modifier = modifier,
        style = TextStyle(color = color, fontWeight = weight, textAlign = textAlign),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        autoSize = TextAutoSize.StepBased(minFontSize = minSize, maxFontSize = maxSize, stepSize = 0.5.sp),
    )
}

/** Back button on its own row, then a centred title and subtitle (Saved lists, Settings). */
@Composable
fun CenteredHeader(title: String, subtitle: String?, onBack: () -> Unit, sidePadding: Dp) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = sidePadding + Dimens.horizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(Modifier.fillMaxWidth().padding(top = 8.dp)) { BackButton(onBack) }
        Text(
            text = title,
            color = ScrollTheme.colors.textPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 4.dp).semantics { heading() },
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                color = ScrollTheme.colors.textSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        Spacer(Modifier.height(16.dp))
    }
}

// ---------------------------------------------------------------------------
// Loading, empty and error states
// ---------------------------------------------------------------------------

/** Centres a state message; scrolls rather than clips when the window is very short. */
@Composable
private fun CenteredState(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp)
                .padding(top = 24.dp, bottom = navigationBarBottom() + 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            content()
        }
    }
}

@Composable
fun LoadingState(message: String) {
    CenteredState {
        CircularProgressIndicator(color = ScrollTheme.colors.accent, strokeWidth = 3.dp, modifier = Modifier.size(32.dp))
        Spacer(Modifier.height(12.dp))
        Text(message, color = ScrollTheme.colors.textSecondary, fontSize = 14.sp)
    }
}

@Composable
fun EmptyState(icon: ImageVector, title: String, hint: String) {
    CenteredState {
        Icon(icon, contentDescription = null, tint = ScrollTheme.colors.disabled, modifier = Modifier.size(52.dp))
        Spacer(Modifier.height(12.dp))
        Text(title, color = ScrollTheme.colors.textSecondary, fontSize = 17.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(hint, color = ScrollTheme.colors.textTertiary, fontSize = 14.sp, textAlign = TextAlign.Center)
    }
}

/** A load that failed (or came back empty): a short reason and a way to try again. */
@Composable
fun RetryState(title: String, hint: String, onRetry: () -> Unit, icon: ImageVector = Icons.Filled.CloudOff) {
    CenteredState {
        Icon(icon, contentDescription = null, tint = ScrollTheme.colors.disabled, modifier = Modifier.size(52.dp))
        Spacer(Modifier.height(12.dp))
        Text(title, color = ScrollTheme.colors.textSecondary, fontSize = 17.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(hint, color = ScrollTheme.colors.textTertiary, fontSize = 14.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(20.dp))
        Row(
            modifier = Modifier
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(ScrollTheme.colors.accent)
                .clickable(role = Role.Button, onClick = onRetry)
                .padding(horizontal = 28.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = null, tint = ScrollTheme.colors.onAccent, modifier = Modifier.size(18.dp))
            Text("Retry", color = ScrollTheme.colors.onAccent, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}

// ---------------------------------------------------------------------------
// Verse card
// ---------------------------------------------------------------------------

enum class VerseCardAction { Save, Saved, Remove }

@Composable
fun VerseCard(
    verse: BibleVerse,
    action: VerseCardAction,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ScrollTheme.colors
    ScrollCard(modifier = modifier.fillMaxWidth(), onClick = onOpen, onClickLabel = "Open chapter") {
        Column(Modifier.padding(Dimens.cardPadding), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(verse.text, color = colors.textPrimary, style = VerseTextStyle)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = verse.reference,
                    color = colors.accent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.size(8.dp))

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.accentLight)
                        .clickable(role = Role.Button, onClick = onShare),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Outlined.Share, contentDescription = "Share verse", tint = colors.accent, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.size(8.dp))

                val (icon, label) = when (action) {
                    VerseCardAction.Save -> Icons.Outlined.BookmarkBorder to "Save"
                    VerseCardAction.Saved -> Icons.Filled.Bookmark to "Saved"
                    VerseCardAction.Remove -> Icons.Outlined.BookmarkRemove to "Remove"
                }
                val foreground = when (action) {
                    VerseCardAction.Save -> colors.accent
                    VerseCardAction.Saved -> colors.onAccent
                    VerseCardAction.Remove -> colors.destructive
                }
                val background = when (action) {
                    VerseCardAction.Save -> colors.accentLight
                    VerseCardAction.Saved -> colors.accent
                    VerseCardAction.Remove -> colors.destructiveLight
                }
                Row(
                    modifier = Modifier
                        .height(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(background)
                        .clickable(role = Role.Button, onClick = onAction)
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(icon, contentDescription = null, tint = foreground, modifier = Modifier.size(17.dp))
                    Text(label, color = foreground, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Toast
// ---------------------------------------------------------------------------

/** The teal pill that drops in under the status bar for two seconds. */
@Stable
class ToastState {
    internal var message by mutableStateOf<String?>(null)
    internal var shownCount by mutableIntStateOf(0)

    fun show(text: String) {
        message = text
        shownCount++ // restarts the timer when the same message is shown twice in a row
    }
}

@Composable
fun rememberToastState(): ToastState = remember { ToastState() }

@Composable
private fun ToastHost(state: ToastState, modifier: Modifier = Modifier) {
    val message = state.message
    // Keeps the text on screen while the pill animates away.
    var lastMessage by remember { mutableStateOf("") }
    if (message != null) lastMessage = message

    LaunchedEffect(state.shownCount) {
        if (state.message != null) {
            delay(2000)
            state.message = null
        }
    }

    AnimatedVisibility(
        visible = message != null,
        modifier = modifier,
        enter = slideInVertically { -it * 2 } + fadeIn(),
        exit = slideOutVertically { -it * 2 } + fadeOut(),
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(ScrollTheme.colors.accent)
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .semantics { liveRegion = LiveRegionMode.Polite },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(toastIcon(lastMessage), contentDescription = null, tint = ScrollTheme.colors.onAccent, modifier = Modifier.size(16.dp))
            Text(lastMessage, color = ScrollTheme.colors.onAccent, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 2)
        }
    }
}

private fun toastIcon(message: String): ImageVector = when {
    "Removed" in message -> Icons.Filled.BookmarkRemove
    "Copied" in message -> Icons.Filled.ContentCopy
    "Refreshed" in message -> Icons.Filled.Refresh
    "Couldn" in message -> Icons.Filled.CloudOff
    else -> Icons.Filled.Bookmark
}

// ---------------------------------------------------------------------------
// System hand-offs
// ---------------------------------------------------------------------------

/** Opens the Android share sheet with plain text. */
fun shareText(context: Context, text: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    try {
        context.startActivity(Intent.createChooser(send, null))
    } catch (_: ActivityNotFoundException) {
        // No app can receive text: nothing to offer.
    }
}

fun copyText(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(ClipboardManager::class.java) ?: return
    clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
}
