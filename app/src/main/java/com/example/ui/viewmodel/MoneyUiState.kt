package com.example.ui.viewmodel

import com.example.data.model.AccountEntity
import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.DebtEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.util.AppLanguage

enum class NavigationTab {
    DASHBOARD,
    TRANSACTIONS,
    ANALYTICS,
    DENA_PAONA,
    ACCOUNTS
}

enum class TimeFilter {
    TODAY,
    THIS_WEEK,
    THIS_MONTH,
    ALL
}

data class MoneyUiState(
    val accounts: List<AccountEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val transactions: List<TransactionEntity> = emptyList(),
    val debts: List<DebtEntity> = emptyList(),
    val budgets: List<BudgetEntity> = emptyList(),
    val selectedTab: NavigationTab = NavigationTab.DASHBOARD,
    val selectedTimeFilter: TimeFilter = TimeFilter.THIS_MONTH,
    val selectedTypeFilter: TransactionType? = null,
    val searchQuery: String = "",
    val language: AppLanguage = AppLanguage.BENGALI,
    val useBengaliDigits: Boolean = true,
    val isDarkMode: Boolean = false,
    val isLoading: Boolean = false,
    val updateInfo: com.example.util.AppUpdateInfo? = null,
    val isCheckingUpdate: Boolean = false,
    val updateCheckMessage: String? = null
) {
    val totalBalance: Double
        get() = accounts.sumOf { it.balance }

    val currentMonthTransactions: List<TransactionEntity>
        get() {
            val now = java.util.Calendar.getInstance()
            val currentYear = now.get(java.util.Calendar.YEAR)
            val currentMonth = now.get(java.util.Calendar.MONTH)
            val cal = java.util.Calendar.getInstance()

            return transactions.filter {
                cal.timeInMillis = it.timestamp
                cal.get(java.util.Calendar.YEAR) == currentYear &&
                        cal.get(java.util.Calendar.MONTH) == currentMonth
            }
        }

    val monthlyIncome: Double
        get() = currentMonthTransactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }

    val monthlyExpense: Double
        get() = currentMonthTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }

    val monthlySavings: Double
        get() = monthlyIncome - monthlyExpense

    val totalReceivable: Double
        get() = debts
            .filter { it.type == com.example.data.model.DebtType.RECEIVABLE && !it.isSettled }
            .sumOf { it.amount }

    val totalPayable: Double
        get() = debts
            .filter { it.type == com.example.data.model.DebtType.PAYABLE && !it.isSettled }
            .sumOf { it.amount }

    val filteredTransactions: List<TransactionEntity>
        get() {
            val now = java.util.Calendar.getInstance()
            val cal = java.util.Calendar.getInstance()

            var list = when (selectedTimeFilter) {
                TimeFilter.TODAY -> transactions.filter {
                    cal.timeInMillis = it.timestamp
                    now.get(java.util.Calendar.YEAR) == cal.get(java.util.Calendar.YEAR) &&
                            now.get(java.util.Calendar.DAY_OF_YEAR) == cal.get(java.util.Calendar.DAY_OF_YEAR)
                }
                TimeFilter.THIS_WEEK -> transactions.filter {
                    cal.timeInMillis = it.timestamp
                    now.get(java.util.Calendar.YEAR) == cal.get(java.util.Calendar.YEAR) &&
                            now.get(java.util.Calendar.WEEK_OF_YEAR) == cal.get(java.util.Calendar.WEEK_OF_YEAR)
                }
                TimeFilter.THIS_MONTH -> currentMonthTransactions
                TimeFilter.ALL -> transactions
            }

            if (selectedTypeFilter != null) {
                list = list.filter { it.type == selectedTypeFilter }
            }

            if (searchQuery.isNotBlank()) {
                val q = searchQuery.trim().lowercase()
                list = list.filter {
                    it.note.lowercase().contains(q) ||
                            it.categoryNameBn.lowercase().contains(q) ||
                            it.categoryNameEn.lowercase().contains(q) ||
                            it.accountNameBn.lowercase().contains(q) ||
                            it.accountNameEn.lowercase().contains(q)
                }
            }

            return list
        }

    val categoryExpenseBreakdown: Map<String, Double>
        get() = currentMonthTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { if (language == AppLanguage.BENGALI) it.categoryNameBn else it.categoryNameEn }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
}
