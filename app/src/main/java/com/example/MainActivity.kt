package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.TransactionType
import com.example.ui.screens.AccountsScreen
import com.example.ui.screens.AddAccountDialog
import com.example.ui.screens.AddDebtDialog
import com.example.ui.screens.AddTransactionBottomSheet
import com.example.ui.screens.AiAdvisorBottomSheet
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.DenaPaonaScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.SetBudgetDialog
import com.example.ui.screens.TransactionsScreen
import com.example.ui.screens.TransferDialog
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MoneyViewModel
import com.example.ui.viewmodel.NavigationTab
import com.example.util.AppLanguage
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MoneyViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            MyApplicationTheme(darkTheme = uiState.isDarkMode, language = uiState.language) {
                BengaliMoneyManagerApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BengaliMoneyManagerApp(
    viewModel: MoneyViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isBn = uiState.language == AppLanguage.BENGALI

    // Sheet & Dialog States
    var showAddTransactionSheet by remember { mutableStateOf(false) }
    var addTransactionInitialType by remember { mutableStateOf(TransactionType.EXPENSE) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    var showTransferDialog by remember { mutableStateOf(false) }
    var showAddDebtDialog by remember { mutableStateOf(false) }
    var showSetBudgetDialog by remember { mutableStateOf(false) }
    var showAddAccountDialog by remember { mutableStateOf(false) }
    var showAiAdvisorSheet by remember { mutableStateOf(false) }

    // Handle back button: if not on Dashboard, navigate to Dashboard first
    BackHandler(enabled = uiState.selectedTab != NavigationTab.DASHBOARD) {
        viewModel.setTab(NavigationTab.DASHBOARD)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .testTag("bottom_nav_bar")
                    .windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                val navItems = listOf(
                    NavigationItemData(
                        tab = NavigationTab.DASHBOARD,
                        labelBn = "ড্যাশবোর্ড",
                        labelEn = "Home",
                        selectedIcon = Icons.Filled.Dashboard,
                        unselectedIcon = Icons.Outlined.Dashboard,
                        tag = "nav_dashboard"
                    ),
                    NavigationItemData(
                        tab = NavigationTab.TRANSACTIONS,
                        labelBn = "লেনদেন",
                        labelEn = "History",
                        selectedIcon = Icons.Filled.ReceiptLong,
                        unselectedIcon = Icons.Outlined.ReceiptLong,
                        tag = "nav_transactions"
                    ),
                    NavigationItemData(
                        tab = NavigationTab.ANALYTICS,
                        labelBn = "বাজেট",
                        labelEn = "Budgets",
                        selectedIcon = Icons.Filled.PieChart,
                        unselectedIcon = Icons.Outlined.PieChart,
                        tag = "nav_analytics"
                    ),
                    NavigationItemData(
                        tab = NavigationTab.DENA_PAONA,
                        labelBn = "দেনা-পাওনা",
                        labelEn = "Debts",
                        selectedIcon = Icons.Filled.Payments,
                        unselectedIcon = Icons.Outlined.Payments,
                        tag = "nav_dena_paona"
                    ),
                    NavigationItemData(
                        tab = NavigationTab.ACCOUNTS,
                        labelBn = "অ্যাকাউন্ট",
                        labelEn = "Wallets",
                        selectedIcon = Icons.Filled.AccountBalanceWallet,
                        unselectedIcon = Icons.Outlined.AccountBalanceWallet,
                        tag = "nav_accounts"
                    )
                )

                navItems.forEach { item ->
                    val selected = uiState.selectedTab == item.tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { viewModel.setTab(item.tab) },
                        icon = {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = if (isBn) item.labelBn else item.labelEn
                            )
                        },
                        label = {
                            Text(
                                text = if (isBn) item.labelBn else item.labelEn,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag(item.tag)
                    )
                }
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = uiState.selectedTab == NavigationTab.DASHBOARD || uiState.selectedTab == NavigationTab.TRANSACTIONS,
                enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)) + fadeIn(tween(200)),
                exit = scaleOut(tween(150)) + fadeOut(tween(150))
            ) {
                FloatingActionButton(
                    onClick = {
                        addTransactionInitialType = TransactionType.EXPENSE
                        showAddTransactionSheet = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    modifier = Modifier
                        .testTag("main_add_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Transaction")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = uiState.selectedTab,
                transitionSpec = {
                    if (targetState.ordinal > initialState.ordinal) {
                        (slideInHorizontally(
                            animationSpec = tween(300, easing = FastOutSlowInEasing),
                            initialOffsetX = { fullWidth -> (fullWidth * 0.25f).toInt() }
                        ) + fadeIn(animationSpec = tween(300))).togetherWith(
                            slideOutHorizontally(
                                animationSpec = tween(250, easing = FastOutSlowInEasing),
                                targetOffsetX = { fullWidth -> (-fullWidth * 0.25f).toInt() }
                            ) + fadeOut(animationSpec = tween(200))
                        )
                    } else {
                        (slideInHorizontally(
                            animationSpec = tween(300, easing = FastOutSlowInEasing),
                            initialOffsetX = { fullWidth -> (-fullWidth * 0.25f).toInt() }
                        ) + fadeIn(animationSpec = tween(300))).togetherWith(
                            slideOutHorizontally(
                                animationSpec = tween(250, easing = FastOutSlowInEasing),
                                targetOffsetX = { fullWidth -> (fullWidth * 0.25f).toInt() }
                            ) + fadeOut(animationSpec = tween(200))
                        )
                    }
                },
                label = "tab_switch_transition",
                modifier = Modifier.fillMaxSize()
            ) { tab ->
                when (tab) {
                    NavigationTab.DASHBOARD -> {
                        DashboardScreen(
                            uiState = uiState,
                            onOpenAddTransaction = { type ->
                                addTransactionInitialType = type
                                showAddTransactionSheet = true
                            },
                            onOpenTransfer = { showTransferDialog = true },
                            onOpenAddDebt = { showAddDebtDialog = true },
                            onNavigateTab = { targetTab -> viewModel.setTab(targetTab) },
                            onToggleLanguage = { viewModel.toggleLanguage() },
                            onToggleBengaliDigits = { viewModel.toggleBengaliDigits() },
                            onToggleTheme = { viewModel.toggleTheme() },
                            onOpenAiAdvisor = { showAiAdvisorSheet = true }
                        )
                    }

                    NavigationTab.TRANSACTIONS -> {
                        TransactionsScreen(
                            uiState = uiState,
                            onTimeFilterSelected = { viewModel.setTimeFilter(it) },
                            onTypeFilterSelected = { viewModel.setTypeFilter(it) },
                            onSearchQueryChanged = { viewModel.setSearchQuery(it) },
                            onDeleteTransaction = { viewModel.deleteTransaction(it) }
                        )
                    }

                    NavigationTab.ANALYTICS -> {
                        AnalyticsScreen(
                            uiState = uiState,
                            onOpenSetBudget = { showSetBudgetDialog = true }
                        )
                    }

                    NavigationTab.DENA_PAONA -> {
                        DenaPaonaScreen(
                            uiState = uiState,
                            onOpenAddDebt = { showAddDebtDialog = true },
                            onToggleSettled = { id, settled -> viewModel.setDebtSettled(id, settled) },
                            onDeleteDebt = { viewModel.deleteDebt(it) }
                        )
                    }

                    NavigationTab.ACCOUNTS -> {
                        AccountsScreen(
                            uiState = uiState,
                            onOpenAddAccount = { showAddAccountDialog = true },
                            onOpenTransfer = { showTransferDialog = true },
                            onToggleLanguage = { viewModel.toggleLanguage() },
                            onToggleBengaliDigits = { viewModel.toggleBengaliDigits() },
                            onToggleTheme = { viewModel.toggleTheme() },
                            onRestoreSampleData = { viewModel.resetAndSeedData() },
                            onClearAllData = { viewModel.clearAllData() },
                            onCheckForUpdates = { viewModel.checkForUpdates(isManual = true) },
                            onSimulateUpdate = { viewModel.simulateUpdateForTesting() },
                            onSetCustomUpdateUrl = { viewModel.setCustomUpdateUrl(it) },
                            onDeleteAccount = { viewModel.deleteAccount(it) }
                        )
                    }
                }
            }
        }
    }

    // Add Transaction Bottom Sheet
    if (showAddTransactionSheet) {
        AddTransactionBottomSheet(
            sheetState = sheetState,
            categories = uiState.categories,
            accounts = uiState.accounts,
            language = uiState.language,
            useBengaliDigits = uiState.useBengaliDigits,
            initialType = addTransactionInitialType,
            onDismiss = {
                coroutineScope.launch {
                    sheetState.hide()
                    showAddTransactionSheet = false
                }
            },
            onSave = { type, amount, accountId, toAccountId, categoryId, note ->
                viewModel.addTransaction(type, amount, accountId, toAccountId, categoryId, note)
                coroutineScope.launch {
                    sheetState.hide()
                    showAddTransactionSheet = false
                }
            }
        )
    }

    // Transfer Funds Dialog
    if (showTransferDialog) {
        TransferDialog(
            accounts = uiState.accounts,
            language = uiState.language,
            useBengaliDigits = uiState.useBengaliDigits,
            onDismiss = { showTransferDialog = false },
            onTransfer = { fromAcc, toAcc, amt, note ->
                viewModel.transferFunds(fromAcc, toAcc, amt, note)
                showTransferDialog = false
            }
        )
    }

    // Add Debt Dialog
    if (showAddDebtDialog) {
        AddDebtDialog(
            language = uiState.language,
            onDismiss = { showAddDebtDialog = false },
            onSave = { person, phone, amt, type, dueDate, note ->
                viewModel.addDebt(person, phone, amt, type, dueDate, note)
                showAddDebtDialog = false
            }
        )
    }

    // Set Budget Dialog
    if (showSetBudgetDialog) {
        val expenseCategories = uiState.categories.filter { it.type == TransactionType.EXPENSE }
        SetBudgetDialog(
            categories = expenseCategories,
            language = uiState.language,
            onDismiss = { showSetBudgetDialog = false },
            onSave = { catId, limit ->
                viewModel.saveBudget(catId, limit)
                showSetBudgetDialog = false
            }
        )
    }

    // Add Account Dialog
    if (showAddAccountDialog) {
        AddAccountDialog(
            language = uiState.language,
            onDismiss = { showAddAccountDialog = false },
            onSave = { nameEn, nameBn, type, initialBal, accNum ->
                viewModel.addAccount(nameEn, nameBn, type, initialBal, accNum)
                showAddAccountDialog = false
            }
        )
    }

    // AI Financial Advisor Bottom Sheet
    if (showAiAdvisorSheet) {
        AiAdvisorBottomSheet(
            uiState = uiState,
            onDismiss = { showAiAdvisorSheet = false }
        )
    }

    // In-App Update Dialog Alert (When new APK is released)
    var dismissedUpdatePrompt by remember { mutableStateOf(false) }
    val update = uiState.updateInfo
    val context = androidx.compose.ui.platform.LocalContext.current
    val downloadState by com.example.util.AppUpdateManager.downloadState.collectAsStateWithLifecycle()

    LaunchedEffect(update) {
        if (update?.hasUpdate == true) {
            dismissedUpdatePrompt = false
            com.example.util.AppUpdateManager.resetDownloadState()
        }
    }

    if (update != null && update.hasUpdate && !dismissedUpdatePrompt) {
        when (val dState = downloadState) {
            is com.example.util.UpdateDownloadState.Idle -> {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { dismissedUpdatePrompt = true },
                    icon = {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    title = {
                        androidx.compose.material3.Text(
                            text = if (isBn) "নতুন আপডেট এসেছে! (v${update.latestVersionName})" else "New Update Available! (v${update.latestVersionName})",
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    },
                    text = {
                        androidx.compose.foundation.layout.Column {
                            androidx.compose.material3.Text(
                                text = if (isBn) "একটি নতুন আপডেট ডাউনলোড ও ইনস্টল করার জন্য প্রস্তুত। নতুন পরিবর্তনসমূহ:" else "A new version of Taka Manager is available with the following improvements:",
                                fontSize = 13.sp
                            )
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                            androidx.compose.material3.Text(
                                text = update.releaseNotes.ifBlank { "Bug fixes and performance improvements." },
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    confirmButton = {
                        androidx.compose.material3.Button(
                            onClick = {
                                coroutineScope.launch {
                                    com.example.util.AppUpdateManager.startDownloadAndAutoInstall(context, update.downloadUrl)
                                }
                            }
                        ) {
                            androidx.compose.material3.Text(if (isBn) "এখনই আপডেট করুন" else "Update Now")
                        }
                    },
                    dismissButton = {
                        if (!update.isMandatory) {
                            androidx.compose.material3.TextButton(onClick = { dismissedUpdatePrompt = true }) {
                                androidx.compose.material3.Text(if (isBn) "পরে করব" else "Later")
                            }
                        }
                    }
                )
            }
            is com.example.util.UpdateDownloadState.Downloading -> {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { /* Keep visible during download */ },
                    icon = {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.padding(bottom = 4.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp
                        )
                    },
                    title = {
                        androidx.compose.material3.Text(
                            text = if (isBn) "আপডেট ডাউনলোড হচ্ছে..." else "Downloading Update...",
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    },
                    text = {
                        androidx.compose.foundation.layout.Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                        ) {
                            androidx.compose.material3.LinearProgressIndicator(
                                progress = { dState.progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                            androidx.compose.foundation.layout.Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                            ) {
                                androidx.compose.material3.Text(
                                    text = "${(dState.progress * 100).toInt()}%",
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                androidx.compose.material3.Text(
                                    text = "${dState.downloadedMb} / ${dState.totalMb}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            androidx.compose.material3.Text(
                                text = if (isBn) "ডাউনলোড শেষ হলে স্বয়ংক্রিয়ভাবে ইনস্টলেশন শুরু হবে।" else "Installation will start automatically when download completes.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        androidx.compose.material3.TextButton(
                            onClick = {
                                com.example.util.AppUpdateManager.resetDownloadState()
                                dismissedUpdatePrompt = true
                            }
                        ) {
                            androidx.compose.material3.Text(if (isBn) "বাতিল করুন" else "Cancel")
                        }
                    }
                )
            }
            is com.example.util.UpdateDownloadState.ReadyToInstall -> {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = {
                        dismissedUpdatePrompt = true
                        com.example.util.AppUpdateManager.resetDownloadState()
                    },
                    icon = {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    title = {
                        androidx.compose.material3.Text(
                            text = if (isBn) "ইনস্টল করার জন্য প্রস্তুত!" else "Ready to Install!",
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    },
                    text = {
                        androidx.compose.material3.Text(
                            text = if (isBn) "নতুন সংস্করণ ডাউনলোড সম্পন্ন হয়েছে। ইনস্টলেশন উইন্ডো চালু না হলে নিচের বাটনে ট্যাপ করুন।" else "Update downloaded. Tap the button below if installer didn't appear.",
                            fontSize = 13.sp
                        )
                    },
                    confirmButton = {
                        androidx.compose.material3.Button(
                            onClick = {
                                com.example.util.AppUpdateManager.installApk(context, dState.apkFile)
                            }
                        ) {
                            androidx.compose.material3.Text(if (isBn) "ইনস্টল সম্পন্ন করুন" else "Complete Install")
                        }
                    },
                    dismissButton = {
                        androidx.compose.material3.TextButton(
                            onClick = {
                                dismissedUpdatePrompt = true
                                com.example.util.AppUpdateManager.resetDownloadState()
                            }
                        ) {
                            androidx.compose.material3.Text(if (isBn) "বন্ধ করুন" else "Close")
                        }
                    }
                )
            }
            is com.example.util.UpdateDownloadState.Error -> {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = {
                        com.example.util.AppUpdateManager.resetDownloadState()
                    },
                    title = {
                        androidx.compose.material3.Text(
                            text = if (isBn) "ডাউনলোড সমস্যা" else "Download Failed",
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    },
                    text = {
                        androidx.compose.material3.Text(
                            text = "${dState.message}\n\n" + if (isBn) "আপনি চাইলে সরাসরি ব্রাউজার দিয়ে নতুন ভার্সন ডাউনলোড করে নিতে পারেন।" else "You can also download directly from browser.",
                            fontSize = 13.sp
                        )
                    },
                    confirmButton = {
                        androidx.compose.material3.Button(
                            onClick = {
                                com.example.util.AppUpdateManager.openBrowser(context, update.downloadUrl)
                                com.example.util.AppUpdateManager.resetDownloadState()
                                dismissedUpdatePrompt = true
                            }
                        ) {
                            androidx.compose.material3.Text(if (isBn) "ব্রাউজারে খুলুন" else "Open in Browser")
                        }
                    },
                    dismissButton = {
                        androidx.compose.material3.TextButton(
                            onClick = {
                                com.example.util.AppUpdateManager.resetDownloadState()
                            }
                        ) {
                            androidx.compose.material3.Text(if (isBn) "পুনরায় চেষ্টা" else "Retry")
                        }
                    }
                )
            }
        }
    }
}

private data class NavigationItemData(
    val tab: NavigationTab,
    val labelBn: String,
    val labelEn: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val tag: String
)
