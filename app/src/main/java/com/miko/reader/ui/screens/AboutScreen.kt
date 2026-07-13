package com.miko.reader.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miko.reader.ui.theme.AppTheme
import com.miko.reader.BuildConfig
import com.miko.reader.util.UpdateChecker
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    optInAds: Boolean,
    onOptInAdsChange: (Boolean) -> Unit,
    themeMode: Int,
    onThemeModeChange: (Int) -> Unit,
    selectedTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit,
    carouselCardSize: Int,
    onCarouselCardSizeChange: (Int) -> Unit,
    hapticsEnabled: Boolean,
    onHapticsEnabledChange: (Boolean) -> Unit
) {
    var currentSection by remember { mutableStateOf<String?>(null) }

    BackHandler(enabled = currentSection != null) {
        currentSection = null
    }

    AnimatedContent(targetState = currentSection, label = "about_nav") { section ->
        if (section == null) {
            MainSettingsScreen(
                onSectionClick = { currentSection = it }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                TopAppBar(
                    title = { Text(section, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { currentSection = null }) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    when (section) {
                        "Support Developer" -> SupportDeveloperContent(optInAds, onOptInAdsChange)
                        "Appearance" -> AppearanceContent(
                            themeMode, onThemeModeChange,
                            selectedTheme, onThemeChange,
                            carouselCardSize, onCarouselCardSizeChange,
                            hapticsEnabled, onHapticsEnabledChange
                        )
                        "Developer" -> DeveloperContent()
                        "Project Miko" -> ProjectMikoContent()
                    }
                }
            }
        }
    }
}

@Composable
fun MainSettingsScreen(onSectionClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 48.dp, bottom = 32.dp)
    ) {
        Text(
            "Settings",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black,
            letterSpacing = (-1).sp,
            modifier = Modifier.padding(start = 8.dp, bottom = 24.dp)
        )

        SettingsGroup("General") {
            SettingsItem(
                icon = Icons.Default.Favorite,
                title = "Support Developer",
                subtitle = "Ads, UPI, GitHub",
                onClick = { onSectionClick("Support Developer") }
            )
            SettingsDivider()
            SettingsItem(
                icon = Icons.Default.Palette,
                title = "Appearance",
                subtitle = "Theme, Colors, Haptics",
                onClick = { onSectionClick("Appearance") }
            )
            SettingsDivider()
            SettingsItem(
                icon = Icons.Default.Person,
                title = "Developer",
                subtitle = "hotaro, Discord, Pinterest",
                onClick = { onSectionClick("Developer") }
            )
            SettingsDivider()
            SettingsItem(
                icon = Icons.Default.Info,
                title = "Project Miko",
                subtitle = "Source code, License, API",
                onClick = { onSectionClick("Project Miko") }
            )
        }
    }
}

