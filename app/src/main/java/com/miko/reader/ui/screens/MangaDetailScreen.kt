package com.miko.reader.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.List
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
import com.miko.reader.api.MangaDexApi
import com.miko.reader.model.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MangaDetailScreen(
    api: MangaDexApi, 
    mangaId: String, 
    db: MikoDatabase,
    onBack: () -> Unit, 
    onChapterClick: (MangaData, ChapterData) -> Unit
) {
    var manga by remember { mutableStateOf<MangaData?>(null) }
    var chapters by remember { mutableStateOf(emptyList<ChapterData>()) }
    var isLoading by remember { mutableStateOf(true) }
    
    val isFav by db.favouriteDao().isFavourite(mangaId).collectAsState(initial = false)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(mangaId) {
        try {
            val mangaRes = api.getManga(mangaId)
            manga = mangaRes.data
            val chapterRes = api.getMangaChapters(mangaId)
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
                    val title = manga?.attributes?.title?.get("en") ?: "Manga"
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
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(strokeCap = androidx.compose.ui.graphics.StrokeCap.Round)
            }
        } else {
            manga?.let { currentManga ->
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    LazyColumn(modifier = Modifier.fillMaxSize().widthIn(max = 800.dp)) {
                        item {
                            MangaHeaderExpressive(currentManga, isFav) {
                            scope.launch {
                                if (isFav) {
                                    db.favouriteDao().delete(FavouriteManga(currentManga.id, "", ""))
                                } else {
                                    val title = currentManga.attributes.title["en"] ?: currentManga.attributes.title.values.firstOrNull() ?: "Unknown"
                                    val coverFileName = currentManga.relationships.find { it.type == "cover_art" }?.attributes?.fileName
                                    val coverUrl = if (coverFileName != null) "https://uploads.mangadex.org/covers/${currentManga.id}/$coverFileName.256.jpg" else null
                                    db.favouriteDao().insert(FavouriteManga(currentManga.id, title, coverUrl))
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
                            Button(
                                onClick = { chapters.firstOrNull()?.let { onChapterClick(currentManga, it) } },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer, 
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            ) {
                                Text("Start Reading")
                                Spacer(Modifier.width(8.dp))
                                Icon(Icons.Default.KeyboardArrowRight, null)
                            }
                        }
                    }
                    items(chapters, key = { it.id }) { chapter ->
                        ChapterItemMinimal(chapter) { onChapterClick(currentManga, chapter) }
                    }
                    item {
                        Spacer(Modifier.height(32.dp))
                    }
                }
            } ?: Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Failed to load manga")
            }
        }
    }
}
}

@Composable
fun MangaHeaderExpressive(manga: MangaData, isFav: Boolean, onFavClick: () -> Unit) {
    val title = manga.attributes.title["en"] ?: manga.attributes.title.values.firstOrNull() ?: "Unknown"
    val description = manga.attributes.description["en"] ?: manga.attributes.description.values.firstOrNull() ?: ""
    val coverRel = manga.relationships.find { it.type == "cover_art" }
    val coverFileName = coverRel?.attributes?.fileName
    val coverUrl = if (!coverFileName.isNullOrEmpty()) {
        "https://uploads.mangadex.org/covers/${manga.id}/$coverFileName"
    } else null

    Column {
        Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
            AsyncImage(
                model = coverUrl,
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
                    title, 
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
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        Text(
            "Description", 
            style = MaterialTheme.typography.titleMedium, 
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            description, 
            style = MaterialTheme.typography.bodyMedium, 
            maxLines = 5, 
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ChapterItemMinimal(chapter: ChapterData, onClick: () -> Unit) {
    val num = chapter.attributes.chapter ?: "?"
    val title = chapter.attributes.title ?: "Chapter $num"
    
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
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
            Icon(
                Icons.Default.KeyboardArrowRight, 
                null, 
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}
