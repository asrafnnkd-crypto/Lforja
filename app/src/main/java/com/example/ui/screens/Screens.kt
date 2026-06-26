package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.example.data.models.Channel
import com.example.data.models.Match
import com.example.data.models.UserAccount
import com.example.ui.MainViewModel

// Define Colors matching Lfraja 2.3 branding (Glossy Deep Purple and Gold theme)
val LfrajaPurple = Color(0xFFFFD700) // Luxurious Gold Accent
val LfrajaLightPurple = Color(0xFF2B1C4B) // Premium Glossy Deep Purple
val LfrajaDeepPurple = Color(0xFF130A24) // Royal Midnight Deep Purple
val WhatsAppGreen = Color(0xFF25D366)
val FacebookBlue = Color(0xFF1877F2)
val BackgroundGray = Color(0xFFFAFAFC)

@Composable
fun LfrajaAppContent(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentActiveScreen.collectAsStateWithLifecycle()
    val adminNotification by viewModel.adminNotification.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Observe Admin Push Notification Dialog
    LaunchedEffect(adminNotification) {
        adminNotification?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            // Keep notification visible in the UI for 5s, then dismiss
            kotlinx.coroutines.delay(5000)
            viewModel.dismissNotification()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.White
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                "splash" -> SplashScreen(viewModel)
                "main" -> MainScreen(viewModel)
                "player" -> {
                    // Keep MainScreen active underneath to preserve state perfectly
                    MainScreen(viewModel)
                    
                    // Show player in a completely separate window/view (Dialog overlay)
                    androidx.compose.ui.window.Dialog(
                        onDismissRequest = { viewModel.stopPlayer() },
                        properties = androidx.compose.ui.window.DialogProperties(
                            usePlatformDefaultWidth = false,
                            dismissOnBackPress = true,
                            dismissOnClickOutside = false
                        )
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = Color.Black
                        ) {
                            VideoPlayerScreen(viewModel)
                        }
                    }
                }
            }

            // Global Overlay Dialog (Simulating local notification push alert)
            adminNotification?.let { msg ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(LfrajaPurple, RoundedCornerShape(12.dp))
                        .shadow(4.dp, RoundedCornerShape(12.dp))
                        .align(Alignment.TopCenter)
                        .clickable { viewModel.dismissNotification() }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Notification",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = msg,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

// 1. SPLASH SCREEN (Centered Purple Logo & Loading Bar)
@Composable
fun SplashScreen(viewModel: MainViewModel) {
    val progress by viewModel.splashProgress.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1.2f))

        // Elegant cursive styled Lfraja logo
        Text(
            text = "Lfraja",
            fontSize = 62.sp,
            fontWeight = FontWeight.Bold,
            color = LfrajaPurple,
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .testTag("splash_logo")
                .padding(bottom = 16.dp)
        )

        // Animated loader block (4 segment bar from image_0.png)
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            for (i in 0..3) {
                val isActive = progress >= (i + 1) / 4f
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .width(42.dp)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isActive) LfrajaPurple else Color(0xFFECECEC))
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Branding label at the bottom
        Text(
            text = "SL MEDIA",
            fontSize = 14.sp,
            fontWeight = FontWeight.Light,
            color = Color.Gray,
            letterSpacing = 2.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 48.dp)
        )
    }
}

