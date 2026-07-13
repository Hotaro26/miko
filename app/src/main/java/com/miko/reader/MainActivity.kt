package com.miko.reader

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.layout.onGloballyPositioned
import kotlin.math.roundToInt
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import androidx.room.Room
import com.google.android.gms.ads.MobileAds
import com.miko.reader.api.AniListApi
import com.miko.reader.api.MangaDexApi
import com.miko.reader.model.*
import com.miko.reader.ui.components.MikoLoadingScreen
import com.miko.reader.ui.screens.*
import com.miko.reader.ui.screens.LogsScreen
import com.miko.reader.ui.theme.AppTheme
import com.miko.reader.ui.theme.MikoTheme
import com.miko.reader.ui.theme.pressToRaise
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.miko.reader.util.AdHelper
import kotlinx.coroutines.delay
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

    private val aniListApi: AniListApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://graphql.anilist.co")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AniListApi::class.java)
    }

    private fun triggerHaptic(enabled: Boolean) {
        if (!enabled) return
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator
        if (vibrator != null && vibrator.hasVibrator()) {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    vibrator.vibrate(android.os.VibrationEffect.createPredefined(android.os.VibrationEffect.EFFECT_CLICK))
                } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator.vibrate(android.os.VibrationEffect.createOneShot(30, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(30)
                }
            } catch (e: Exception) {
                // Fallback in case of lack of framework support for predefined effects
                try {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        vibrator.vibrate(android.os.VibrationEffect.createOneShot(30, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(30)
                    }
                } catch (ex: Exception) { ex.printStackTrace() }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        MobileAds.initialize(this) {}
        AdHelper.loadInterstitialAd(this)

        val prefs = getSharedPreferences("miko_prefs", Context.MODE_PRIVATE)

        intent?.data?.let { uri ->
            if (uri.scheme == "miko" && uri.host == "anilist") {
                val fragment = uri.fragment
                if (fragment != null && fragment.contains("access_token=")) {
                    val token = fragment.split("access_token=")[1].split("&")[0]
                    prefs.edit().putString("anilist_token", token).apply()
                }
            }
        }

        setContent {
            var isAppInitializing by remember { mutableStateOf(true) }
            var themeMode by remember { mutableIntStateOf(prefs.getInt("theme_mode", 0)) }
            var selectedTheme by remember { 
                val themeName = prefs.getString("selected_theme", AppTheme.Dynamic.name)
                mutableStateOf(AppTheme.valueOf(themeName ?: AppTheme.Dynamic.name))
            }
            var carouselCardSize by remember {
                mutableIntStateOf(prefs.getInt("carousel_card_size", 100))
            }
            var hapticsEnabled by remember {
                mutableStateOf(prefs.getBoolean("haptics_enabled", false))
            }

            val isDarkTheme = when (themeMode) {
                1 -> false
                2 -> true
                else -> isSystemInDarkTheme()
            }

            var aniListUser by remember { mutableStateOf<AniListUser?>(null) }
            val aniListToken = remember { prefs.getString("anilist_token", null) }

            LaunchedEffect(Unit) {
                if (aniListToken != null) {
                    try {
                        val res = aniListApi.getCurrentUser(
                            token = "Bearer $aniListToken",
                            request = AniListRequest(query = AniListApi.VIEWER_QUERY)
                        )
                        aniListUser = res.data.viewer
                    } catch (e: Exception) {
                        if (e.message?.contains("401") == true) {
                            prefs.edit().remove("anilist_token").apply()
                        }
                    }
                }
                delay(800)
                isAppInitializing = false
            }

            MikoTheme(darkTheme = isDarkTheme, theme = selectedTheme) {
                var updateUrl by remember { mutableStateOf<String?>(null) }
                var updateVersion by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(Unit) {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        try {
                            val url = java.net.URL("https://api.github.com/repos/Hotaro26/miko/releases/latest")
                            val connection = url.openConnection() as java.net.HttpURLConnection
                            connection.requestMethod = "GET"
                            if (connection.responseCode == 200) {
                                val response = connection.inputStream.bufferedReader().use { it.readText() }
                                val json = org.json.JSONObject(response)
                                val tagName = json.getString("tag_name")
                                val htmlUrl = json.getString("html_url")
                                val currentVersion = BuildConfig.VERSION_NAME
                                if (com.miko.reader.util.UpdateChecker.isNewerVersion(currentVersion.replace("v", ""), tagName.replace("v", ""))) {
                                    updateVersion = tagName
                                    updateUrl = htmlUrl
                                }
                            }
                        } catch (e: Exception) { e.printStackTrace() }
                    }
                }

                if (updateUrl != null && updateVersion != null) {
                    AlertDialog(
                        onDismissRequest = { updateUrl = null },
                        title = { Text("Update Available") },
                        text = { Text("A new version ($updateVersion) of Miko is available. Would you like to update?") },
                        confirmButton = {
                            TextButton(onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl))
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                                updateUrl = null
                            }) {
                                Text("Update")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { updateUrl = null }) {
                                Text("Dismiss")
                            }
                        }
                    )
                }

                var hasSeenOnboarding by remember { mutableStateOf(prefs.getBoolean("has_seen_onboarding", false)) }

                if (isAppInitializing) {
                    MikoLoadingScreen("Initializing Miko...")
                } else if (!hasSeenOnboarding) {
                    OnboardingScreen(
                        currentTheme = selectedTheme,
                        onThemeSelected = { 
                            selectedTheme = it
                            prefs.edit().putString("app_theme", it.name).apply()
                        },
                        onComplete = { 
                            prefs.edit().putBoolean("has_seen_onboarding", true).apply()
                            hasSeenOnboarding = true 
                        }
                    )
                } else {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val navController = rememberNavController()
                        val scope = rememberCoroutineScope()
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntry?.destination?.route

                        var bottomBarHeightPx by remember { mutableFloatStateOf(0f) }
                        val bottomBarOffsetHeightPx = remember { mutableStateOf(0f) }

                        LaunchedEffect(currentRoute) {
                            bottomBarOffsetHeightPx.value = 0f
                        }

                        val nestedScrollConnection = remember(currentRoute, bottomBarHeightPx) {
                            object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
                                override fun onPreScroll(
                                    available: androidx.compose.ui.geometry.Offset,
                                    source: androidx.compose.ui.input.nestedscroll.NestedScrollSource
                                ): androidx.compose.ui.geometry.Offset {
                                    if ((currentRoute == "explore" || currentRoute == "anilist") && bottomBarHeightPx > 0f) {
                                        val delta = available.y
                                        val newOffset = bottomBarOffsetHeightPx.value + delta
                                        bottomBarOffsetHeightPx.value = newOffset.coerceIn(-bottomBarHeightPx, 0f)
                                    }
                                    return androidx.compose.ui.geometry.Offset.Zero
                                }
                            }
                        }

                        var optInAds by remember { mutableStateOf(prefs.getBoolean("opt_in_ads", true)) }

                        fun navigateWithAds(route: String) {
                            if (optInAds) {
                                AdHelper.showInterstitialAd(this) { navController.navigate(route) }
                            } else {
                                navController.navigate(route)
                            }
                        }

                        val configuration = LocalConfiguration.current
                        val screenWidth = configuration.screenWidthDp.dp
                        val showSideNav = screenWidth >= 600.dp && currentRoute != null && !currentRoute.startsWith("reader")

                        Row(modifier = Modifier.fillMaxSize()) {
                            if (showSideNav) {
                                NavigationRail(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.fillMaxHeight()
                                ) {
                                    Spacer(Modifier.height(48.dp))
                                    NavigationRailItem(
                                        icon = {
                                            AnimatedNavigationIcon(
                                                selected = currentRoute == "home",
                                                outlinedIcon = Icons.Outlined.Home,
                                                filledIcon = Icons.Filled.Home,
                                                contentDescription = "Home"
                                            )
                                        },
                                        label = { Text("Home") },
                                        selected = currentRoute == "home",
                                        onClick = { 
                                            triggerHaptic(hapticsEnabled)
                                            navController.navigate("home") {
                                                popUpTo("home") { inclusive = true }
                                                launchSingleTop = true
                                            }
                                        }
                                    )
                                    NavigationRailItem(
                                        icon = {
                                            AnimatedNavigationIcon(
                                                selected = currentRoute == "fav",
                                                outlinedIcon = Icons.Outlined.FavoriteBorder,
                                                filledIcon = Icons.Filled.Favorite,
                                                contentDescription = "Library"
                                            )
                                        },
                                        label = { Text("Library") },
                                        selected = currentRoute == "fav",
                                        onClick = { 
                                            triggerHaptic(hapticsEnabled)
                                            navController.navigate("fav") { launchSingleTop = true } 
                                        }
                                    )
                                    NavigationRailItem(
                                        icon = {
                                            AnimatedNavigationIcon(
                                                selected = currentRoute == "explore",
                                                outlinedIcon = Icons.Outlined.Explore,
                                                filledIcon = Icons.Filled.Explore,
                                                contentDescription = "Explore",
                                                animationType = NavIconAnimation.Rotate
                                            )
                                        },
                                        label = { Text("Explore") },
                                        selected = currentRoute == "explore",
                                        onClick = { 
                                            triggerHaptic(hapticsEnabled)
                                            navController.navigate("explore") { launchSingleTop = true } 
                                        }
                                    )
                                    NavigationRailItem(
                                        icon = {
                                            AnimatedNavigationIcon(
                                                selected = currentRoute == "anilist",
                                                outlinedIcon = Icons.Outlined.Cloud,
                                                filledIcon = Icons.Filled.Cloud,
                                                contentDescription = "AniList"
                                            )
                                        },
                                        label = { Text("AniList") },
                                        selected = currentRoute == "anilist",
                                        onClick = { 
                                            triggerHaptic(hapticsEnabled)
                                            navController.navigate("anilist") { launchSingleTop = true } 
                                        }
                                    )
                                    NavigationRailItem(
                                        icon = {
                                            AnimatedNavigationIcon(
                                                selected = currentRoute == "about",
                                                outlinedIcon = Icons.Outlined.Info,
                                                filledIcon = Icons.Filled.Info,
                                                contentDescription = "About",
                                                animationType = NavIconAnimation.Rotate
                                            )
                                        },
                                        label = { Text("About") },
                                        selected = currentRoute == "about",
                                        onClick = { 
                                            triggerHaptic(hapticsEnabled)
                                            navController.navigate("about") { launchSingleTop = true } 
                                        }
                                    )
                                }
                            }

                            Scaffold(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .nestedScroll(nestedScrollConnection),
                                containerColor = Color.Transparent,
                                floatingActionButton = {
                                    if (currentRoute == "home") {
                                        val historyList by db.historyDao().getAllHistory().collectAsState(initial = emptyList())
                                        if (historyList.isNotEmpty()) {
                                            val fabInteractionSource = remember { MutableInteractionSource() }
                                            ExtendedFloatingActionButton(
                                                onClick = {
                                                    triggerHaptic(hapticsEnabled)
                                                    val lastRead = historyList.first()
                                                    navigateWithAds("reader/${lastRead.id}/${lastRead.lastChapterId}?page=${lastRead.lastPage}")
                                                },
                                                icon = { Icon(Icons.Default.PlayArrow, null) },
                                                text = { Text("Continue") },
                                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                                shape = RoundedCornerShape(16.dp),
                                                interactionSource = fabInteractionSource,
                                                modifier = Modifier.pressToRaise(fabInteractionSource)
                                            )
                                        }
                                    }
                                },
                                bottomBar = {
                                    if (currentRoute != null && !currentRoute.startsWith("reader") && !showSideNav) {
                                        NavigationBar(
                                            containerColor = MaterialTheme.colorScheme.surface,
                                            tonalElevation = 3.dp,
                                            modifier = Modifier
                                                .onGloballyPositioned { coordinates ->
                                                    bottomBarHeightPx = coordinates.size.height.toFloat()
                                                }
                                                .offset {
                                                    IntOffset(
                                                        x = 0,
                                                        y = -bottomBarOffsetHeightPx.value.roundToInt()
                                                    )
                                                }
                                        ) {
                                            NavigationBarItem(
                                                icon = {
                                                    AnimatedNavigationIcon(
                                                        selected = currentRoute == "home",
                                                        outlinedIcon = Icons.Outlined.Home,
                                                        filledIcon = Icons.Filled.Home,
                                                        contentDescription = "Home"
                                                    )
                                                },
                                                label = { Text("Home") },
                                                selected = currentRoute == "home",
                                                onClick = { 
                                                    triggerHaptic(hapticsEnabled)
                                                    navController.navigate("home") {
                                                        popUpTo("home") { inclusive = true }
                                                        launchSingleTop = true
                                                    }
                                                }
                                            )
                                            NavigationBarItem(
                                                icon = {
                                                    AnimatedNavigationIcon(
                                                        selected = currentRoute == "fav",
                                                        outlinedIcon = Icons.Outlined.FavoriteBorder,
                                                        filledIcon = Icons.Filled.Favorite,
                                                        contentDescription = "Library"
                                                    )
                                                },
                                                label = { Text("Library") },
                                                selected = currentRoute == "fav",
                                                onClick = { 
                                                    triggerHaptic(hapticsEnabled)
                                                    navController.navigate("fav") { launchSingleTop = true } 
                                                }
                                            )
                                            NavigationBarItem(
                                                icon = {
                                                    AnimatedNavigationIcon(
                                                        selected = currentRoute == "explore",
                                                        outlinedIcon = Icons.Outlined.Explore,
                                                        filledIcon = Icons.Filled.Explore,
                                                        contentDescription = "Explore",
                                                        animationType = NavIconAnimation.Rotate
                                                    )
                                                },
                                                label = { Text("Explore") },
                                                selected = currentRoute == "explore",
                                                onClick = { 
                                                    triggerHaptic(hapticsEnabled)
                                                    navController.navigate("explore") { launchSingleTop = true } 
                                                }
                                            )
                                            NavigationBarItem(
                                                icon = {
                                                    AnimatedNavigationIcon(
                                                        selected = currentRoute == "offline",
                                                        outlinedIcon = Icons.Outlined.Download,
                                                        filledIcon = Icons.Filled.Download,
                                                        contentDescription = "Offline"
                                                    )
                                                },
                                                label = { Text("Offline") },
                                                selected = currentRoute == "offline",
                                                onClick = { 
                                                    triggerHaptic(hapticsEnabled)
                                                    navController.navigate("offline") { launchSingleTop = true } 
                                                }
                                            )
                                            NavigationBarItem(
                                                icon = {
                                                    AnimatedNavigationIcon(
                                                        selected = currentRoute == "about",
                                                        outlinedIcon = Icons.Outlined.Info,
                                                        filledIcon = Icons.Filled.Info,
                                                        contentDescription = "About",
                                                        animationType = NavIconAnimation.Rotate
                                                    )
                                                },
                                                label = { Text("About") },
                                                selected = currentRoute == "about",
                                                onClick = { 
                                                    triggerHaptic(hapticsEnabled)
                                                    navController.navigate("about") { launchSingleTop = true } 
                                                }
                                            )
                                        }
                                    }
                                }
                            ) { padding ->
                                Box(
                                    modifier = Modifier
                                        .padding(
                                            top = padding.calculateTopPadding(),
                                        bottom = if (currentRoute == "explore" || currentRoute == "anilist_full") 0.dp else padding.calculateBottomPadding()
                                        )
                                        .fillMaxSize()
                                ) {
                                    NavHost(
                                        navController = navController,
                                        startDestination = "home",
                                        enterTransition = {
                                            androidx.compose.animation.scaleIn(
                                                initialScale = 0.9f,
                                                animationSpec = androidx.compose.animation.core.tween(150)
                                            ) + androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(150))
                                        },
                                        exitTransition = {
                                            androidx.compose.animation.scaleOut(
                                                targetScale = 0.9f,
                                                animationSpec = androidx.compose.animation.core.tween(150)
                                            ) + androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(150))
                                        },
                                        popEnterTransition = {
                                            androidx.compose.animation.scaleIn(
                                                initialScale = 0.9f,
                                                animationSpec = androidx.compose.animation.core.tween(150)
                                            ) + androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(150))
                                        },
                                        popExitTransition = {
                                            androidx.compose.animation.scaleOut(
                                                targetScale = 0.9f,
                                                animationSpec = androidx.compose.animation.core.tween(150)
                                            ) + androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(150))
                                        }
                                    ) {
                                         composable("home") {
                                             HomeScreen(
                                                 historyFlow = db.historyDao().getAllHistory(),
                                                 favouritesFlow = db.favouriteDao().getAllFavourites(),
                                                 carouselCardSize = carouselCardSize,
                                                 onHistoryClick = { history ->
                                                     triggerHaptic(hapticsEnabled)
                                                     navigateWithAds("detail/${history.id}")
                                                 },
                                                 onFavouriteClick = { fav ->
                                                     triggerHaptic(hapticsEnabled)
                                                     navigateWithAds("detail/${fav.id}")
                                                 }
                                             )
                                         }
                                        composable("fav") {
                                             FavouritesScreen(
                                                 favouritesFlow = db.favouriteDao().getAllFavourites(),
                                                 carouselCardSize = carouselCardSize,
                                                 onAniListClick = {
                                                     triggerHaptic(hapticsEnabled)
                                                     navController.navigate("anilist_full")
                                                 },
                                                 onMangaClick = { mangaId ->
                                                     triggerHaptic(hapticsEnabled)
                                                     navigateWithAds("detail/$mangaId")
                                                 }
                                             )
                                         }
                                         composable("offline") {
                                             OfflineScreen(
                                                 downloadsFlow = db.downloadDao().getAllDownloads(),
                                                 carouselCardSize = carouselCardSize,
                                                 onMangaClick = { mangaId ->
                                                     triggerHaptic(hapticsEnabled)
                                                     navigateWithAds("detail/$mangaId")
                                                 },
                                                 onLogsClick = {
                                                     triggerHaptic(hapticsEnabled)
                                                     navController.navigate("logs")
                                                 }
                                             )
                                         }
                                        composable("logs") {
                                            LogsScreen(
                                                logsFlow = com.miko.reader.util.DownloadManager.logs,
                                                onBack = { navController.popBackStack() },
                                                onClearLogs = { com.miko.reader.util.DownloadManager.clearLogs() }
                                            )
                                        }
                                         composable("explore") {
                                             ExploreScreen(
                                                 api = api,
                                                 carouselCardSize = carouselCardSize,
                                                 onLibraryClick = {
                                                     triggerHaptic(hapticsEnabled)
                                                     navController.navigate("fav") { launchSingleTop = true }
                                                 }
                                             ) { manga ->
                                                 triggerHaptic(hapticsEnabled)
                                                 navigateWithAds("detail/${manga.id}")
                                             }
                                         }
                                         composable("anilist_full") {
                                             AniListScreen(
                                                 api = aniListApi, 
                                                 user = aniListUser,
                                                 carouselCardSize = carouselCardSize,
                                                 onConnectClick = {
                                                    triggerHaptic(hapticsEnabled)
                                                    val clientId = 21782 
                                                    val authUrl = "https://anilist.co/api/v2/oauth/authorize?client_id=$clientId&response_type=token"
                                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authUrl))
                                                    startActivity(intent)
                                                 },
                                                 onMediaClick = { media ->
                                                     triggerHaptic(hapticsEnabled)
                                                     scope.launch {
                                                         try {
                                                             val searchRes = api.searchManga(
                                                                 title = media.title.userPreferred(),
                                                                 includes = listOf("cover_art"),
                                                                 limit = 1
                                                             )
                                                             val mdManga = searchRes.data.firstOrNull()
                                                             if (mdManga != null) {
                                                                 navigateWithAds("detail/${mdManga.id}")
                                                             } else {
                                                                 val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://anilist.co/manga/${media.id}")).apply {
                                                                     addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                                 }
                                                                 startActivity(intent)
                                                                 Toast.makeText(this@MainActivity, "Not found on MangaDex, opening browser", Toast.LENGTH_SHORT).show()
                                                             }
                                                         } catch (e: Exception) { e.printStackTrace() }
                                                     }
                                                 },
                                                 onBack = { navController.popBackStack() }
                                             )
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
                                                 },
                                                 carouselCardSize = carouselCardSize,
                                                 onCarouselCardSizeChange = {
                                                     carouselCardSize = it
                                                     prefs.edit().putInt("carousel_card_size", it).apply()
                                                 },
                                                 hapticsEnabled = hapticsEnabled,
                                                 onHapticsEnabledChange = {
                                                     hapticsEnabled = it
                                                     prefs.edit().putBoolean("haptics_enabled", it).apply()
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
                                                aniListApi = aniListApi,
                                                mangaId = mangaId,
                                                db = db,
                                                onBack = { navController.popBackStack() },
                                                onChapterClick = { manga, chapter, page, _ ->
                                                    scope.launch {
                                                        try {
                                                            val existing = db.historyDao().getHistoryById(manga.id)
                                                            db.historyDao().insert(HistoryEntry(
                                                                id = manga.id,
                                                                title = manga.getTitle(),
                                                                coverUrl = manga.getCoverUrl(),
                                                                lastChapterId = chapter.id,
                                                                lastChapterNum = chapter.attributes.chapter,
                                                                lastPage = page ?: existing?.lastPage ?: 0
                                                            ))
                                                        } catch (e: Exception) { e.printStackTrace() }
                                                    }
                                                    navigateWithAds("reader/${manga.id}/${chapter.id}?page=${page ?: 0}")
                                                }
                                            )
                                        }
                                        composable(
                                            route = "reader/{mangaId}/{chapterId}?page={page}",
                                            arguments = listOf(
                                                navArgument("mangaId") { type = NavType.StringType },
                                                navArgument("chapterId") { type = NavType.StringType },
                                                navArgument("page") { type = NavType.IntType; defaultValue = 0 }
                                            )
                                        ) { backStackEntry ->
                                            val mangaId = backStackEntry.arguments?.getString("mangaId") ?: ""
                                            val chapterId = backStackEntry.arguments?.getString("chapterId") ?: ""
                                            val page = backStackEntry.arguments?.getInt("page") ?: 0
                                            
                                            ReaderScreen(
                                                api = api,
                                                db = db,
                                                mangaId = mangaId,
                                                chapterId = chapterId, 
                                                initialPage = page,
                                                onNextChapter = { nextId ->
                                                    navController.navigate("reader/$mangaId/$nextId?page=0") {
                                                        popUpTo("reader/$mangaId/$chapterId?page=$page") { inclusive = true }
                                                    }
                                                },
                                                onPageChange = { newPage ->
                                                    scope.launch {
                                                        try {
                                                            val history = db.historyDao().getAllHistory().first()
                                                            val entry = history.find { it.lastChapterId == chapterId }
                                                            if (entry != null) {
                                                                db.historyDao().insert(entry.copy(lastPage = newPage, timestamp = System.currentTimeMillis()))
                                                            }
                                                        } catch (e: Exception) { e.printStackTrace() }
                                                    }
                                                },
                                                onBack = { navController.popBackStack() }
                                            )
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
}

