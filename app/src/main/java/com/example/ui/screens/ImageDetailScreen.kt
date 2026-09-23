package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import android.app.Activity
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import com.example.ads.AdManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.ImagePost
import com.example.ui.components.ChatGPTTypingBox
import com.example.ui.components.NativeAdCard
import com.example.ui.viewmodel.PromptXoViewModel

@Composable
fun ImageDetailScreen(
    imagePost: ImagePost,
    viewModel: PromptXoViewModel,
    onNavigateBack: () -> Unit,
    onSelectImagePost: (ImagePost) -> Unit = {},
    onNavigateToImages: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val snackbarHostState = remember { SnackbarHostState() }
    val favorites by viewModel.imageFavorites.collectAsState()
    val isFav = favorites.any { it.id == imagePost.id }

    val allImagePosts by viewModel.allImagePosts.collectAsState()
    // Limit to 5 suggested image posts
    val suggestedImages = remember(allImagePosts, imagePost.id) {
        allImagePosts.filter { it.id != imagePost.id }.take(5)
    }

    val totalSliderItems = suggestedImages.size + 1
    val suggestedImageListState = rememberLazyListState()

    // Automatic smooth looping slider for suggested images
    LaunchedEffect(suggestedImages) {
        if (totalSliderItems > 1) {
            while (true) {
                delay(2500)
                if (!suggestedImageListState.isScrollInProgress) {
                    val nextIndex = (suggestedImageListState.firstVisibleItemIndex + 1) % totalSliderItems
                    suggestedImageListState.animateScrollToItem(
                        index = nextIndex,
                        scrollOffset = 0
                    )
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color(0xFF090A12),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(56.dp)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF141728))
                        .testTag("image_detail_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFFE2E8F0),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = imagePost.displayTitle,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                )

                IconButton(
                    onClick = { viewModel.toggleImagePostFavorite(imagePost, isFav) },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF141728))
                        .testTag("image_detail_favorite_button")
                ) {
                    Icon(
                        imageVector = if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFav) Color(0xFFF43F5E) else Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .testTag("image_detail_content")
        ) {
            // Hero Image Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF141728))
                    .border(1.dp, Color(0xFF242A44), RoundedCornerShape(20.dp))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imagePost.displayThumbnail.ifBlank { "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&auto=format&fit=crop&q=80" })
                        .crossfade(200)
                        .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                        .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                        .build(),
                    contentDescription = imagePost.displayTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title & Category
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = imagePost.displayTitle,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF241C3D))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = imagePost.displayCategory,
                        color = Color(0xFFC4B5FD),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Generate Prompt Box with streaming typing and copy/try actions
            ChatGPTTypingBox(
                promptText = imagePost.displayPrompt,
                toolName = imagePost.imagetoolname.ifBlank { "Try in ChatGPT ->" },
                toolLink = imagePost.imagetoollink.ifBlank { "https://chatgpt.com" },
                snackbarHostState = snackbarHostState
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Native Ad Card
            NativeAdCard()

            Spacer(modifier = Modifier.height(24.dp))

            // Suggested Images Slider (Previous / other image prompts)
            if (suggestedImages.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(4.dp, 18.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFF7C3AED))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Suggested Images",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    state = suggestedImageListState,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(suggestedImages, key = { it.id }) { item ->
                        Box(
                            modifier = Modifier
                                .width(150.dp)
                                .height(190.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF141728))
                                .border(1.dp, Color(0xFF242A44), RoundedCornerShape(14.dp))
                                .clickable {
                                    if (activity != null) {
                                        AdManager.onPostClicked(activity) {
                                            onSelectImagePost(item)
                                        }
                                    } else {
                                        onSelectImagePost(item)
                                    }
                                }
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(item.displayThumbnail.ifBlank { "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&auto=format&fit=crop&q=80" })
                                    .crossfade(200)
                                    .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                                    .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                                    .build(),
                                contentDescription = item.displayTitle,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Title overlay gradient at bottom
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .background(Color(0xB3000000))
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = item.displayTitle,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Load More card at the end in the exact same 150dp x 190dp shape
                    item(key = "load_more_images_card") {
                        Box(
                            modifier = Modifier
                                .width(150.dp)
                                .height(190.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF141728))
                                .border(1.dp, Color(0xFF7C3AED), RoundedCornerShape(14.dp))
                                .clickable {
                                    onNavigateToImages()
                                }
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF7C3AED)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "Load More",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Load More",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Explore Images",
                                    color = Color(0xFFA78BFA),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