@Composable
fun SupportDeveloperContent(optInAds: Boolean, onOptInAdsChange: (Boolean) -> Unit) {
    val context = LocalContext.current
    var showSupportDialog by remember { mutableStateOf(false) }
    var pleadingStage by remember { mutableIntStateOf(0) }
    val myUpiId = "sakibreza035@okaxis"

    // Dialogs
    if (showSupportDialog) {
        AlertDialog(
            onDismissRequest = { showSupportDialog = false },
            confirmButton = { TextButton(onClick = { showSupportDialog = false }) { Text("Got it") } },
            icon = { Icon(Icons.Default.Favorite, null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Support hotaro", textAlign = TextAlign.Center) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("If your UPI app didn't open automatically, you can use this ID:", textAlign = TextAlign.Center)
                    Spacer(Modifier.height(16.dp))
                    Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = MaterialTheme.shapes.small) {
                        Text(myUpiId, modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }

    if (pleadingStage == 1) {
        AlertDialog(
            onDismissRequest = { pleadingStage = 0 },
            confirmButton = { TextButton(onClick = { pleadingStage = 2 }) { Text("Yes, turn off") } },
            dismissButton = { TextButton(onClick = { pleadingStage = 0 }) { Text("No, keep them") } },
            title = { Text("Are you sure? 🥺") },
            text = { Text("Showing ads helps keep Miko alive and free!") }
        )
    }
    if (pleadingStage == 2) {
        AlertDialog(
            onDismissRequest = { pleadingStage = 0 },
            confirmButton = { TextButton(onClick = { pleadingStage = 3 }) { Text("Turn off anyway") } },
            dismissButton = { TextButton(onClick = { pleadingStage = 0 }) { Text("Keep it") } },
            title = { Text("Pretty please? 😭") },
            text = { Text("It only shows occasional ads and supports development.") }
        )
    }
    if (pleadingStage == 3) {
        AlertDialog(
            onDismissRequest = { pleadingStage = 0 },
            confirmButton = { TextButton(onClick = { pleadingStage = 0; onOptInAdsChange(false) }) { Text("Disable Ads") } },
            dismissButton = { TextButton(onClick = { pleadingStage = 0 }) { Text("Keep Ads Enabled") } },
            title = { Text("Last try... 💖") },
            text = { Text("Okay, if you really must, you can turn them off.") }
        )
    }

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        SettingsGroup("Support Options") {
            SettingsItem(
                icon = Icons.Default.FavoriteBorder,
                title = "Opt-in Ads",
                subtitle = "Show ads to support me",
                onClick = { if (optInAds) pleadingStage = 1 else onOptInAdsChange(true) },
                trailing = { Switch(checked = optInAds, onCheckedChange = { if (!it) pleadingStage = 1 else onOptInAdsChange(true) }) }
            )
            SettingsDivider()
            SettingsItem(
                icon = Icons.Default.Payments,
                title = "Support via UPI",
                subtitle = "Directly support Miko's development",
                onClick = {
                    val uri = Uri.parse("upi://pay").buildUpon()
                        .appendQueryParameter("pa", myUpiId)
                        .appendQueryParameter("pn", "Hotaro")
                        .appendQueryParameter("tn", "Support Miko")
                        .appendQueryParameter("cu", "INR")
                        .build()
                    try { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) } catch (e: Exception) { showSupportDialog = true }
                }
            )
            SettingsDivider()
            SettingsItem(
                icon = Icons.Default.StarBorder,
                title = "Star Miko",
                subtitle = "Show love on GitHub",
                onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Hotaro26/miko"))) }
            )
        }
    }
}

@Composable
fun AppearanceContent(
    themeMode: Int,
    onThemeModeChange: (Int) -> Unit,
    selectedTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit,
    carouselCardSize: Int,
    onCarouselCardSizeChange: (Int) -> Unit,
    hapticsEnabled: Boolean,
    onHapticsEnabledChange: (Boolean) -> Unit
) {
    var showThemeModeDialog by remember { mutableStateOf(false) }
    var showColorSchemeDialog by remember { mutableStateOf(false) }
    var showCardSizeDialog by remember { mutableStateOf(false) }

    if (showThemeModeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeModeDialog = false },
            title = { Text("Theme Mode") },
            text = {
                Column {
                    listOf("System", "Light", "Dark").forEachIndexed { index, label ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable { onThemeModeChange(index); showThemeModeDialog = false }.padding(vertical = 12.dp)
                        ) {
                            RadioButton(selected = themeMode == index, onClick = null)
                            Spacer(Modifier.width(16.dp))
                            Text(label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showThemeModeDialog = false }) { Text("Cancel") } }
        )
    }

    if (showColorSchemeDialog) {
        AlertDialog(
            onDismissRequest = { showColorSchemeDialog = false },
            title = { Text("Color Scheme") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    AppTheme.values().forEach { theme ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable { onThemeChange(theme); showColorSchemeDialog = false }.padding(vertical = 12.dp)
                        ) {
                            RadioButton(selected = selectedTheme == theme, onClick = null)
                            Spacer(Modifier.width(16.dp))
                            Text(theme.label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showColorSchemeDialog = false }) { Text("Cancel") } }
        )
    }

    if (showCardSizeDialog) {
        AlertDialog(
            onDismissRequest = { showCardSizeDialog = false },
            title = { Text("Manga Card Size") },
            text = {
                Column {
                    Text("${carouselCardSize}dp", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
                    Spacer(Modifier.height(16.dp))
                    Slider(
                        value = carouselCardSize.toFloat(),
                        onValueChange = { onCarouselCardSizeChange(it.toInt()) },
                        valueRange = 100f..220f,
                        steps = 5
                    )
                }
            },
            confirmButton = { TextButton(onClick = { showCardSizeDialog = false }) { Text("Done") } }
        )
    }

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        SettingsGroup("Customisation") {
            SettingsItem(
                icon = Icons.Default.DarkMode,
                title = "Theme Mode",
                subtitle = when(themeMode) { 0 -> "System"; 1 -> "Light"; 2 -> "Dark"; else -> "System" },
                onClick = { showThemeModeDialog = true }
            )
            SettingsDivider()
            SettingsItem(
                icon = Icons.Default.Palette,
                title = "Color Scheme",
                subtitle = selectedTheme.label,
                onClick = { showColorSchemeDialog = true }
            )
            SettingsDivider()
            SettingsItem(
                icon = Icons.Default.PhotoSizeSelectActual,
                title = "Manga Card Size",
                subtitle = "${carouselCardSize}dp",
                onClick = { showCardSizeDialog = true }
            )
            SettingsDivider()
            SettingsItem(
                icon = Icons.Default.Vibration,
                title = "Haptic Feedback",
                subtitle = "Vibrate on interactions",
                onClick = { onHapticsEnabledChange(!hapticsEnabled) },
                trailing = { Switch(checked = hapticsEnabled, onCheckedChange = onHapticsEnabledChange) }
            )
        }
    }
}

@Composable
fun DeveloperContent() {
    val context = LocalContext.current
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        SettingsGroup("About Me") {
            SettingsItem(
                icon = null,
                iconRes = com.miko.reader.R.drawable.ic_github,
                title = "hotaro",
                subtitle = "Building crisp, fast, and secure software.",
                onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Hotaro26"))) }
            )
            SettingsDivider()
            SettingsItem(
                icon = null,
                iconRes = com.miko.reader.R.drawable.ic_discord,
                title = "Discord",
                subtitle = "oi.hotaro",
                onClick = { Toast.makeText(context, "Discord: oi.hotaro", Toast.LENGTH_LONG).show() },
                trailing = {}
            )
            SettingsDivider()
            SettingsItem(
                icon = null,
                iconRes = com.miko.reader.R.drawable.ic_pinterest,
                title = "Pinterest",
                subtitle = "hotaro344",
                onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://pinterest.com/hotaro344"))) }
            )
        }
    }
}

