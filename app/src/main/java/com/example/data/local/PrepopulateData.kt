package com.example.data.local

import com.example.data.model.AccountEntity
import com.example.data.model.AccountType
import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.DebtEntity
import com.example.data.model.DebtType
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PrepopulateData {

    fun getDefaultCategories(): List<CategoryEntity> = listOf(
        // Expense
        CategoryEntity(1, "Food & Grocery", "খাদ্য ও বাজার", TransactionType.EXPENSE, "restaurant", "#EF4444"),
        CategoryEntity(2, "Transport & Rent", "যাতায়াত ও ভাড়া", TransactionType.EXPENSE, "directions_bus", "#F59E0B"),
        CategoryEntity(3, "Bills & Utilities", "বিল ও ইউটিলিটি", TransactionType.EXPENSE, "receipt_long", "#3B82F6"),
        CategoryEntity(4, "Shopping", "কেনাকাটা", TransactionType.EXPENSE, "shopping_bag", "#EC4899"),
        CategoryEntity(5, "Education", "শিক্ষা", TransactionType.EXPENSE, "school", "#8B5CF6"),
        CategoryEntity(6, "Healthcare", "চিকিৎসা ও ওষুধ", TransactionType.EXPENSE, "medical_services", "#10B981"),
        CategoryEntity(7, "Entertainment", "বিনোদন", TransactionType.EXPENSE, "movie", "#6366F1"),
        CategoryEntity(8, "Family Support", "পরিবার ও আত্মীয়", TransactionType.EXPENSE, "family_restroom", "#F97316"),
        CategoryEntity(9, "Charity & Sadakah", "দান ও সদকা", TransactionType.EXPENSE, "volunteer_activism", "#14B8A6"),
        CategoryEntity(10, "Other Expense", "অন্যান্য ব্যয়", TransactionType.EXPENSE, "more_horiz", "#6B7280"),

        // Income
        CategoryEntity(11, "Salary", "মাসিক বেতন", TransactionType.INCOME, "payments", "#10B981"),
        CategoryEntity(12, "Business", "ব্যবসা", TransactionType.INCOME, "storefront", "#3B82F6"),
        CategoryEntity(13, "Freelancing", "ফ্রিল্যান্সিং", TransactionType.INCOME, "laptop_mac", "#8B5CF6"),
        CategoryEntity(14, "Debt Repayment", "ধার ফেরত", TransactionType.INCOME, "currency_exchange", "#F59E0B"),
        CategoryEntity(15, "Investment", "বিনিয়োগ", TransactionType.INCOME, "trending_up", "#059669"),
        CategoryEntity(16, "Gift", "উপহার", TransactionType.INCOME, "card_giftcard", "#EC4899"),
        CategoryEntity(17, "Other Income", "অন্যান্য আয়", TransactionType.INCOME, "savings", "#6B7280")
    )

    fun getDefaultZeroAccounts(): List<AccountEntity> = listOf(
        AccountEntity(1, "Cash", "নগদ টাকা", AccountType.CASH, 0.0, "payments", "#10B981"),
        AccountEntity(2, "bKash", "বিকাশ", AccountType.BKASH, 0.0, "account_balance_wallet", "#E2136E", "017XXXXXXXX"),
        AccountEntity(3, "Nagad", "নগদ", AccountType.NAGAD, 0.0, "send_to_mobile", "#F97316", "018XXXXXXXX"),
        AccountEntity(4, "Bank Account", "ব্যাংক একাউন্ট", AccountType.BANK, 0.0, "account_balance", "#2563EB", "****0000")
    )

    suspend fun clearAllData(database: AppDatabase) {
        val accountDao = database.accountDao()
        val categoryDao = database.categoryDao()
        val transactionDao = database.transactionDao()
        val debtDao = database.debtDao()
        val budgetDao = database.budgetDao()

        // 1. Wipe all transactional data
        transactionDao.clearAllTransactions()
        debtDao.clearAllDebts()
        budgetDao.clearAllBudgets()
        accountDao.clearAllAccounts()
        categoryDao.clearAllCategories()

        // 2. Setup fresh zero-balance wallets & standard categories
        accountDao.insertAccounts(getDefaultZeroAccounts())
        categoryDao.insertCategories(getDefaultCategories())
    }

    suspend fun seedDatabase(database: AppDatabase) {
        val accountDao = database.accountDao()
        val categoryDao = database.categoryDao()
        val transactionDao = database.transactionDao()
        val debtDao = database.debtDao()
        val budgetDao = database.budgetDao()

        // Wipe old records first to avoid duplicates
        transactionDao.clearAllTransactions()
        debtDao.clearAllDebts()
        budgetDao.clearAllBudgets()
        accountDao.clearAllAccounts()
        categoryDao.clearAllCategories()

        // 1. Prepopulate Accounts with realistic balances
        val accounts = listOf(
            AccountEntity(1, "Cash", "নগদ টাকা", AccountType.CASH, 5800.0, "payments", "#10B981"),
            AccountEntity(2, "bKash", "বিকাশ", AccountType.BKASH, 16250.0, "account_balance_wallet", "#E2136E", "017XXXXXXXX"),
            AccountEntity(3, "Nagad", "নগদ", AccountType.NAGAD, 4200.0, "send_to_mobile", "#F97316", "018XXXXXXXX"),
            AccountEntity(4, "Bank Account", "ব্যাংক একাউন্ট", AccountType.BANK, 45000.0, "account_balance", "#2563EB", "****4582")
        )
        accountDao.insertAccounts(accounts)

        // 2. Prepopulate Categories
        categoryDao.insertCategories(getDefaultCategories())

        // 3. Prepopulate Sample Transactions
        val now = System.currentTimeMillis()
        val dayMillis = 86400000L
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val transactions = listOf(
            TransactionEntity(
                id = 1,
                type = TransactionType.INCOME,
                amount = 45000.0,
                accountId = 4,
                categoryId = 11,
                categoryNameBn = "মাসিক বেতন",
                categoryNameEn = "Salary",
                categoryIcon = "payments",
                categoryColorHex = "#10B981",
                accountNameBn = "ব্যাংক একাউন্ট",
                accountNameEn = "Bank Account",
                note = "সেপ্টেম্বর মাসের বেতন",
                timestamp = now - (dayMillis * 3),
                dateString = dateFormat.format(Date(now - (dayMillis * 3)))
            ),
            TransactionEntity(
                id = 2,
                type = TransactionType.INCOME,
                amount = 12000.0,
                accountId = 2,
                categoryId = 13,
                categoryNameBn = "ফ্রিল্যান্সিং",
                categoryNameEn = "Freelancing",
                categoryIcon = "laptop_mac",
                categoryColorHex = "#8B5CF6",
                accountNameBn = "বিকাশ",
                accountNameEn = "bKash",
                note = "ওয়েব ডিজাইন প্রজেক্টের সম্মানী",
                timestamp = now - (dayMillis * 2),
                dateString = dateFormat.format(Date(now - (dayMillis * 2)))
            ),
            TransactionEntity(
                id = 3,
                type = TransactionType.EXPENSE,
                amount = 2650.0,
                accountId = 1,
                categoryId = 1,
                categoryNameBn = "খাদ্য ও বাজার",
                categoryNameEn = "Food & Grocery",
                categoryIcon = "restaurant",
                categoryColorHex = "#EF4444",
                accountNameBn = "নগদ টাকা",
                accountNameEn = "Cash",
                note = "কাঁচা বাজার, মাছ ও মুরগি ক্রয়",
                timestamp = now - dayMillis,
                dateString = dateFormat.format(Date(now - dayMillis))
            ),
            TransactionEntity(
                id = 4,
                type = TransactionType.EXPENSE,
                amount = 1450.0,
                accountId = 2,
                categoryId = 3,
                categoryNameBn = "বিল ও ইউটিলিটি",
                categoryNameEn = "Bills & Utilities",
                categoryIcon = "receipt_long",
                categoryColorHex = "#3B82F6",
                accountNameBn = "বিকাশ",
                accountNameEn = "bKash",
                note = "ওয়াইফাই ইন্টারনেট ও বিদ্যুৎ বিল",
                timestamp = now - 18000000L,
                dateString = dateFormat.format(Date(now - 18000000L))
            ),
            TransactionEntity(
                id = 5,
                type = TransactionType.EXPENSE,
                amount = 350.0,
                accountId = 1,
                categoryId = 2,
                categoryNameBn = "যাতায়াত ও ভাড়া",
                categoryNameEn = "Transport & Rent",
                categoryIcon = "directions_bus",
                categoryColorHex = "#F59E0B",
                accountNameBn = "নগদ টাকা",
                accountNameEn = "Cash",
                note = "মেট্রোরেল ও সিএনজি ভাড়া",
                timestamp = now - 7200000L,
                dateString = dateFormat.format(Date(now - 7200000L))
            ),
            TransactionEntity(
                id = 6,
                type = TransactionType.EXPENSE,
                amount = 850.0,
                accountId = 3,
                categoryId = 1,
                categoryNameBn = "খাদ্য ও বাজার",
                categoryNameEn = "Food & Grocery",
                categoryIcon = "restaurant",
                categoryColorHex = "#EF4444",
                accountNameBn = "নগদ",
                accountNameEn = "Nagad",
                note = "রেস্তোরাঁয় দুপুরের লাঞ্চ",
                timestamp = now - 3600000L,
                dateString = dateFormat.format(Date(now - 3600000L))
            )
        )
        transactionDao.insertTransactions(transactions)

        // 4. Prepopulate Debts (দেনা-পাওনা)
        val debts = listOf(
            DebtEntity(
                id = 1,
                personName = "আরিফ হাসান",
                personPhone = "01711223344",
                amount = 3000.0,
                type = DebtType.RECEIVABLE,
                dueDate = now + (dayMillis * 7),
                createdDate = now - (dayMillis * 5),
                note = "জরুরি প্রয়োজনে ধার নিয়েছিল",
                isSettled = false
            ),
            DebtEntity(
                id = 2,
                personName = "তানভীর আহমেদ",
                personPhone = "01822334455",
                amount = 1500.0,
                type = DebtType.PAYABLE,
                dueDate = now + (dayMillis * 4),
                createdDate = now - (dayMillis * 3),
                note = "অফিস ট্যুরের শেয়ার খরচ",
                isSettled = false
            ),
            DebtEntity(
                id = 3,
                personName = "রাশেদ চৌধুরী",
                personPhone = "01933445566",
                amount = 5000.0,
                type = DebtType.RECEIVABLE,
                dueDate = now - dayMillis,
                createdDate = now - (dayMillis * 15),
                note = "বই কেনার জন্য নিয়েছিল",
                isSettled = true,
                settledDate = now - dayMillis
            )
        )
        for (debt in debts) {
            debtDao.insertDebt(debt)
        }

        // 5. Prepopulate Budgets
        val currentMonthYear = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        budgetDao.insertBudget(BudgetEntity(id = 1, categoryId = 1, monthlyLimit = 15000.0, monthYear = currentMonthYear))
        budgetDao.insertBudget(BudgetEntity(id = 2, categoryId = 2, monthlyLimit = 5000.0, monthYear = currentMonthYear))
        budgetDao.insertBudget(BudgetEntity(id = 3, categoryId = 3, monthlyLimit = 4000.0, monthYear = currentMonthYear))
        budgetDao.insertBudget(BudgetEntity(id = 4, categoryId = 4, monthlyLimit = 6000.0, monthYear = currentMonthYear))
    }
}
