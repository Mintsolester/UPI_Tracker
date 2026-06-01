package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "upi_payments")
data class UpiPayment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val payeeName: String,
    val payeeUpiId: String?,
    val amount: Double,
    val category: String,
    val description: String?,
    val timestamp: Long = System.currentTimeMillis(),
    val isOutgoing: Boolean = true,
    val transactionRef: String? = null
)
