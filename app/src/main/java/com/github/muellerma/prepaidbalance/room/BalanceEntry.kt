package com.github.muellerma.prepaidbalance.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.math.abs

@Entity
data class BalanceEntry(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val timestamp: Long,
    @ColumnInfo(name = "balance") val balance: Double,
    val fullResponse: String?
) {
    fun nearlyEquals(other: BalanceEntry): Boolean {
        if (balance != other.balance) {
            return false
        }

        return abs(timestamp - other.timestamp) < 15 * 60 * 1000
    }
}