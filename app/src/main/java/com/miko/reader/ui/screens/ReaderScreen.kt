package com.miko.reader.ui.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.miko.reader.api.MangaDexApi
import com.miko.reader.ui.components.MikoLoadingScreen
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import com.miko.reader.model.ChapterData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import com.miko.reader.model.MikoDatabase
import java.io.File

enum class ReadingMode { Vertical, Paged }

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ReaderScreen(
    api: MangaDexApi, 
    db: MikoDatabase,
    mangaId: String,
    chapterId: String, 
    initialPage: Int = 0,
    onNextChapter: (String) -> Unit = {},
    onPageChange: (Int) -> Unit = {},
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("miko_prefs", Context.MODE_PRIVATE) }
    val scope = rememberCoroutineScope()
    
    var images by remember { mutableStateOf<List<Any>>(emptyList()) }
    var chapters by remember { mutableStateOf<List<ChapterData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    var readingMode by remember { 
        val savedMode = prefs.getString("reading_mode", ReadingMode.Vertical.name)
        mutableStateOf(ReadingMode.valueOf(savedMode ?: ReadingMode.Vertical.name))
    }
    
    var showControls by remember { mutableStateOf(true) }

    LaunchedEffect(chapterId) {
        isLoading = true
        try {
            if (chapters.isEmpty()) {
                val chapterRes = api.getMangaChapters(
                    mangaId = mangaId,
                    langs = listOf("en"),
                    order = "asc",
                    limit = 100
                )
                chapters = chapterRes.data
            }

            val downloadedChapter = db.downloadDao().getDownloadByChapterId(chapterId)
            if (downloadedChapter != null && downloadedChapter.isDownloadComplete) {
                val folder = File(downloadedChapter.folderPath)
                if (folder.exists()) {
                    val localImages = mutableListOf<File>()
                    for (i in 0 until downloadedChapter.totalPages) {
                        val file = File(folder, "$i.jpg")
                        if (file.exists()) localImages.add(file)
                    }
                    if (localImages.size == downloadedChapter.totalPages) {
                        images = localImages
                        return@LaunchedEffect
                    }
                }
            }

            val res = api.getAtHomeServer(chapterId)
            val base = res.baseUrl
            val hash = res.chapter.hash
            images = res.chapter.data.map { "$base/data/$hash/$it" }
        } catch (e: Exception) { e.printStackTrace() }
        finally { isLoading = false }
    }

    val nextChapterId = remember(chapterId, chapters) {
        val currentIndex = chapters.indexOfFirst { it.id == chapterId }
        if (currentIndex != -1 && currentIndex < chapters.size - 1) {
            chapters[currentIndex + 1].id
        } else null
    }

    val pagerState = if (readingMode == ReadingMode.Paged) {
        rememberPagerState(pageCount = { images.size })
    } else null

    val listState = rememberLazyListState()

    LaunchedEffect(isLoading, images) {
        if (!isLoading && images.isNotEmpty() && initialPage > 0) {
            val targetPage = initialPage.coerceIn(0, images.size - 1)
            delay(200)
            if (readingMode == ReadingMode.Vertical) {
                listState.scrollToItem(targetPage)
            } else {
                pagerState?.scrollToPage(targetPage)
            }
        }
    }

    val currentPage = if (readingMode == ReadingMode.Paged) pagerState?.currentPage ?: 0 else listState.firstVisibleItemIndex
    LaunchedEffect(currentPage) {
        if (!isLoading && images.isNotEmpty()) {
            onPageChange(currentPage)
        }
    }

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
            MikoLoadingScreen("Streaming Pages...")
        } else {
            if (readingMode == ReadingMode.Vertical) {
                LazyColumn(modifier = Modifier.fillMaxSize(), state = listState) {
                    itemsIndexed(images) { _, url ->
                        coil.compose.SubcomposeAsyncImage(
                            model = url,
                            contentDescription = null,
                            loading = {
                                Box(
                                    modifier = Modifier.fillMaxWidth().aspectRatio(0.7f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.05f)),
                            contentScale = ContentScale.FillWidth
                        )
                    }
                    
                    if (nextChapterId != null) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Button(
                                    onClick = { onNextChapter(nextChapterId) },
                                    shape = MaterialTheme.shapes.medium,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Icon(Icons.Default.SkipNext, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Next Chapter")
                                }
                            }
                        }
                    }
                }
            } else {
                pagerState?.let { state ->
                    HorizontalPager(
                        state = state,
                        modifier = Modifier.fillMaxSize(),
                        pageSpacing = 8.dp
                    ) { page ->
                        coil.compose.SubcomposeAsyncImage(
                            model = images[page],
                            contentDescription = null,
                            loading = {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                }
                            },
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }

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
                        val newMode = if (readingMode == ReadingMode.Vertical) ReadingMode.Paged else ReadingMode.Vertical 
                        readingMode = newMode
                        prefs.edit().putString("reading_mode", newMode.name).apply()
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

        if (!isLoading && images.isNotEmpty()) {
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Page ${currentPage + 1} / ${images.size}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            if (nextChapterId != null && readingMode == ReadingMode.Paged && currentPage == images.size - 1) {
                                TextButton(
                                    onClick = { onNextChapter(nextChapterId) }
                                ) {
                                    Text("Next Chapter")
                                    Spacer(Modifier.width(4.dp))
                                    Icon(Icons.Default.SkipNext, null, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Slider(
                            value = currentPage.toFloat(),
                            onValueChange = { pageFloat ->
                                val targetPage = pageFloat.toInt().coerceIn(0, images.size - 1)
                                scope.launch {
                                    if (readingMode == ReadingMode.Vertical) {
                                        listState.scrollToItem(targetPage)
                                    } else {
                                        pagerState?.scrollToPage(targetPage)
                                    }
                                }
                            },
                            valueRange = 0f..(images.size - 1).toFloat(),
                            modifier = Modifier.fillMaxWidth(),
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                            )
                        )
                    }
                }
            }
        }
    }
}
