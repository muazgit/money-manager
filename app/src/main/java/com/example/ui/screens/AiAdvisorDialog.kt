package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionType
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.viewmodel.MoneyUiState
import com.example.util.AppLanguage
import com.example.util.LocalizationUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class AiAdviceResult(
    val title: String,
    val scoreText: String,
    val scoreValue: Int, // 0 - 100
    val analysis: String,
    val recommendations: List<String>,
    val warning: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAdvisorBottomSheet(
    uiState: MoneyUiState,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isBn = uiState.language == AppLanguage.BENGALI
    val scope = rememberCoroutineScope()

    var customQuestion by remember { mutableStateOf("") }
    var isAnalyzing by remember { mutableStateOf(false) }
    var adviceResult by remember { mutableStateOf<AiAdviceResult?>(null) }
    var selectedPromptIndex by remember { mutableStateOf(0) }

    // Quick prompt list
    val promptOptions = if (isBn) {
        listOf(
            "📊 সামগ্রিক খরচের পর্যালোচনা" to "GENERAL_ANALYSIS",
            "💡 টাকা জমানোর কৌশল" to "SAVINGS_TIPS",
            "⚠️ অতিরিক্ত খরচের সতর্কতা" to "OVERSPEND_CHECK",
            "🎯 দেনা পরিশোধের পরামর্শ" to "DEBT_STRATEGY"
        )
    } else {
        listOf(
            "📊 Full Spending Overview" to "GENERAL_ANALYSIS",
            "💡 Smart Savings Tips" to "SAVINGS_TIPS",
            "⚠️ Overspending Alerts" to "OVERSPEND_CHECK",
            "🎯 Debt Payoff Strategy" to "DEBT_STRATEGY"
        )
    }

    fun generateAnalysis(promptType: String, customQuery: String = "") {
        isAnalyzing = true
        scope.launch {
            delay(650) // Realistic processing feedback
            adviceResult = computeAiAdvice(uiState, promptType, customQuery, isBn)
            isAnalyzing = false
        }
    }

    LaunchedEffect(Unit) {
        generateAnalysis(promptOptions[0].second)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("ai_advisor_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF8B5CF6), Color(0xFF3B82F6), Color(0xFF10B981))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isBn) "AI আর্থিক উপদেষ্টা" else "AI Financial Advisor",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isBn) "স্মার্ট ব্যয় বিশ্লেষণ ও পরামর্শ" else "Smart Spending & Savings Analysis",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Prompt Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(promptOptions.indices.toList()) { index ->
                    val (label, type) = promptOptions[index]
                    val isSelected = selectedPromptIndex == index
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {
                                selectedPromptIndex = index
                                generateAnalysis(type)
                            }
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Analysis Content
            if (isAnalyzing) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(36.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isBn) "আপনার আয়-ব্যয় ডেটা বিশ্লেষণ করা হচ্ছে..." else "Analyzing your financial records...",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                adviceResult?.let { advice ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Score Card
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (advice.scoreValue >= 70) IncomeGreen.copy(alpha = 0.12f)
                                    else if (advice.scoreValue >= 45) Color(0xFFF59E0B).copy(alpha = 0.12f)
                                    else ExpenseRed.copy(alpha = 0.12f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = advice.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = advice.scoreText,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Surface(
                                        shape = CircleShape,
                                        color = if (advice.scoreValue >= 70) IncomeGreen
                                        else if (advice.scoreValue >= 45) Color(0xFFD97706)
                                        else ExpenseRed,
                                        modifier = Modifier.size(46.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "${advice.scoreValue}%",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Summary Analysis Text
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Psychology,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isBn) "সারসংক্ষেপ ও বিশ্লেষণ" else "Summary & Analysis",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = advice.analysis,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }

                        // Warning If any
                        if (advice.warning != null) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = ExpenseRed.copy(alpha = 0.1f),
                                    border = BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = ExpenseRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = advice.warning,
                                            fontSize = 12.sp,
                                            color = ExpenseRed,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        // Actionable Tips
                        item {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = null,
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isBn) "করণীয় পরামর্শ" else "Actionable Recommendations",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                advice.recommendations.forEach { tip ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(
                                            text = "• ",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = tip,
                                            fontSize = 13.sp,
                                            lineHeight = 17.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Custom Ask Input
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = customQuestion,
                    onValueChange = { customQuestion = it },
                    placeholder = {
                        Text(
                            text = if (isBn) "AI কে আর্থিক প্রশ্ন করুন..." else "Ask AI any financial question...",
                            fontSize = 13.sp
                        )
                    },
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (customQuestion.isNotBlank()) {
                            generateAnalysis("CUSTOM", customQuestion)
                            customQuestion = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Ask",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

private fun computeAiAdvice(
    uiState: MoneyUiState,
    promptType: String,
    customQuery: String,
    isBn: Boolean
): AiAdviceResult {
    val totalIncome = uiState.monthlyIncome
    val totalExpense = uiState.monthlyExpense
    val totalBalance = uiState.totalBalance
    val debtsToCollect = uiState.totalReceivable
    val debtsToPay = uiState.totalPayable

    val expenseRatio = if (totalIncome > 0) (totalExpense / totalIncome) * 100 else 100.0
    val savingsRate = if (totalIncome > 0) ((totalIncome - totalExpense) / totalIncome) * 100 else 0.0

    // Compute top category
    val expenseTxByCat = uiState.currentMonthTransactions
        .filter { it.type == TransactionType.EXPENSE }
        .groupBy { it.categoryId }
        .mapNotNull { (catId, txList) ->
            val cat = uiState.categories.find { it.id == catId }
            if (cat != null) {
                val sum = txList.sumOf { it.amount }
                val pct = if (totalExpense > 0) (sum / totalExpense) * 100 else 0.0
                Triple(cat, sum, pct)
            } else null
        }

    val topExpenseCategory = expenseTxByCat.maxByOrNull { it.second }

    // Calculate a realistic Financial Health Score (0 - 100)
    var score = 70
    if (savingsRate > 30) score += 20
    else if (savingsRate > 15) score += 10
    else if (savingsRate < 0) score -= 30

    if (debtsToPay > totalBalance && totalBalance > 0) score -= 15
    if (topExpenseCategory != null && topExpenseCategory.second > (totalExpense * 0.5)) score -= 10

    score = score.coerceIn(15, 98)

    val scoreDesc = if (isBn) {
        when {
            score >= 80 -> "চমৎকার আর্থিক স্বাস্থ্য"
            score >= 60 -> "সন্তোষজনক কিন্তু উন্নতির সুযোগ আছে"
            else -> "সতর্কতা প্রয়োজন: ব্যয়ের লাগাম টানুন"
        }
    } else {
        when {
            score >= 80 -> "Excellent Financial Health"
            score >= 60 -> "Fair - Opportunities to save more"
            else -> "Attention needed: High expense ratio"
        }
    }

    when (promptType) {
        "SAVINGS_TIPS" -> {
            return if (isBn) {
                AiAdviceResult(
                    title = "টাকা জমানোর স্মার্ট কৌশল",
                    scoreText = "বর্তমান সঞ্চয়ের হার: ${savingsRate.toInt()}%",
                    scoreValue = score,
                    analysis = "আপনার মোট আয়ের ${(savingsRate).coerceAtLeast(0.0).toInt()}% এই মুহূর্তে সাশ্রয় হচ্ছে। আরও টেকসই সঞ্চয় গড়তে ৫০-৩০-২০ নিয়ম মেনে চলুন (৫০% প্রয়োজন, ৩০% শখ, ২০% জরুরি তহবিল)।",
                    recommendations = listOf(
                        "প্রতি মাসের শুরুতে বেতন/আয় পাওয়ার সাথে সাথে ২০% সেভিংস অ্যাকাউন্টে সরিয়ে ফেলুন।",
                        if (topExpenseCategory != null) "আপনার সবচেয়ে বেশি খরচ '${topExpenseCategory.first.nameBn}' খাতে। এখানে ১০-১৫% কমানোর চেষ্টা করুন।" else "অপ্রয়োজনীয় সাবস্ক্রিপশন ও বাইরের খাওয়া সীমিত করুন।",
                        "বিকাশ/নগদে ক্যাশআউট খরচ কমাতে প্রয়োজনীয় ক্ষেত্রে ব্যাংক ট্রান্সফার বা সরাসরি পে ব্যবহার করুন।"
                    )
                )
            } else {
                AiAdviceResult(
                    title = "Smart Savings Strategy",
                    scoreText = "Current Savings Rate: ${savingsRate.toInt()}%",
                    scoreValue = score,
                    analysis = "You are currently saving ${(savingsRate).coerceAtLeast(0.0).toInt()}% of your income. Aim for the classic 50-30-20 rule (50% essentials, 30% lifestyle, 20% emergency savings).",
                    recommendations = listOf(
                        "Set aside at least 20% into savings as soon as income arrives.",
                        if (topExpenseCategory != null) "Your biggest expense is '${topExpenseCategory.first.nameEn}'. Try cutting it by 10-15%." else "Reduce discretionary takeout and impulse purchases.",
                        "Track micro-expenses daily to avoid unaccounted leakages."
                    )
                )
            }
        }

        "OVERSPEND_CHECK" -> {
            val highExpense = topExpenseCategory != null && topExpenseCategory.third > 35
            return if (isBn) {
                AiAdviceResult(
                    title = "অতিরিক্ত ব্যয়ের সতর্কতা ও পর্যবেক্ষণ",
                    scoreText = if (highExpense) "উচ্চ ঝুঁকিপূর্ণ খাত চিহ্নিত" else "ব্যয়ের ভারসাম্য স্বাভাবিক",
                    scoreValue = (100 - expenseRatio.toInt()).coerceIn(20, 95),
                    analysis = if (topExpenseCategory != null) {
                        "আপনার মোট ব্যয়ের ${topExpenseCategory.third.toInt()}% খরচ হয়েছে '${topExpenseCategory.first.nameBn}' খাতে (${LocalizationUtil.formatMoney(topExpenseCategory.second, true)})।"
                    } else "আপনার খরচের মাত্রা বর্তমানে পরিমিত রয়েছে।",
                    recommendations = listOf(
                        "প্রতিটি গুরুত্বপূর্ণ ক্যাটাগরিতে মাসিক 'বাজেট লিমিট' সেট করুন।",
                        "বাজেটের ৮০% খরচ হয়ে গেলে সতর্কতা নোটিফিকেশন মেনে চলুন।",
                        "কেনাকাটা করার আগে ৩ দিনের কুলিং-অফ পিরিয়ড অনুসরণ করুন।"
                    ),
                    warning = if (highExpense) "সতর্কতা: '${topExpenseCategory?.first?.nameBn}' খাতে আপনার ব্যয়ের বড় অংশ চলে যাচ্ছে!" else null
                )
            } else {
                AiAdviceResult(
                    title = "Overspending Alert & Assessment",
                    scoreText = if (highExpense) "High concentration detected" else "Balanced spending profile",
                    scoreValue = (100 - expenseRatio.toInt()).coerceIn(20, 95),
                    analysis = if (topExpenseCategory != null) {
                        "${topExpenseCategory.third.toInt()}% of total expenditure is consumed by '${topExpenseCategory.first.nameEn}' (${LocalizationUtil.formatMoney(topExpenseCategory.second, false)})."
                    } else "Your spending levels are currently well within normal ranges.",
                    recommendations = listOf(
                        "Set explicit monthly spending limits on volatile categories.",
                        "Review recurring charges and utility bills.",
                        "Use cash/wallets strictly according to predefined category caps."
                    ),
                    warning = if (highExpense) "Warning: '${topExpenseCategory?.first?.nameEn}' is taking up over 35% of total expenses!" else null
                )
            }
        }

        "DEBT_STRATEGY" -> {
            return if (isBn) {
                AiAdviceResult(
                    title = "দেনা-পাওনা নিষ্পত্তির রূপরেখা",
                    scoreText = "বকেয়া দেনা: ${LocalizationUtil.formatMoney(debtsToPay, true)} | পাওনা: ${LocalizationUtil.formatMoney(debtsToCollect, true)}",
                    scoreValue = if (debtsToPay > 0) 55 else 90,
                    analysis = if (debtsToPay > 0) "আপনার ${LocalizationUtil.formatMoney(debtsToPay, true)} টাকা দেনা অপরিশোধিত রয়েছে। এটি দ্রুত পরিশোধ করলে মানসিক স্বস্তি ও আর্থিক স্বাধীনতা নিশ্চিত হবে।"
                    else "আপনার কোনো বড় ধরনের অপরিশোধিত দেনা নেই। এটি একটি চমৎকার আর্থিক অর্জন!",
                    recommendations = listOf(
                        "দেনা শোধের জন্য 'Debt Snowball' পদ্ধতি অবলম্বন করুন (ছোট দেনাগুলো আগে শোধ করুন)।",
                        "পাওনাদারদের সময়মত মৃদু তাগাদা দিন যাতে আপনার ক্যাশ ফ্লো স্বাভাবিক থাকে।",
                        "হিসাব পরিচ্ছন্ন রাখতে দেনা পরিশোধের সাথে সাথে টিক দিন।"
                    )
                )
            } else {
                AiAdviceResult(
                    title = "Debt Clearance & Receivables Strategy",
                    scoreText = "Payables: ${LocalizationUtil.formatMoney(debtsToPay, false)} | Receivables: ${LocalizationUtil.formatMoney(debtsToCollect, false)}",
                    scoreValue = if (debtsToPay > 0) 55 else 90,
                    analysis = if (debtsToPay > 0) "You have ${LocalizationUtil.formatMoney(debtsToPay, false)} in pending payables. Clearing high-priority debts improves cash liquidity."
                    else "You have zero outstanding debt burdens. Great financial milestone!",
                    recommendations = listOf(
                        "Use the Debt Snowball method to tackle smaller debts first for quick wins.",
                        "Follow up with debtors on due dates to maintain healthy cash flow.",
                        "Always log settlements immediately to keep records accurate."
                    )
                )
            }
        }

        else -> {
            // General Spending Overview / Custom
            return if (isBn) {
                AiAdviceResult(
                    title = "সামগ্রিক আর্থিক বিশ্লেষণ",
                    scoreText = scoreDesc,
                    scoreValue = score,
                    analysis = if (customQuery.isNotBlank()) "আপনার প্রশ্ন: '$customQuery'\n\nআপনার বর্তমান মোট ব্যালেন্স ${LocalizationUtil.formatMoney(totalBalance, true)} এবং মাসিক সঞ্চয় ${LocalizationUtil.formatMoney((totalIncome - totalExpense).coerceAtLeast(0.0), true)}। সার্বিক হিসাব অনুযায়ী আপনার বাজেট নিয়ন্ত্রণে রাখা সবচেয়ে জরুরি।"
                    else "এই মাসে আপনার মোট আয় ${LocalizationUtil.formatMoney(totalIncome, true)} এবং ব্যয় ${LocalizationUtil.formatMoney(totalExpense, true)}। নেট ব্যালেন্স ${LocalizationUtil.formatMoney(totalBalance, true)}।",
                    recommendations = listOf(
                        "দৈনিক ছোট ছোট খরচেরও হিসেব রাখুন যাতে মাস শেষে ঘাটতি না হয়।",
                        "জরুরি প্রয়োজনের জন্য অন্তত ৩-৬ মাসের জীবনযাত্রার ব্যয় জমা রাখুন।",
                        "বাজেট সেকশনে গিয়ে প্রতিটি প্রয়োজনীয় ক্যাটাগরিতে মাসিক লিমিট সেট করুন।"
                    )
                )
            } else {
                AiAdviceResult(
                    title = "Comprehensive Financial Overview",
                    scoreText = scoreDesc,
                    scoreValue = score,
                    analysis = if (customQuery.isNotBlank()) "Query: '$customQuery'\n\nBased on your total balance of ${LocalizationUtil.formatMoney(totalBalance, false)} and income of ${LocalizationUtil.formatMoney(totalIncome, false)}, maintaining strict budget caps will yield optimal savings."
                    else "Your total income is ${LocalizationUtil.formatMoney(totalIncome, false)} and expenses are ${LocalizationUtil.formatMoney(totalExpense, false)}. Current net balance stands at ${LocalizationUtil.formatMoney(totalBalance, false)}.",
                    recommendations = listOf(
                        "Log every daily micro-transaction to prevent unnoticed budget drain.",
                        "Maintain an emergency cushion worth 3-6 months of living expenses.",
                        "Review your Monthly Budgets tab regularly to stay on target."
                    )
                )
            }
        }
    }
}