// 2. MAIN INTERFACE (Multi-tab system: Matches, Channels, Admin)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val isMenuOpen by viewModel.isMenuOverlayOpen.collectAsStateWithLifecycle()
    val isDark by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp),
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Right options: Refresh, Theme Toggle & Menu
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                viewModel.runAiAnalyticsEngine()
                                Toast.makeText(context, "تم تحديث البيانات المباشرة", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("header_refresh_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        IconButton(
                            onClick = { viewModel.openMenuOverlay(true) },
                            modifier = Modifier.testTag("header_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = "Menu Overlay",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    // Centered branding verbatim title "Lfraja 2.3"
                    Text(
                        text = "Lfraja 2.3",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        fontFamily = FontFamily.Cursive,
                        color = LfrajaPurple,
                        textAlign = TextAlign.End
                    )
                }
            }

            // Tab screen loader
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (selectedTab) {
                    "matches" -> MatchesTabScreen(viewModel)
                    "channels" -> ChannelsTabScreen(viewModel)
                    "subscribe" -> SubscriptionTabScreen(viewModel)
                    "admin" -> AdminPanelScreen(viewModel)
                }
            }

            // Navigation Bar
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = selectedTab == "admin",
                    onClick = { viewModel.selectTab("admin") },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Admin Panel") },
                    label = { Text("التحكم الذكي AI", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("tab_admin")
                )

                NavigationBarItem(
                    selected = selectedTab == "subscribe",
                    onClick = { viewModel.selectTab("subscribe") },
                    icon = { Icon(Icons.Default.CardMembership, contentDescription = "Subscriptions") },
                    label = { Text("الاشتراكات VIP", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("tab_subscribe")
                )

                NavigationBarItem(
                    selected = selectedTab == "channels",
                    onClick = { viewModel.selectTab("channels") },
                    icon = { Icon(Icons.Default.Tv, contentDescription = "Channels") },
                    label = { Text("قنوات", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("tab_channels")
                )

                NavigationBarItem(
                    selected = selectedTab == "matches",
                    onClick = { viewModel.selectTab("matches") },
                    icon = { Icon(Icons.Default.SportsSoccer, contentDescription = "Matches") },
                    label = { Text("مباريات", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("tab_matches")
                )
            }
        }

        // Animated Menu Overlay
        if (isMenuOpen) {
            MenuOverlayScreen(viewModel)
        }
    }
}

// 2.A. MATCHES TAB (Live scoreboard, logos, social popup)
@Composable
fun MatchesTabScreen(viewModel: MainViewModel) {
    val matchesList by viewModel.matches.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp) // Avoid overlap with popup
        ) {
            items(matchesList) { match ->
                MatchCard(match = match, onClick = { viewModel.playMatch(match) })
            }
        }

        // Floating 'لا يفوتك الجديد!' Social Popup (Facebook / WhatsApp)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("social_popup")
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "لا يفوتك الجديد ! 📢",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.SansSerif,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "تابعنا للتوصل بالتحديثات و جديدنا أول بأول",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.SansSerif,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://chat.whatsapp.com/mock-lfraja"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "WhatsApp",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("واتساب", color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif, fontSize = 12.sp)
                            }
                        }

                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://facebook.com/mock-lfraja"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Facebook",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("فيسبوك", color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MatchCard(match: Match, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    // Smooth TV/remote focus scaling & shadow elevation transitions (horizontal card uses elegant 1.04f scale)
    val focusScale by animateFloatAsState(
        targetValue = if (isFocused) 1.04f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "matchFocusScale"
    )
    val focusElevation by animateDpAsState(
        targetValue = if (isFocused) 10.dp else 4.dp,
        animationSpec = tween(durationMillis = 150),
        label = "matchFocusElevation"
    )
    val focusBorderColor by animateColorAsState(
        targetValue = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
        animationSpec = tween(durationMillis = 150),
        label = "matchFocusBorder"
    )

    // Pulse scale for active live match cards
    val isLiveMatch = match.status == "جارية الآن" || match.isLive
    val pulseScale by if (isLiveMatch) {
        val infiniteTransition = rememberInfiniteTransition(label = "matchPulse")
        infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.02f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "matchPulseScale"
        )
    } else {
        remember { mutableStateOf(1.0f) }
    }

    val finalScale = focusScale * pulseScale

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .graphicsLayer {
                scaleX = finalScale
                scaleY = finalScale
            }
            .shadow(
                elevation = focusElevation,
                shape = RoundedCornerShape(16.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            )
            .border(
                BorderStroke(
                    width = if (isFocused) 2.dp else 1.dp,
                    color = focusBorderColor
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick
            )
            .focusable(interactionSource = interactionSource),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tournament Name
            Text(
                text = match.tournament,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                fontSize = 12.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Teams, flags and Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Team 1 (Left)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1.2f)
                ) {
                    Text(
                        text = match.team1Flag,
                        fontSize = 28.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = match.team1Name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Match Status & Scores (Center)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    if (match.status == "جارية الآن" || match.isLive) {
                        // Live indicator and score
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFDCFCE7))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "مباشر 🔴",
                                color = Color(0xFF16A34A),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${match.score1} - ${match.score2}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.SansSerif,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        // Not live yet: display start time
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f))
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = match.status,
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Team 2 (Right)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.weight(1.2f)
                ) {
                    Text(
                        text = match.team2Name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                     )
                     Text(
                        text = match.team2Flag,
                        fontSize = 28.sp,
                        modifier = Modifier.padding(start = 8.dp)
                     )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer (Channel and commentator)
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), thickness = 1.dp)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Commentator (Right aligned in RTL)
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Commentator",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = match.commentator,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.SansSerif,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                // Channel Name (Left aligned in RTL)
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = "Channel",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = match.channelName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// 2.B. CHANNELS TAB (Interactive grid of channel cards with trophies)
@Composable
fun ChannelsTabScreen(viewModel: MainViewModel) {
    val channelList by viewModel.channels.collectAsStateWithLifecycle()
    val categories = listOf("الكل", "beIN Sports", "المغربية", "ترفيه وأفلام")
    var selectedCategory by remember { mutableStateOf("الكل") }

    val filteredChannels = remember(channelList, selectedCategory) {
        if (selectedCategory == "الكل") {
            channelList
        } else {
            channelList.filter { it.category == selectedCategory }
        }
    }

    val focusRequester = remember { FocusRequester() }
    var hasRequestedFocus by remember { mutableStateOf(false) }

    LaunchedEffect(filteredChannels) {
        if (filteredChannels.isNotEmpty() && !hasRequestedFocus) {
            try {
                focusRequester.requestFocus()
                hasRequestedFocus = true
            } catch (e: Exception) {
                // Ignore focus errors
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Category filters tab with dynamic material colors
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                val isActive = selectedCategory == cat
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                        .border(
                            1.dp,
                            if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { selectedCategory = cat }
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = cat,
                        color = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    )
                }
            }
        }

        // Channels Grid with autofocus on launch support
        androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            itemsIndexed(filteredChannels) { index, channel ->
                val itemModifier = if (index == 0) Modifier.focusRequester(focusRequester) else Modifier
                ChannelGridItem(
                    channel = channel,
                    modifier = itemModifier,
                    onClick = { viewModel.playChannel(channel) }
                )
            }
        }
    }
}

