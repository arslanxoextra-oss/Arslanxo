package com.example.data

import android.util.Log
import com.example.data.local.FavoriteDao
import com.example.data.local.FavoriteEntity
import com.example.model.AppPolicies
import com.example.model.ImagePost
import com.example.model.Post
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FirebaseRepository(
    private val favoriteDao: FavoriteDao
) {
    private val TAG = "FirebaseRepository"
    private val firestore by lazy {
        try {
            val db = FirebaseFirestore.getInstance()
            try {
                val settings = FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .build()
                db.firestoreSettings = settings
            } catch (e: Exception) {
                Log.w(TAG, "Firestore settings already initialized: ${e.message}")
            }
            db
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Firestore", e)
            null
        }
    }

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    private val _imagePosts = MutableStateFlow<List<ImagePost>>(emptyList())
    val imagePosts: StateFlow<List<ImagePost>> = _imagePosts.asStateFlow()

    private val _policies = MutableStateFlow<AppPolicies>(SeedData.defaultPolicies)
    val policies: StateFlow<AppPolicies> = _policies.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        startSync()
    }

    fun startSync() {
        _isLoading.value = true
        _errorMessage.value = null

        val db = firestore
        if (db == null) {
            _isLoading.value = false
            return
        }

        // Listen to "posts" (or fallback "Post") collection
        fun registerPostListener(collectionName: String, onEmptyOrError: () -> Unit) {
            try {
                db.collection(collectionName)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.w(TAG, "Listen failed for $collectionName", error)
                            onEmptyOrError()
                        } else if (snapshot != null && !snapshot.isEmpty) {
                            val list = snapshot.documents.mapNotNull { doc ->
                                try {
                                    val post = doc.toObject(Post::class.java)?.copy(id = doc.id)
                                    post?.let {
                                        // Standardize thumbnail & description
                                        it.copy(
                                            thumbnail = it.displayThumbnail,
                                            description = it.displayDescription
                                        )
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error parsing Post doc: ${doc.id}", e)
                                    null
                                }
                            }
                            if (list.isNotEmpty()) {
                                // Sort descending by createdAt: newest posts added today show first!
                                _posts.value = list.sortedByDescending { it.creationTimestamp }
                            } else {
                                onEmptyOrError()
                            }
                        } else {
                            onEmptyOrError()
                        }
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error registering $collectionName listener", e)
                onEmptyOrError()
            }
        }

        // Primary: "posts" collection requested by user
        registerPostListener("posts") {
            // Secondary fallback: "Post"
            registerPostListener("Post") {
                // If neither collection has data, keep empty list - no placeholder posts!
                _isLoading.value = false
            }
        }

        // Listen to "imageposts" / "imagepost"
        fun registerImageListener(collectionName: String, onEmptyOrError: () -> Unit) {
            try {
                db.collection(collectionName)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.w(TAG, "Listen failed for $collectionName", error)
                            onEmptyOrError()
                        } else if (snapshot != null && !snapshot.isEmpty) {
                            val list = snapshot.documents.mapNotNull { doc ->
                                try {
                                    val img = doc.toObject(ImagePost::class.java)?.copy(id = doc.id)
                                    img?.let {
                                        it.copy(
                                            imagetitle = it.displayTitle,
                                            imageprompt = it.displayPrompt,
                                            thumbnail = it.displayThumbnail,
                                            categoryforall = it.displayCategory
                                        )
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error parsing ImagePost doc: ${doc.id}", e)
                                    null
                                }
                            }
                            if (list.isNotEmpty()) {
                                // Sort descending by createdAt: newest image posts show first!
                                _imagePosts.value = list.sortedByDescending { it.creationTimestamp }
                            } else {
                                onEmptyOrError()
                            }
                        } else {
                            onEmptyOrError()
                        }
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error registering $collectionName listener", e)
                onEmptyOrError()
            }
        }

        registerImageListener("imageposts") {
            registerImageListener("imagepost") {
                registerImageListener("images") {
                    // If no collection has images, keep empty list - no placeholder images!
                    _isLoading.value = false
                }
            }
        }

        // Listen to "apppolices" collection
        try {
            db.collection("apppolices")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Listen failed for apppolices", error)
                    } else if (snapshot != null && !snapshot.isEmpty) {
                        val doc = snapshot.documents.firstOrNull()
                        val policy = doc?.toObject(AppPolicies::class.java)
                        if (policy != null) {
                            _policies.value = policy.copy(id = doc.id)
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error registering apppolices listener", e)
        }
    }

    // Favorites Room DAO helpers
    fun getAllFavorites(): Flow<List<FavoriteEntity>> = favoriteDao.getAllFavorites()

    fun getFavoritesByType(type: String): Flow<List<FavoriteEntity>> =
        favoriteDao.getFavoritesByType(type)

    fun isFavorite(id: String): Flow<Boolean> = favoriteDao.isFavorite(id)

    suspend fun togglePostFavorite(post: Post, isCurrentlyFav: Boolean) {
        if (isCurrentlyFav) {
            favoriteDao.deleteFavorite(post.id)
        } else {
            favoriteDao.insertFavorite(
                FavoriteEntity(
                    id = post.id,
                    type = "POST",
                    title = post.title,
                    description = post.displayDescription,
                    thumbnail = post.displayThumbnail,
                    category = post.category,
                    stepsNumbers = post.StepsNumbers,
                    step1title = post.step1title,
                    step1description = post.step1description,
                    step1prompt = post.step1prompt,
                    step1toollink = post.step1toollink,
                    step1toolname = post.step1toolname,
                    step2title = post.step2title,
                    step2description = post.step2description,
                    step2prompt = post.step2prompt,
                    step2toollink = post.step2toollink,
                    step2toolname = post.step2toolname,
                    has2bean = post.has2bean
                )
            )
        }
    }

    suspend fun toggleImagePostFavorite(imagePost: ImagePost, isCurrentlyFav: Boolean) {
        if (isCurrentlyFav) {
            favoriteDao.deleteFavorite(imagePost.id)
        } else {
            favoriteDao.insertFavorite(
                FavoriteEntity(
                    id = imagePost.id,
                    type = "IMAGE",
                    title = imagePost.displayTitle,
                    description = imagePost.displayPrompt,
                    thumbnail = imagePost.displayThumbnail,
                    category = imagePost.displayCategory,
                    step1prompt = imagePost.displayPrompt,
                    step1toollink = imagePost.imagetoollink,
                    step1toolname = imagePost.imagetoolname
                )
            )
        }
    }

    suspend fun removeFavoriteById(id: String) {
        favoriteDao.deleteFavorite(id)
    }
}
