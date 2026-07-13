package com.miko.reader.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.miko.reader.api.AniListApi
import com.miko.reader.api.MangaDexApi
import com.miko.reader.model.*
import com.miko.reader.ui.components.MikoLoadingScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MangaDetailScreen(
    api: MangaDexApi, 
    aniListApi: AniListApi,
    mangaId: String, 
    db: MikoDatabase,
    onBack: () -> Unit, 
    onChapterClick: (MangaData, ChapterData, Int?, String?) -> Unit
) {
    var manga by remember { mutableStateOf<MangaData?>(null) }
    var aniListMedia by remember { mutableStateOf<AniListMedia?>(null) }
    var chapters by remember { mutableStateOf(emptyList<ChapterData>()) }
    var isLoading by remember { mutableStateOf(true) }
    var historyEntry by remember { mutableStateOf<HistoryEntry?>(null) }
    
    val isFav by db.favouriteDao().isFavourite(mangaId).collectAsState(initial = false)
    val downloads by db.downloadDao().getDownloadsForManga(mangaId).collectAsState(initial = emptyList())
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(mangaId) {
        try {
            historyEntry = db.historyDao().getHistoryById(mangaId)
            val mangaRes = api.getManga(mangaId, includes = listOf("cover_art"))
            manga = mangaRes.data
            
            launch {
                try {
                    val aniRes = aniListApi.getMedia(AniListRequest(
                        query = AniListApi.MEDIA_QUERY,
                        variables = mapOf("search" to mangaRes.data.getTitle())
                    ))
                    aniListMedia = aniRes.data.media
                } catch (e: Exception) { e.printStackTrace() }
            }

            val chapterRes = api.getMangaChapters(
                mangaId = mangaId,
                langs = listOf("en"),
                order = "asc",
                limit = 100
            )
            chapters = chapterRes.data
        } catch (e: Exception) { 
            e.printStackTrace() 
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                    ) { Icon(Icons.Default.ArrowBack, null) }
                },
                actions = {
                    val title = manga?.getTitle() ?: "Manga"
                    IconButton(onClick = { 
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "Read $title on Miko: https://mangadex.org/title/$mangaId")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, null))
                    }) { Icon(Icons.Default.Share, null) }
                    
                    Box {
                        IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, null) }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Open in Browser") }, 
                                onClick = { 
                                    showMenu = false
                                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://mangadex.org/title/$mangaId"))
                                    context.startActivity(browserIntent)
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { _ ->
        if (isLoading) {
            MikoLoadingScreen("Syncing Manga Data...")
        } else {
            manga?.let { currentManga ->
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    LazyColumn(modifier = Modifier.fillMaxSize().widthIn(max = 800.dp)) {
                        item {
                            MangaHeaderExpressive(currentManga, isFav, aniListMedia) {
                                scope.launch {
                                    if (isFav) {
                                        db.favouriteDao().delete(FavouriteManga(currentManga.id, "", ""))
                                    } else {
                                        db.favouriteDao().insert(FavouriteManga(
                                            id = currentManga.id, 
                                            title = currentManga.getTitle(), 
                                            coverUrl = currentManga.getCoverUrl()
                                        ))
                                    }
                                }
                            }
                        }
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "${chapters.size} Chapters",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                val currentIndex = chapters.indexOfFirst { it.id == historyEntry?.lastChapterId }
                                val resumeChapter = if (currentIndex != -1) chapters[currentIndex] else chapters.firstOrNull()
                                val nextChapterId = if (currentIndex != -1 && currentIndex < chapters.size - 1) chapters[currentIndex + 1].id else if (currentIndex == -1 && chapters.size > 1) chapters[1].id else null
                                
                                Button(
                                    onClick = { 
                                        resumeChapter?.let { 
                                            onChapterClick(currentManga, it, if (it.id == historyEntry?.lastChapterId) historyEntry?.lastPage else 0, nextChapterId) 
                                        } 
                                    },
                                    shape = MaterialTheme.shapes.medium,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer, 
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                ) {
                                    Text(if (historyEntry != null) "Resume Ch ${historyEntry?.lastChapterNum ?: "?"}" else "Start Reading")
                                    Spacer(Modifier.width(8.dp))
                                    Icon(Icons.Default.KeyboardArrowRight, null)
                                }
                            }
                        }
                        itemsIndexed(chapters, key = { _, item -> item.id }) { index, chapter ->
                            val nextId = if (index < chapters.size - 1) chapters[index + 1].id else null
                            val downloadState = downloads.find { it.chapterId == chapter.id }
                            
                            ChapterItemMinimal(
                                chapter = chapter,
                                downloadState = downloadState,
                                onClick = { onChapterClick(currentManga, chapter, 0, nextId) },
                                onDownloadClick = {
                                    scope.launch {
                                        com.miko.reader.util.DownloadManager.downloadChapter(
                                            context = context,
                                            api = api,
                                            db = db,
                                            mangaId = mangaId,
                                            mangaTitle = currentManga.getTitle(),
                                            mangaCoverUrl = currentManga.getCoverUrl(),
                                            chapterId = chapter.id,
                                            chapterTitle = chapter.attributes.title ?: "Chapter ${chapter.attributes.chapter ?: "?"}",
                                            chapterNum = chapter.attributes.chapter ?: "?"
                                        )
                                    }
                                }
                            )
                        }
                        item {
                            Spacer(Modifier.height(32.dp))
                        }
                    }
                }
            } ?: Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Failed to load manga")
            }
        }
    }
}