@Composable
fun ChannelGridItem(channel: Channel, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    // Smooth TV/remote focus scaling & shadow elevation transitions
    val focusScale by animateFloatAsState(
        targetValue = if (isFocused) 1.10f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "focusScale"
    )
    val logoScale by animateFloatAsState(
        targetValue = if (isFocused) 1.15f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "logoScale"
    )
    val glowColor = if (channel.id.contains("bein") || channel.isLive) Color(0xFF9C27B0) else Color(0xFFFFB300)
    val glowAlpha by animateFloatAsState(
        targetValue = if (isFocused) 0.35f else 0.0f,
        animationSpec = tween(durationMillis = 200),
        label = "glowAlpha"
    )
    val focusElevation by animateDpAsState(
        targetValue = if (isFocused) 12.dp else 4.dp,
        animationSpec = tween(durationMillis = 150),
        label = "focusElevation"
    )

    // Gentle 2-second client-side pulse animation on active live channel cards
    val pulseScale by if (channel.isLive) {
        val infiniteTransition = rememberInfiniteTransition(label = "livePulse")
        infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.04f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )
    } else {
        remember { mutableStateOf(1.0f) }
    }

    val finalScale = focusScale * pulseScale

    Card(
        modifier = modifier
            .padding(6.dp)
            .aspectRatio(0.85f)
            .graphicsLayer {
                scaleX = finalScale
                scaleY = finalScale
            }
            .shadow(
                elevation = focusElevation,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color(0xFF9C27B0).copy(alpha = 0.12f),
                spotColor = Color(0xFF9C27B0).copy(alpha = 0.18f)
            )
            .border(
                BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick
            )
            .focusable(interactionSource = interactionSource),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Official Logo centered and high quality with transparent background
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    val resolvedLogoUrl = if (channel.logoUrl.isNotEmpty()) {
                        channel.logoUrl
                    } else {
                        // Dynamic robust fallback if logoUrl is empty
                        when {
                            channel.id.contains("bein_max") || channel.id.contains("news") -> "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c8/BeIN_Sports_logo.svg/512px-BeIN_Sports_logo.svg.png"
                            channel.id.contains("arryadia") -> "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d4/Arryadia_logo.svg/512px-Arryadia_logo.svg.png"
                            channel.id.contains("aloula") -> "https://upload.wikimedia.org/wikipedia/commons/thumb/d/df/Al_Aoula.svg/512px-Al_Aoula.svg.png"
                            channel.id.contains("2m") -> "https://upload.wikimedia.org/wikipedia/commons/thumb/d/df/Logo_2M_Maroc.png/320px-Logo_2M_Maroc.png"
                            else -> ""
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .graphicsLayer {
                                scaleX = logoScale
                                scaleY = logoScale
                            }
                            .coloredShadow(
                                color = glowColor,
                                alpha = glowAlpha,
                                borderRadius = 12.dp,
                                shadowRadius = 16.dp,
                                spread = 4.dp,
                                offsetX = 0.dp,
                                offsetY = 0.dp
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (resolvedLogoUrl.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color(0xFF6B21A8), Color(0xFF3B0764))
                                        )
                                    )
                                    .padding(6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = channel.name,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        } else {
                            SubcomposeAsyncImage(
                                model = resolvedLogoUrl,
                                contentDescription = channel.name,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .size(70.dp)
                                    .padding(4.dp),
                                loading = {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            color = Color(0xFF9C27B0),
                                            strokeWidth = 2.dp,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                },
                                error = {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(Color(0xFF6B21A8), Color(0xFF3B0764))
                                                )
                                            )
                                            .padding(6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = channel.name,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            maxLines = 3,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            )
                        }
                    }

                    // Pulse/active live indicator label
                    if (channel.isLive) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .background(Color(0xFFEF4444), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "مباشر",
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.SansSerif
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Channel Name with Cairo/system style Arabic Font
                Text(
                    text = channel.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Trophy indicator (Upgraded gold star badge)
            if (channel.hasTrophy) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFFEF3C7), Color(0xFFFDE68A))
                            )
                        )
                        .border(1.dp, Color(0xFFF59E0B), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Stars,
                        contentDescription = "World Cup",
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

// Custom colored soft shadow for elegant floating premium items
fun Modifier.coloredShadow(
    color: Color = Color(0xFF9C27B0),
    alpha: Float = 0.12f,
    borderRadius: androidx.compose.ui.unit.Dp = 20.dp,
    shadowRadius: androidx.compose.ui.unit.Dp = 20.dp,
    spread: androidx.compose.ui.unit.Dp = 2.dp,
    offsetX: androidx.compose.ui.unit.Dp = 0.dp,
    offsetY: androidx.compose.ui.unit.Dp = 10.dp
): Modifier = this.drawBehind {
    val shadowColor = color.copy(alpha = alpha).toArgb()
    drawIntoCanvas { canvas ->
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        frameworkPaint.color = android.graphics.Color.TRANSPARENT
        frameworkPaint.setShadowLayer(
            shadowRadius.toPx(),
            offsetX.toPx(),
            offsetY.toPx(),
            shadowColor
        )
        canvas.drawRoundRect(
            left = -spread.toPx(),
            top = -spread.toPx(),
            right = size.width + spread.toPx(),
            bottom = size.height + spread.toPx(),
            radiusX = borderRadius.toPx(),
            radiusY = borderRadius.toPx(),
            paint = paint
        )
    }
}

// 2.C. SUBSCRIPTION TAB (Premium list of subscription cards and a gorgeous hero header)
@Composable
fun SubscriptionTabScreen(viewModel: MainViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hero Header Card (Beautiful visual introduction for subscription)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .coloredShadow(
                    color = Color(0xFF9C27B0),
                    alpha = 0.12f,
                    borderRadius = 20.dp,
                    shadowRadius = 20.dp,
                    spread = 2.dp,
                    offsetX = 0.dp,
                    offsetY = 10.dp
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Crown / Membership vector icon (replaces emoji)
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CardMembership,
                        contentDescription = "VIP Subscriptions",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "باقات الاشتراك الفريدة VIP",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "اشترك في باقات Lfraja VIP واحصل على بث مباشر بدون إعلانات وبأعلى جودة ممكنة لدعم استمرار السيرفرات المجانية للجميع.",
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    fontFamily = FontFamily.SansSerif,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Subscription Cards List
        SubscriptionCard(
            name = "الباقة الأساسية المجانية",
            price = "0 درهم",
            channels = "20 قناة مجانية",
            quality = "جودة عادية SD",
            desc = "الولوج الأساسي للقنوات المفتوحة والمغربية بجودة قياسية تناسب اتصالات الهواتف العادية والإنترنت المحدود.",
            colorStart = Color(0xFF9CA3AF),
            colorEnd = Color(0xFF4B5563),
            icon = Icons.Default.StarBorder,
            waText = "أريد تجربة الباقة الأساسية المجانية"
        )

        SubscriptionCard(
            name = "الباقة الفضية المتكاملة",
            price = "30 درهم",
            channels = "100 قناة ممتازة",
            quality = "جودة عالية HD",
            desc = "تغطية كاملة وشاملة لأبرز المباريات، المسلسلات، وقنوات الأطفال بجودة عالية الدقة بدون انقطاع.",
            colorStart = Color(0xFF3B82F6),
            colorEnd = Color(0xFF1D4ED8),
            icon = Icons.Default.MilitaryTech,
            waText = "أريد الاشتراك في الباقة الفضية المتكاملة بسعر 30 درهم"
        )

        SubscriptionCard(
            name = "الباقة الماسية 4K VIP",
            price = "50 درهم",
            channels = "150 قناة كاملة",
            quality = "جودة فائقة 4K ULTRA",
            desc = "الولوج الكامل والحصري لكافة القنوات الرياضية والترفيهية بجودة 4K حقيقية مع سيرفرات احتياطية سحابية لضمان ثبات تام للمشاهدة مع دعم فني 24 ساعة.",
            colorStart = Color(0xFFFBBF24),
            colorEnd = Color(0xFFD97706),
            icon = Icons.Default.Diamond,
            isPremium = true,
            waText = "أريد الاشتراك في الباقة الماسية 4K VIP بسعر 50 درهم"
        )
    }
}

// 3. MENU OVERLAY (verbatim Image 3 options and row of social purple icons)
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MenuOverlayScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val currentActivity = context as? ComponentActivity
    val star6Mode by viewModel.morocco6Mode.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { viewModel.openMenuOverlay(false) }
    ) {
        // Slide out sheet from right
        Card(
            modifier = Modifier
                .fillMaxHeight()
                .width(280.dp)
                .align(Alignment.CenterStart)
                .clickable(enabled = false) {}
                .shadow(12.dp),
            shape = RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header verbatim logo
                Text(
                    text = "Lfraja 2.3",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 24.dp)
                )

                Text(
                    text = "أيقونة الترفيه بإمتياز",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.SansSerif,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), thickness = 1.dp)

                Spacer(modifier = Modifier.height(16.dp))

                // Switch for Morocco *6 Data Saver Mode
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = star6Mode,
                            onCheckedChange = { viewModel.toggleMorocco6Mode() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.scale(0.8f)
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "توفير البيانات (*6)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.SansSerif,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Five verbatim options matching image_3.png
                MenuOptionItem(
                    title = "إعادة تشغيل التطبيق",
                    icon = Icons.Default.Refresh,
                    onClick = {
                        viewModel.openMenuOverlay(false)
                        Toast.makeText(context, "إعادة تشغيل النظام وتحديث المحتوى...", Toast.LENGTH_SHORT).show()
                    }
                )

                MenuOptionItem(
                    title = "مشاركة شاشة الهاتف",
                    icon = Icons.Default.Cast,
                    onClick = {
                        Toast.makeText(context, "البحث عن أجهزة Smart TV متصلة بالشبكة لإلقاء البث...", Toast.LENGTH_LONG).show()
                    }
                )

                MenuOptionItem(
                    title = "الإبلاغ عن المشاكل",
                    icon = Icons.Default.BugReport,
                    onClick = {
                        Toast.makeText(context, "تم إرسال بلاغ فني إلى الإدارة. نشكر مساهمتك!", Toast.LENGTH_SHORT).show()
                    }
                )

                MenuOptionItem(
                    title = "روابط وأكواد التطبيق",
                    icon = Icons.Default.Link,
                    onClick = {
                        Toast.makeText(context, "كود التطبيق الفريد: LF-2026. الرابط: lfraja.ma", Toast.LENGTH_LONG).show()
                    }
                )

                MenuOptionItem(
                    title = "الخروج من التطبيق",
                    icon = Icons.Default.ExitToApp,
                    onClick = {
                        currentActivity?.finish()
                    }
                )

                Spacer(modifier = Modifier.weight(1f))

                // Row of modern social icons
                Text(
                    text = "تابعنا على منصاتنا الاجتماعية",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.SansSerif,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val socials: List<Pair<ImageVector, String>> = listOf(
                        Icons.Default.Call to "https://wa.me/mock",
                        Icons.Default.Share to "https://facebook.com/mock",
                        Icons.Default.Camera to "https://instagram.com/mock",
                        Icons.Default.Send to "https://t.me/mock",
                        Icons.Default.PlayArrow to "https://tiktok.com/mock"
                    )

                    socials.forEach { (icon, url) ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                .clickable {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MenuOptionItem(title: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.size(16.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                    textAlign = TextAlign.End
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// 4. VIDEO PLAYER SCREEN (ExoPlayer with Morocco *6 Optimization)
@Composable
fun VideoPlayerScreen(viewModel: MainViewModel) {
    val selectedChannel by viewModel.selectedChannel.collectAsStateWithLifecycle()
    val star6Mode by viewModel.morocco6Mode.collectAsStateWithLifecycle()
    val adBlocker by viewModel.adBlockerEnabled.collectAsStateWithLifecycle()
    val drmEnabled by viewModel.drmEnabled.collectAsStateWithLifecycle()

    val context = LocalContext.current

    // Initialize ExoPlayer
    val exoPlayer = remember(context) {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Listen to ExoPlayer events
    DisposableEffect(exoPlayer) {
        val listener = object : androidx.media3.common.Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == androidx.media3.common.Player.STATE_BUFFERING) {
                    isLoading = true
                } else if (playbackState == androidx.media3.common.Player.STATE_READY) {
                    isLoading = false
                    errorMessage = null
                } else if (playbackState == androidx.media3.common.Player.STATE_IDLE) {
                    isLoading = false
                }
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                isLoading = false
                errorMessage = "خطأ في تشغيل البث المباشر: ${error.localizedMessage ?: "تنسيق غير مدعوم أو رابط تالف"}"
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
        }
    }

    // Set media item when selection changes
    LaunchedEffect(selectedChannel) {
        selectedChannel?.let { ch ->
            try {
                isLoading = true
                errorMessage = null
                val mediaItem = MediaItem.fromUri(Uri.parse(ch.streamUrl))
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.play()
            } catch (e: Exception) {
                isLoading = false
                errorMessage = "فشل بدء البث: ${e.localizedMessage ?: "رابط البث غير صالح"}"
            }
        }
    }

    val activity = context as? android.app.Activity

    // Dispose player and restore orientation
    DisposableEffect(Unit) {
        onDispose {
            try {
                activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } catch (e: Exception) {
                // Safe fall-back
            }
            exoPlayer.release()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Player header bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.8f))
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.stopPlayer() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Text(
                text = selectedChannel?.name ?: "البث المباشر",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            // Dynamic Live status indicator and screen rotation toggle
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Interactive screen flip button
                IconButton(
                    onClick = {
                        try {
                            val currentOrientation = activity?.requestedOrientation
                            if (currentOrientation == android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                                activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            } else {
                                activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                            }
                        } catch (e: Exception) {
                            // Safe fall-back
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ScreenRotation,
                        contentDescription = "تدوير الشاشة",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.Red)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("بث حي", color = Color.White, fontSize = 12.sp)
            }
        }

        // Native Video View
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.1f)
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = true
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Spinner during loading
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(48.dp)
                )
            }

            // Error Message display
            errorMessage?.let { errorText ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.85f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⚠️",
                            fontSize = 24.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = errorText,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                selectedChannel?.let { ch ->
                                    try {
                                        isLoading = true
                                        errorMessage = null
                                        val mediaItem = MediaItem.fromUri(Uri.parse(ch.streamUrl))
                                        exoPlayer.setMediaItem(mediaItem)
                                        exoPlayer.prepare()
                                        exoPlayer.play()
                                    } catch (e: Exception) {
                                        isLoading = false
                                        errorMessage = "فشل بدء البث: ${e.localizedMessage ?: "رابط البث غير صالح"}"
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("إعادة المحاولة", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Ad-Blocker blocked overlays
            if (adBlocker) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(LfrajaPurple.copy(alpha = 0.8f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("Ad-Block النشط", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 5. VIP SUBSCRIPTION AND DIAGNOSTIC CONTROLS PANEL
        var isAdvancedSettingsOpen by remember { mutableStateOf(false) }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f)
                .shadow(8.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Catchy Arabic VIP header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ترقية الاشتراك لخدمة VIP 🚀",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = LfrajaPurple
                    )
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E8FF)),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "بث آمن وبدون إعلانات",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = LfrajaPurple,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "اختر الباقة المناسبة لك واستمتع بمشاهدة جميع قنواتك المفضلة بدون انقطاع.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Subscription Tiers List
                SubscriptionCard(
                    name = "الباقة الأساسية المجانية",
                    price = "0 درهم",
                    channels = "20 قناة مجانية",
                    quality = "جودة عادية SD",
                    desc = "الولوج الأساسي للقنوات المفتوحة والمغربية بجودة قياسية تناسب اتصالات الهواتف العادية والإنترنت المحدود.",
                    colorStart = Color(0xFF9CA3AF),
                    colorEnd = Color(0xFF4B5563),
                    icon = Icons.Default.StarBorder,
                    waText = "أريد تجربة الباقة الأساسية المجانية"
                )

                SubscriptionCard(
                    name = "الباقة الفضية المتكاملة",
                    price = "30 درهم",
                    channels = "100 قناة ممتازة",
                    quality = "جودة عالية HD",
                    desc = "تغطية كاملة وشاملة لأبرز المباريات، المسلسلات، وقنوات الأطفال بجودة عالية الدقة بدون انقطاع.",
                    colorStart = Color(0xFF3B82F6),
                    colorEnd = Color(0xFF1D4ED8),
                    icon = Icons.Default.MilitaryTech,
                    waText = "أريد الاشتراك في الباقة الفضية المتكاملة بسعر 30 درهم"
                )

                SubscriptionCard(
                    name = "الباقة الماسية 4K VIP",
                    price = "50 درهم",
                    channels = "150 قناة كاملة",
                    quality = "جودة فائقة 4K ULTRA",
                    desc = "الولوج الكامل والحصري لكافة القنوات الرياضية والترفيهية بجودة 4K حقيقية مع سيرفرات احتياطية سحابية لضمان ثبات تام للمشاهدة مع دعم فني 24 ساعة.",
                    colorStart = Color(0xFFFBBF24),
                    colorEnd = Color(0xFFD97706),
                    icon = Icons.Default.Diamond,
                    isPremium = true,
                    waText = "أريد الاشتراك في الباقة الماسية 4K VIP بسعر 50 درهم"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Expandable Advanced Tuning Section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { isAdvancedSettingsOpen = !isAdvancedSettingsOpen },
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                            .animateContentSize()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isAdvancedSettingsOpen) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Toggle",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "إعدادات البث وتعديل الإشارة المتقدمة",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "🛠️", fontSize = 12.sp)
                            }
                        }

                        if (isAdvancedSettingsOpen) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(8.dp))

                            // 1. Morocco *6 Optimization toggle
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Switch(
                                    checked = star6Mode,
                                    onCheckedChange = { viewModel.toggleMorocco6Mode() },
                                    colors = SwitchDefaults.colors(checkedThumbColor = LfrajaPurple, checkedTrackColor = LfrajaLightPurple)
                                )
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("6* المغربية📶", color = LfrajaPurple, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Text("تحسين البث ليتناسب مع اشتراكات السوشيال ميديا.", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                            }

                            // 2. Ad-Blocker Toggle
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Switch(
                                    checked = adBlocker,
                                    onCheckedChange = { viewModel.toggleAdBlocker() }
                                )
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("حاجب الإعلانات والمنبثقات 🚫", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Text("عزل الإعلانات المزعجة التلقائية أثناء البث.", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                            }

                            // 3. DRM / Security Toggle
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Switch(
                                    checked = drmEnabled,
                                    onCheckedChange = { viewModel.toggleDrm() }
                                )
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("التشفير التلقائي وحل الشاشة السوداء (DRM) 🔐", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Text("حل مشاكل القنوات ذات الحماية العالية.", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 5. ADMINISTRATIVE CONTROL & ADVANCED FEATURES PANEL
@Composable
fun AdminPasscodeScreen(viewModel: MainViewModel) {
    var codeInput by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Pulsing Lock Icon
        val infiniteTransition = rememberInfiniteTransition(label = "lock_pulse")
        val scale by infiniteTransition.animateFloat(
            initialValue = 0.95f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )

        Box(
            modifier = Modifier
                .size(90.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale)
                .background(LfrajaLightPurple, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Secure",
                tint = LfrajaPurple,
                modifier = Modifier.size(42.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "قسم المشرفين فقط 🔐",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "الرجاء إدخال الرمز السري المكون من 4 أرقام لتأكيد صلاحيات المشرف الخاصة بك.",
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Passcode Indicators (4 Dots)
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 1..4) {
                val active = codeInput.length >= i
                val dotColor = when {
                    showError -> Color.Red
                    active -> LfrajaPurple
                    else -> Color(0xFFECECEC)
                }
                val dotSize by animateDpAsState(
                    targetValue = if (active) 18.dp else 14.dp,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )

                Box(
                    modifier = Modifier
                        .size(dotSize)
                        .background(dotColor, CircleShape)
                        .border(1.dp, if (active) Color.Transparent else Color(0xFFD1D5DB), CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedVisibility(visible = showError) {
            Text(
                text = "رمز سري خاطئ! حاول مجدداً.",
                color = Color.Red,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Custom Numeric Keypad
        Column(
            modifier = Modifier.width(280.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val keys = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("مسح", "0", "⌫")
            )

            for (row in keys) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for (key in row) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1.5f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (key == "مسح" || key == "⌫") Color(0xFFF3F4F6) else Color.White
                                )
                                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                                .clickable {
                                    if (showError) {
                                        showError = false
                                        codeInput = ""
                                    }
                                    when (key) {
                                        "مسح" -> codeInput = ""
                                        "⌫" -> {
                                            if (codeInput.isNotEmpty()) {
                                                codeInput = codeInput.dropLast(1)
                                            }
                                        }
                                        else -> {
                                            if (codeInput.length < 4) {
                                                codeInput += key
                                                if (codeInput.length == 4) {
                                                    val success = viewModel.verifyAdminCode(codeInput)
                                                    if (!success) {
                                                        showError = true
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = key,
                                fontSize = if (key == "مسح" || key == "⌫") 14.sp else 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (key == "مسح") Color.Gray else if (key == "⌫") Color.Red else Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminPanelScreen(viewModel: MainViewModel) {
    val isAdminUnlocked by viewModel.isAdminUnlocked.collectAsStateWithLifecycle()

    if (!isAdminUnlocked) {
        AdminPasscodeScreen(viewModel)
    } else {
        val matchesList by viewModel.matches.collectAsStateWithLifecycle()
        val channelsList by viewModel.channels.collectAsStateWithLifecycle()
        val user by viewModel.userAccount.collectAsStateWithLifecycle()
        val predictedLoad by viewModel.predictedServerLoad.collectAsStateWithLifecycle()
        val aiSuggestions by viewModel.aiOptimizedSuggestions.collectAsStateWithLifecycle()
        val firebaseStatus by viewModel.firebaseStatus.collectAsStateWithLifecycle()

        var activeSubTab by remember { mutableStateOf("ai") } // "ai", "matches", "channels", "m3u"
        val context = LocalContext.current

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "لوحة التحكم والمشرف لـ Lfraja",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = LfrajaPurple
                )
                IconButton(onClick = { viewModel.lockAdmin() }) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "تسجيل الخروج", tint = Color.Red)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Real-time Firebase Sync Status Bar
            val isFirebaseActive = firebaseStatus.contains("Firebase") || firebaseStatus.contains("متصل")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .background(if (isFirebaseActive) Color(0xFFE8F5E9) else Color(0xFFFFF3E0), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(if (isFirebaseActive) Color(0xFF4CAF50) else Color(0xFFFF9800), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = firebaseStatus,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isFirebaseActive) Color(0xFF2E7D32) else Color(0xFFE65100)
                    )
                }
                Text(
                    text = "مزامنة سحابية فورية",
                    fontSize = 10.sp,
                    color = if (isFirebaseActive) Color(0xFF43A047) else Color(0xFFFB8C00),
                    fontWeight = FontWeight.Medium
                )
            }

            // Administrative Sub-tabs Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "ai" to "الذكاء AI والتحليلات",
                    "matches" to "محرر المباريات",
                    "channels" to "محرر القنوات",
                    "m3u" to "استيراد M3U",
                    "config" to "إعدادات Remote Config"
                ).forEach { (tabId, label) ->
                    val isActive = activeSubTab == tabId
                    Button(
                        onClick = { activeSubTab = tabId },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isActive) LfrajaPurple else LfrajaLightPurple
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isActive) Color(0xFF130A24) else LfrajaPurple,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display contents depending on active admin panel subtab
            when (activeSubTab) {
                "ai" -> AdminAiTab(predictedLoad, aiSuggestions, user, viewModel)
                "matches" -> AdminMatchesTab(matchesList, viewModel)
                "channels" -> AdminChannelsTab(channelsList, viewModel)
                "m3u" -> AdminM3uTab(viewModel)
                "config" -> AdminRemoteConfigTab(viewModel)
            }
        }
    }
}

@Composable
fun AdminAiTab(load: Float, suggestions: List<String>, user: UserAccount?, viewModel: MainViewModel) {
    var notificationInput by remember { mutableStateOf("") }
    val context = LocalContext.current
    val firebaseStatus by viewModel.firebaseStatus.collectAsStateWithLifecycle()
    val isFirebaseActive = firebaseStatus.contains("Firebase") || firebaseStatus.contains("متصل")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Current logged-in user details (Section II: User Account System)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE5E7EB))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("بيانات حساب المشترك النشط", fontWeight = FontWeight.Bold, color = LfrajaPurple, fontSize = 13.sp)
                    Card(
                        shape = RoundedCornerShape(4.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isFirebaseActive) Color(0xFFE8F5E9) else Color(0xFFFFF3E0))
                    ) {
                        Text(
                            text = if (isFirebaseActive) "سحابي ومزامن" else "مخزن محلياً",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isFirebaseActive) Color(0xFF2E7D32) else Color(0xFFE65100),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(user?.membershipType ?: "العضوية العادية", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp)
                    Text(user?.username ?: "غير مسجل", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 12.sp)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Token: ${user?.token ?: "N/A"}", color = Color.LightGray, fontSize = 10.sp)
                    Text(user?.email ?: "", color = Color.Gray, fontSize = 11.sp)
                }
            }
        }

        // Server Load Prediction Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE5E7EB))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val percentage = (load * 100).toInt()
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (load > 0.75f) Color(0xFFFEE2E2) else Color(0xFFDCFCE7))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "%$percentage ضغط",
                            color = if (load > 0.75f) Color.Red else Color(0xFF16A34A),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                    Text("حمل خادم البث المتوقع (AI Load Model)", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = load,
                    color = if (load > 0.75f) Color.Red else LfrajaPurple,
                    trackColor = Color(0xFFECECEC),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "يتنبأ نموذج الذكاء الاصطناعي بمعدل الحمل على البنية التحتية بالاعتماد على ذروة مشاهدة جدول المباريات.",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // AI Recommendations
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF5FF)),
            border = BorderStroke(1.dp, LfrajaLightPurple)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("توصيات تحسين البث بالذكاء الاصطناعي", fontWeight = FontWeight.Bold, color = LfrajaPurple, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                
                if (suggestions.isEmpty()) {
                    Text("لا توجد مقترحات حالية. أضف قنوات أو مباريات مباشرة لتحليل البيانات.", fontSize = 11.sp, color = Color.Gray)
                } else {
                    suggestions.forEach { sug ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = sug,
                                fontSize = 11.sp,
                                color = Color.DarkGray,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("🤖", fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        // Broadcaster Console (Push Notification Dispatcher)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE5E7EB))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("لوحة بث الإشعارات العاجلة", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notificationInput,
                    onValueChange = { notificationInput = it },
                    label = { Text("محتوى الإشعار") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_notif_input"),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (notificationInput.trim().isNotEmpty()) {
                            viewModel.postAdminNotification(notificationInput)
                            notificationInput = ""
                            Toast.makeText(context, "تم بث الإشعار لجميع المشتركين!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("admin_broadcast_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = LfrajaPurple),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("بث إشعار فوري", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AdminMatchesTab(matches: List<Match>, viewModel: MainViewModel) {
    var team1 by remember { mutableStateOf("") }
    var team1Flag by remember { mutableStateOf("🇲🇦") }
    var team2 by remember { mutableStateOf("") }
    var team2Flag by remember { mutableStateOf("🇪🇸") }
    var score1 by remember { mutableStateOf("0") }
    var score2 by remember { mutableStateOf("0") }
    var tournament by remember { mutableStateOf("كأس العالم 2026") }
    var status by remember { mutableStateOf("21:00") }
    var channelName by remember { mutableStateOf("beIN Sports MAX 1") }
    var streamUrl by remember { mutableStateOf("https://demo.unified-streaming.com/k8s/live/stable/sintel.smil/.m3u8") }
    var commentator by remember { mutableStateOf("رؤوف خليف") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text("إضافة / تعديل مباراة مباشرة", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = team1,
                onValueChange = { team1 = it },
                label = { Text("الفريق 1") },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp),
                shape = RoundedCornerShape(8.dp)
            )
            OutlinedTextField(
                value = team1Flag,
                onValueChange = { team1Flag = it },
                label = { Text("علم 1") },
                modifier = Modifier
                    .width(70.dp)
                    .padding(end = 4.dp),
                shape = RoundedCornerShape(8.dp)
            )
            OutlinedTextField(
                value = score1,
                onValueChange = { score1 = it },
                label = { Text("هدف 1") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.width(70.dp),
                shape = RoundedCornerShape(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = team2,
                onValueChange = { team2 = it },
                label = { Text("الفريق 2") },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp),
                shape = RoundedCornerShape(8.dp)
            )
            OutlinedTextField(
                value = team2Flag,
                onValueChange = { team2Flag = it },
                label = { Text("علم 2") },
                modifier = Modifier
                    .width(70.dp)
                    .padding(end = 4.dp),
                shape = RoundedCornerShape(8.dp)
            )
            OutlinedTextField(
                value = score2,
                onValueChange = { score2 = it },
                label = { Text("هدف 2") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.width(70.dp),
                shape = RoundedCornerShape(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = tournament,
            onValueChange = { tournament = it },
            label = { Text("البطولة أو الجولة") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = status,
                onValueChange = { status = it },
                label = { Text("الحالة (مثال: جارية الآن أو التوقيت)") },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp),
                shape = RoundedCornerShape(8.dp)
            )
            OutlinedTextField(
                value = channelName,
                onValueChange = { channelName = it },
                label = { Text("القناة الناقلة") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = commentator,
            onValueChange = { commentator = it },
            label = { Text("المعلق الرياضي") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = streamUrl,
            onValueChange = { streamUrl = it },
            label = { Text("رابط البث (HLS/M3U8)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                if (team1.isNotEmpty() && team2.isNotEmpty()) {
                    val m = Match(
                        tournament = tournament,
                        team1Name = team1,
                        team1Flag = team1Flag,
                        team2Name = team2,
                        team2Flag = team2Flag,
                        score1 = score1,
                        score2 = score2,
                        status = status,
                        channelName = channelName,
                        streamUrl = streamUrl,
                        isLive = status == "جارية الآن",
                        commentator = commentator
                    )
                    viewModel.saveMatch(m)
                    team1 = ""; team2 = ""
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = LfrajaPurple),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("إضافة المباراة للجدول المباشر")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // List of current matches for quick deletion
        Text("المباريات المجدولة حالياً", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        matches.forEach { match ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.deleteMatch(match) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                    }
                    Text(
                        "${match.team1Name} ${match.score1} - ${match.score2} ${match.team2Name}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun AdminChannelsTab(channels: List<Channel>, viewModel: MainViewModel) {
    var name by remember { mutableStateOf("") }
    var cat by remember { mutableStateOf("beIN Sports") }
    var streamUrl by remember { mutableStateOf("https://demo.unified-streaming.com/k8s/live/stable/sintel.smil/.m3u8") }
    var logoUrl by remember { mutableStateOf("") }
    var hasTrophy by remember { mutableStateOf(false) }

    var editingChannelId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = LfrajaLightPurple.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, LfrajaPurple.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (editingChannelId == null) "إضافة قناة جديدة" else "تعديل القناة النشطة",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = LfrajaDeepPurple
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم القناة") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = cat,
                    onValueChange = { cat = it },
                    label = { Text("الفئة (beIN Sports، المغربية، ترفيه وأفلام...)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = streamUrl,
                    onValueChange = { streamUrl = it },
                    label = { Text("رابط البث (HLS/DASH/M3U8)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = logoUrl,
                    onValueChange = { logoUrl = it },
                    label = { Text("رابط الشعار (صورة من Google أو Facebook)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    placeholder = { Text("https://example.com/logo.png") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = hasTrophy,
                        onCheckedChange = { hasTrophy = it },
                        colors = CheckboxDefaults.colors(checkedColor = LfrajaPurple)
                    )
                    Text("إظهار كأس العالم الذهبي بجانب أيقونة القناة", fontSize = 12.sp, color = Color.DarkGray)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            if (name.isNotEmpty()) {
                                val finalId = editingChannelId ?: name.lowercase().replace(" ", "_")
                                val c = Channel(
                                    id = finalId,
                                    name = name,
                                    streamUrl = streamUrl,
                                    logoUrl = logoUrl,
                                    category = cat,
                                    isLive = true,
                                    hasTrophy = hasTrophy
                                )
                                viewModel.saveChannel(c)

                                // Reset form values
                                name = ""
                                cat = "beIN Sports"
                                streamUrl = "https://demo.unified-streaming.com/k8s/live/stable/sintel.smil/.m3u8"
                                logoUrl = ""
                                hasTrophy = false
                                editingChannelId = null
                            }
                        },
                        modifier = Modifier.weight(1.5f),
                        colors = ButtonDefaults.buttonColors(containerColor = LfrajaPurple),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (editingChannelId == null) "حفظ وإضافة القناة" else "تعديل وحفظ القناة")
                    }

                    if (editingChannelId != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = {
                                // Cancel edit and reset
                                name = ""
                                cat = "beIN Sports"
                                streamUrl = "https://demo.unified-streaming.com/k8s/live/stable/sintel.smil/.m3u8"
                                logoUrl = ""
                                hasTrophy = false
                                editingChannelId = null
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("إلغاء التعديل")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "قنوات البث المتاحة (انقر للتعديل)",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        channels.forEach { channel ->
            val isBeingEdited = editingChannelId == channel.id
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        editingChannelId = channel.id
                        name = channel.name
                        cat = channel.category
                        streamUrl = channel.streamUrl
                        logoUrl = channel.logoUrl
                        hasTrophy = channel.hasTrophy
                    },
                colors = CardDefaults.cardColors(
                    containerColor = if (isBeingEdited) Color(0xFFF3E8FF) else Color(0xFFF9FAFB)
                ),
                shape = RoundedCornerShape(8.dp),
                border = if (isBeingEdited) BorderStroke(1.5.dp, LfrajaPurple) else null
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.deleteChannel(channel.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                        }
                        IconButton(
                            onClick = {
                                editingChannelId = channel.id
                                name = channel.name
                                cat = channel.category
                                streamUrl = channel.streamUrl
                                logoUrl = channel.logoUrl
                                hasTrophy = channel.hasTrophy
                            }
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = LfrajaPurple)
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Text(channel.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text(channel.category, fontSize = 10.sp, color = Color.Gray)
                        }

                        // Logo preview
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(LfrajaPurple.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (channel.logoUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = channel.logoUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(Icons.Default.Tv, contentDescription = null, tint = LfrajaPurple, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminM3uTab(viewModel: MainViewModel) {
    var rawM3uInput by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        Text("مستورد قوائم التشغيل M3U الذكي", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "الصق نص ملف M3U هنا لاستخلاص القنوات ومعلومات البث واللوغوهات آلياً وحفظها في قاعدة البيانات المحلية.",
            fontSize = 11.sp,
            color = Color.Gray,
            textAlign = TextAlign.End
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = rawM3uInput,
            onValueChange = { rawM3uInput = it },
            placeholder = { Text("#EXTM3U\n#EXTINF:-1 tvg-id=\"bein1\" tvg-logo=\"http://logo...\" group-title=\"beIN Sports\",beIN Sports MAX 1\nhttp://stream-link...") },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (rawM3uInput.trim().isNotEmpty()) {
                    viewModel.importM3UPlaylist(rawM3uInput)
                    rawM3uInput = ""
                } else {
                    Toast.makeText(context, "الرجاء إلصاق محتوى صالح أولاً", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LfrajaPurple),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("استيراد وتحليل القنوات", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AdminRemoteConfigTab(viewModel: MainViewModel) {
    val channelsList by viewModel.channels.collectAsStateWithLifecycle()
    val remoteConfigStatus by viewModel.remoteConfigStatus.collectAsStateWithLifecycle()
    
    var selectedChannelId by remember { mutableStateOf("") }
    var logoUrlOverride by remember { mutableStateOf("") }
    var streamUrlOverride by remember { mutableStateOf("") }

    // Update selected channel inputs when channel selection changes
    LaunchedEffect(selectedChannelId) {
        val selectedCh = channelsList.find { it.id == selectedChannelId }
        if (selectedCh != null) {
            logoUrlOverride = selectedCh.logoUrl
            streamUrlOverride = selectedCh.streamUrl
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text("مركز التحكم في المتغيرات عن بعد Remote Config 🌐", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "تحديث شعارات القنوات وروابط البث عن بعد فورياً لجميع المشاهدين وبشكل ديناميكي عبر Firebase Remote Config.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.End
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Remote Config Status Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🛰️", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("حالة Remote Config:", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text(remoteConfigStatus, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Selector / input card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("تجاوز إعدادات القناة (Logo & Stream) عن بعد", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(12.dp))

                // Select Channel Row / Dropdown simulation
                Text("1. اختر القناة المراد تحديثها:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                
                // Horizontal list of selectable channels
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(channelsList.size) { index ->
                        val ch = channelsList[index]
                        val isSelected = selectedChannelId == ch.id
                        Card(
                            modifier = Modifier.clickable { selectedChannelId = ch.id },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) LfrajaPurple else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(1.dp, if (isSelected) Color.Transparent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = ch.name,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedChannelId.isNotEmpty()) {
                    Text("2. أدخل القيم الجديدة لـ Remote Config:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = logoUrlOverride,
                        onValueChange = { logoUrlOverride = it },
                        label = { Text("رابط الشعار الجديد (Logo URL Override)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = streamUrlOverride,
                        onValueChange = { streamUrlOverride = it },
                        label = { Text("رابط البث الجديد (Stream URL Override)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.publishRemoteConfigOverride(selectedChannelId, logoUrlOverride, streamUrlOverride)
                            viewModel.fetchAndApplyRemoteConfig()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = LfrajaPurple),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("نشر التحديث لـ Remote Config", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("الرجاء تحديد قناة من القائمة أعلاه للبدء", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Manual fetch button
        OutlinedButton(
            onClick = { viewModel.fetchAndApplyRemoteConfig() },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("جلب وتطبيق تحديثات Remote Config فوراً", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SubscriptionCard(
    name: String,
    price: String,
    channels: String,
    quality: String,
    desc: String,
    colorStart: Color,
    colorEnd: Color,
    icon: ImageVector,
    isPremium: Boolean = false,
    waText: String
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    // Scale on press animation
    val scale by animateFloatAsState(
        targetValue = if (expanded) 1.02f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .shadow(if (isPremium) 8.dp else 2.dp, RoundedCornerShape(16.dp))
            .border(
                border = BorderStroke(
                    width = if (isPremium) 2.dp else 1.dp,
                    color = if (isPremium) Color(0xFFFBBF24) else Color.Transparent
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(colorStart.copy(alpha = 0.08f), colorEnd.copy(alpha = 0.02f))
                    )
                )
                .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Price
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = price,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isPremium) Color(0xFFD97706) else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "شهرياً",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.SansSerif,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Light
                    )
                }

                // Right: Name and Icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isPremium) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.padding(end = 6.dp)
                                ) {
                                    Text(
                                        text = "الخيار الأفضل ✨",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.SansSerif,
                                        color = Color(0xFFD97706),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.SansSerif,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = quality,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.SansSerif
                            )
                            Text(
                                text = " • ",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                            )
                            Text(
                                text = channels,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Medium,
                                fontFamily = FontFamily.SansSerif
                            )
                        }
                    }

                    // Large circular background for icon (vector replacing emoji)
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(colorStart.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = colorStart,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Expanded Features and WhatsApp Buy button
            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = desc,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    fontFamily = FontFamily.SansSerif,
                    textAlign = TextAlign.End,
                    lineHeight = 16.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Feature Checklist with Modern Vector Icons (Replacing Emoji ticks)
                val features = listOf(
                    "بث فائق السرعة بدون انقطاع" to Icons.Default.Speed,
                    "تحديث تلقائي وفوري للقنوات والروابط" to Icons.Default.Autorenew,
                    "دعم فني مخصص ومتاح 24/7 على الواتساب" to Icons.Default.Forum
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    features.forEach { feature ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = feature.first,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.SansSerif,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                textAlign = TextAlign.End
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = feature.second,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Dynamic WhatsApp subscribe button
                Button(
                    onClick = { launchWhatsApp(context, waText) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "اشترك الآن وتواصل لتفعيل خدمتك فورا",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "Chat on WhatsApp",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

fun launchWhatsApp(context: android.content.Context, text: String) {
    val number = "+212643316085"
    val url = "https://api.whatsapp.com/send?phone=${number.replace("+", "")}&text=${android.net.Uri.encode(text)}"
    try {
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url)).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "لم يتم العثور على تطبيق واتساب!", android.widget.Toast.LENGTH_LONG).show()
    }
}
