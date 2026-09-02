package com.example.data.repository

import com.example.data.local.AccountDao
import com.example.data.local.BudgetDao
import com.example.data.local.CategoryDao
import com.example.data.local.DebtDao
import com.example.data.local.TransactionDao
import com.example.data.model.AccountEntity
import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.DebtEntity
import com.example.data.model.DebtType
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MoneyRepository(
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao,
    private val debtDao: DebtDao,
    private val budgetDao: BudgetDao
) {
    val allAccounts: Flow<List<AccountEntity>> = accountDao.getAllAccounts()
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val allDebts: Flow<List<DebtEntity>> = debtDao.getAllDebts()

    fun getCategoriesByType(type: TransactionType): Flow<List<CategoryEntity>> =
        categoryDao.getCategoriesByType(type)

    fun getBudgetsForMonth(monthYear: String): Flow<List<BudgetEntity>> =
        budgetDao.getBudgetsForMonth(monthYear)

    suspend fun addTransaction(transaction: TransactionEntity) {
        transactionDao.insertTransaction(transaction)
        when (transaction.type) {
            TransactionType.EXPENSE -> {
                accountDao.updateBalance(transaction.accountId, -transaction.amount)
            }
            TransactionType.INCOME -> {
                accountDao.updateBalance(transaction.accountId, transaction.amount)
            }
            TransactionType.TRANSFER -> {
                accountDao.updateBalance(transaction.accountId, -transaction.amount)
                transaction.toAccountId?.let { toId ->
                    accountDao.updateBalance(toId, transaction.amount)
                }
            }
        }
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.deleteTransaction(transaction)
        // Reverse account balance effect
        when (transaction.type) {
            TransactionType.EXPENSE -> {
                accountDao.updateBalance(transaction.accountId, transaction.amount)
            }
            TransactionType.INCOME -> {
                accountDao.updateBalance(transaction.accountId, -transaction.amount)
            }
            TransactionType.TRANSFER -> {
                accountDao.updateBalance(transaction.accountId, transaction.amount)
                transaction.toAccountId?.let { toId ->
                    accountDao.updateBalance(toId, -transaction.amount)
                }
            }
        }
    }

    suspend fun transferFunds(
        fromAccount: AccountEntity,
        toAccount: AccountEntity,
        amount: Double,
        note: String
    ) {
        val now = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val transaction = TransactionEntity(
            type = TransactionType.TRANSFER,
            amount = amount,
            accountId = fromAccount.id,
            toAccountId = toAccount.id,
            categoryId = 0,
            categoryNameBn = "তহবিল স্থানান্তর",
            categoryNameEn = "Fund Transfer",
            categoryIcon = "swap_horiz",
            categoryColorHex = "#6366F1",
            accountNameBn = fromAccount.nameBn,
            accountNameEn = fromAccount.nameEn,
            toAccountNameBn = toAccount.nameBn,
            toAccountNameEn = toAccount.nameEn,
            note = note.ifEmpty { "${fromAccount.nameBn} হতে ${toAccount.nameBn}" },
            timestamp = now,
            dateString = dateFormat.format(Date(now))
        )
        addTransaction(transaction)
    }

    suspend fun addAccount(account: AccountEntity): Long = accountDao.insertAccount(account)

    suspend fun updateAccount(account: AccountEntity) = accountDao.updateAccount(account)

    suspend fun deleteAccount(account: AccountEntity) = accountDao.deleteAccount(account)

    suspend fun addCategory(category: CategoryEntity): Long = categoryDao.insertCategory(category)

    suspend fun addDebt(debt: DebtEntity): Long = debtDao.insertDebt(debt)

    suspend fun setDebtSettled(debtId: Long, settled: Boolean) {
        val settledDate = if (settled) System.currentTimeMillis() else null
        debtDao.setSettledStatus(debtId, settled, settledDate)
    }

    suspend fun deleteDebt(debt: DebtEntity) = debtDao.deleteDebt(debt)

    suspend fun saveBudget(budget: BudgetEntity) = budgetDao.insertBudget(budget)
}
