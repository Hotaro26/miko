package com.miko.reader.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miko.reader.model.DownloadedChapter
import com.miko.reader.model.FavouriteManga
import kotlinx.coroutines.flow.Flow

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Terminal

@Composable
fun OfflineScreen(
    downloadsFlow: Flow<List<DownloadedChapter>>,
    onMangaClick: (String) -> Unit,
    onLogsClick: () -> Unit
) {
    val allDownloads by downloadsFlow.collectAsState(initial = emptyList())
    
    val downloadedManga = remember(allDownloads) {
        allDownloads.groupBy { it.mangaId }.map { entry ->
            val mangaId = entry.key
            val chapters = entry.value
            val firstChapter = chapters.first()
            FavouriteManga(
                id = mangaId,
                title = firstChapter.mangaTitle,
                coverUrl = firstChapter.mangaCoverUrl
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 48.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Offline",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp
            )
            
            Surface(
                onClick = onLogsClick,
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Terminal,
                        contentDescription = "Logs",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Logs",
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (downloadedManga.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "No downloaded manga", 
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 120.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(downloadedManga, key = { it.id }) { manga ->
                    FavouriteCard(manga, { onMangaClick(it.id) })
                }
            }
        }
    }
}
