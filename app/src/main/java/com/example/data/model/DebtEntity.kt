package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "debts")
data class DebtEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val personName: String,
    val personPhone: String = "",
    val amount: Double,
    val type: DebtType,
    val dueDate: Long? = null,
    val createdDate: Long = System.currentTimeMillis(),
    val note: String = "",
    val isSettled: Boolean = false,
    val settledDate: Long? = null
)
