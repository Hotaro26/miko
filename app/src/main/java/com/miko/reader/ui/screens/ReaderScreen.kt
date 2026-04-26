package com.miko.reader.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.miko.reader.api.MangaDexApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

enum class ReadingMode { Vertical, Paged }

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ReaderScreen(api: MangaDexApi, chapterId: String, onBack: () -> Unit) {
    var images by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var readingMode by remember { mutableStateOf(ReadingMode.Vertical) }
    var showControls by remember { mutableStateOf(true) }

    LaunchedEffect(chapterId) {
        try {
            val res = api.getAtHomeServer(chapterId)
            val base = res.baseUrl
            val hash = res.chapter.hash
            images = res.chapter.data.map { "$base/data/$hash/$it" }
        } catch (e: Exception) { e.printStackTrace() }
        finally { isLoading = false }
    }

    val pagerState = if (readingMode == ReadingMode.Paged) {
        rememberPagerState(pageCount = { images.size })
    } else null

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { showControls = !showControls }
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        } else {
            if (readingMode == ReadingMode.Vertical) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(images) { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.FillWidth
                        )
                    }
                }
            } else {
                pagerState?.let { state ->
                    HorizontalPager(
                        state = state,
                        modifier = Modifier.fillMaxSize(),
                        pageSpacing = 8.dp
                    ) { page ->
                        AsyncImage(
                            model = images[page],
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }

        // Overlay Controls
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it })
        ) {
            TopAppBar(
                title = { 
                    Text(
                        "Reading",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { 
                        Icon(Icons.Default.ArrowBack, null) 
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        readingMode = if (readingMode == ReadingMode.Vertical) ReadingMode.Paged else ReadingMode.Vertical 
                    }) {
                        Icon(
                            if (readingMode == ReadingMode.Vertical) Icons.Default.ViewCarousel else Icons.Default.SwapVert, 
                            contentDescription = "Toggle Mode"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            )
        }
        
        // Progress Indicator for Paged Mode
        if (readingMode == ReadingMode.Paged && !isLoading && images.isNotEmpty() && pagerState != null) {
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Page ${pagerState.currentPage + 1} / ${images.size}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = (pagerState.currentPage + 1).toFloat() / images.size,
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    }
                }
            }
        }
    }
}
