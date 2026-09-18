package com.example.feawse.ui.main

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.feawse.theme.*
import com.example.feawse.ui.cheats.CheatScreen
import com.example.feawse.ui.convoy.ConvoyScreen
import com.example.feawse.ui.home.HomeScreen
import com.example.feawse.ui.progress.ProgressScreen
import com.example.feawse.ui.components.AwakeningGlowNavigationBar
import com.example.feawse.ui.components.AwakeningToast
import com.example.feawse.ui.components.GlowNavItem
import com.example.feawse.ui.components.ToastData
import com.example.feawse.ui.units.UnitEditScreen
import com.example.feawse.ui.units.UnitListScreen
import com.example.feawse.viewmodel.SaveFileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(
    viewModel: SaveFileViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var activeToast by remember { mutableStateOf<ToastData?>(null) }
    var showSaveConfirmDialog by remember { mutableStateOf(false) }

    // Unit search state (hoisted from UnitListScreen)
    var unitSearchQuery by rememberSaveable { mutableStateOf("") }
    var isUnitSearchMode by rememberSaveable { mutableStateOf(false) }
    val searchFocusRequester = remember { FocusRequester() }

    // Import launcher (hoisted from UnitListScreen)
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.importUnitFromUri(context, uri)
        }
    }

    // Auto-focus search field when search mode activates
    LaunchedEffect(isUnitSearchMode) {
        if (isUnitSearchMode) {
            kotlinx.coroutines.delay(100)
            searchFocusRequester.requestFocus()
        }
    }

    // Clear search when navigating away from Birimler tab
    LaunchedEffect(uiState.activeTab) {
        if (uiState.activeTab != 1) {
            isUnitSearchMode = false
            unitSearchQuery = ""
        }
    }

    LaunchedEffect(uiState.infoMessage, uiState.errorMessage) {
        val info = uiState.infoMessage
        val error = uiState.errorMessage
        if (info != null) {
            activeToast = ToastData(message = info, isError = false)
            val currentTs = activeToast?.timestamp
            viewModel.clearMessages()
            kotlinx.coroutines.delay(2000)
            if (activeToast?.timestamp == currentTs) {
                activeToast = null
            }
        } else if (error != null) {
            activeToast = ToastData(message = error, isError = true)
            val currentTs = activeToast?.timestamp
            viewModel.clearMessages()
            kotlinx.coroutines.delay(2000)
            if (activeToast?.timestamp == currentTs) {
                activeToast = null
            }
        }
    }

    // Back handling: if search is active, close search first; if unit edit is open, close it
    BackHandler(enabled = uiState.selectedUnit != null || isUnitSearchMode) {
        if (isUnitSearchMode) {
            isUnitSearchMode = false
            unitSearchQuery = ""
        } else {
            viewModel.selectUnit(null)
        }
    }

    Scaffold(
        topBar = {
            if (uiState.selectedUnit == null) {
                val isOnBirimlerTab = uiState.activeTab == 1 && uiState.chapterFile != null && uiState.globalFile == null

                if (isOnBirimlerTab && isUnitSearchMode) {
                    // Search mode TopAppBar — TextField replaces title
                    TopAppBar(
                        navigationIcon = {
                            IconButton(onClick = {
                                isUnitSearchMode = false
                                unitSearchQuery = ""
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Aramayı Kapat",
                                    tint = AwakeningTextSecondary
                                )
                            }
                        },
                        title = {
                            TextField(
                                value = unitSearchQuery,
                                onValueChange = { unitSearchQuery = it },
                                placeholder = {
                                    Text(
                                        "Karakter veya sınıf ara...",
                                        color = AwakeningTextTertiary,
                                        fontSize = 14.sp
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(searchFocusRequester),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = AwakeningGold,
                                    focusedTextColor = AwakeningTextPrimary,
                                    unfocusedTextColor = AwakeningTextPrimary
                                )
                            )
                        },
                        actions = {
                            if (unitSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { unitSearchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Temizle",
                                        tint = AwakeningTextSecondary
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = AwakeningDarkBg
                        )
                    )
                } else {
                    // Normal TopAppBar
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = "FE: Awakening Save Editor",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AwakeningGoldBright
                                )
                                Text(
                                    text = uiState.slotInfo?.displayName ?: (uiState.currentFileName ?: "Kayıt Dosyası Açılmadı"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AwakeningTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        },
                        actions = {
                            // Search icon — only on Birimler tab
                            if (isOnBirimlerTab) {
                                IconButton(onClick = { isUnitSearchMode = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Ara",
                                        tint = AwakeningTextSecondary
                                    )
                                }
                            }

                            // Save button
                            if (uiState.chapterFile != null || uiState.globalFile != null) {
                                if (uiState.isModified) {
                                    IconButton(onClick = { showSaveConfirmDialog = true }) {
                                        Icon(
                                            imageVector = Icons.Default.Save,
                                            contentDescription = "Kaydet",
                                            tint = AwakeningGoldBright
                                        )
                                    }
                                }
                            }

                            // Overflow menu — only on Birimler tab
                            if (isOnBirimlerTab) {
                                var showOverflowMenu by remember { mutableStateOf(false) }
                                Box {
                                    IconButton(onClick = { showOverflowMenu = true }) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = "Daha Fazla",
                                            tint = AwakeningTextSecondary
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = showOverflowMenu,
                                        onDismissRequest = { showOverflowMenu = false },
                                        containerColor = AwakeningNavySurface,
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    "Birim İçe Aktar (.fe13u)",
                                                    color = AwakeningTextPrimary,
                                                    fontSize = 14.sp
                                                )
                                            },
                                            onClick = {
                                                showOverflowMenu = false
                                                importLauncher.launch(arrayOf("*/*"))
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.FileOpen,
                                                    contentDescription = null,
                                                    tint = AwakeningGold,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = AwakeningDarkBg
                        )
                    )
                }
            }
        },
        bottomBar = {
            if (uiState.selectedUnit == null) {
                if (uiState.globalFile != null) {
                    val globalItems = remember {
                        listOf(
                            GlowNavItem(tabId = 0, title = "Ana Sayfa", icon = Icons.Default.Home),
                            GlowNavItem(tabId = 1, title = "Günlük (99)", icon = Icons.Default.MenuBook),
                            GlowNavItem(tabId = 2, title = "Kilitler", icon = Icons.Default.MilitaryTech),
                            GlowNavItem(tabId = 3, title = "Sistem", icon = Icons.Default.Settings)
                        )
                    }
                    AwakeningGlowNavigationBar(
                        items = globalItems,
                        activeTabId = uiState.activeTab,
                        onTabSelected = { viewModel.setActiveTab(it) }
                    )
                } else {
                    val chapterItems = remember {
                        listOf(
                            GlowNavItem(tabId = 0, title = "Ana Sayfa", icon = Icons.Default.Home),
                            GlowNavItem(tabId = 1, title = "Birimler", icon = Icons.Default.Shield),
                            GlowNavItem(tabId = 2, title = "Konvoy", icon = Icons.Default.Inventory),
                            GlowNavItem(tabId = 3, title = "İlerleme", icon = Icons.Default.MilitaryTech),
                            GlowNavItem(tabId = 4, title = "Hileler", icon = Icons.Default.Bolt)
                        )
                    }
                    AwakeningGlowNavigationBar(
                        items = chapterItems,
                        activeTabId = uiState.activeTab,
                        onTabSelected = { viewModel.setActiveTab(it) }
                    )
                }
            }
        },
        snackbarHost = {},
        containerColor = AwakeningDarkBg
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.selectedUnit != null) {
                UnitEditScreen(
                    unit = uiState.selectedUnit!!,
                    viewModel = viewModel,
                    uiState = uiState,
                    onBack = { viewModel.selectUnit(null) }
                )
            } else {
                AnimatedContent(
                    targetState = uiState.activeTab,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally(animationSpec = tween(180, easing = FastOutSlowInEasing)) { width -> width / 4 } +
                                    fadeIn(animationSpec = tween(180)))
                                .togetherWith(
                                    slideOutHorizontally(animationSpec = tween(180, easing = FastOutSlowInEasing)) { width -> -width / 4 } +
                                            fadeOut(animationSpec = tween(120))
                                )
                        } else {
                            (slideInHorizontally(animationSpec = tween(180, easing = FastOutSlowInEasing)) { width -> -width / 4 } +
                                    fadeIn(animationSpec = tween(180)))
                                .togetherWith(
                                    slideOutHorizontally(animationSpec = tween(180, easing = FastOutSlowInEasing)) { width -> width / 4 } +
                                            fadeOut(animationSpec = tween(120))
                                )
                        }
                    },
                    label = "TabScreenTransition",
                    modifier = Modifier.fillMaxSize()
                ) { currentTab ->
                    if (uiState.globalFile != null) {
                        when (currentTab) {
                            0 -> HomeScreen(viewModel = viewModel, uiState = uiState, onRequestSave = { showSaveConfirmDialog = true })
                            else -> com.example.feawse.ui.global.GlobalScreen(
                                viewModel = viewModel,
                                uiState = uiState,
                                initialTab = (currentTab - 1).coerceIn(0, 2)
                            )
                        }
                    } else {
                        when (currentTab) {
                            0 -> HomeScreen(viewModel = viewModel, uiState = uiState, onRequestSave = { showSaveConfirmDialog = true })
                            1 -> UnitListScreen(
                                viewModel = viewModel,
                                uiState = uiState,
                                onSelectUnit = { unit -> viewModel.selectUnit(unit) },
                                searchQuery = unitSearchQuery
                            )
                            2 -> ConvoyScreen(viewModel = viewModel, uiState = uiState)
                            3 -> ProgressScreen(viewModel = viewModel, uiState = uiState)
                            4 -> CheatScreen(viewModel = viewModel, uiState = uiState)
                        }
                    }
                }
            }

            // Themed Bottom Toast Notification (Altta kayarak beliren altın temalı toast)
            AwakeningToast(
                toastData = activeToast,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp),
                isBottom = true,
                onDismiss = { activeToast = null }
            )

            // Loading overlay indicator
            if (uiState.isLoading) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AwakeningDarkBg.copy(alpha = 0.7f)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        CircularProgressIndicator(color = AwakeningGold)
                    }
                }
            }

            // Save Confirmation Dialog (Fire Emblem Awakening Theme)
            if (showSaveConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showSaveConfirmDialog = false },
                    containerColor = AwakeningNavySurface,
                    shape = RoundedCornerShape(16.dp),
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = AwakeningRoyalBlueContainer,
                                modifier = Modifier.size(36.dp),
                                border = BorderStroke(1.dp, AwakeningGold.copy(alpha = 0.6f))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Save,
                                        contentDescription = null,
                                        tint = AwakeningGoldBright,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Kayıt Dosyası Kaydedilsin mi?",
                                fontWeight = FontWeight.Bold,
                                color = AwakeningTextPrimary,
                                fontSize = 16.sp
                            )
                        }
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Yapılan düzenlemeler doğrudan kayıt dosyasına kalıcı olarak yazılacaktır. Devam etmek istiyor musunuz?",
                                color = AwakeningTextSecondary,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = AwakeningDarkBg,
                                border = BorderStroke(1.dp, AwakeningBorderSubtle),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Description,
                                            contentDescription = null,
                                            tint = AwakeningRoyalBlueBright,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = uiState.currentFileName ?: "Kayıt Dosyası",
                                            color = AwakeningTextPrimary,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp
                                        )
                                    }
                                    uiState.slotInfo?.let { slot ->
                                        Text(
                                            text = slot.displayName,
                                            color = AwakeningFalchionCyan,
                                            fontSize = 11.5.sp
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showSaveConfirmDialog = false
                                viewModel.saveFile(context)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AwakeningRoyalBlue,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Evet, Kaydet", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = { showSaveConfirmDialog = false },
                            border = BorderStroke(1.dp, AwakeningBorderSubtle),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AwakeningTextSecondary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text("İptal", fontSize = 12.5.sp)
                        }
                    }
                )
            }
        }
    }
}