@Composable
fun ProjectMikoContent() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isChecking by remember { mutableStateOf(false) }

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        SettingsGroup("Info") {
            SettingsItem(
                icon = Icons.Default.SystemUpdate,
                title = "Check for updates",
                subtitle = if (isChecking) "Checking..." else "Current version: v${BuildConfig.VERSION_NAME}",
                onClick = {
                    if (!isChecking) {
                        isChecking = true
                        scope.launch {
                            UpdateChecker.checkUpdate(context, showToastIfLatest = true)
                            isChecking = false
                        }
                    }
                }, trailing = {}
            )
            SettingsDivider()
            SettingsItem(
                icon = Icons.Default.Code,
                title = "Source Code",
                subtitle = "Open Source",
                onClick = {}, trailing = {}
            )
            SettingsDivider()
            SettingsItem(
                icon = Icons.Default.Description,
                title = "License",
                subtitle = "MIT / Apache 2.0",
                onClick = {}, trailing = {}
            )
            SettingsDivider()
            SettingsItem(
                icon = Icons.Default.Link,
                title = "API",
                subtitle = "MangaDex API",
                onClick = {}, trailing = {}
            )
        }
        
        Spacer(Modifier.height(32.dp))
        Text(
            "Version ${BuildConfig.VERSION_NAME}",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun SettingsGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun SettingsDivider() {
    Divider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
        thickness = 1.dp,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector? = null,
    title: String,
    subtitle: String,
    iconRes: Int? = null,
    onClick: () -> Unit,
    trailing: @Composable () -> Unit = { 
        Icon(Icons.Default.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) 
    }
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
        } else if (iconRes != null) {
            Icon(androidx.compose.ui.res.painterResource(id = iconRes), null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        trailing()
    }
}
