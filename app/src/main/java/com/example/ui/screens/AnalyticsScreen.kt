package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionType
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.viewmodel.MoneyUiState
import com.example.util.AppLanguage
import com.example.util.LocalizationUtil

@Composable
fun AnalyticsScreen(
    uiState: MoneyUiState,
    onOpenSetBudget: () -> Unit
) {
    val isBn = uiState.language == AppLanguage.BENGALI
    val context = LocalContext.current

    val currentMonthTxs = uiState.currentMonthTransactions
    val totalExpense = currentMonthTxs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val totalIncome = currentMonthTxs.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val savings = totalIncome - totalExpense

    val categoryBreakdown = uiState.categoryExpenseBreakdown

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("analytics_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isBn) "বাজেট ও আর্থিক বিশ্লেষণ" else "Analytics & Budgets",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = LocalizationUtil.getCurrentMonthName(isBn),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Share Summary Report Button
                OutlinedButton(
                    onClick = {
                        val reportText = buildString {
                            appendLine(if (isBn) "📊 মাসিক হিসাব বিবরণী - ${LocalizationUtil.getCurrentMonthName(true)}" else "📊 Monthly Financial Report - ${LocalizationUtil.getCurrentMonthName(false)}")
                            appendLine("----------------------------------")
                            appendLine(if (isBn) "মোট আয়: ${LocalizationUtil.formatMoney(totalIncome, uiState.useBengaliDigits)}" else "Total Income: ${LocalizationUtil.formatMoney(totalIncome, false)}")
                            appendLine(if (isBn) "মোট ব্যয়: ${LocalizationUtil.formatMoney(totalExpense, uiState.useBengaliDigits)}" else "Total Expense: ${LocalizationUtil.formatMoney(totalExpense, false)}")
                            appendLine(if (isBn) "নিট সঞ্চয়: ${LocalizationUtil.formatMoney(savings, uiState.useBengaliDigits)}" else "Net Savings: ${LocalizationUtil.formatMoney(savings, false)}")
                            appendLine("----------------------------------")
                            appendLine(if (isBn) "প্রধান প্রধান ব্যয়ের খাত:" else "Top Expense Categories:")
                            categoryBreakdown.entries.sortedByDescending { it.value }.take(5).forEach { (cat, amt) ->
                                val pct = if (totalExpense > 0) (amt / totalExpense * 100).toInt() else 0
                                appendLine("• $cat: ${LocalizationUtil.formatMoney(amt, uiState.useBengaliDigits)} ($pct%)")
                            }
                            appendLine("\nহিসাব খাতা / Taka Manager অ্যাপ দ্বারা প্রস্তুতকৃত")
                        }

                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, reportText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, if (isBn) "হিসাবের বিবরণী পাঠান" else "Share Report"))
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("share_report_btn")
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isBn) "শেয়ার" else "Share", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Summary Statistics Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (isBn) "চলতি মাসের সঞ্চয়" else "Net Savings",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = LocalizationUtil.formatMoney(savings, uiState.useBengaliDigits),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (savings >= 0) IncomeGreen else ExpenseRed
                            )
                        }

                        // Savings rate
                        val savingsRate = if (totalIncome > 0) ((savings / totalIncome) * 100).toInt() else 0
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (savingsRate >= 0) IncomeGreen.copy(alpha = 0.15f) else ExpenseRed.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = (if (isBn) "সঞ্চয় হার: " else "Rate: ") +
                                        (if (isBn && uiState.useBengaliDigits) LocalizationUtil.toBengaliDigits(savingsRate.toString()) else savingsRate.toString()) + "%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (savingsRate >= 0) IncomeGreen else ExpenseRed,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Proportional Visual Bar
                    val expenseRatio = if (totalIncome > 0) (totalExpense / totalIncome).toFloat().coerceIn(0f, 1f) else 1f
                    LinearProgressIndicator(
                        progress = { expenseRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = ExpenseRed,
                        trackColor = IncomeGreen,
                        strokeCap = StrokeCap.Round
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = (if (isBn) "ব্যয়: " else "Expense: ") + LocalizationUtil.formatMoney(totalExpense, uiState.useBengaliDigits),
                            fontSize = 11.sp,
                            color = ExpenseRed,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = (if (isBn) "আয়: " else "Income: ") + LocalizationUtil.formatMoney(totalIncome, uiState.useBengaliDigits),
                            fontSize = 11.sp,
                            color = IncomeGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Category Breakdown Header
        item {
            Text(
                text = if (isBn) "ব্যয়ের খাতভিত্তিক বিশ্লেষণ" else "Expense by Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (categoryBreakdown.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isBn) "এই মাসে এখনও কোনো ব্যয় লিপিবদ্ধ হয়নি" else "No expenses recorded this month",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        } else {
            val sortedCategories = categoryBreakdown.entries.sortedByDescending { it.value }
            items(sortedCategories) { (catName, amount) ->
                val percentage = if (totalExpense > 0) (amount / totalExpense).toFloat() else 0f
                val pctInt = (percentage * 100).toInt()

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = catName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = LocalizationUtil.formatMoney(amount, uiState.useBengaliDigits),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(${if (isBn && uiState.useBengaliDigits) LocalizationUtil.toBengaliDigits(pctInt.toString()) else pctInt}%)",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { percentage.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeCap = StrokeCap.Round
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Budget Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBn) "মাসিক বাজেট নিয়ন্ত্রণ" else "Monthly Budgets",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = onOpenSetBudget,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("set_budget_btn")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isBn) "বাজেট সেট" else "Set Budget", fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (uiState.budgets.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isBn) "কোনো মাসিক বাজেট সেট করা হয়নি" else "No budgets set for this month",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isBn) "বাজেট সেট করলে অতিরিক্ত খরচের সতর্কবার্তা পাবেন" else "Set limits to get alerts on overspending",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        } else {
            items(uiState.budgets) { budget ->
                val category = uiState.categories.find { it.id == budget.categoryId }
                val spent = currentMonthTxs
                    .filter { it.type == TransactionType.EXPENSE && it.categoryId == budget.categoryId }
                    .sumOf { it.amount }

                val progress = if (budget.monthlyLimit > 0) (spent / budget.monthlyLimit).toFloat() else 0f
                val isOverBudget = spent > budget.monthlyLimit

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isBn) category?.nameBn ?: "বাজেট" else category?.nameEn ?: "Budget",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )

                            if (isOverBudget) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = ExpenseRed.copy(alpha = 0.15f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = ExpenseRed,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = if (isBn) "সীমা অতিক্রান্ত!" else "Over Budget!",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ExpenseRed
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = (if (isBn) "খরচ: " else "Spent: ") + LocalizationUtil.formatMoney(spent, uiState.useBengaliDigits),
                                fontSize = 12.sp,
                                color = if (isOverBudget) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = (if (isBn) "সীমা: " else "Limit: ") + LocalizationUtil.formatMoney(budget.monthlyLimit, uiState.useBengaliDigits),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val progressColor = when {
                            isOverBudget -> ExpenseRed
                            progress >= 0.7f -> AmberSecondary
                            else -> IncomeGreen
                        }

                        LinearProgressIndicator(
                            progress = { progress.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = progressColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeCap = StrokeCap.Round
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(96.dp))
        }
    }
}
