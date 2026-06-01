package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bill_owes")
data class BillOwe(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val billId: Long,
    val debtorMemberId: Long,
    val amount: Double,
    val isSettled: Boolean = false
)
