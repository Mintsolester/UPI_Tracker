package com.example.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class CategoryInfo(
    val name: String,
    val icon: ImageVector,
    val color: Color
)

object CategoryRegistry {
    val categories = listOf(
        CategoryInfo("Food & Dining", Icons.Default.Restaurant, Color(0xFFFF5722)),
        CategoryInfo("Shopping", Icons.Default.ShoppingBag, Color(0xFFE91E63)),
        CategoryInfo("Utilities & Bills", Icons.Default.ReceiptLong, Color(0xFF2196F3)),
        CategoryInfo("Transport & Fuel", Icons.Default.DirectionsCar, Color(0xFF9C27B0)),
        CategoryInfo("Entertainment", Icons.Default.ConfirmationNumber, Color(0xFFE040FB)),
        CategoryInfo("Medical & Health", Icons.Default.MedicalServices, Color(0xFF4CAF50)),
        CategoryInfo("Travel", Icons.Default.Flight, Color(0xFF00BCD4)),
        CategoryInfo("Others", Icons.Default.Category, Color(0xFF607D8B))
    )

    fun getIcon(categoryName: String): ImageVector {
        return categories.find { it.name.equals(categoryName, ignoreCase = true) }?.icon ?: Icons.Default.Category
    }

    fun getColor(categoryName: String): Color {
        return categories.find { it.name.equals(categoryName, ignoreCase = true) }?.color ?: Color(0xFF607D8B)
    }
}
