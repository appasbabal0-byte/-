package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.dialogs.AddPartDialog
import com.example.ui.dialogs.BarcodeScannerDialog
import com.example.ui.dialogs.CheckInDialog
import com.example.ui.dialogs.CheckOutDialog
import com.example.ui.dialogs.PartDetailDialog
import com.example.ui.dialogs.VisualAiDialog
import com.example.ui.screens.AiAnalyticsScreen
import com.example.ui.screens.AuditTrailScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.theme.AmberCaution
import com.example.ui.theme.FireRedBright
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.FireStockViewModel
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {

    private val viewModel: FireStockViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                // RTL layout provider for natural Arabic interface
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    FireStockApp(viewModel = viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FireStockApp(viewModel: FireStockViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val allParts by viewModel.allParts.collectAsState()
    val filteredParts by viewModel.filteredParts.collectAsState()
    val allLogs by viewModel.allLogs.collectAsState()
    val recentLogs by viewModel.recentLogs.collectAsState()
    val aiPredictions by viewModel.aiPredictions.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedPart by viewModel.selectedPart.collectAsState()

    val isCheckInOpen by viewModel.isCheckInDialogOpen.collectAsState()
    val isCheckOutOpen by viewModel.isCheckOutDialogOpen.collectAsState()
    val isAddPartOpen by viewModel.isAddPartDialogOpen.collectAsState()
    val isScannerOpen by viewModel.isScannerDialogOpen.collectAsState()
    val isVisualAiOpen by viewModel.isVisualAiDialogOpen.collectAsState()
    val isPartDetailOpen by viewModel.isPartDetailDialogOpen.collectAsState()
    val activeDialogPart by viewModel.activeDialogPart.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.toastMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = FireRedBright,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "FireStock AI",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = "إدارة مخزون سيارات الإطفاء (Offline)",
                                fontSize = 10.sp,
                                color = AmberCaution
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.openVisualAi() },
                        modifier = Modifier.testTag("top_bar_visual_ai_button")
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "التعرف البصري الذكي",
                            tint = AmberCaution
                        )
                    }
                    IconButton(
                        onClick = { viewModel.openScanner() },
                        modifier = Modifier.testTag("top_bar_scanner_button")
                    ) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = "مسح باركود",
                            tint = FireRedBright
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == AppTab.DASHBOARD,
                    onClick = { viewModel.setTab(AppTab.DASHBOARD) },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = AppTab.DASHBOARD.titleAr) },
                    label = { Text(AppTab.DASHBOARD.titleAr, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("nav_tab_dashboard")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.INVENTORY,
                    onClick = { viewModel.setTab(AppTab.INVENTORY) },
                    icon = { Icon(Icons.Default.Inventory2, contentDescription = AppTab.INVENTORY.titleAr) },
                    label = { Text(AppTab.INVENTORY.titleAr, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("nav_tab_inventory")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.AUDIT_TRAIL,
                    onClick = { viewModel.setTab(AppTab.AUDIT_TRAIL) },
                    icon = { Icon(Icons.Default.History, contentDescription = AppTab.AUDIT_TRAIL.titleAr) },
                    label = { Text(AppTab.AUDIT_TRAIL.titleAr, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("nav_tab_audit_trail")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.AI_ANALYTICS,
                    onClick = { viewModel.setTab(AppTab.AI_ANALYTICS) },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = AppTab.AI_ANALYTICS.titleAr) },
                    label = { Text(AppTab.AI_ANALYTICS.titleAr, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = AmberCaution
                    ),
                    modifier = Modifier.testTag("nav_tab_ai_analytics")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.DASHBOARD -> DashboardScreen(
                    parts = allParts,
                    recentLogs = recentLogs,
                    predictions = aiPredictions,
                    onOpenCheckIn = { viewModel.openCheckIn(null) },
                    onOpenCheckOut = { viewModel.openCheckOut(null) },
                    onOpenScanner = { viewModel.openScanner() },
                    onOpenInventory = { viewModel.setTab(AppTab.INVENTORY) },
                    onOpenVisualAi = { viewModel.openVisualAi() },
                    onSelectPart = { part -> viewModel.selectPart(part) },
                    onViewAllLogs = { viewModel.setTab(AppTab.AUDIT_TRAIL) },
                    onViewAiAnalytics = { viewModel.setTab(AppTab.AI_ANALYTICS) }
                )

                AppTab.INVENTORY -> InventoryScreen(
                    parts = filteredParts,
                    searchQuery = searchQuery,
                    selectedCategory = selectedCategory,
                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                    onCategoryChange = { viewModel.setCategory(it) },
                    onOpenScanner = { viewModel.openScanner() },
                    onOpenAddPart = { viewModel.openAddPart() },
                    onOpenCheckIn = { part -> viewModel.openCheckIn(part) },
                    onOpenCheckOut = { part -> viewModel.openCheckOut(part) },
                    onSelectPart = { part -> viewModel.selectPart(part) }
                )

                AppTab.AUDIT_TRAIL -> AuditTrailScreen(logs = allLogs)

                AppTab.AI_ANALYTICS -> AiAnalyticsScreen(
                    predictions = aiPredictions,
                    allParts = allParts,
                    onOpenCheckIn = { part -> viewModel.openCheckIn(part) }
                )
            }
        }
    }

    // Modal Dialogs

    if (isCheckInOpen) {
        CheckInDialog(
            initialPart = activeDialogPart,
            allParts = allParts,
            onDismiss = { viewModel.isCheckInDialogOpen.value = false },
            onConfirm = { partId, qty, supplier, tech, notes ->
                viewModel.performCheckIn(partId, qty, supplier, tech, notes)
            }
        )
    }

    if (isCheckOutOpen) {
        CheckOutDialog(
            initialPart = activeDialogPart,
            allParts = allParts,
            onDismiss = { viewModel.isCheckOutDialogOpen.value = false },
            onConfirm = { partId, qty, tech, fireTruck, notes ->
                viewModel.performCheckOut(partId, qty, tech, fireTruck, notes)
            }
        )
    }

    if (isAddPartOpen) {
        AddPartDialog(
            onDismiss = { viewModel.isAddPartDialogOpen.value = false },
            onGenerateBarcode = { viewModel.generateNewBarcode() },
            onConfirm = { partNumber, name, category, barcode, binLoc, wh, aisle, shelf, bin, qty, min, unit, supp, comp, notes, tech ->
                viewModel.createPart(
                    partNumber, name, category, barcode, binLoc, wh, aisle, shelf, bin, qty, min, unit, supp, comp, notes, tech
                )
            }
        )
    }

    if (isScannerOpen) {
        BarcodeScannerDialog(
            sampleParts = allParts,
            onDismiss = { viewModel.isScannerDialogOpen.value = false },
            onBarcodeScanned = { barcode ->
                viewModel.searchByBarcode(barcode)
            }
        )
    }

    if (isVisualAiOpen) {
        VisualAiDialog(
            allParts = allParts,
            onDismiss = { viewModel.isVisualAiDialogOpen.value = false },
            onSelectPart = { part ->
                viewModel.isVisualAiDialogOpen.value = false
                viewModel.selectPart(part)
            }
        )
    }

    if (isPartDetailOpen && selectedPart != null) {
        val partLogs = remember(selectedPart, allLogs) {
            allLogs.filter { it.partId == selectedPart!!.id }
        }
        PartDetailDialog(
            part = selectedPart!!,
            partLogs = partLogs,
            onDismiss = { viewModel.dismissDetailDialog() },
            onOpenCheckIn = { part ->
                viewModel.dismissDetailDialog()
                viewModel.openCheckIn(part)
            },
            onOpenCheckOut = { part ->
                viewModel.dismissDetailDialog()
                viewModel.openCheckOut(part)
            },
            onUpdateLocation = { partId, newLoc, wh, aisle, shelf, bin, tech, notes ->
                viewModel.updateBinLocation(partId, newLoc, wh, aisle, shelf, bin, tech, notes)
            }
        )
    }
}
