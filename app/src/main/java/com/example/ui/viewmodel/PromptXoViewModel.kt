package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import coil.imageLoader
import coil.request.ImageRequest
import com.example.data.FirebaseRepository
import com.example.data.NetworkMonitor
import com.example.data.local.AppDatabase
import com.example.data.local.FavoriteEntity
import com.example.model.AppPolicies
import com.example.model.ImagePost
import com.example.model.Post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PromptXoViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val repository = FirebaseRepository(database.favoriteDao())
    private val networkMonitor = NetworkMonitor(application)

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = networkMonitor.isCurrentlyOnline()
    )

    val allPosts: StateFlow<List<Post>> = repository.posts
    val allImagePosts: StateFlow<List<ImagePost>> = repository.imagePosts
    val policies: StateFlow<AppPolicies> = repository.policies
    val isLoading: StateFlow<Boolean> = repository.isLoading

    init {
        // Automatically preload initial image thumbnails into Coil cache
        viewModelScope.launch(Dispatchers.IO) {
            allPosts.collectLatest { posts ->
                val loader = getApplication<Application>().imageLoader
                posts.take(10).forEach { post ->
                    val url = post.displayThumbnail
                    if (url.isNotBlank()) {
                        val req = ImageRequest.Builder(getApplication())
                            .data(url)
                            .build()
                        loader.enqueue(req)
                    }
                }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            allImagePosts.collectLatest { images ->
                val loader = getApplication<Application>().imageLoader
                images.take(12).forEach { img ->
                    val url = img.displayThumbnail
                    if (url.isNotBlank()) {
                        val req = ImageRequest.Builder(getApplication())
                            .data(url)
                            .build()
                        loader.enqueue(req)
                    }
                }
            }
        }
    }

    // Home Screen Filtering & Pagination
    val homeSearchQuery = MutableStateFlow("")
    val homeSelectedCategory = MutableStateFlow("All")
    private val _homeVisibleLimit = MutableStateFlow(5) // User requested: first 5 posts
    val homeVisibleLimit: StateFlow<Int> = _homeVisibleLimit.asStateFlow()

    // Images Screen Filtering & Pagination
    val imageSearchQuery = MutableStateFlow("")
    val imageSelectedCategory = MutableStateFlow("All")
    private val _imageVisibleLimit = MutableStateFlow(8) // User requested: first 8 posts
    val imageVisibleLimit: StateFlow<Int> = _imageVisibleLimit.asStateFlow()

    // Filtered Home Posts
    val filteredHomePosts: StateFlow<List<Post>> = combine(
        allPosts,
        homeSearchQuery,
        homeSelectedCategory
    ) { posts, query, category ->
        posts.filter { post ->
            val matchesCategory = (category == "All") ||
                    post.homeCategory.equals(category, ignoreCase = true) ||
                    post.category.equals(category, ignoreCase = true) ||
                    post.title.contains(category, ignoreCase = true) ||
                    post.displayDescription.contains(category, ignoreCase = true)

            val matchesQuery = query.isBlank() ||
                    post.title.contains(query, ignoreCase = true) ||
                    post.displayDescription.contains(query, ignoreCase = true) ||
                    post.step1prompt.contains(query, ignoreCase = true)

            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Image Posts
    val filteredImagePosts: StateFlow<List<ImagePost>> = combine(
        allImagePosts,
        imageSearchQuery,
        imageSelectedCategory
    ) { imagePosts, query, category ->
        imagePosts.filter { post ->
            val matchesCategory = (category == "All") ||
                    post.categoryforall.equals(category, ignoreCase = true) ||
                    post.categoryfornew.equals(category, ignoreCase = true) ||
                    post.categoryfortrending.equals(category, ignoreCase = true) ||
                    post.displayCategory.equals(category, ignoreCase = true) ||
                    post.displayTitle.contains(category, ignoreCase = true) ||
                    post.displayPrompt.contains(category, ignoreCase = true)

            val matchesQuery = query.isBlank() ||
                    post.displayTitle.contains(query, ignoreCase = true) ||
                    post.displayPrompt.contains(query, ignoreCase = true)

            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Favorites from Room
    val videoFavorites: StateFlow<List<FavoriteEntity>> = repository.getFavoritesByType("POST")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val imageFavorites: StateFlow<List<FavoriteEntity>> = repository.getFavoritesByType("IMAGE")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Pagination Actions
    fun loadMoreHomePosts() {
        // User requested: Home page loads 3 more posts on each Load More
        _homeVisibleLimit.value += 3
    }

    fun loadMoreImagePosts() {
        // User requested: Image page loads 4 more posts on each Load More
        _imageVisibleLimit.value += 4
    }

    // Toggle Favorites
    fun togglePostFavorite(post: Post, isCurrentlyFav: Boolean) {
        viewModelScope.launch {
            repository.togglePostFavorite(post, isCurrentlyFav)
        }
    }

    fun toggleImagePostFavorite(imagePost: ImagePost, isCurrentlyFav: Boolean) {
        viewModelScope.launch {
            repository.toggleImagePostFavorite(imagePost, isCurrentlyFav)
        }
    }

    fun removeFavoriteById(id: String) {
        viewModelScope.launch {
            repository.removeFavoriteById(id)
        }
    }

    fun retrySync() {
        repository.startSync()
    }

    suspend fun checkAndRetryConnection(): Boolean {
        val online = networkMonitor.recheckConnectivity()
        if (online) {
            repository.startSync()
        }
        return online
    }
}
