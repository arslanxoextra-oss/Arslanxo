package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ImagePost
import com.example.model.Post
import com.example.ui.viewmodel.PromptXoViewModel

@Composable
fun FavoritesScreen(
    viewModel: PromptXoViewModel,
    onNavigateToPostDetail: (Post) -> Unit,
    onNavigateToImageDetail: (ImagePost) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("YouTube", "Images")

    val videoFavorites by viewModel.videoFavorites.collectAsState()
    val imageFavorites by viewModel.imageFavorites.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090A12))
            .padding(horizontal = 16.dp)
            .testTag("favorites_screen")
    ) {
        // Tab Row (YouTube / Images)
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color(0xFF101323),
            contentColor = Color.White,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = Color(0xFF7C3AED),
                    height = 3.dp
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 14.sp,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTabIndex == index) Color.White else Color(0xFF94A3B8)
                        )
                    },
                    modifier = Modifier.testTag("fav_tab_$index")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTabIndex == 0) {
            // Video / Post Favorites
            if (videoFavorites.isEmpty()) {
                EmptyFavoritesState(message = "No favorite video prompts saved yet.")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(videoFavorites, key = { it.id }) { fav ->
                        val post = Post(
                            id = fav.id,
                            title = fav.title,
                            description = fav.description,
                            thumbnail = fav.thumbnail,
                            category = fav.category,
                            StepsNumbers = fav.stepsNumbers,
                            step1title = fav.step1title,
                            step1description = fav.step1description,
                            step1prompt = fav.step1prompt,
                            step1toollink = fav.step1toollink,
                            step1toolname = fav.step1toolname,
                            step2title = fav.step2title,
                            step2description = fav.step2description,
                            step2prompt = fav.step2prompt,
                            step2toollink = fav.step2toollink,
                            step2toolname = fav.step2toolname,
                            has2bean = fav.has2bean
                        )

                        HomePostCard(
                            post = post,
                            isFavorite = true,
                            onFavoriteClick = { viewModel.removeFavoriteById(fav.id) },
                            onClick = { onNavigateToPostDetail(post) }
                        )
                    }
                }
            }
        } else {
            // Image Favorites
            if (imageFavorites.isEmpty()) {
                EmptyFavoritesState(message = "No favorite image prompts saved yet.")
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(bottom = 100.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(imageFavorites, key = { it.id }) { fav ->
                        val imagePost = ImagePost(
                            id = fav.id,
                            imagetitle = fav.title,
                            imageprompt = fav.step1prompt,
                            imagetoollink = fav.step1toollink,
                            imagetoolname = fav.step1toolname,
                            thumbnail = fav.thumbnail,
                            categoryforall = fav.category
                        )

                        ImagePostCard(
                            imagePost = imagePost,
                            isFavorite = true,
                            onFavoriteClick = { viewModel.removeFavoriteById(fav.id) },
                            onClick = { onNavigateToImageDetail(imagePost) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyFavoritesState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 80.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF141728)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No Favorites Yet",
                color = Color(0xFFF1F5F9),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = message,
                color = Color(0xFF94A3B8),
                fontSize = 13.sp
            )
        }
    }
}
