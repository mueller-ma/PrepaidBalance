package com.github.muellerma.prepaidbalance.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class BalanceEntry(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val timestamp: Long,
    @ColumnInfo(name = "balance") val balance: Double,
    val fullResponse: String?
)