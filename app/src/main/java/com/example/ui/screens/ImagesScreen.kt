package com.example.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ads.AdManager
import com.example.model.ImagePost
import com.example.ui.components.ImageCardShimmer
import com.example.ui.components.NativeAdCard
import com.example.ui.viewmodel.PromptXoViewModel

@Composable
fun ImagesScreen(
    viewModel: PromptXoViewModel,
    onNavigateToImageDetail: (ImagePost) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val focusManager = LocalFocusManager.current

    val searchQuery by viewModel.imageSearchQuery.collectAsState()
    val selectedCategory by viewModel.imageSelectedCategory.collectAsState()
    val imagePosts by viewModel.filteredImagePosts.collectAsState()
    val visibleLimit by viewModel.imageVisibleLimit.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val favorites by viewModel.imageFavorites.collectAsState()

    val favIds = favorites.map { it.id }.toSet()

    // Categories specified by user for Images screen
    val categories = listOf("All", "Trending", "New", "Portrait", "Video")

    val visibleImages = imagePosts.take(visibleLimit)
    val hasMore = imagePosts.size > visibleLimit

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090A12))
            .padding(horizontal = 16.dp)
            .testTag("images_screen")
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.imageSearchQuery.value = it },
            placeholder = {
                Text(
                    text = "Search prompts...",
                    color = Color(0xFF64748B),
                    fontSize = 14.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.imageSearchQuery.value = "" }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF131627),
                unfocusedContainerColor = Color(0xFF131627),
                focusedBorderColor = Color(0xFF7C3AED),
                unfocusedBorderColor = Color(0xFF222842),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color(0xFFA78BFA)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .testTag("images_search_bar")
        )

        // Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { category ->
                val isSelected = selectedCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.imageSelectedCategory.value = category },
                    label = {
                        Text(
                            text = category,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color(0xFF141728),
                        labelColor = Color(0xFF94A3B8),
                        selectedContainerColor = Color(0xFF7C3AED),
                        selectedLabelColor = Color.White
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = Color(0xFF242A45),
                        selectedBorderColor = Color(0xFF7C3AED),
                        enabled = true,
                        selected = isSelected
                    ),
                    modifier = Modifier.testTag("chip_image_$category")
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Grid of Images with Native Ads every 4 posts
        if (isLoading && imagePosts.isEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(3) { ImageCardShimmer() }
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(3) { ImageCardShimmer() }
                }
            }
        } else if (visibleImages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF131726))
                            .border(1.dp, Color(0xFF222840), RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Image,
                            contentDescription = "No images",
                            tint = Color(0xFF6C5CE7),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Image Prompts Found",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "Try a different search term" else "New image prompts will appear here soon.",
                        color = Color(0xFF8E95AF),
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            // Group visible images in chunks of 4 to display 4 posts (2x2) then 1 full-width Native Ad beneath
            val chunks = visibleImages.chunked(4)

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(bottom = 100.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                chunks.forEachIndexed { chunkIndex, chunk ->
                    // 4 image posts
                    items(
                        items = chunk,
                        key = { it.id }
                    ) { imagePost ->
                        val isFav = favIds.contains(imagePost.id)

                        ImagePostCard(
                            imagePost = imagePost,
                            isFavorite = isFav,
                            onFavoriteClick = {
                                viewModel.toggleImagePostFavorite(imagePost, isFav)
                            },
                            onClick = {
                                if (activity != null) {
                                    AdManager.onPostClicked(activity) {
                                        onNavigateToImageDetail(imagePost)
                                    }
                                } else {
                                    onNavigateToImageDetail(imagePost)
                                }
                            }
                        )
                    }

                    // Native Ad after every 4 posts spanning full width (2 columns)
                    if (chunk.size == 4) {
                        item(span = { GridItemSpan(2) }, key = "native_ad_chunk_$chunkIndex") {
                            NativeAdCard()
                        }
                    }
                }

                // Load More Button (Initially 8 images, loads 4 more on each rewarded ad)
                if (hasMore) {
                    item(span = { GridItemSpan(2) }) {
                        Button(
                            onClick = {
                                if (activity != null) {
                                    AdManager.showRewardedAd(activity) {
                                        viewModel.loadMoreImagePosts()
                                    }
                                } else {
                                    viewModel.loadMoreImagePosts()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .padding(vertical = 4.dp)
                                .testTag("load_more_images_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F243B))
                        ) {
                            Text(
                                text = "Load More",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Image card on Images page.
 * User requirement:
 * 1. Larger size (height 220dp).
 * 2. NO TEXT on the card (pure clean photo).
 * 3. Clicking photo opens detail directly.
 */
@Composable
fun ImagePostCard(
    imagePost: ImagePost,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF141728))
            .border(1.dp, Color(0xFF242A44), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("image_post_${imagePost.id}")
    ) {
        // Pure high quality photo
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(imagePost.displayThumbnail.ifBlank { "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&auto=format&fit=crop&q=80" })
                .crossfade(true)
                .placeholder(com.example.R.drawable.ic_image_placeholder)
                .error(com.example.R.drawable.ic_image_placeholder)
                .fallback(com.example.R.drawable.ic_image_placeholder)
                .build(),
            contentDescription = imagePost.displayTitle,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Top-right Favorite Heart
        IconButton(
            onClick = onFavoriteClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0x80000000))
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (isFavorite) Color(0xFFF43F5E) else Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
