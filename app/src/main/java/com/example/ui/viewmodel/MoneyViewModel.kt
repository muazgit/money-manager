package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.PrepopulateData
import com.example.data.model.AccountEntity
import com.example.data.model.AccountType
import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.DebtEntity
import com.example.data.model.DebtType
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.repository.MoneyRepository
import com.example.util.AppLanguage
import com.example.util.LocalizationUtil
import kotlinx.coroutines.Dispatchers
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MoneyViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("taka_manager_user_prefs", Context.MODE_PRIVATE)

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = MoneyRepository(
        accountDao = database.accountDao(),
        categoryDao = database.categoryDao(),
        transactionDao = database.transactionDao(),
        debtDao = database.debtDao(),
        budgetDao = database.budgetDao()
    )

    private val _uiState = MutableStateFlow(
        MoneyUiState(
            isDarkMode = prefs.getBoolean("pref_dark_mode", false),
            language = if (prefs.getString("pref_language", "BENGALI") == "ENGLISH") AppLanguage.ENGLISH else AppLanguage.BENGALI,
            useBengaliDigits = prefs.getBoolean("pref_bengali_digits", true)
        )
    )
    val uiState: StateFlow<MoneyUiState> = _uiState.asStateFlow()

    init {
        observeData()
        checkForUpdates(isManual = false)
    }

    private fun observeData() {
        val currentMonthYear = LocalizationUtil.getCurrentMonthYearString()

        viewModelScope.launch {
            repository.allAccounts.collect { accounts ->
                _uiState.update { it.copy(accounts = accounts) }
            }
        }

        viewModelScope.launch {
            repository.allCategories.collect { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }

        viewModelScope.launch {
            repository.allTransactions.collect { transactions ->
                _uiState.update { it.copy(transactions = transactions) }
            }
        }

        viewModelScope.launch {
            repository.allDebts.collect { debts ->
                _uiState.update { it.copy(debts = debts) }
            }
        }

        viewModelScope.launch {
            repository.getBudgetsForMonth(currentMonthYear).collect { budgets ->
                _uiState.update { it.copy(budgets = budgets) }
            }
        }
    }

    fun setTab(tab: NavigationTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun setTimeFilter(filter: TimeFilter) {
        _uiState.update { it.copy(selectedTimeFilter = filter) }
    }

    fun setTypeFilter(type: TransactionType?) {
        _uiState.update { it.copy(selectedTypeFilter = type) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun toggleLanguage() {
        val newLang = if (_uiState.value.language == AppLanguage.BENGALI) AppLanguage.ENGLISH else AppLanguage.BENGALI
        prefs.edit().putString("pref_language", newLang.name).apply()
        _uiState.update { it.copy(language = newLang) }
    }

    fun toggleBengaliDigits() {
        val newValue = !_uiState.value.useBengaliDigits
        prefs.edit().putBoolean("pref_bengali_digits", newValue).apply()
        _uiState.update { it.copy(useBengaliDigits = newValue) }
    }

    fun toggleTheme() {
        val newMode = !_uiState.value.isDarkMode
        prefs.edit().putBoolean("pref_dark_mode", newMode).apply()
        _uiState.update { it.copy(isDarkMode = newMode) }
    }

    fun setTheme(isDark: Boolean) {
        prefs.edit().putBoolean("pref_dark_mode", isDark).apply()
        _uiState.update { it.copy(isDarkMode = isDark) }
    }

    fun addTransaction(
        type: TransactionType,
        amount: Double,
        accountId: Long,
        toAccountId: Long? = null,
        categoryId: Long,
        note: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val account = _uiState.value.accounts.find { it.id == accountId }
            val toAccount = toAccountId?.let { id -> _uiState.value.accounts.find { it.id == id } }
            val category = _uiState.value.categories.find { it.id == categoryId }

            val now = System.currentTimeMillis()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            val transaction = TransactionEntity(
                type = type,
                amount = amount,
                accountId = accountId,
                toAccountId = toAccountId,
                categoryId = categoryId,
                categoryNameBn = category?.nameBn ?: "লেনদেন",
                categoryNameEn = category?.nameEn ?: "Transaction",
                categoryIcon = category?.iconName ?: "payments",
                categoryColorHex = category?.colorHex ?: "#10B981",
                accountNameBn = account?.nameBn ?: "হিসাব",
                accountNameEn = account?.nameEn ?: "Account",
                toAccountNameBn = toAccount?.nameBn,
                toAccountNameEn = toAccount?.nameEn,
                note = note,
                timestamp = now,
                dateString = dateFormat.format(Date(now))
            )

            repository.addTransaction(transaction)
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTransaction(transaction)
        }
    }

    fun transferFunds(
        fromAccount: AccountEntity,
        toAccount: AccountEntity,
        amount: Double,
        note: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.transferFunds(fromAccount, toAccount, amount, note)
        }
    }

    fun addAccount(
        nameEn: String,
        nameBn: String,
        type: AccountType,
        initialBalance: Double,
        accountNumber: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val (icon, color) = when (type) {
                AccountType.CASH -> Pair("payments", "#10B981")
                AccountType.BKASH -> Pair("account_balance_wallet", "#E2136E")
                AccountType.NAGAD -> Pair("send_to_mobile", "#F97316")
                AccountType.ROCKET -> Pair("account_balance_wallet", "#8B5CF6")
                AccountType.BANK -> Pair("account_balance", "#2563EB")
                AccountType.CARD -> Pair("credit_card", "#0284C7")
                AccountType.SAVINGS -> Pair("savings", "#D97706")
                AccountType.OTHER -> Pair("wallet", "#6B7280")
            }

            val account = AccountEntity(
                nameEn = nameEn,
                nameBn = nameBn,
                type = type,
                balance = initialBalance,
                iconName = icon,
                colorHex = color,
                accountNumber = accountNumber
            )
            repository.addAccount(account)
        }
    }

    fun deleteAccount(account: AccountEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAccount(account)
        }
    }

    fun addDebt(
        personName: String,
        personPhone: String,
        amount: Double,
        type: DebtType,
        dueDate: Long?,
        note: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val debt = DebtEntity(
                personName = personName,
                personPhone = personPhone,
                amount = amount,
                type = type,
                dueDate = dueDate,
                note = note
            )
            repository.addDebt(debt)
        }
    }

    fun setDebtSettled(debtId: Long, settled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.setDebtSettled(debtId, settled)
        }
    }

    fun deleteDebt(debt: DebtEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteDebt(debt)
        }
    }

    fun saveBudget(categoryId: Long, monthlyLimit: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentMonthYear = LocalizationUtil.getCurrentMonthYearString()
            val existing = _uiState.value.budgets.find { it.categoryId == categoryId }
            val budget = BudgetEntity(
                id = existing?.id ?: 0,
                categoryId = categoryId,
                monthlyLimit = monthlyLimit,
                monthYear = currentMonthYear
            )
            repository.saveBudget(budget)
        }
    }

    fun resetAndSeedData() {
        viewModelScope.launch(Dispatchers.IO) {
            PrepopulateData.seedDatabase(database)
        }
    }

    fun clearAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            PrepopulateData.clearAllData(database)
        }
    }

    fun checkForUpdates(isManual: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingUpdate = true, updateCheckMessage = null) }
            val updateInfo = com.example.util.AppUpdateManager.checkForUpdate(getApplication(), isManual)
            _uiState.update {
                it.copy(
                    isCheckingUpdate = false,
                    updateInfo = updateInfo,
                    updateCheckMessage = if (updateInfo.hasUpdate) {
                        if (it.language == AppLanguage.BENGALI) "নতুন আপডেট এসেছে: v${updateInfo.latestVersionName}" else "New update available: v${updateInfo.latestVersionName}"
                    } else if (isManual) {
                        if (it.language == AppLanguage.BENGALI) "আপনার অ্যাপটি লেটেস্ট সংস্করণে আছে (v${updateInfo.currentVersionName})" else "You are already using the latest version (v${updateInfo.currentVersionName})"
                    } else null
                )
            }
        }
    }

    fun setCustomUpdateUrl(url: String) {
        com.example.util.AppUpdateManager.setUpdateUrl(getApplication(), url)
        checkForUpdates(isManual = true)
    }

    fun clearUpdateMessage() {
        _uiState.update { it.copy(updateCheckMessage = null) }
    }
}
