package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "split_bills")
data class SplitBill(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val totalAmount: Double,
    val paidByMemberId: Long,
    val timestamp: Long = System.currentTimeMillis()
)
