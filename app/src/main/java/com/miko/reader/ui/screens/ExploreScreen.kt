package com.miko.reader.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.miko.reader.api.MangaDexApi
import com.miko.reader.model.MangaData
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(api: MangaDexApi, onMangaClick: (MangaData) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var searchMangaList by remember { mutableStateOf(emptyList<MangaData>()) }
    var suggestedMangaList by remember { mutableStateOf(emptyList<MangaData>()) }
    var isLoadingSearch by remember { mutableStateOf(false) }
    var isLoadingSuggestions by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            val res = api.getMangaList(limit = 15)
            suggestedMangaList = res.data
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoadingSuggestions = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "Explore",
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 48.dp, bottom = 16.dp),
            fontWeight = FontWeight.Black,
            letterSpacing = (-1).sp
        )

        SearchBar(
            query = searchQuery,
            onQueryChange = {
                searchQuery = it
                if (it.length > 2) {
                    isLoadingSearch = true
                    scope.launch {
                        try {
                            val res = api.searchManga(it)
                            searchMangaList = res.data
                        } catch (e: Exception) { e.printStackTrace() }
                        finally { isLoadingSearch = false }
                    }
                } else if (it.isEmpty()) {
                    searchMangaList = emptyList()
                    isLoadingSearch = false
                }
            },
            onClear = {
                searchQuery = ""
                searchMangaList = emptyList()
                isLoadingSearch = false
            },
            placeholder = "Search MangaDex..."
        )

        if (searchQuery.isEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ShortcutButton(
                    icon = Icons.Outlined.Casino, 
                    label = "Random", 
                    modifier = Modifier.weight(1f)
                ) {
                    scope.launch {
                        try {
                            val res = api.getRandomManga()
                            onMangaClick(res.data)
                        } catch (e: Exception) { e.printStackTrace() }
                    }
                }
                ShortcutButton(
                    icon = Icons.Outlined.Bookmark, 
                    label = "Library", 
                    modifier = Modifier.weight(1f)
                ) {
                    // Library shortcut logic can be added here
                }
            }

            Text(
                "Discover something new",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp).padding(top = 16.dp),
                fontWeight = FontWeight.Bold
            )

            if (isLoadingSuggestions) {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(strokeCap = androidx.compose.ui.graphics.StrokeCap.Round)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 120.dp),
                    contentPadding = PaddingValues(16.dp),
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
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(strokeCap = androidx.compose.ui.graphics.StrokeCap.Round)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 120.dp),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(searchMangaList, key = { "search_${it.id}" }) { manga ->
                        ExploreMangaCard(manga, onMangaClick)
                    }
                }
            }        }
    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit, onClear: () -> Unit, placeholder: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(64.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            Icon(
                Icons.Default.Search, 
                contentDescription = null, 
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(16.dp))
            Box(Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        placeholder, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    singleLine = true
                )
            }
            
            AnimatedVisibility(
                visible = query.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                IconButton(onClick = onClear) {
                    Icon(
                        Icons.Default.Clear, 
                        contentDescription = "Clear",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
    Surface(
        modifier = modifier.height(64.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
        onClick = onClick
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                icon, 
                contentDescription = null, 
                modifier = Modifier.size(24.dp), 
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                label, 
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun ExploreMangaCard(manga: MangaData, onClick: (MangaData) -> Unit) {
    val title = manga.attributes.title["en"] ?: manga.attributes.title.values.firstOrNull() ?: "Unknown"
    val coverRel = manga.relationships.find { it.type == "cover_art" }
    val coverFileName = coverRel?.attributes?.fileName
    val coverUrl = if (!coverFileName.isNullOrEmpty()) {
        "https://uploads.mangadex.org/covers/${manga.id}/$coverFileName.256.jpg"
    } else null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(manga) }
    ) {
        AsyncImage(
            model = coverUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.7f)
                .clip(RoundedCornerShape(24.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 16.sp
        )
    }
}
