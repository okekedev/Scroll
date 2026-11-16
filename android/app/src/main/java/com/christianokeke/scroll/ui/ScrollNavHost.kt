package com.christianokeke.scroll.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.christianokeke.scroll.data.SavedStore

private object Routes {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val SAVED_VERSES = "saved-verses"
    const val SAVED_CHAPTERS = "saved-chapters"
    const val VIRTUE = "virtue/{${VirtueViewModel.ARG_INDEX}}"
    const val CHAPTER = "chapter/{${ChapterViewModel.ARG_TRANSLATION}}/{${ChapterViewModel.ARG_BOOK}}" +
        "/{${ChapterViewModel.ARG_CHAPTER}}?verse={${ChapterViewModel.ARG_VERSE}}"

    fun virtue(index: Int) = "virtue/$index"
    fun chapter(translation: String, book: Int, chapter: Int, verse: Int = 0) =
        "chapter/$translation/$book/$chapter?verse=$verse"
}

private const val SLIDE_MS = 280

@Composable
fun ScrollNavHost(store: SavedStore) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        enterTransition = { slideInHorizontally(tween(SLIDE_MS)) { it } },
        exitTransition = { slideOutHorizontally(tween(SLIDE_MS)) { -it / 4 } + fadeOut(tween(SLIDE_MS)) },
        popEnterTransition = { slideInHorizontally(tween(SLIDE_MS)) { -it / 4 } + fadeIn(tween(SLIDE_MS)) },
        popExitTransition = { slideOutHorizontally(tween(SLIDE_MS)) { it } },
    ) {
        composable(Routes.HOME) { entry ->
            val verses by store.verses.collectAsStateWithLifecycle()
            val chapters by store.chapters.collectAsStateWithLifecycle()
            HomeScreen(
                savedVerseCount = verses.size,
                savedChapterCount = chapters.size,
                onOpenVirtue = { navController.navigateFrom(entry, Routes.virtue(it)) },
                onOpenSavedVerses = { navController.navigateFrom(entry, Routes.SAVED_VERSES) },
                onOpenSavedChapters = { navController.navigateFrom(entry, Routes.SAVED_CHAPTERS) },
                onOpenSettings = { navController.navigateFrom(entry, Routes.SETTINGS) },
            )
        }

        composable(
            route = Routes.VIRTUE,
            arguments = listOf(navArgument(VirtueViewModel.ARG_INDEX) { type = NavType.IntType }),
        ) { entry ->
            val viewModel = viewModel { VirtueViewModel(createSavedStateHandle(), store) }
            VirtueScreen(
                viewModel = viewModel,
                store = store,
                onBack = { navController.backFrom(entry) },
                onOpenVerse = { verse ->
                    navController.navigateFrom(entry, Routes.chapter(verse.translation, verse.book, verse.chapter, verse.verse))
                },
            )
        }

        composable(
            route = Routes.CHAPTER,
            arguments = listOf(
                navArgument(ChapterViewModel.ARG_TRANSLATION) { type = NavType.StringType },
                navArgument(ChapterViewModel.ARG_BOOK) { type = NavType.IntType },
                navArgument(ChapterViewModel.ARG_CHAPTER) { type = NavType.IntType },
                navArgument(ChapterViewModel.ARG_VERSE) { type = NavType.IntType; defaultValue = 0 },
            ),
        ) { entry ->
            val viewModel = viewModel { ChapterViewModel(createSavedStateHandle()) }
            ChapterScreen(viewModel = viewModel, store = store, onBack = { navController.backFrom(entry) })
        }

        composable(Routes.SAVED_VERSES) { entry ->
            SavedVersesScreen(
                store = store,
                onBack = { navController.backFrom(entry) },
                onOpenVerse = { verse ->
                    navController.navigateFrom(entry, Routes.chapter(verse.translation, verse.book, verse.chapter, verse.verse))
                },
            )
        }

        composable(Routes.SAVED_CHAPTERS) { entry ->
            SavedChaptersScreen(
                store = store,
                onBack = { navController.backFrom(entry) },
                onOpenChapter = { chapter ->
                    navController.navigateFrom(entry, Routes.chapter(chapter.translation, chapter.book, chapter.chapter))
                },
            )
        }

        composable(Routes.SETTINGS) { entry ->
            SettingsScreen(onBack = { navController.backFrom(entry) })
        }
    }
}

// Both helpers act only while the calling screen is the one in front, so a
// double tap cannot open two screens or pop past the one underneath.

private fun NavHostController.navigateFrom(from: NavBackStackEntry, route: String) {
    if (from.lifecycle.currentState == Lifecycle.State.RESUMED) navigate(route)
}

private fun NavHostController.backFrom(from: NavBackStackEntry) {
    if (from.lifecycle.currentState == Lifecycle.State.RESUMED) popBackStack()
}