enum class NavIconAnimation { Fill, Rotate }

@Composable
fun AnimatedNavigationIcon(
    selected: Boolean,
    outlinedIcon: ImageVector,
    filledIcon: ImageVector,
    contentDescription: String? = null,
    animationType: NavIconAnimation = NavIconAnimation.Fill
) {
    if (animationType == NavIconAnimation.Rotate) {
        val rotation by animateFloatAsState(
            targetValue = if (selected) 180f else 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "rotation"
        )
        Icon(
            imageVector = if (selected) filledIcon else outlinedIcon,
            contentDescription = contentDescription,
            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer {
                    rotationZ = rotation
                }
        )
    } else {
        val progress by animateFloatAsState(
            targetValue = if (selected) 1f else 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "fill_progress"
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(24.dp)
        ) {
            // Base outlined icon
            Icon(
                imageVector = outlinedIcon,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            // Filled icon that gets clipped from bottom to top as progress increases
            if (progress > 0f) {
                Icon(
                    imageVector = filledIcon,
                    contentDescription = contentDescription,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clip(
                        shape = object : Shape {
                            override fun createOutline(
                                size: Size,
                                layoutDirection: LayoutDirection,
                                density: androidx.compose.ui.unit.Density
                            ): Outline {
                                val y = size.height * (1f - progress)
                                return Outline.Rectangle(
                                    Rect(
                                        left = 0f,
                                        top = y,
                                        right = size.width,
                                        bottom = size.height
                                    )
                                )
                            }
                        }
                    )
                )
            }
        }
    }
}
