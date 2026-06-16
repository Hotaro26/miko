package com.miko.reader.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showSupportDialog by remember { mutableStateOf(false) }
    var pleadingStage by remember { mutableIntStateOf(0) }
    val myUpiId = "sakibreza035@okaxis"

    if (showSupportDialog) {
        AlertDialog(
            onDismissRequest = { showSupportDialog = false },
            confirmButton = {
                TextButton(onClick = { showSupportDialog = false }) {
                    Text("Got it")
                }
            },
            icon = { Icon(Icons.Default.Favorite, null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Support hotaro", textAlign = TextAlign.Center) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Thank you for considering support! It means a lot to me. If your UPI app didn't open automatically, you can use this ID:",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(16.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = myUpiId,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Every bit of support helps keep Miko growing and stays crisp. ❤️",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }

    if (pleadingStage == 1) {
        AlertDialog(
            onDismissRequest = { pleadingStage = 0 },
            confirmButton = {
                TextButton(onClick = { pleadingStage = 2 }) {
                    Text("Yes, turn off")
                }
            },
            dismissButton = {
                TextButton(onClick = { pleadingStage = 0 }) {
                    Text("No, keep them")
                }
            },
            title = { Text("Are you sure? 🥺") },
            text = { Text("Showing ads helps keep Miko alive and completely free! Do you really want to turn them off?") },
            shape = RoundedCornerShape(28.dp)
        )
    }

    if (pleadingStage == 2) {
        AlertDialog(
            onDismissRequest = { pleadingStage = 0 },
            confirmButton = {
                TextButton(onClick = { pleadingStage = 3 }) {
                    Text("Turn off anyway")
                }
            },
            dismissButton = {
                TextButton(onClick = { pleadingStage = 0 }) {
                    Text("Reconsider & Keep")
                }
            },
            title = { Text("Pretty please? 😭") },
            text = { Text("It only shows occasional ads and supports developer hotaro's work. Would you reconsider keeping it enabled?") },
            shape = RoundedCornerShape(28.dp)
        )
    }

    if (pleadingStage == 3) {
        AlertDialog(
            onDismissRequest = { pleadingStage = 0 },
            confirmButton = {
                TextButton(onClick = { 
                    pleadingStage = 0
                    onOptInAdsChange(false) 
                }) {
                    Text("Disable Ads")
                }
            },
            dismissButton = {
                TextButton(onClick = { pleadingStage = 0 }) {
                    Text("Keep Ads Enabled")
                }
            },
            title = { Text("Last try... 💖") },
            text = { Text("Okay, if you really must, you can turn them off. But we'd love it if you keep supporting us. Disable ads?") },
            shape = RoundedCornerShape(28.dp)
        )
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 800.dp)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
                .padding(top = 48.dp, bottom = 32.dp)
        ) {
            Text(
                "Miko",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp
            )
            
            Spacer(Modifier.height(24.dp))

            // Support Developer Section
            AboutSectionHeader("Support Developer")
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Favorite, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Support Miko Development",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Help keep Miko alive and ad-free by default. Opt-in to show interstitial ads occasionally or support via UPI.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(16.dp))
                    
                    // Ad Toggle
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Opt-in Ads", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                Text("Show ads to support me", style = MaterialTheme.typography.labelSmall)
                            }
                            Switch(
                                checked = optInAds,
                                onCheckedChange = { checked ->
                                    if (!checked) {
                                        pleadingStage = 1
                                    } else {
                                        onOptInAdsChange(true)
                                    }
                                }
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(12.dp))
                    
                    // UPI Support Button
                    Button(
                        onClick = {
                            val payeeName = "Hotaro"
                            val transactionNote = "Support Miko Development"
                            
                            val uri = Uri.parse("upi://pay").buildUpon()
                                .appendQueryParameter("pa", myUpiId)
                                .appendQueryParameter("pn", payeeName)
                                .appendQueryParameter("tn", transactionNote)
                                .appendQueryParameter("am", "0")
                                .appendQueryParameter("cu", "INR")
                                .build()
                            
                            val upiIntent = Intent(Intent.ACTION_VIEW, uri)
                            try {
                                context.startActivity(upiIntent)
                            } catch (e: Exception) {
                                showSupportDialog = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.Default.Payments, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Support via UPI")
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    
                    // Star GitHub Project Button
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Hotaro26/miko"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        )
                    ) {
                        Icon(Icons.Default.Star, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Star Miko on GitHub")
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Customise Section
            AboutSectionHeader("Customise")
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("Appearance", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("System", "Light", "Dark").forEachIndexed { index, label ->
                            FilterChip(
                                selected = themeMode == index,
                                onClick = { onThemeModeChange(index) },
                                label = { Text(label) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    Text("Color Scheme", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppTheme.values().forEach { theme ->
                            FilterChip(
                                selected = selectedTheme == theme,
                                onClick = { onThemeChange(theme) },
                                label = { Text(theme.label) },
                                leadingIcon = if (selectedTheme == theme) {
                                    { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    Text("Manga Carousel Card Size", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Slider(
                            value = carouselCardSize.toFloat(),
                            onValueChange = { onCarouselCardSizeChange(it.toInt()) },
                            valueRange = 100f..220f,
                            steps = 5, // 100, 120, 140, 160, 180, 200, 220
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                                activeTickColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                                inactiveTickColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        )
                        Text(
                            text = "${carouselCardSize}dp",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(54.dp)
                        )
                    }

                    Spacer(Modifier.height(24.dp))
                    Text("Haptic Feedback", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Vibration on tap", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                Text("Vibrate when tapping tabs, buttons, or opening manga", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = hapticsEnabled,
                                onCheckedChange = onHapticsEnabledChange
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Developer Section
            AboutSectionHeader("Developer")
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        "hotaro",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Building crisp, fast, and secure software.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AboutChip("GitHub") {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Hotaro26"))
                            context.startActivity(intent)
                        }
                        AboutChip("Discord") {
                            Toast.makeText(context, "Discord: oi.hotaro", Toast.LENGTH_LONG).show()
                        }
                        AboutChip("Pinterest") {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://pinterest.com/hotaro344"))
                            context.startActivity(intent)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // App Info Section
            AboutSectionHeader("Project Miko")
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            ) {
                Column(Modifier.padding(20.dp)) {
                    AboutInfoRow(Icons.Default.Code, "Source Code", "Open Source")
                    AboutInfoRow(Icons.Default.Description, "License", "MIT / Apache 2.0")
                    AboutInfoRow(Icons.Default.Link, "API", "MangaDex API")
                }
            }

            Spacer(Modifier.height(32.dp))
            
            Text(
                "Version 1.2.1",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun AboutSectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
    )
}

@Composable
fun AboutChip(label: String, onClick: () -> Unit) {
    AssistChip(
        onClick = onClick,
        label = { Text(label) },
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun AboutInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
    }
}
