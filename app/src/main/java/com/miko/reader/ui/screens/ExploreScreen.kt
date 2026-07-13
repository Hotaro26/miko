package com.miko.reader.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.miko.reader.api.MangaDexApi
import com.miko.reader.model.MangaData
import com.miko.reader.ui.components.MikoLoadingScreen
import com.miko.reader.ui.theme.pressToRaiseClickable
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    api: MangaDexApi, 
    carouselCardSize: Int,
    onLibraryClick: () -> Unit, 
    onMangaClick: (MangaData) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var searchMangaList by remember { mutableStateOf(emptyList<MangaData>()) }
    var suggestedMangaList by remember { mutableStateOf(emptyList<MangaData>()) }
    var isLoadingSearch by remember { mutableStateOf(false) }
    var isLoadingSuggestions by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    val gridState = rememberLazyGridState()
    val isScrolled by remember { 
        derivedStateOf { 
            gridState.firstVisibleItemIndex > 0 || gridState.firstVisibleItemScrollOffset > 20 
        } 
    }

    LaunchedEffect(Unit) {
        try {
            val res = api.getMangaList(
                limit = 30,
                offset = 0,
                includes = listOf("cover_art"),
                contentRating = listOf("safe", "suggestive")
            )
            suggestedMangaList = res.data
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoadingSuggestions = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = !isScrolled,
            enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
        ) {
            Text(
                "Explore",
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 48.dp, bottom = 16.dp),
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp
            )
        }

        val searchBarTopPadding by animateDpAsState(
            targetValue = if (isScrolled) 16.dp else 0.dp, 
            animationSpec = tween(300),
            label = "searchBarTopPadding"
        )
        val searchBarHorizontalPadding by animateDpAsState(
            targetValue = if (isScrolled) 8.dp else 16.dp, 
            animationSpec = tween(300),
            label = "searchBarHorizontalPadding"
        )
        val searchBarCornerRadius by animateDpAsState(
            targetValue = if (isScrolled) 16.dp else 28.dp, 
            animationSpec = tween(300),
            label = "searchBarCornerRadius"
        )

        Spacer(modifier = Modifier.height(searchBarTopPadding))
        
        androidx.compose.material3.SearchBar(
            query = searchQuery,
            onQueryChange = {
                searchQuery = it
                if (it.length > 2) {
                    isLoadingSearch = true
                    scope.launch {
                        try {
                            val res = api.searchManga(
                                title = it,
                                includes = listOf("cover_art"),
                                limit = 20
                            )
                            searchMangaList = res.data
                        } catch (e: Exception) { e.printStackTrace() }
                        finally { isLoadingSearch = false }
                    }
                } else if (it.isEmpty()) {
                    searchMangaList = emptyList()
                    isLoadingSearch = false
                }
            },
            onSearch = { },
            active = false,
            onActiveChange = { },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = searchBarHorizontalPadding)
                .padding(bottom = 8.dp),
            placeholder = { Text("Search MangaDex...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = {
                        searchQuery = ""
                        searchMangaList = emptyList()
                        isLoadingSearch = false
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            shape = RoundedCornerShape(searchBarCornerRadius),
            colors = SearchBarDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {}

        AnimatedVisibility(
            visible = !isScrolled && searchQuery.isEmpty(),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ShortcutButton(
                        icon = Icons.Outlined.Casino, 
                        label = "Random", 
                        modifier = Modifier.weight(1f)
                    ) {
                        scope.launch {
                            try {
                                val res = api.getRandomManga(includes = listOf("cover_art"))
                                onMangaClick(res.data)
                            } catch (e: Exception) { e.printStackTrace() }
                        }
                    }
                    ShortcutButton(
                        icon = Icons.Outlined.Bookmark, 
                        label = "Library", 
                        modifier = Modifier.weight(1f)
                    ) {
                        onLibraryClick()
                    }
                }

                Text(
                    "Discover something new",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp).padding(top = 8.dp),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (searchQuery.isEmpty()) {
            if (isLoadingSuggestions) {
                Box(Modifier.fillMaxWidth().height(400.dp)) {
                    MikoLoadingScreen("Finding Suggestions...")
                }
            } else {
                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Adaptive(minSize = carouselCardSize.dp),
                    contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(suggestedMangaList, key = { it.id }) { manga ->
                        ExploreMangaCard(manga, onMangaClick)
                    }
                }
            }
        } else {
            if (isLoadingSearch) {
                MikoLoadingScreen("Searching MangaDex...")
            } else {
                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Adaptive(minSize = carouselCardSize.dp),
                    contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(searchMangaList, key = { "search_${it.id}" }) { manga ->
                        ExploreMangaCard(manga, onMangaClick)
                    }
                }
            }
        }
    }
}

@Composable
fun ShortcutButton(
    icon: ImageVector, 
    label: String, 
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.height(72.dp),
        shape = MaterialTheme.shapes.large,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                icon, 
                contentDescription = null, 
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                label, 
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ExploreMangaCard(manga: MangaData, onClick: (MangaData) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .pressToRaiseClickable { onClick(manga) }
    ) {
        AsyncImage(
            model = manga.getCoverUrl(),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.7f)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = manga.getTitle(),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 18.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit, onClear: () -> Unit, placeholder: String) {
    androidx.compose.material3.SearchBar(
        query = query,
        onQueryChange = onQueryChange,
        onSearch = { },
        active = false,
        onActiveChange = { },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                }
            }
        },
        shape = androidx.compose.foundation.shape.CircleShape
    ) {}
}
