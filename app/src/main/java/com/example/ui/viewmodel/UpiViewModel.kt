package com.example.ui.viewmodel

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * UI State for Budget vs Spending analysis
 */
data class BudgetStatus(
    val category: String,
    val limitAmount: Double,
    val spentAmount: Double,
    val percentageUsed: Float,
    val statusLevel: BudgetLevel // NORMAL, WARNING_50, WARNING_80, EXCEEDED
)

enum class BudgetLevel {
    NORMAL, WARNING_50, WARNING_80, EXCEEDED
}

data class InAppNotification(
    val id: Long = System.currentTimeMillis() + (0..1000).random(),
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class ExtendedBudgetStatus(
    val id: Long,
    val type: String, // "OVERALL", "CATEGORY", "WEEKLY", "CUSTOM"
    val category: String?,
    val limitAmount: Double,
    val spentAmount: Double,
    val periodLabel: String,
    val startDate: Long,
    val endDate: Long,
    val daysRemaining: Int,
    val percentageUsed: Float,
    val statusLevel: BudgetLevel
)

data class GroupOweDetail(
    val oweId: Long,
    val billId: Long,
    val billTitle: String,
    val debtorId: Long,
    val debtorName: String,
    val amount: Double,
    val creditorId: Long,
    val creditorName: String,
    val creditorUpi: String?,
    val isSettled: Boolean
)

data class BudgetRecommendation(
    val category: String,
    val recommendedLimit: Double,
    val reason: String
)

class UpiViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: UpiRepository
    val allPayments: StateFlow<List<UpiPayment>>
    val allBudgets: StateFlow<List<CategoryBudget>>
    val allExtendedBudgets: StateFlow<List<ExtendedBudget>>
    val allMembers: StateFlow<List<GroupMember>>
    val allGoals: StateFlow<List<SharedGoal>>
    val allSplitBills: StateFlow<List<SplitBill>>
    val allBillOwes: StateFlow<List<BillOwe>>

    // Flow for in-app floating warnings
    private val _inAppNotifications = MutableStateFlow<List<InAppNotification>>(emptyList())
    val inAppNotifications: StateFlow<List<InAppNotification>> = _inAppNotifications

    init {
        val database = AppDatabase.getDatabase(application)
        repository = UpiRepository(database.upiDao())

        allPayments = repository.allPayments.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allBudgets = repository.allBudgets.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allExtendedBudgets = repository.allExtendedBudgets.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allMembers = repository.allMembers.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allGoals = repository.allGoals.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allSplitBills = repository.allSplitBills.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allBillOwes = repository.allBillOwes.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        createNotificationChannel()
    }

    // List of active budget statuses for the current month
    val budgetStatuses: StateFlow<List<BudgetStatus>> = combine(allPayments, allBudgets) { payments, budgets ->
        val currentMonthPayments = payments.filter { isTimestampInCurrentMonth(it.timestamp) && it.isOutgoing }
        val spendingByCategory = currentMonthPayments.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        budgets.map { budget ->
            val spent = spendingByCategory[budget.category] ?: 0.0
            val ratio = if (budget.limitAmount > 0) (spent / budget.limitAmount).toFloat() else 0f
            val level = when {
                ratio >= 1.0f -> BudgetLevel.EXCEEDED
                ratio >= 0.8f -> BudgetLevel.WARNING_80
                ratio >= 0.5f -> BudgetLevel.WARNING_50
                else -> BudgetLevel.NORMAL
            }
            BudgetStatus(
                category = budget.category,
                limitAmount = budget.limitAmount,
                spentAmount = spent,
                percentageUsed = ratio,
                statusLevel = level
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Advanced Budget Progress Trackers
    val extendedBudgetStatuses: StateFlow<List<ExtendedBudgetStatus>> = combine(
        allPayments,
        allExtendedBudgets
    ) { payments, budgets ->
        val now = System.currentTimeMillis()
        budgets.map { budget ->
            val spent = payments.filter {
                it.isOutgoing &&
                it.timestamp >= budget.startDate &&
                it.timestamp <= budget.endDate &&
                (budget.category == null || budget.category == "" || it.category == budget.category)
            }.sumOf { it.amount }

            val ratio = if (budget.limitAmount > 0) (spent / budget.limitAmount).toFloat() else 0f
            val daysRemaining = if (budget.endDate > now) {
                ((budget.endDate - now) / (1000 * 60 * 60 * 24)).toInt()
            } else {
                0
            }

            val level = when {
                ratio >= 1.0f -> BudgetLevel.EXCEEDED
                ratio >= 0.8f -> BudgetLevel.WARNING_80
                ratio >= 0.5f -> BudgetLevel.WARNING_50
                else -> BudgetLevel.NORMAL
            }

            ExtendedBudgetStatus(
                id = budget.id,
                type = budget.type,
                category = budget.category,
                limitAmount = budget.limitAmount,
                spentAmount = spent,
                periodLabel = budget.periodLabel,
                startDate = budget.startDate,
                endDate = budget.endDate,
                daysRemaining = daysRemaining,
                percentageUsed = ratio,
                statusLevel = level
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Detailed breakdown of who owes whom
    val activeBillOwes: StateFlow<List<GroupOweDetail>> = combine(
        allSplitBills,
        allBillOwes,
        allMembers
    ) { bills, owes, members ->
        owes.mapNotNull { owe ->
            val bill = bills.find { it.id == owe.billId } ?: return@mapNotNull null
            val debtor = members.find { it.id == owe.debtorMemberId } ?: return@mapNotNull null
            val creditor = members.find { it.id == bill.paidByMemberId } ?: return@mapNotNull null
            GroupOweDetail(
                oweId = owe.id,
                billId = bill.id,
                billTitle = bill.title,
                debtorId = debtor.id,
                debtorName = debtor.name,
                amount = owe.amount,
                creditorId = creditor.id,
                creditorName = creditor.name,
                creditorUpi = creditor.upiId,
                isSettled = owe.isSettled
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Budget recommendations generator based on historical spending
    val budgetRecommendations: StateFlow<List<BudgetRecommendation>> = allPayments.combine(allExtendedBudgets) { payments, budgets ->
        val outgoingPayments = payments.filter { it.isOutgoing }
        if (outgoingPayments.isEmpty()) return@combine emptyList()

        val spendByCategory = outgoingPayments.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        val cats = spendByCategory.keys.toList()
        cats.map { category ->
            val spent = spendByCategory[category] ?: 0.0
            // Recommend budget representation limit: historical average with 15% head-room
            val recommended = Math.round((spent * 1.15) / 100.0) * 100.0
            val roundedRec = if (recommended < 500.0) 500.0 else recommended.toDouble()
            BudgetRecommendation(
                category = category,
                recommendedLimit = roundedRec,
                reason = "Based on total historical category spending of ₹${String.format(Locale.getDefault(), "%,.0f", spent)} with a 15% safety buffer."
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addPayment(
        payeeName: String,
        payeeUpiId: String?,
        amount: Double,
        category: String,
        description: String?,
        timestamp: Long = System.currentTimeMillis(),
        isOutgoing: Boolean = true
    ) {
        viewModelScope.launch {
            val payment = UpiPayment(
                payeeName = payeeName,
                payeeUpiId = payeeUpiId,
                amount = amount,
                category = category,
                description = description,
                timestamp = timestamp,
                isOutgoing = isOutgoing
            )
            repository.insertPayment(payment)

            if (isOutgoing) {
                // Now check budget for this category & overall budgets
                checkBudgetAndTriggerNotifications(category, amount)
            }
        }
    }

    fun deletePayment(payment: UpiPayment) {
        viewModelScope.launch {
            repository.deletePayment(payment)
        }
    }

    fun setBudget(category: String, limitAmount: Double) {
        viewModelScope.launch {
            val budget = CategoryBudget(category = category, limitAmount = limitAmount)
            repository.insertBudget(budget)
        }
    }

    fun deleteBudget(budget: CategoryBudget) {
        viewModelScope.launch {
            repository.deleteBudget(budget)
        }
    }

    // Extended budgets handling
    fun addExtendedBudget(
        type: String,
        category: String?,
        limitAmount: Double,
        periodLabel: String,
        startDate: Long,
        endDate: Long
    ) {
        viewModelScope.launch {
            val budget = ExtendedBudget(
                type = type,
                category = category,
                limitAmount = limitAmount,
                periodLabel = periodLabel,
                startDate = startDate,
                endDate = endDate
            )
            repository.insertExtendedBudget(budget)
        }
    }

    fun deleteExtendedBudget(id: Long) {
        viewModelScope.launch {
            repository.deleteExtendedBudgetById(id)
        }
    }

    // Group members handling
    fun addGroupMember(name: String, upiId: String?) {
        viewModelScope.launch {
            val member = GroupMember(name = name, upiId = upiId)
            repository.insertMember(member)
        }
    }

    fun deleteGroupMember(id: Long) {
        viewModelScope.launch {
            repository.deleteMemberById(id)
        }
    }

    // Goals handling
    fun addSharedGoal(title: String, targetAmount: Double, savedAmount: Double, targetDate: Long) {
        viewModelScope.launch {
            val goal = SharedGoal(
                title = title,
                targetAmount = targetAmount,
                savedAmount = savedAmount,
                targetDate = targetDate
            )
            repository.insertGoal(goal)
        }
    }

    fun updateGoalSaving(goal: SharedGoal, savedAmount: Double) {
        viewModelScope.launch {
            repository.insertGoal(goal.copy(savedAmount = savedAmount))
        }
    }

    fun deleteGoal(id: Long) {
        viewModelScope.launch {
            repository.deleteGoalById(id)
        }
    }

    // Split bills handling
    fun addSplitBill(
        title: String,
        totalAmount: Double,
        paidByMemberId: Long,
        owes: List<Pair<Long, Double>>
    ) {
        viewModelScope.launch {
            val bill = SplitBill(
                title = title,
                totalAmount = totalAmount,
                paidByMemberId = paidByMemberId
            )
            val billOwesList = owes.map {
                BillOwe(
                    billId = 0, // Assigned by repository transaction
                    debtorMemberId = it.first,
                    amount = it.second,
                    isSettled = false
                )
            }
            repository.insertSplitBill(bill, billOwesList)
        }
    }

    fun settleOwe(oweId: Long, isSettled: Boolean) {
        viewModelScope.launch {
            repository.updateOweSettlement(oweId, isSettled)
        }
    }

    fun deleteSplitBill(id: Long) {
        viewModelScope.launch {
            repository.deleteSplitBillById(id)
        }
    }

    fun removeInAppNotification(id: Long) {
        _inAppNotifications.value = _inAppNotifications.value.filterNot { it.id == id }
    }

    private suspend fun checkBudgetAndTriggerNotifications(category: String, incomingAmount: Double) {
        // 1. Check legacy monthly category budgets
        val budget = allBudgets.value.find { it.category == category }
        if (budget != null) {
            val currentMonthPaymentsOfCategory = allPayments.value.filter {
                it.category == category && isTimestampInCurrentMonth(it.timestamp) && it.isOutgoing
            }
            val totalSpent = currentMonthPaymentsOfCategory.sumOf { it.amount }
            val ratio = totalSpent / budget.limitAmount

            val budgetFormatted = String.format(Locale.getDefault(), "₹%.2f", budget.limitAmount)
            val spentFormatted = String.format(Locale.getDefault(), "₹%.2f", totalSpent)

            if (ratio >= 1.0) {
                triggerNotificationAlerts(
                    "🚨 Budget Exceeded for $category",
                    "You spent $spentFormatted. This exceeds your monthly budget limit of $budgetFormatted!"
                )
            } else if (ratio >= 0.8) {
                triggerNotificationAlerts(
                    "⚠️ Budget Limit Alert: $category (80%)",
                    "You spent $spentFormatted of $budgetFormatted budget. You have hit ${Math.round(ratio * 100)}% of your limit!"
                )
            } else if (ratio >= 0.5) {
                triggerNotificationAlerts(
                    "ℹ️ Budget Progress Alert: $category (50%)",
                    "You reached over half your monthly limit for $category: spent $spentFormatted of $budgetFormatted!"
                )
            }
        }

        // 2. Check modern extended budgets (weekly, overall, custom)
        val now = System.currentTimeMillis()
        val activeBudgets = allExtendedBudgets.value.filter {
            now >= it.startDate && now <= it.endDate &&
            (it.category == null || it.category == "" || it.category == category)
        }

        activeBudgets.forEach { b ->
            val spent = allPayments.value.filter {
                it.isOutgoing &&
                it.timestamp >= b.startDate &&
                it.timestamp <= b.endDate &&
                (b.category == null || b.category == "" || it.category == b.category)
            }.sumOf { it.amount }

            val ratio = spent / b.limitAmount
            val spentFormat = String.format(Locale.getDefault(), "₹%.2f", spent)
            val limitFormat = String.format(Locale.getDefault(), "₹%.2f", b.limitAmount)

            val label = b.periodLabel.ifEmpty { b.type }

            if (ratio >= 1.0) {
                triggerNotificationAlerts(
                    "🚨 Budget Exceeded: $label",
                    "Limit of $limitFormat reached. Total spent is $spentFormat!"
                )
            } else if (ratio >= 0.8) {
                triggerNotificationAlerts(
                    "⚠️ Budget Limit Alert: $label (80%)",
                    "You spent $spentFormat of $limitFormat. You are at ${Math.round(ratio * 100)}% of limit!"
                )
            } else if (ratio >= 0.5) {
                triggerNotificationAlerts(
                    "ℹ️ Budget Progress Alert: $label (50%)",
                    "You are half-way! Spent $spentFormat of $limitFormat limit."
                )
            }
        }
    }

    private fun triggerNotificationAlerts(title: String, message: String) {
        val notificationItem = InAppNotification(title = title, message = message)
        _inAppNotifications.value = _inAppNotifications.value + notificationItem
        sendSystemNotification(title, message)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "BUDGET_LIMIT_CHANNEL",
                "UPI Tracker Budget Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Warns you when you run close to or exceed your custom category budgets."
            }
            val manager = getApplication<Application>().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun sendSystemNotification(title: String, message: String) {
        val context = getApplication<Application>()
        try {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notification = NotificationCompat.Builder(context, "BUDGET_LIMIT_CHANNEL")
                .setSmallIcon(context.applicationInfo.icon)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()

            manager.notify(title.hashCode(), notification)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun triggerShareReport(context: Context, monthOffset: Int) {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -monthOffset)
        val targetMonthNum = calendar.get(Calendar.MONTH)
        val targetYearNum = calendar.get(Calendar.YEAR)

        val monthNameFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val monthName = monthNameFormat.format(calendar.time)

        val paymentsInTargetMonth = allPayments.value.filter {
            val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            cal.get(Calendar.MONTH) == targetMonthNum && cal.get(Calendar.YEAR) == targetYearNum
        }

        val csvBuilder = StringBuilder()
        csvBuilder.append("UPI Budget & Expense Report - $monthName\n")
        csvBuilder.append("Generated on: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}\n\n")
        csvBuilder.append("Date,Payee Name,UPI ID,Amount (INR),Category,Description,Reference,Type\n")

        var totalSpent = 0.0
        var totalIncome = 0.0
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        paymentsInTargetMonth.forEach { payment ->
            val dateStr = dateFormat.format(Date(payment.timestamp))
            val safeName = payment.payeeName.replace(",", " ")
            val safeUpi = payment.payeeUpiId?.replace(",", " ") ?: "N/A"
            val safeDesc = payment.description?.replace(",", " ") ?: "N/A"
            val safeRef = payment.transactionRef?.replace(",", " ") ?: "N/A"
            val typeStr = if (payment.isOutgoing) "EXPENSE" else "INCOME"
            csvBuilder.append("$dateStr,$safeName,$safeUpi,${payment.amount},${payment.category},$safeDesc,$safeRef,$typeStr\n")
            if (payment.isOutgoing) {
                totalSpent += payment.amount
            } else {
                totalIncome += payment.amount
            }
        }

        csvBuilder.append("\nSummary Analysis\n")
        csvBuilder.append("Total Income: ₹$totalIncome\n")
        csvBuilder.append("Total Expense: ₹$totalSpent\n")
        csvBuilder.append("Net Savings: ₹${totalIncome - totalSpent}\n")

        csvBuilder.append("\nExpenses by Category:\n")
        paymentsInTargetMonth.filter { it.isOutgoing }
            .groupBy { it.category }
            .forEach { (cat, list) ->
                val sum = list.sumOf { it.amount }
                csvBuilder.append("$cat, ₹$sum\n")
            }

        try {
            val cacheDirectory = File(context.cacheDir, "reports")
            if (!cacheDirectory.exists()) {
                cacheDirectory.mkdirs()
            }
            val reportFile = File(cacheDirectory, "UPI_Report_${monthName.replace(" ", "_")}.csv")
            val writer = FileWriter(reportFile)
            writer.write(csvBuilder.toString())
            writer.flush()
            writer.close()

            val fileUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                reportFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, "UPI Monthly Expense Report - $monthName")
                putExtra(Intent.EXTRA_TEXT, "Report files successfully exported for budget analysis: $monthName")
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooserIntent = Intent.createChooser(shareIntent, "Share Monthly Report").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooserIntent)
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                Toast.makeText(context, "Failed to share report: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            } catch (fallbackEx: Exception) {
                fallbackEx.printStackTrace()
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
}
