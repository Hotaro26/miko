package com.miko.reader

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import androidx.room.Room
import com.google.android.gms.ads.MobileAds
import com.miko.reader.api.MangaDexApi
import com.miko.reader.model.*
import com.miko.reader.ui.screens.*
import com.miko.reader.ui.theme.AppTheme
import com.miko.reader.ui.theme.MikoTheme
import com.miko.reader.util.AdHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : ComponentActivity() {

    private val db by lazy {
        Room.databaseBuilder(applicationContext, MikoDatabase::class.java, "miko.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    private val api: MangaDexApi by lazy {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "MikoReader/1.0")
                    .build()
                chain.proceed(request)
            }
            .build()

        Retrofit.Builder()
            .baseUrl("https://api.mangadex.org/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MangaDexApi::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        // Initialize AdMob
        MobileAds.initialize(this) {}
        AdHelper.loadInterstitialAd(this)

        val prefs = getSharedPreferences("miko_prefs", Context.MODE_PRIVATE)

        setContent {
            // Theme settings
            var themeMode by remember { mutableIntStateOf(prefs.getInt("theme_mode", 0)) } // 0: System, 1: Light, 2: Dark
            var selectedTheme by remember { 
                val themeName = prefs.getString("selected_theme", AppTheme.Dynamic.name)
                mutableStateOf(AppTheme.valueOf(themeName ?: AppTheme.Dynamic.name))
            }

            val isDarkTheme = when (themeMode) {
                1 -> false
                2 -> true
                else -> isSystemInDarkTheme()
            }

            MikoTheme(darkTheme = isDarkTheme, theme = selectedTheme) {
                // Ensure the entire screen background respects the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val scope = rememberCoroutineScope()
                    
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    // Reactive preference for opt-in ads
                    var optInAds by remember { mutableStateOf(prefs.getBoolean("opt_in_ads", false)) }

                    fun navigateWithAds(route: String) {
                        if (optInAds) {
                            AdHelper.showInterstitialAd(this) {
                                navController.navigate(route)
                            }
                        } else {
                            navController.navigate(route)
                        }
                    }

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = Color.Transparent, // Use Surface background
                        floatingActionButton = {
                            if (currentRoute == "home") {
                                val historyList by db.historyDao().getAllHistory().collectAsState(initial = emptyList())
                                if (historyList.isNotEmpty()) {
                                    ExtendedFloatingActionButton(
                                        onClick = {
                                            val lastRead = historyList.first()
                                            navigateWithAds("reader/${lastRead.lastChapterId}")
                                        },
                                        icon = { Icon(Icons.Default.PlayArrow, null) },
                                        text = { Text("Continue") },
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                }
                            }
                        },
                        bottomBar = {
                            if (currentRoute != null && !currentRoute.startsWith("reader")) {
                                NavigationBar(
                                    containerColor = Color.Transparent,
                                    tonalElevation = 0.dp
                                ) {
                                    NavigationBarItem(
                                        icon = { Icon(Icons.Outlined.Home, null) },
                                        label = { Text("Home") },
                                        selected = currentRoute == "home",
                                        onClick = { 
                                            navController.navigate("home") {
                                                popUpTo("home") { inclusive = true }
                                                launchSingleTop = true
                                            }
                                        }
                                    )
                                    NavigationBarItem(
                                        icon = { Icon(Icons.Outlined.FavoriteBorder, null) },
                                        label = { Text("Library") },
                                        selected = currentRoute == "fav",
                                        onClick = { 
                                            navController.navigate("fav") {
                                                launchSingleTop = true
                                            }
                                        }
                                    )
                                    NavigationBarItem(
                                        icon = { Icon(Icons.Outlined.Explore, null) },
                                        label = { Text("Explore") },
                                        selected = currentRoute == "explore",
                                        onClick = { 
                                            navController.navigate("explore") {
                                                launchSingleTop = true
                                            }
                                        }
                                    )
                                    NavigationBarItem(
                                        icon = { Icon(Icons.Outlined.Info, null) },
                                        label = { Text("About") },
                                        selected = currentRoute == "about",
                                        onClick = { 
                                            navController.navigate("about") {
                                                launchSingleTop = true
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    ) { padding ->
                        Box(Modifier.padding(padding).fillMaxSize()) {
                            NavHost(navController = navController, startDestination = "home") {
                                composable("home") {
                                    HomeScreen(db.historyDao().getAllHistory()) { history ->
                                        navigateWithAds("detail/${history.id}")
                                    }
                                }
                                composable("fav") {
                                    FavouritesScreen(db.favouriteDao().getAllFavourites()) { fav ->
                                        navigateWithAds("detail/${fav.id}")
                                    }
                                }
                                composable("explore") {
                                    ExploreScreen(api) { manga ->
                                        navigateWithAds("detail/${manga.id}")
                                    }
                                }
                                composable("about") {
                                    AboutScreen(
                                        optInAds = optInAds,
                                        onOptInAdsChange = {
                                            optInAds = it
                                            prefs.edit().putBoolean("opt_in_ads", it).apply()
                                        },
                                        themeMode = themeMode,
                                        onThemeModeChange = {
                                            themeMode = it
                                            prefs.edit().putInt("theme_mode", it).apply()
                                        },
                                        selectedTheme = selectedTheme,
                                        onThemeChange = {
                                            selectedTheme = it
                                            prefs.edit().putString("selected_theme", it.name).apply()
                                        }
                                    )
                                }
                                composable(
                                    route = "detail/{mangaId}",
                                    arguments = listOf(navArgument("mangaId") { type = NavType.StringType })
                                ) { backStackEntry ->
                                    val mangaId = backStackEntry.arguments?.getString("mangaId") ?: ""
                                    MangaDetailScreen(
                                        api = api,
                                        mangaId = mangaId,
                                        db = db,
                                        onBack = { navController.popBackStack() },
                                        onChapterClick = { manga, chapter ->
                                            scope.launch {
                                                try {
                                                    val coverFileName = manga.relationships.find { it.type == "cover_art" }?.attributes?.fileName
                                                    val coverUrl = if (coverFileName != null) "https://uploads.mangadex.org/covers/${manga.id}/$coverFileName.256.jpg" else null
                                                    db.historyDao().insert(HistoryEntry(
                                                        id = manga.id,
                                                        title = manga.attributes.title["en"] ?: manga.attributes.title.values.firstOrNull() ?: "Unknown",
                                                        coverUrl = coverUrl,
                                                        lastChapterId = chapter.id,
                                                        lastChapterNum = chapter.attributes.chapter
                                                    ))
                                                } catch (e: Exception) { e.printStackTrace() }
                                            }
                                            navigateWithAds("reader/${chapter.id}")
                                        }
                                    )
                                }
                                composable(
                                    route = "reader/{chapterId}",
                                    arguments = listOf(navArgument("chapterId") { type = NavType.StringType })
                                ) { backStackEntry ->
                                    val chapterId = backStackEntry.arguments?.getString("chapterId") ?: ""
                                    ReaderScreen(api, chapterId) {
                                        navController.popBackStack()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
