package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.ads.AdManager
import com.example.model.ImagePost
import com.example.model.Post
import com.example.ui.components.AppBottomBar
import com.example.ui.components.AppDrawerContent
import com.example.ui.components.AppSplashScreen
import com.example.ui.components.AppTopBar
import com.example.ui.components.OfflineScreen
import com.example.ui.components.ScreenTab
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ImageDetailScreen
import com.example.ui.screens.ImagesScreen
import com.example.ui.screens.PostDetailScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.PromptXoViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: PromptXoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize AdMob
        AdManager.initialize(this)

        setContent {
            MyApplicationTheme {
                PromptXoApp(viewModel = viewModel)
            }
        }
    }
}

sealed class AppDestination {
    object MainTabs : AppDestination()
    data class PostDetail(val post: Post) : AppDestination()
    data class ImageDetail(val imagePost: ImagePost) : AppDestination()
}

@Composable
fun PromptXoApp(viewModel: PromptXoViewModel) {
    val isOnline by viewModel.isOnline.collectAsState()
    val policies by viewModel.policies.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val posts by viewModel.allPosts.collectAsState()
    val imagePosts by viewModel.allImagePosts.collectAsState()

    // Splash screen state: keep splash visible until Firebase data arrives (or up to 2.5s max fallback)
    var isSplashVisible by remember { mutableStateOf(true) }

    LaunchedEffect(isLoading, posts, imagePosts) {
        if (!isLoading && (posts.isNotEmpty() || imagePosts.isNotEmpty())) {
            // Once Firebase has delivered posts, dismiss splash smoothly
            delay(400)
            isSplashVisible = false
        }
    }

    // Safety timeout: if device is offline or collection is empty, don't block forever
    LaunchedEffect(Unit) {
        delay(2500)
        isSplashVisible = false
    }

    if (isSplashVisible) {
        AppSplashScreen()
        return
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    var currentTab by remember { mutableStateOf<ScreenTab>(ScreenTab.Home) }
    var currentDestination by remember { mutableStateOf<AppDestination>(AppDestination.MainTabs) }

    // Enforce internet connectivity: user explicitly requested app must not be usable without internet
    if (!isOnline) {
        OfflineScreen(
            onRetry = {
                viewModel.checkAndRetryConnection()
            }
        )
        return
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                policies = policies,
                onClose = {
                    coroutineScope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            containerColor = Color(0xFF090A12),
            topBar = {
                if (currentDestination is AppDestination.MainTabs) {
                    AppTopBar(
                        onOpenDrawer = {
                            coroutineScope.launch { drawerState.open() }
                        }
                    )
                }
            },
            bottomBar = {
                if (currentDestination is AppDestination.MainTabs) {
                    AppBottomBar(
                        currentRoute = currentTab.route,
                        onTabSelected = { tab ->
                            currentTab = tab
                        }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Smooth sliding and fading transition animations between screens
                AnimatedContent(
                    targetState = currentDestination,
                    transitionSpec = {
                        if (targetState is AppDestination.PostDetail || targetState is AppDestination.ImageDetail) {
                            (slideInHorizontally(animationSpec = tween(300)) { fullWidth -> fullWidth } + fadeIn(animationSpec = tween(300)))
                                .togetherWith(slideOutHorizontally(animationSpec = tween(300)) { fullWidth -> -fullWidth / 3 } + fadeOut(animationSpec = tween(300)))
                        } else {
                            (slideInHorizontally(animationSpec = tween(300)) { fullWidth -> -fullWidth / 3 } + fadeIn(animationSpec = tween(300)))
                                .togetherWith(slideOutHorizontally(animationSpec = tween(300)) { fullWidth -> fullWidth } + fadeOut(animationSpec = tween(300)))
                        }
                    },
                    label = "screen_transition"
                ) { destination ->
                    when (destination) {
                        is AppDestination.MainTabs -> {
                            Crossfade(
                                targetState = currentTab,
                                animationSpec = tween(220),
                                label = "tab_crossfade"
                            ) { tab ->
                                when (tab) {
                                    ScreenTab.Home -> {
                                        HomeScreen(
                                            viewModel = viewModel,
                                            onNavigateToPostDetail = { post ->
                                                currentDestination = AppDestination.PostDetail(post)
                                            }
                                        )
                                    }

                                    ScreenTab.Images -> {
                                        ImagesScreen(
                                            viewModel = viewModel,
                                            onNavigateToImageDetail = { imagePost ->
                                                currentDestination = AppDestination.ImageDetail(imagePost)
                                            }
                                        )
                                    }

                                    ScreenTab.Favourite -> {
                                        FavoritesScreen(
                                            viewModel = viewModel,
                                            onNavigateToPostDetail = { post ->
                                                currentDestination = AppDestination.PostDetail(post)
                                            },
                                            onNavigateToImageDetail = { imagePost ->
                                                currentDestination = AppDestination.ImageDetail(imagePost)
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        is AppDestination.PostDetail -> {
                            BackHandler {
                                currentDestination = AppDestination.MainTabs
                            }
                            PostDetailScreen(
                                post = destination.post,
                                viewModel = viewModel,
                                onNavigateBack = {
                                    currentDestination = AppDestination.MainTabs
                                }
                            )
                        }

                        is AppDestination.ImageDetail -> {
                            BackHandler {
                                currentDestination = AppDestination.MainTabs
                            }
                            ImageDetailScreen(
                                imagePost = destination.imagePost,
                                viewModel = viewModel,
                                onNavigateBack = {
                                    currentDestination = AppDestination.MainTabs
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
