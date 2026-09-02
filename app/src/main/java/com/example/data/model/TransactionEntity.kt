package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: TransactionType,
    val amount: Double,
    val accountId: Long,
    val toAccountId: Long? = null,
    val categoryId: Long,
    val categoryNameBn: String,
    val categoryNameEn: String,
    val categoryIcon: String,
    val categoryColorHex: String,
    val accountNameBn: String,
    val accountNameEn: String,
    val toAccountNameBn: String? = null,
    val toAccountNameEn: String? = null,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String // YYYY-MM-DD
)
