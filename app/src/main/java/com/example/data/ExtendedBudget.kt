package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "extended_budgets")
data class ExtendedBudget(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "OVERALL", "CATEGORY", "WEEKLY", "CUSTOM"
    val category: String? = null,
    val limitAmount: Double,
    val periodLabel: String,
    val startDate: Long,
    val endDate: Long
)