@Composable
fun MangaHeaderExpressive(manga: MangaData, isFav: Boolean, aniListMedia: AniListMedia?, onFavClick: () -> Unit) {
    Column {
        Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
            AsyncImage(
                model = manga.getCoverUrl("512"),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, MaterialTheme.colorScheme.background),
                            startY = 100f
                        )
                    )
            )
            
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    manga.getTitle(), 
                    style = MaterialTheme.typography.headlineMedium, 
                    fontWeight = FontWeight.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    letterSpacing = (-1).sp,
                    lineHeight = 32.sp
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AssistChip(
                        onClick = onFavClick,
                        label = { Text(if (isFav) "Favourited" else "Add to Library") },
                        leadingIcon = {
                            Icon(
                                if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder, 
                                null, 
                                modifier = Modifier.size(18.dp),
                                tint = if (isFav) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        shape = MaterialTheme.shapes.medium
                    )
                }
            }
        }
        
        aniListMedia?.let { AniListInfoCard(it) }

        Spacer(Modifier.height(16.dp))
        Text(
            "Description", 
            style = MaterialTheme.typography.titleMedium, 
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            manga.getDescription(), 
            style = MaterialTheme.typography.bodyMedium, 
            maxLines = 10, 
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AniListInfoCard(media: AniListMedia) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    "AniList Insights",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = media.status ?: "Unknown Status",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(8.dp))
                    if (media.averageScore != null) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Text(
                                "${media.averageScore}%",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            
            val context = LocalContext.current
            Button(
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://anilist.co/manga/${media.id}")).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) { e.printStackTrace() }
                },
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Text("View")
            }
        }
    }
}

@Composable
fun ChapterItemMinimal(
    chapter: ChapterData, 
    downloadState: DownloadedChapter?,
    onClick: () -> Unit,
    onDownloadClick: () -> Unit
) {
    val num = chapter.attributes.chapter ?: "?"
    val title = chapter.attributes.title ?: "Chapter $num"
    
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    "Chapter $num", 
                    style = MaterialTheme.typography.titleSmall, 
                    fontWeight = FontWeight.Bold
                )
                if (!chapter.attributes.title.isNullOrEmpty()) {
                    Text(
                        title, 
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // Download indicator
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                if (downloadState != null) {
                    if (downloadState.isDownloadComplete) {
                        Icon(
                            Icons.Default.CheckCircle, 
                            contentDescription = "Downloaded",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        val progress = if (downloadState.totalPages > 0) {
                            downloadState.downloadedPages.toFloat() / downloadState.totalPages.toFloat()
                        } else 0f
                        CircularProgressIndicator(
                            progress = progress,
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                    }
                } else {
                    IconButton(onClick = onDownloadClick) {
                        Icon(
                            Icons.Default.Download, 
                            contentDescription = "Download",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            Spacer(Modifier.width(8.dp))
            Icon(
                Icons.Default.KeyboardArrowRight, 
                null, 
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}
