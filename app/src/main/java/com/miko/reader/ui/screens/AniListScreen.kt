package com.miko.reader.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.miko.reader.api.AniListApi
import com.miko.reader.model.AniListMedia
import com.miko.reader.model.AniListRequest
import com.miko.reader.model.AniListUser
import com.miko.reader.ui.components.MikoLoadingScreen
import com.miko.reader.ui.theme.pressToRaiseClickable
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AniListScreen(
    api: AniListApi, 
    user: AniListUser?, 
    carouselCardSize: Int,
    onConnectClick: () -> Unit,
    onMediaClick: (AniListMedia) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var searchMediaList by remember { mutableStateOf(emptyList<AniListMedia>()) }
    var trendingMediaList by remember { mutableStateOf(emptyList<AniListMedia>()) }
    var releasingMediaList by remember { mutableStateOf(emptyList<AniListMedia>()) }
    var isLoadingSearch by remember { mutableStateOf(false) }
    var isLoadingTrending by remember { mutableStateOf(true) }
    var isLoadingReleasing by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        launch {
            try {
                val res = api.post(AniListRequest(
                    query = AniListApi.SEARCH_QUERY,
                    variables = mapOf("page" to 1, "perPage" to 10, "sort" to listOf("TRENDING_DESC"))
                ))
                trendingMediaList = res.data.page.media
            } catch (e: Exception) { e.printStackTrace() }
            finally { isLoadingTrending = false }
        }
        launch {
            try {
                val res = api.getReleasing(AniListRequest(
                    query = AniListApi.SEARCH_QUERY,
                    variables = mapOf(
                        "page" to 1, 
                        "perPage" to 10, 
                        "status" to "RELEASING", 
                        "sort" to listOf("UPDATED_AT_DESC")
                    )
                ))
                releasingMediaList = res.data.page.media
            } catch (e: Exception) { e.printStackTrace() }
            finally { isLoadingReleasing = false }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 48.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "AniList",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1).sp
                )
                if (user != null) {
                    Text(
                        "Hi, ${user.name}!",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            if (user == null) {
                Button(
                    onClick = onConnectClick,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(Icons.Outlined.Sync, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Connect")
                }
            } else {
                AsyncImage(
                    model = user.avatar?.large,
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }
        }

        SearchBar(
            query = searchQuery,
            onQueryChange = {
                searchQuery = it
                if (it.length > 2) {
                    isLoadingSearch = true
                    scope.launch {
                        try {
                            val res = api.post(AniListRequest(
                                query = AniListApi.SEARCH_QUERY,
                                variables = mapOf("search" to it, "page" to 1, "perPage" to 20)
                            ))
                            searchMediaList = res.data.page.media
                        } catch (e: Exception) { e.printStackTrace() }
                        finally { isLoadingSearch = false }
                    }
                } else if (it.isEmpty()) {
                    searchMediaList = emptyList()
                    isLoadingSearch = false
                }
            },
            onClear = {
                searchQuery = ""
                searchMediaList = emptyList()
            },
            placeholder = "Search manga..."
        )

        if (searchQuery.isEmpty()) {
            if (isLoadingTrending || isLoadingReleasing) {
                Box(Modifier.fillMaxSize()) {
                    MikoLoadingScreen("Fetching AniList Trends...")
                }
            } else {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    SectionHeader("Trending Now")
                    HorizontalMediaList(trendingMediaList, carouselCardSize, onMediaClick)

                    SectionHeader("Recently Updated (Releasing)")
                    HorizontalMediaList(releasingMediaList, carouselCardSize, onMediaClick)
                    
                    Spacer(Modifier.height(180.dp))
                }
            }
        } else {
            if (isLoadingSearch) {
                Box(Modifier.fillMaxSize()) {
                    MikoLoadingScreen("Searching AniList...")
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = carouselCardSize.dp),
                    contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 180.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(searchMediaList, key = { "search_${it.id}" }) { media ->
                        AniListMediaCard(media, modifier = Modifier.fillMaxWidth(), onClick = { onMediaClick(media) })
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
fun HorizontalMediaList(
    list: List<AniListMedia>, 
    cardSize: Int,
    onMediaClick: (AniListMedia) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(list, key = { it.id }) { media ->
            AniListMediaCard(media, modifier = Modifier.width(cardSize.dp), onClick = { onMediaClick(media) })
        }
    }
}

@Composable
fun AniListMediaCard(media: AniListMedia, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier.pressToRaiseClickable { onClick() }
    ) {
        Box {
            AsyncImage(
                model = media.coverImage?.large,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.7f)
                    .clip(RoundedCornerShape(20.dp)),
                contentScale = ContentScale.Crop
            )
            if (media.averageScore != null) {
                Surface(
                    modifier = Modifier.padding(8.dp).align(Alignment.TopEnd),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Text(
                        text = "${media.averageScore}%",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = media.title.userPreferred(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 16.sp
        )
        Text(
            text = media.status ?: "",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
