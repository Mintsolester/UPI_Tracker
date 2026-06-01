package com.example

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                UpiPayTrackApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpiPayTrackApp() {
    val context = LocalContext.current
    val viewModel: UpiViewModel = viewModel()

    // Screen State: 0 = Payments, 1 = Budgets, 2 = Analyzer/Reports
    var currentTab by remember { mutableStateOf(0) }

    // Dialog triggering states
    var showAddPaymentDialog by remember { mutableStateOf(false) }
    var showAddBudgetDialog by remember { mutableStateOf(false) }
    var showAddExtendedBudgetDialog by remember { mutableStateOf(false) }
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var showAddSplitBillDialog by remember { mutableStateOf(false) }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    
    var fundingGoalSelected by remember { mutableStateOf<SharedGoal?>(null) }

    // Observed Flow States
    val payments by viewModel.allPayments.collectAsStateWithLifecycle()
    val budgets by viewModel.allBudgets.collectAsStateWithLifecycle()
    val budgetStatuses by viewModel.budgetStatuses.collectAsStateWithLifecycle()
    val extendedBudgets by viewModel.allExtendedBudgets.collectAsStateWithLifecycle()
    val extendedBudgetStatuses by viewModel.extendedBudgetStatuses.collectAsStateWithLifecycle()
    val members by viewModel.allMembers.collectAsStateWithLifecycle()
    val goals by viewModel.allGoals.collectAsStateWithLifecycle()
    val splitBills by viewModel.allSplitBills.collectAsStateWithLifecycle()
    val activeBillOwes by viewModel.activeBillOwes.collectAsStateWithLifecycle()
    val recommendations by viewModel.budgetRecommendations.collectAsStateWithLifecycle()
    val inAppNotifications by viewModel.inAppNotifications.collectAsStateWithLifecycle()

    // Notification Permission Handling
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            Toast.makeText(context, "Notifications enabled! You will now receive alert notifications close to limits.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Permission denied. Budget Warnings will appear inside the app only.", Toast.LENGTH_LONG).show()
        }
    }

    // Edge-to-edge standard scaffold
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column {
                Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

                // Sleek minimal header row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = "Wallet Icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "UPI PayTrack",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = when (currentTab) {
                                    0 -> "Personal Ledger"
                                    1 -> "Group Bill Splits"
                                    2 -> "Limits & Budgets"
                                    3 -> "Funding & Goals"
                                    4 -> "Smart Analytics"
                                    5 -> "Instruction Manual"
                                    else -> "Budget & Flow"
                                },
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "Fintech Suite",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val tabItems = listOf(
                        Triple("Ledger", Icons.Default.ReceiptLong, 0),
                        Triple("Splits", Icons.Default.Group, 1),
                        Triple("Budgets", Icons.Default.TrackChanges, 2),
                        Triple("Goals", Icons.Default.Stars, 3),
                        Triple("Analytics", Icons.Default.Analytics, 4),
                        Triple("Manual", Icons.Default.MenuBook, 5)
                    )
                    tabItems.forEach { (title, icon, index) ->
                        val selected = currentTab == index
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { currentTab = index }
                                .padding(vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = title,
                                    tint = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = title,
                                fontSize = 8.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            when (currentTab) {
                0 -> {
                    ExtendedFloatingActionButton(
                        text = { Text("Add payment") },
                        icon = { Icon(Icons.Default.Add, contentDescription = "Add Payment Icon") },
                        onClick = { showAddPaymentDialog = true },
                        modifier = Modifier.testTag("add_payment_fab"),
                        expanded = true
                    )
                }
                1 -> {
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ExtendedFloatingActionButton(
                            text = { Text("Add roommate") },
                            icon = { Icon(Icons.Default.PersonAdd, contentDescription = "Add Member Icon") },
                            onClick = { showAddMemberDialog = true },
                            expanded = true
                        )
                        ExtendedFloatingActionButton(
                            text = { Text("Split a bill") },
                            icon = { Icon(Icons.Default.Group, contentDescription = "Split Icon") },
                            onClick = { showAddSplitBillDialog = true },
                            expanded = true
                        )
                    }
                }
                2 -> {
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ExtendedFloatingActionButton(
                            text = { Text("Sub Budget") },
                            icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Set Budget Target") },
                            onClick = { showAddExtendedBudgetDialog = true },
                            expanded = true
                        )
                        ExtendedFloatingActionButton(
                            text = { Text("Set Budget") },
                            icon = { Icon(Icons.Default.TrackChanges, contentDescription = "Set Category Budget") },
                            onClick = { showAddBudgetDialog = true },
                            modifier = Modifier.testTag("add_budget_fab"),
                            expanded = true
                        )
                    }
                }
                3 -> {
                    ExtendedFloatingActionButton(
                        text = { Text("New Goal") },
                        icon = { Icon(Icons.Default.Stars, contentDescription = "New Goal Icon") },
                        onClick = { showAddGoalDialog = true },
                        expanded = true
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val totalSpentThisMonth = remember(payments) {
                payments.filter { isTimestampInCurrentMonth(it.timestamp) && it.isOutgoing }
                    .sumOf { it.amount }
            }
            val totalIncomeThisMonth = remember(payments) {
                payments.filter { isTimestampInCurrentMonth(it.timestamp) && !it.isOutgoing }
                    .sumOf { it.amount }
            }
            val totalBudgetAmount = remember(budgets) {
                budgets.sumOf { it.limitAmount }
            }

            // Core Switch Tabs Renderer
            when (currentTab) {
                0 -> PaymentsHub(
                    subTabSelector = 0,
                    paymentsList = payments,
                    membersList = members,
                    savingGoals = goals,
                    billOwes = activeBillOwes,
                    totalSpentThisMonth = totalSpentThisMonth,
                    totalIncomeThisMonth = totalIncomeThisMonth,
                    totalBudgetAmount = totalBudgetAmount,
                    onDelete = { viewModel.deletePayment(it) },
                    onAddMember = { showAddMemberDialog = true },
                    onDeleteMember = { viewModel.deleteGroupMember(it) },
                    onAddGoal = { showAddGoalDialog = true },
                    onAddGoalSavings = { fundingGoalSelected = it },
                    onDeleteGoal = { viewModel.deleteGoal(it) },
                    onSettleOwe = { oweId -> 
                        viewModel.settleOwe(oweId, true)
                        Toast.makeText(context, "Marked split owe as settled!", Toast.LENGTH_SHORT).show()
                    },
                    onDeleteSplitBill = { viewModel.deleteSplitBill(it) }
                )
                1 -> PaymentsHub(
                    subTabSelector = 1,
                    paymentsList = payments,
                    membersList = members,
                    savingGoals = goals,
                    billOwes = activeBillOwes,
                    totalSpentThisMonth = totalSpentThisMonth,
                    totalIncomeThisMonth = totalIncomeThisMonth,
                    totalBudgetAmount = totalBudgetAmount,
                    onDelete = { viewModel.deletePayment(it) },
                    onAddMember = { showAddMemberDialog = true },
                    onDeleteMember = { viewModel.deleteGroupMember(it) },
                    onAddGoal = { showAddGoalDialog = true },
                    onAddGoalSavings = { fundingGoalSelected = it },
                    onDeleteGoal = { viewModel.deleteGoal(it) },
                    onSettleOwe = { oweId -> 
                        viewModel.settleOwe(oweId, true)
                        Toast.makeText(context, "Marked split owe as settled!", Toast.LENGTH_SHORT).show()
                    },
                    onDeleteSplitBill = { viewModel.deleteSplitBill(it) }
                )
                2 -> BudgetsManager(
                    budgetStatuses = budgetStatuses,
                    extendedBudgetStatuses = extendedBudgetStatuses,
                    recommendations = recommendations,
                    onDeleteBudget = { viewModel.deleteBudget(it) },
                    onDeleteExtendedBudget = { viewModel.deleteExtendedBudget(it) },
                    onAcceptRecommendation = { rec ->
                        viewModel.setBudget(rec.category, rec.recommendedLimit)
                        Toast.makeText(context, "Added budget cap for ${rec.category} of ₹${rec.recommendedLimit}!", Toast.LENGTH_SHORT).show()
                    }
                )
                3 -> PaymentsHub(
                    subTabSelector = 2,
                    paymentsList = payments,
                    membersList = members,
                    savingGoals = goals,
                    billOwes = activeBillOwes,
                    totalSpentThisMonth = totalSpentThisMonth,
                    totalIncomeThisMonth = totalIncomeThisMonth,
                    totalBudgetAmount = totalBudgetAmount,
                    onDelete = { viewModel.deletePayment(it) },
                    onAddMember = { showAddMemberDialog = true },
                    onDeleteMember = { viewModel.deleteGroupMember(it) },
                    onAddGoal = { showAddGoalDialog = true },
                    onAddGoalSavings = { fundingGoalSelected = it },
                    onDeleteGoal = { viewModel.deleteGoal(it) },
                    onSettleOwe = { oweId -> 
                        viewModel.settleOwe(oweId, true)
                        Toast.makeText(context, "Marked split owe as settled!", Toast.LENGTH_SHORT).show()
                    },
                    onDeleteSplitBill = { viewModel.deleteSplitBill(it) }
                )
                4 -> ReportAnalyzer(
                    paymentsList = payments,
                    extendedBudgetStatuses = extendedBudgetStatuses,
                    onExportClick = { offset ->
                        viewModel.triggerShareReport(context, offset)
                    }
                )
                5 -> InstructionManualHub()
            }

            // In-app notifications overlay panel stacking
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = "Notification Prompt",
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Enable system alerts?",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Text(
                                        text = "Receive notifications whenever you exceed budget levels.",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }
                            Button(
                                onClick = { requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Enable", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }

                inAppNotifications.take(2).forEach { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                            .clickable { viewModel.removeInAppNotification(item.id) },
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Alert Icon",
                                    tint = if (item.title.contains("Exceeded")) MaterialTheme.colorScheme.error else Color(0xFFF57C00),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = item.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = item.message,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                }
                            }
                            IconButton(
                                onClick = { viewModel.removeInAppNotification(item.id) },
                                modifier = Modifier.size(18.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close Warning", modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal dialogues triggering rendering
    if (showAddPaymentDialog) {
        AddPaymentDialog(
            onDismiss = { showAddPaymentDialog = false },
            onConfirm = { payee, upi, amt, cat, desc, isOutgoing ->
                viewModel.addPayment(payee, upi, amt, cat, desc, isOutgoing = isOutgoing)
                showAddPaymentDialog = false
            }
        )
    }

    if (showAddBudgetDialog) {
        AddBudgetDialog(
            onDismiss = { showAddBudgetDialog = false },
            onConfirm = { cat, lim ->
                viewModel.setBudget(cat, lim)
                showAddBudgetDialog = false
            }
        )
    }

    if (showAddExtendedBudgetDialog) {
        AddExtendedBudgetDialog(
            onDismiss = { showAddExtendedBudgetDialog = false },
            onConfirm = { type, category, limitAmount, label, start, end ->
                viewModel.addExtendedBudget(type, category, limitAmount, label, start, end)
                showAddExtendedBudgetDialog = false
            }
        )
    }

    if (showAddMemberDialog) {
        AddGroupMemberDialog(
            onDismiss = { showAddMemberDialog = false },
            onConfirm = { name, upi ->
                viewModel.addGroupMember(name, upi)
                showAddMemberDialog = false
                Toast.makeText(context, "Member $name registered!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showAddSplitBillDialog) {
        AddSplitBillDialog(
            members = members,
            onDismiss = { showAddSplitBillDialog = false },
            onConfirm = { title, amount, payerId, selectedDebtors ->
                val owes = selectedDebtors.map { debtorId ->
                    debtorId to (amount / (selectedDebtors.size + 1))
                }
                viewModel.addSplitBill(title, amount, payerId, owes)
                showAddSplitBillDialog = false
                Toast.makeText(context, "Split recorded successfully among ${selectedDebtors.size + 1} users!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showAddGoalDialog) {
        AddSavingGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onConfirm = { title, target, saved, date ->
                viewModel.addSharedGoal(title, target, saved, date)
                showAddGoalDialog = false
                Toast.makeText(context, "Shared saving goal created!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    fundingGoalSelected?.let { goal ->
        AddFundGoalDialog(
            goal = goal,
            onDismiss = { fundingGoalSelected = null },
            onConfirm = { addedSavings ->
                viewModel.updateGoalSaving(goal, goal.savedAmount + addedSavings)
                fundingGoalSelected = null
                Toast.makeText(context, "Savings added to ${goal.title}!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

// ==================== QUICK SUMMARY CARD ====================
@Composable
fun QuickSummaryCard(
    totalSpentThisMonth: Double,
    totalIncomeThisMonth: Double,
    totalBudgetAmount: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = "Wallet Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Monthly Flow",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Real-time ledger overview",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Active Status",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Spent This Month",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "₹%,.2f", totalSpentThisMonth),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Income Generated",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "₹%,.0f", totalIncomeThisMonth),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                }
            }

            if (totalBudgetAmount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                val overallUsage = (totalSpentThisMonth / totalBudgetAmount).toFloat().coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { overallUsage },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (overallUsage >= 1.0f) MaterialTheme.colorScheme.error 
                            else if (overallUsage >= 0.8f) Color(0xFFF57C00) 
                            else if (overallUsage >= 0.5f) Color(0xFFFBC02D)
                            else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.inverseOnSurface
                )
            }
        }
    }
}

// ==================== PAYMENTS HUB (TAB 0) ====================
@Composable
fun PaymentsHub(
    subTabSelector: Int,
    paymentsList: List<UpiPayment>,
    membersList: List<GroupMember>,
    savingGoals: List<SharedGoal>,
    billOwes: List<GroupOweDetail>,
    totalSpentThisMonth: Double,
    totalIncomeThisMonth: Double,
    totalBudgetAmount: Double,
    onDelete: (UpiPayment) -> Unit,
    onAddMember: () -> Unit,
    onDeleteMember: (Long) -> Unit,
    onAddGoal: () -> Unit,
    onAddGoalSavings: (SharedGoal) -> Unit,
    onDeleteGoal: (Long) -> Unit,
    onSettleOwe: (Long) -> Unit,
    onDeleteSplitBill: (Long) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterCategory by remember { mutableStateOf<String?>(null) }

    val filteredPayments = remember(paymentsList, searchQuery, selectedFilterCategory) {
        paymentsList.filter {
            val matchesSearch = it.payeeName.contains(searchQuery, ignoreCase = true) ||
                    (it.payeeUpiId?.contains(searchQuery, ignoreCase = true) == true) ||
                    (it.category.contains(searchQuery, ignoreCase = true))
            
            val matchesCategory = selectedFilterCategory == null || it.category.equals(selectedFilterCategory, ignoreCase = true)
            matchesSearch && matchesCategory
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(6.dp))

        when (subTabSelector) {
            0 -> {
                // Personal ledger view containing search & categories filter
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search transactions...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("payments_search_bar"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Scrollable category filter pills
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    item {
                        ElevatedFilterChip(
                            selected = selectedFilterCategory == null,
                            onClick = { selectedFilterCategory = null },
                            label = { Text("All") }
                        )
                    }
                    items(CategoryRegistry.categories) { cat ->
                        ElevatedFilterChip(
                            selected = selectedFilterCategory == cat.name,
                            onClick = { selectedFilterCategory = cat.name },
                            label = { Text(cat.name) },
                            leadingIcon = {
                                Icon(cat.icon, contentDescription = null, tint = cat.color, modifier = Modifier.size(14.dp))
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f).testTag("payment_list"),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        QuickSummaryCard(
                            totalSpentThisMonth = totalSpentThisMonth,
                            totalIncomeThisMonth = totalIncomeThisMonth,
                            totalBudgetAmount = totalBudgetAmount
                        )
                    }
                    if (filteredPayments.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.ReceiptLong,
                                        contentDescription = "Empty",
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("No records matched.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                }
                            }
                        }
                    } else {
                        items(filteredPayments) { payment ->
                            PaymentCard(payment = payment, onDelete = { onDelete(payment) })
                        }
                    }
                }
            }

            1 -> {
                // Bill Splits & Friend Settlements Section
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // HORIZONTAL MEMBERS BAR
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Roommates & Family", 
                                            fontWeight = FontWeight.Bold, 
                                            fontSize = 13.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(onClick = onAddMember, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Default.PersonAdd, contentDescription = "Add Member", modifier = Modifier.size(18.dp))
                                        }
                                    }
                                Spacer(modifier = Modifier.height(8.dp))

                                if (membersList.isEmpty()) {
                                    Text("No members registered. Tap '+' icon to add some!", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                } else {
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        items(membersList) { member ->
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.width(60.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = member.name.take(1).uppercase(),
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = member.name,
                                                    fontSize = 11.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "Delete",
                                                    fontSize = 9.sp,
                                                    color = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.clickable { onDeleteMember(member.id) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ACTIVE DUE SETTLEMENTS LIST
                    item {
                        Text(
                            text = "Group Balances & Settlements",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    val ongoingOwes = billOwes.filter { !it.isSettled }
                    if (ongoingOwes.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.DoneAll, contentDescription = null, tint = Color(0xFF4CAF50))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("Everyone is fully settled up! No active debts.", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    } else {
                        items(ongoingOwes) { owe ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = owe.billTitle,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "${owe.debtorName} owes ₹${String.format("%.2f", owe.amount)} to ${owe.creditorName}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (owe.creditorUpi != null) {
                                            Text(
                                                text = "UPI: ${owe.creditorUpi}",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    
                                    Button(
                                        onClick = { onSettleOwe(owe.oweId) },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                        modifier = Modifier.height(32.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Settle", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // Shared Saving Goals
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Saving Targets & Goals", 
                                fontWeight = FontWeight.Bold, 
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f).padding(end = 6.dp)
                            )
                            Button(
                                onClick = onAddGoal, 
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp), 
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("New Goal", fontSize = 10.sp)
                            }
                        }
                    }

                    if (savingGoals.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Stars,
                                        contentDescription = "Empty goals",
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Configure a shared goal for vacations or investments!", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                }
                            }
                        }
                    } else {
                        items(savingGoals) { goal ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(goal.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        IconButton(onClick = { onDeleteGoal(goal.id) }, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete Goal", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    val progress = if (goal.targetAmount > 0) (goal.savedAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f) else 0f
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Saved: ₹${goal.savedAmount} / ₹${goal.targetAmount}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("${Math.round(progress * 100)}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                        color = if (progress >= 1.0f) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val daysLeft = ((goal.targetDate - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).coerceIn(0L..1000L).toInt()
                                        Text("Days left: $daysLeft", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        
                                        Button(
                                            onClick = { onAddGoalSavings(goal) },
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text("Adjust funding", fontSize = 10.sp)
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

@Composable
fun PaymentCard(
    payment: UpiPayment,
    onDelete: () -> Unit
) {
    val categoryColor = CategoryRegistry.getColor(payment.category)
    val categoryIcon = CategoryRegistry.getIcon(payment.category)
    val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (payment.isOutgoing) MaterialTheme.colorScheme.surface 
                             else Color(0xFFE8F5E9).copy(alpha = 0.6f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            if (payment.isOutgoing) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f) 
            else Color(0xFFC8E6C9)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (payment.isOutgoing) categoryColor.copy(alpha = 0.12f) else Color(0xFF4CAF50).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (payment.isOutgoing) categoryIcon else Icons.Default.ArrowDownward,
                        contentDescription = payment.category,
                        tint = if (payment.isOutgoing) categoryColor else Color(0xFF4CAF50),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = payment.payeeName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${payment.category} • ${dateFormat.format(Date(payment.timestamp))}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!payment.description.isNullOrEmpty()) {
                        Text(
                            text = payment.description,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${if (payment.isOutgoing) "-" else "+"}₹${String.format(Locale.getDefault(), "%,.0f", payment.amount)}",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = if (payment.isOutgoing) MaterialTheme.colorScheme.onSurface else Color(0xFF2E7D32)
                )
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Payment",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}


// ==================== AGGREGATE BUDGETS & TRACKING (TAB 1) ====================
@Composable
fun BudgetsManager(
    budgetStatuses: List<BudgetStatus>,
    extendedBudgetStatuses: List<ExtendedBudgetStatus>,
    recommendations: List<BudgetRecommendation>,
    onDeleteBudget: (CategoryBudget) -> Unit,
    onDeleteExtendedBudget: (Long) -> Unit,
    onAcceptRecommendation: (BudgetRecommendation) -> Unit
) {
    var plannerTab by remember { mutableStateOf(0) } // 0 = Category caps, 1 = Weekly/Custom, 2 = Intelligent Recommendations

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = plannerTab == 0,
                onClick = { plannerTab = 0 },
                label = { Text("Category Budgets") }
            )
            FilterChip(
                selected = plannerTab == 1,
                onClick = { plannerTab = 1 },
                label = { Text("Period/Weekly Caps") }
            )
            FilterChip(
                selected = plannerTab == 2,
                onClick = { plannerTab = 2 },
                label = { Text("Smart Suggestions") }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (plannerTab) {
            0 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f).testTag("budget_list"),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (budgetStatuses.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.TrackChanges,
                                        contentDescription = "Empty",
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("No monthly category budget rules defined.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                }
                            }
                        }
                    } else {
                        items(budgetStatuses) { status ->
                            BudgetCard(status = status, onDelete = {
                                onDeleteBudget(CategoryBudget(category = status.category, limitAmount = status.limitAmount))
                            })
                        }
                    }
                }
            }

            1 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (extendedBudgetStatuses.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = "Empty",
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Record a custom period cap (weekly, trip overall or general).", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                }
                            }
                        }
                    } else {
                        items(extendedBudgetStatuses) { status ->
                            ExtBudgetCard(status = status, onDelete = { onDeleteExtendedBudget(status.id) })
                        }
                    }
                }
            }

            2 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("💡 Intelligent Budget Formulation", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Our systems evaluate your payments ledger to formulate suggested monthly allowance limits with integrated risk margins.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    if (recommendations.isEmpty()) {
                        item {
                            Text("Record some ledger transactions to produce automated suggestions.", modifier = Modifier.padding(12.dp), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        items(recommendations) { rec ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Recommendation for ${rec.category}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(rec.reason, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Suggested limit: ₹${rec.recommendedLimit}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                                        Button(
                                            onClick = { onAcceptRecommendation(rec) },
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text("Apply standard", fontSize = 10.sp)
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

@Composable
fun BudgetCard(
    status: BudgetStatus,
    onDelete: () -> Unit
) {
    val categoryColor = CategoryRegistry.getColor(status.category)
    val categoryIcon = CategoryRegistry.getIcon(status.category)

    val progressValue = status.percentageUsed.coerceIn(0f, 1f)
    val levelColor = when (status.statusLevel) {
        BudgetLevel.EXCEEDED -> MaterialTheme.colorScheme.error
        BudgetLevel.WARNING_80 -> Color(0xFFF57C00)
        BudgetLevel.WARNING_50 -> Color(0xFFFBC02D)
        BudgetLevel.NORMAL -> MaterialTheme.colorScheme.primary
    }

    val fractionText = String.format(Locale.getDefault(), "₹%,.0f / ₹%,.0f", status.spentAmount, status.limitAmount)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("budget_item_${status.category}"),
        colors = CardDefaults.cardColors(
            containerColor = levelColor.copy(alpha = 0.04f)
        ),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, levelColor.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(categoryColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = status.category,
                            tint = categoryColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = status.category,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(levelColor.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = when (status.statusLevel) {
                                BudgetLevel.EXCEEDED -> "LIMIT REACHED"
                                BudgetLevel.WARNING_80 -> "80% SPENT"
                                BudgetLevel.WARNING_50 -> "50% SPENT"
                                BudgetLevel.NORMAL -> "HEALTHY"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = levelColor
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Budget cap",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Percentage indicator meter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "Ratio used: ${Math.round(status.percentageUsed * 100)}%",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = fractionText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { progressValue },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = levelColor,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
            )
        }
    }
}

@Composable
fun ExtBudgetCard(
    status: ExtendedBudgetStatus,
    onDelete: () -> Unit
) {
    val levelColor = when (status.statusLevel) {
        BudgetLevel.EXCEEDED -> MaterialTheme.colorScheme.error
        BudgetLevel.WARNING_80 -> Color(0xFFF71C00)
        BudgetLevel.WARNING_50 -> Color(0xFFFBC02D)
        BudgetLevel.NORMAL -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = levelColor.copy(alpha = 0.03f)),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, levelColor.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(status.periodLabel, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Type: ${status.type} ${if (!status.category.isNullOrEmpty()) "(${status.category})" else ""}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(levelColor.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(status.statusLevel.name, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = levelColor)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val progress = status.percentageUsed.coerceIn(0f, 1f)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Spent: ₹${status.spentAmount} of ₹${status.limitAmount}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Text("${Math.round(status.percentageUsed * 100)}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = levelColor)
            }

            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = levelColor,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
            )

            Spacer(modifier = Modifier.height(6.dp))
            Text("Days remaining: ${status.daysRemaining}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}


// ==================== VISUAL ANALYTICS & DASHBOARD (TAB 2) ====================
@Composable
fun ReportAnalyzer(
    paymentsList: List<UpiPayment>,
    extendedBudgetStatuses: List<ExtendedBudgetStatus>,
    onExportClick: (Int) -> Unit
) {
    var monthOffset by remember { mutableStateOf(0) } // 0 = current Month, 1 = previous Month

    val calendar = Calendar.getInstance()
    calendar.add(Calendar.MONTH, -monthOffset)
    val targetMonthNum = calendar.get(Calendar.MONTH)
    val targetYearNum = calendar.get(Calendar.YEAR)

    // Filter payments in the selected month
    val paymentsInMonth = remember(paymentsList, monthOffset) {
        paymentsList.filter {
            val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            cal.get(Calendar.MONTH) == targetMonthNum && cal.get(Calendar.YEAR) == targetYearNum
        }
    }

    val totalOutgoing = remember(paymentsInMonth) {
        paymentsInMonth.filter { it.isOutgoing }.sumOf { it.amount }
    }
    val totalIncoming = remember(paymentsInMonth) {
        paymentsInMonth.filter { !it.isOutgoing }.sumOf { it.amount }
    }
    val netSavings = totalIncoming - totalOutgoing
    val savingRate = if (totalIncoming > 0) ((netSavings / totalIncoming) * 100).toInt() else 0

    // Financial Health calculation
    val healthScore = remember(totalIncoming, totalOutgoing) {
        if (totalIncoming == 0.0) {
            if (totalOutgoing == 0.0) 100 else 40
        } else {
            val ratio = totalOutgoing / totalIncoming
            when {
                ratio <= 0.3 -> 95
                ratio <= 0.5 -> 85
                ratio <= 0.7 -> 70
                ratio <= 0.9 -> 50
                else -> 25
            }
        }
    }

    // Move remember targets outside of the LazyColumn DSL context to avoid Composable context validation errors
    val transactionsByDay = remember(paymentsInMonth) {
        val dailyMap = mutableMapOf<Int, Pair<Double, Double>>() // Day -> In, Out
        paymentsInMonth.forEach { p ->
            val cal = Calendar.getInstance().apply { timeInMillis = p.timestamp }
            val day = cal.get(Calendar.DAY_OF_MONTH)
            val current = dailyMap[day] ?: (0.0 to 0.0)
            dailyMap[day] = if (p.isOutgoing) {
                current.first to (current.second + p.amount)
            } else {
                (current.first + p.amount) to current.second
            }
        }
        dailyMap.toSortedMap()
    }

    val categoryGroups = remember(paymentsInMonth) {
        paymentsInMonth.filter { it.isOutgoing }
            .groupBy { it.category }
            .mapValues { it.value.sumOf { p -> p.amount } }
            .toList()
            .sortedByDescending { it.second }
    }

    val weekendSpent = remember(paymentsInMonth) {
        paymentsInMonth.filter { p ->
            if (!p.isOutgoing) return@filter false
            val cal = Calendar.getInstance().apply { timeInMillis = p.timestamp }
            val day = cal.get(Calendar.DAY_OF_WEEK)
            day == Calendar.SATURDAY || day == Calendar.SUNDAY
        }.sumOf { it.amount }
    }

    val subscriptionSpent = remember(paymentsInMonth) {
        paymentsInMonth.filter { p ->
            p.isOutgoing && (p.category.equals("Bills & Utilities", true) || 
            p.description?.contains("sub", true) == true || 
            p.description?.contains("netflix", true) == true ||
            p.description?.contains("youtube", true) == true)
        }.sumOf { it.amount }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))

            // Month switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (monthOffset == 0) "Current Month Analysis" else "Previous Month History",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ElevatedFilterChip(
                        selected = monthOffset == 0,
                        onClick = { monthOffset = 0 },
                        label = { Text("Current") }
                    )
                    ElevatedFilterChip(
                        selected = monthOffset == 1,
                        onClick = { monthOffset = 1 },
                        label = { Text("Previous") }
                    )
                }
            }
        }

        // METRICS DASHBOARD CARD
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Monthly Financial Dashboard", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Outgoings", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹${String.format("%,.2f", totalOutgoing)}", fontWeight = FontWeight.Black, fontSize = 18.sp, color = MaterialTheme.colorScheme.error)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Inflow/Income", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹${String.format("%,.2f", totalIncoming)}", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF4CAF50))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Net Savings Rate", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$savingRate%", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Score Rating", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$healthScore / 100", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (healthScore >= 70) Color(0xFF4CAF50) else Color(0xFFE53935))
                        }
                    }
                }
            }
        }

        // CASH FLOW TIMELINE VISUAL LINE CHART (Canvas-drawn)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Cash Flow Curve", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF4CAF50)))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("In", fontSize = 9.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFF44336)))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Out", fontSize = 9.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .padding(vertical = 4.dp)
                    ) {
                        if (transactionsByDay.isEmpty()) {
                            Text("No timeline data.", modifier = Modifier.align(Alignment.Center), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val chartWidth = size.width
                                val chartHeight = size.height
                                val maxVal = transactionsByDay.values.maxOfOrNull { Math.max(it.first, it.second) } ?: 1.0
                                val daysList = transactionsByDay.keys.toList()
                                if (daysList.size > 1) {
                                    val dx = chartWidth / (daysList.size - 1)
                                    val inPath = Path()
                                    val outPath = Path()

                                    daysList.forEachIndexed { idx, day ->
                                        val entry = transactionsByDay[day] ?: (0.0 to 0.0)
                                        val pyIn = chartHeight - ((entry.first / maxVal) * chartHeight).toFloat()
                                        val pyOut = chartHeight - ((entry.second / maxVal) * chartHeight).toFloat()

                                        if (idx == 0) {
                                            inPath.moveTo(0f, pyIn)
                                            outPath.moveTo(0f, pyOut)
                                        } else {
                                            inPath.lineTo(idx * dx, pyIn)
                                            outPath.lineTo(idx * dx, pyOut)
                                        }
                                    }

                                    drawPath(inPath, color = Color(0xFF4CAF50), style = Stroke(width = 5f))
                                    drawPath(outPath, color = Color(0xFFF44336), style = Stroke(width = 5f))
                                } else {
                                    // single day transaction line point
                                    drawCircle(color = Color(0xFF4CAF50), radius = 10f, center = Offset(chartWidth/2, chartHeight/2))
                                }
                            }
                        }
                    }
                    Text("Days of Month scale (Inflow vs Outgoings balance)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                }
            }
        }

        // CATEGORY BREAKDOWN LISTINGS
        item {
            Text("Group Spending by Category", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        if (categoryGroups.isEmpty()) {
            item {
                Text("No payments found for categorization.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            items(categoryGroups) { (cat, amt) ->
                val ratio = if (totalOutgoing > 0) (amt / totalOutgoing).toFloat() else 0f
                val color = CategoryRegistry.getColor(cat)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(cat, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("₹${String.format("%,.0f", amt)} (${Math.round(ratio * 100)}%)", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { ratio },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = color,
                            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                        )
                    }
                }
            }
        }

        // SPENDING PATTERNS IDENTIFIERS
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🎯 Advanced Habits & Patterns", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Weekend Spends", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("₹${String.format("%,.0f", weekendSpent)}", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Subscription Services", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("₹${String.format("%,.0f", subscriptionSpent)}", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }

        // ACTION EXPORTS
        item {
            Box(
                modifier = Modifier
                    .fillPadding()
                    .padding(vertical = 16.dp)
            ) {
                Button(
                    onClick = { onExportClick(monthOffset) },
                    modifier = Modifier.fillMaxWidth().testTag("export_csv_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share CSVIcon"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Share Certified Monthly CSV Report",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

private fun Modifier.fillPadding() = this.fillMaxWidth()


// ==================== MODAL DIALOG COMPONENTS ====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPaymentDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String?, Double, String, String?, Boolean) -> Unit
) {
    var payeeName by remember { mutableStateOf("") }
    var payeeUpiId by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(CategoryRegistry.categories.first().name) }
    var description by remember { mutableStateOf("") }
    var isOutgoing by remember { mutableStateOf(true) } // true = Send Payment, false = Receive Income

    var isMenuExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .animateContentSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (isOutgoing) "Record UPI Outflow" else "Log Influx Income",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Outgoing vs Incoming switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ElevatedFilterChip(
                        selected = isOutgoing,
                        onClick = { isOutgoing = true },
                        label = { Text("Expense", fontSize = 11.sp) }
                    )
                    ElevatedFilterChip(
                        selected = !isOutgoing,
                        onClick = { isOutgoing = false },
                        label = { Text("Income", fontSize = 11.sp) }
                    )
                }

                OutlinedTextField(
                    value = payeeName,
                    onValueChange = { payeeName = it },
                    label = { Text(if (isOutgoing) "Payee / Merchant Name *" else "Income Payer *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("payee_name_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = payeeUpiId,
                    onValueChange = { payeeUpiId = it },
                    label = { Text("UPI ID (Optional)") },
                    placeholder = { Text("e.g. pay@upi") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (INR) *") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("amount_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Category *", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = isMenuExpanded,
                    onExpandedChange = { isMenuExpanded = !isMenuExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isMenuExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = isMenuExpanded,
                        onDismissRequest = { isMenuExpanded = false }
                    ) {
                        CategoryRegistry.categories.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(option.icon, contentDescription = null, tint = option.color)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(option.name)
                                    }
                                },
                                onClick = {
                                    selectedCategory = option.name
                                    isMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Memo description") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = amountText.toDoubleOrNull() ?: 0.0
                            if (payeeName.isBlank()) payeeName = "General UPI"
                            if (amt > 0) {
                                onConfirm(payeeName, payeeUpiId.ifBlank { null }, amt, selectedCategory, description.ifBlank { null }, isOutgoing)
                            }
                        },
                        enabled = payeeName.isNotBlank() && (amountText.toDoubleOrNull() ?: 0.0) > 0.0
                    ) {
                        Text("Save log")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBudgetDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(CategoryRegistry.categories.first().name) }
    var limitText by remember { mutableStateOf("") }
    var isMenuExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .animateContentSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Configure Budget Cap",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text("Select Category *", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = isMenuExpanded,
                    onExpandedChange = { isMenuExpanded = !isMenuExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isMenuExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = isMenuExpanded,
                        onDismissRequest = { isMenuExpanded = false }
                    ) {
                        CategoryRegistry.categories.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(option.icon, contentDescription = null, tint = option.color)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(option.name)
                                    }
                                },
                                onClick = {
                                    selectedCategory = option.name
                                    isMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = limitText,
                    onValueChange = { limitText = it },
                    label = { Text("Allowance Limit (INR) *") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("budget_limit_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val lim = limitText.toDoubleOrNull() ?: 0.0
                            if (lim > 0.0) {
                                onConfirm(selectedCategory, lim)
                            }
                        },
                        enabled = (limitText.toDoubleOrNull() ?: 0.0) > 0.0
                    ) {
                        Text("Active cap")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExtendedBudgetDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String?, Double, String, Long, Long) -> Unit
) {
    var type by remember { mutableStateOf("WEEKLY") } // WEEKLY, OVERALL, CUSTOM
    var category by remember { mutableStateOf<String?>("") }
    var limitText by remember { mutableStateOf("") }
    var periodLabel by remember { mutableStateOf("") }
    var isMenuExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .animateContentSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Text("New Extended/Period Cap", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))

                // Selector chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ElevatedFilterChip(
                        selected = type == "WEEKLY",
                        onClick = { type = "WEEKLY"; periodLabel = "Weekly Allowance" },
                        label = { Text("Weekly") }
                    )
                    ElevatedFilterChip(
                        selected = type == "OVERALL",
                        onClick = { type = "OVERALL"; periodLabel = "Overall Month Cap" },
                        label = { Text("Overall") }
                    )
                    ElevatedFilterChip(
                        selected = type == "CUSTOM",
                        onClick = { type = "CUSTOM"; periodLabel = "Trip / Custom Period" },
                        label = { Text("Custom Period") }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = periodLabel,
                    onValueChange = { periodLabel = it },
                    label = { Text("Budget Label / Title *") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = limitText,
                    onValueChange = { limitText = it },
                    label = { Text("Limit Amount (INR) *") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Select category bound (optional)
                Text("Category Bound (Optional)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = isMenuExpanded,
                    onExpandedChange = { isMenuExpanded = !isMenuExpanded }
                ) {
                    OutlinedTextField(
                        value = category ?: "General Overall",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isMenuExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = isMenuExpanded,
                        onDismissRequest = { isMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("No category bound (Overall)") },
                            onClick = { category = null; isMenuExpanded = false }
                        )
                        CategoryRegistry.categories.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.name) },
                                onClick = { category = option.name; isMenuExpanded = false }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val lim = limitText.toDoubleOrNull() ?: 0.0
                            val daysRange = if (type == "WEEKLY") 7 else 31
                            val now = System.currentTimeMillis()
                            val end = now + (daysRange * 24L * 60 * 60 * 1000)

                            if (lim > 0) {
                                onConfirm(type, category, lim, periodLabel, now, end)
                            }
                        },
                        enabled = periodLabel.isNotBlank() && (limitText.toDoubleOrNull() ?: 0.0) > 0
                    ) {
                        Text("Add Target")
                    }
                }
            }
        }
    }
}

@Composable
fun AddGroupMemberDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var upi by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Register Group Member", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name *") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = upi,
                    onValueChange = { upi = it },
                    label = { Text("UPI ID (Optional for payout)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onConfirm(name, upi.ifBlank { null }) }, enabled = name.isNotBlank()) {
                        Text("Add")
                    }
                }
            }
        }
    }
}

@Composable
fun AddSplitBillDialog(
    members: List<GroupMember>,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Long, List<Long>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var totalAmountText by remember { mutableStateOf("") }
    var payerId by remember { mutableStateOf(0L) }
    var selectedDebtors = remember { mutableStateListOf<Long>() }

    var isPayerMenuExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            LazyColumn(modifier = Modifier.padding(16.dp)) {
                item {
                    Text("Split UPI Transfer / Bill", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Bill Title (e.g. Dinner, Pizza) *") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = totalAmountText,
                        onValueChange = { totalAmountText = it },
                        label = { Text("Total Bill Amount (INR) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Paid By *", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                }

                item {
                    if (members.isEmpty()) {
                        Text("No members registered yet. Save roommate names first!", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                    } else {
                        Button(onClick = { isPayerMenuExpanded = true }) {
                            val payer = members.find { it.id == payerId }
                            Text("Payer: ${payer?.name ?: "Select Payer"}")
                        }

                        if (isPayerMenuExpanded) {
                            Dialog(onDismissRequest = { isPayerMenuExpanded = false }) {
                                Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.padding(16.dp)) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Select who paid", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        members.forEach { m ->
                                            Text(
                                                text = m.name,
                                                modifier = Modifier.fillMaxWidth().clickable {
                                                    payerId = m.id
                                                    isPayerMenuExpanded = false
                                                }.padding(8.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Select who splits * (Equally distributed)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                items(members) { m ->
                    if (m.id != payerId) {
                        val isChecked = selectedDebtors.contains(m.id)
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable {
                                if (isChecked) selectedDebtors.remove(m.id) else selectedDebtors.add(m.id)
                            }.padding(horizontal = 4.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = {
                                    if (isChecked) selectedDebtors.remove(m.id) else selectedDebtors.add(m.id)
                                }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(m.name, fontSize = 13.sp)
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val amt = totalAmountText.toDoubleOrNull() ?: 0.0
                                if (amt > 0 && payerId != 0L && selectedDebtors.isNotEmpty()) {
                                    onConfirm(title, amt, payerId, selectedDebtors.toList())
                                }
                            },
                            enabled = title.isNotBlank() && (totalAmountText.toDoubleOrNull() ?: 0.0) > 0 && selectedDebtors.isNotEmpty() && payerId != 0L
                        ) {
                            Text("Distribute & save")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddSavingGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Double, Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var targetText by remember { mutableStateOf("") }
    var currentText by remember { mutableStateOf("0") }
    var weeksRangeText by remember { mutableStateOf("12") } // Standard 12 weeks timeline

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Create Saving Target", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Goal Title (e.g. Scooter fund) *") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it },
                    label = { Text("Target Amount (INR) *") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = currentText,
                    onValueChange = { currentText = it },
                    label = { Text("Initial Saved Balance (INR)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = weeksRangeText,
                    onValueChange = { weeksRangeText = it },
                    label = { Text("Timeline timeframe (weeks)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val tar = targetText.toDoubleOrNull() ?: 0.0
                            val cur = currentText.toDoubleOrNull() ?: 0.0
                            val wks = weeksRangeText.toLongOrNull() ?: 12
                            val targetDate = System.currentTimeMillis() + (wks * 7 * 24L * 60 * 60 * 1000)

                            if (tar > 0) {
                                onConfirm(title, tar, cur, targetDate)
                            }
                        },
                        enabled = title.isNotBlank() && (targetText.toDoubleOrNull() ?: 0.0) > 0
                    ) {
                        Text("Launch Goal")
                    }
                }
            }
        }
    }
}

@Composable
fun AddFundGoalDialog(
    goal: SharedGoal,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amountText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Inject Funding to ${goal.title}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Currently saved: ₹${goal.savedAmount} of target ₹${goal.targetAmount}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Added Savings Amount (INR) *") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = amountText.toDoubleOrNull() ?: 0.0
                            if (amt > 0) {
                                onConfirm(amt)
                            }
                        },
                        enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0
                    ) {
                        Text("Add capital")
                    }
                }
            }
        }
    }
}

private fun isTimestampInCurrentMonth(timestamp: Long): Boolean {
    val cal1 = Calendar.getInstance()
    val cal2 = Calendar.getInstance()
    cal2.timeInMillis = timestamp
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstructionManualHub() {
    val categoriesList = listOf("Personal Ledger", "Bill Splitter", "Limits & Budgets", "Communal Goals", "Analytics Tool")
    var activeCategory by remember { mutableStateOf("Personal Ledger") }

    // Sandbox states for live tutorial
    var mockFoodSpent by remember { mutableStateOf(350.0) }
    var mockFoodLimit by remember { mutableStateOf(1000.0) }
    var mockSalaryInflow by remember { mutableStateOf(0.0) }
    var logList by remember { mutableStateOf(listOf("Welcome to the Interactive Sandbox! Try clicking a button below to learn how alerts trigger.")) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcoming card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = "Idea Icon",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "UPI PayTrack User Manual",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Welcome! This onboarding hub teaches you how to manage budgets, settle debts, split group transfers, and analyze cash flows. Select a module below to view guidelines or use the Sandbox Playground to try it live!",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                    lineHeight = 15.sp
                )
            }
        }

        // Horizontal Category Tab Selector
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categoriesList) { cat ->
                val selected = cat == activeCategory
                ElevatedFilterChip(
                    selected = selected,
                    onClick = { activeCategory = cat },
                    label = { Text(cat, fontSize = 11.sp) }
                )
            }
        }

        // Selected topic content
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                when (activeCategory) {
                    "Personal Ledger" -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("1. Personal Ledger Module", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Text("• Record Inflows & Outflows: Tap \"Add Payment\" in your main screen to log salary inflows or daily UPI outflows.", fontSize = 11.sp)
                        Text("• Real-Time Bookkeeper: Dynamic charts compute net-worth changes instantly. Inflows are green while outflows are displayed as high-contrast red.", fontSize = 11.sp)
                        Text("• Categorization of Outlays: Assign every transfer to Food, Leisure, Rent, Health, Utilities, etc. tags.", fontSize = 11.sp)
                        Text("• Smart Search Filter: Instantly query and find specific transactions using payee names or notes.", fontSize = 11.sp)
                    }
                    "Bill Splitter" -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Group, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("2. Bill Splits & Roommates", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Text("• Roommate Database: Add your friends or roommates in the Splits tab with their full names and optional UPI IDs.", fontSize = 11.sp)
                        Text("• Equal Group Distribution: Enter any joint bill (e.g., pizza, rent) and select who was involved. The app computes exact shares.", fontSize = 11.sp)
                        Text("• Single-Tap Clearance: Tap \"Settle Owe\" to mark a split transaction as paid. If they have a configured UPI ID, it facilitates speedy settlement.", fontSize = 11.sp)
                    }
                    "Limits & Budgets" -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TrackChanges, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("3. Limits & Budget Thresholds", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Text("• Setting Caps: Define custom safe-spending thresholds for separate categories (e.g. max ₹8,000 allowance for Food).", fontSize = 11.sp)
                        Text("• Multi-period Budgets: Support weekly allowances or custom-period budgets for trips, festivals, and events.", fontSize = 11.sp)
                        Text("• In-App Notifications & Alerts: PayTrack triggers reactive warnings when spending reaches 50%, 80%, or 100% of the set limits.", fontSize = 11.sp)
                        Text("• AI Budget Recommendations: Auto-generate optimized budget limits based on historical averages to maximize monthly savings.", fontSize = 11.sp)
                    }
                    "Communal Goals" -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Stars, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("4. Shared Goals & Campaigns", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Text("• Launch Projects: Formulate savings goals (e.g. Scooter buy, vacation fund) on a specific timeline.", fontSize = 11.sp)
                        Text("• Safe Accumulator: Add capital injections manually. Set-up dynamic targets with weeks remaining indicators.", fontSize = 11.sp)
                        Text("• Completion Progress: Horizontal radial progress indicators show the completion status at a glance.", fontSize = 11.sp)
                    }
                    "Analytics Tool" -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Analytics, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("5. Financial Health Reports", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Text("• Interactive Spark-lines: Review weekly trends, weekend spending concentrations, and category market slices.", fontSize = 11.sp)
                        Text("• Savings Influx: Displays monthly savings rate targets and overall physical financial wellness grades.", fontSize = 11.sp)
                        Text("• CSV Report Portability: Back-up or import records using the CSV Export button on the Analytics screen.", fontSize = 11.sp)
                    }
                }
            }
        }

        // THE GREAT INTERACTIVE SANDBOX PLAYGROUND CARDS!
        ElevatedCard(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = "Play Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Real-Time Sandbox Simulator",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = "A zero-stakes playground to see how notifications, smart alerts, and split computations are executed under the hood!",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Visual metrics dashboard
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceContainerHighest,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("MOCK FOOD SPENT", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("₹$mockFoodSpent / ₹$mockFoodLimit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        LinearProgressIndicator(
                            progress = { (mockFoodSpent / mockFoodLimit).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier.width(100.dp).padding(top = 4.dp),
                            color = if (mockFoodSpent >= mockFoodLimit) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("MOCK INCOME INFLUX", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("₹$mockSalaryInflow", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (mockSalaryInflow > 0) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface)
                        Text("Rate: +${if (mockSalaryInflow > 0) "42%" else "0%"}", fontSize = 9.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Text("Sandbox Controls (Tap to test):", fontSize = 11.sp, fontWeight = FontWeight.Bold)

                // Buttons container
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val nextVal = mockFoodSpent + 200.0
                            mockFoodSpent = nextVal
                            var alertMsg = ""
                            if (nextVal >= mockFoodLimit) {
                                alertMsg = " [ALERT OVERSPENT: Food limit of ₹$mockFoodLimit exceeded! PayTrack warning panel pop-up triggered]"
                            } else if (nextVal >= mockFoodLimit * 0.8) {
                                alertMsg = " [ALERT 80%: Food budget at 80% capability! Prompt triggered]"
                            } else if (nextVal >= mockFoodLimit * 0.5) {
                                alertMsg = " [ALERT 50%: 50% threshold reached. Safe warnings dispatched]"
                            }
                            logList = listOf("Recorded ₹200 food expense. Food status now at ${String.format(Locale.US, "%.1f", (nextVal / mockFoodLimit * 100))}%$alertMsg") + logList
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Buy ₹200 Snacks", fontSize = 10.sp)
                    }

                    Button(
                        onClick = {
                            mockSalaryInflow += 12000.0
                            logList = listOf("Received salary influx of ₹12,000! Active monthly savings projections increased. Ledger summary updated.") + logList
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Add ₹12k Income", fontSize = 10.sp)
                    }

                    Button(
                        onClick = {
                            logList = listOf("Simulated pizza dinner costing ₹900 divided among 3 roommates (Alice, Bob, You). Each companion's share set to ₹300. Bob settled his ₹300 using companion UPI scanner!") + logList
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Simulate Group Split", fontSize = 10.sp)
                    }

                    Button(
                        onClick = {
                            mockFoodSpent = 0.0
                            mockSalaryInflow = 0.0
                            logList = listOf("Simulator values reset successfully.")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Reset Sandbox", fontSize = 10.sp)
                    }
                }

                // Logging output window
                Text("Live Feedback Terminal:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp)
                        .background(
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    logList.forEach { log ->
                        Text("❯ $log", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), lineHeight = 13.sp)
                    }
                }
            }
        }
    }
}
