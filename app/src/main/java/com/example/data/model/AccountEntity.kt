package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nameEn: String,
    val nameBn: String,
    val type: AccountType,
    val balance: Double,
    val iconName: String = "wallet",
    val colorHex: String = "#059669",
    val accountNumber: String = ""
)
