package com.example.data.model

enum class TransactionType {
    EXPENSE,
    INCOME,
    TRANSFER
}

enum class AccountType {
    CASH,
    BKASH,
    NAGAD,
    ROCKET,
    BANK,
    CARD,
    SAVINGS,
    OTHER
}

enum class DebtType {
    RECEIVABLE, // পাবো (I will receive / Lent to someone)
    PAYABLE     // দেবো (I have to pay / Borrowed from someone)
}
