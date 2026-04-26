package com.miko.reader.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.miko.reader.model.HistoryEntry
import kotlinx.coroutines.flow.Flow

@Composable
fun HomeScreen(historyFlow: Flow<List<HistoryEntry>>, onMangaClick: (HistoryEntry) -> Unit) {
    val history by historyFlow.collectAsState(initial = emptyList())

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 1000.dp)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 80.dp) // Space for bottom bar
        ) {
        item {
            Text(
                "Miko",
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 48.dp, bottom = 24.dp),
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp
            )
        }

        if (history.isNotEmpty()) {
            item {
                Text(
                    "Recent Activity",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(history.take(5), key = { it.id }) { entry ->
                        HistoryCard(entry, onMangaClick)
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(32.dp))
            Text(
                "History",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                fontWeight = FontWeight.Bold
            )
        }

        if (history.isEmpty()) {
            item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(200.dp), 
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No history yet", 
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(history, key = { "list_${it.id}" }) { entry ->
                HistoryItem(entry, onMangaClick)
            }
        }
    }
}
}

@Composable
fun HistoryCard(entry: HistoryEntry, onClick: (HistoryEntry) -> Unit) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable { onClick(entry) }
    ) {
        AsyncImage(
            model = entry.coverUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.7f)
                .clip(RoundedCornerShape(20.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.height(8.dp))
        Text(
            entry.title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            "Ch ${entry.lastChapterNum ?: "?"}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun HistoryItem(entry: HistoryEntry, onClick: (HistoryEntry) -> Unit) {
    Surface(
        onClick = { onClick(entry) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = entry.coverUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp, 90.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    entry.title, 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold, 
                    maxLines = 1, 
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Last read: Chapter ${entry.lastChapterNum ?: "?"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
