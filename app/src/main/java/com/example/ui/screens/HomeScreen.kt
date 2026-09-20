package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FavoriteBorder
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ads.AdManager
import com.example.model.Post
import com.example.ui.components.NativeAdCard
import com.example.ui.components.PostCardShimmer
import com.example.ui.viewmodel.PromptXoViewModel

@Composable
fun HomeScreen(
    viewModel: PromptXoViewModel,
    onNavigateToPostDetail: (Post) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val focusManager = LocalFocusManager.current

    val searchQuery by viewModel.homeSearchQuery.collectAsState()
    val selectedCategory by viewModel.homeSelectedCategory.collectAsState()
    val posts by viewModel.filteredHomePosts.collectAsState()
    val visibleLimit by viewModel.homeVisibleLimit.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val favorites by viewModel.videoFavorites.collectAsState()

    val favIds = favorites.map { it.id }.toSet()

    // Categories specified by the user
    val categories = listOf("All", "Cartoon", "Faceless", "Finance", "Documentary")

    val visiblePosts = posts.take(visibleLimit)
    val hasMore = posts.size > visibleLimit

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090A12))
            .padding(horizontal = 16.dp)
            .testTag("home_screen")
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.homeSearchQuery.value = it },
            placeholder = {
                Text(
                    text = "Search for prompt...",
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
                    IconButton(onClick = { viewModel.homeSearchQuery.value = "" }) {
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
                .testTag("home_search_bar")
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
                    onClick = { viewModel.homeSelectedCategory.value = category },
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
                    modifier = Modifier.testTag("chip_$category")
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Post List with Native Ads every 2 posts
        if (isLoading && posts.isEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                repeat(4) { PostCardShimmer() }
            }
        } else if (visiblePosts.isEmpty()) {
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
                            imageVector = Icons.Outlined.Description,
                            contentDescription = "No prompts",
                            tint = Color(0xFF6C5CE7),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Prompts Found",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "Try a different search term" else "New prompts will appear here soon.",
                        color = Color(0xFF8E95AF),
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                itemsIndexed(
                    items = visiblePosts,
                    key = { _, item -> item.id }
                ) { index, post ->
                    val isFav = favIds.contains(post.id)

                    HomePostCard(
                        post = post,
                        isFavorite = isFav,
                        onFavoriteClick = {
                            viewModel.togglePostFavorite(post, isFav)
                        },
                        onClick = {
                            if (activity != null) {
                                AdManager.onPostClicked(activity) {
                                    onNavigateToPostDetail(post)
                                }
                            } else {
                                onNavigateToPostDetail(post)
                            }
                        }
                    )

                    // Requirement: Show Native Ad after every 2 posts on Home page!
                    if (index > 0 && (index + 1) % 2 == 0) {
                        NativeAdCard()
                    }
                }

                // Load More Button (User requested: initially 5 posts, then 3 more on each rewarded ad)
                if (hasMore) {
                    item {
                        Button(
                            onClick = {
                                if (activity != null) {
                                    AdManager.showRewardedAd(activity) {
                                        viewModel.loadMoreHomePosts()
                                    }
                                } else {
                                    viewModel.loadMoreHomePosts()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .padding(vertical = 4.dp)
                                .testTag("load_more_home_button"),
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

@Composable
fun HomePostCard(
    post: Post,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF131627))
            .border(1.dp, Color(0xFF222842), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
            .testTag("post_card_${post.id}")
    ) {
        Column {
            // Thumbnail with Favorite Heart and Step Badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0F111E))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(post.displayThumbnail.ifBlank { "https://images.unsplash.com/photo-1503676260728-1c00da094a0b?w=600&auto=format&fit=crop&q=80" })
                        .crossfade(true)
                        .placeholder(com.example.R.drawable.ic_image_placeholder)
                        .error(com.example.R.drawable.ic_image_placeholder)
                        .fallback(com.example.R.drawable.ic_image_placeholder)
                        .build(),
                    contentDescription = post.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Top-right Favorite Heart Button
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x80000000))
                        .testTag("fav_btn_${post.id}")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color(0xFFF43F5E) else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Bottom-left Step Indicator Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xCC111827))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (post.has2bean || post.StepsNumbers.contains("2")) "2 Steps" else "1 Step",
                        color = Color(0xFFC4B5FD),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title
            Text(
                text = post.title,
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (post.displayDescription.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = post.displayDescription,
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
