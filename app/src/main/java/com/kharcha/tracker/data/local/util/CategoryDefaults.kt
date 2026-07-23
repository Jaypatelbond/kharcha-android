package com.kharcha.tracker.data.local.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.kharcha.tracker.data.local.entity.CategoryEntity

object CategoryDefaults {
    // Colors from CategoryUi/Theme
    private val Colors = listOf(
        Color(0xFFFF6B6B), // 0: Food (Red)
        Color(0xFF4ECDC4), // 1: Groceries (Teal)
        Color(0xFFFFD93D), // 2: Transport (Yellow)
        Color(0xFFFF9F43), // 3: Auto (Orange)
        Color(0xFF5f27cd), // 4: Train (Purple)
        Color(0xFF54a0ff), // 5: Bills (Blue)
        Color(0xFF2e86de), // 6: Rent (Dark Blue)
        Color(0xFFf368e0), // 7: Electricity (Pink)
        Color(0xFF0abde3), // 8: Fuel (Cyan)
        Color(0xFF1dd1a1), // 9: Shopping (Green)
        Color(0xFFee5253), // 10: Health (Red)
        Color(0xFF00d2d3), // 11: Education (Cyan)
        Color(0xFF5f27cd), // 12: Entertainment (Purple)
        Color(0xFFc8d6e5), // 13: Chai (Grey)
        Color(0xFF222f3e), // 14: EMI (Dark)
        Color(0xFF8395a7), // 15: Subs (Grey)
        Color(0xFFff9ff3), // 16: Gifts (Pink)
        Color(0xFF576574)  // 17: Other (Grey)
    )

    fun getPrepopulatedCategories(): List<CategoryEntity> {
        val list = mutableListOf<CategoryEntity>()
        
        // Expense
        list.add(CategoryEntity(name = "Food & Dining", type = "EXPENSE", iconName = "restaurant", color = Colors[0].toArgb(), isDefault = true))
        list.add(CategoryEntity(name = "Groceries", type = "EXPENSE", iconName = "shopping_cart", color = Colors[1].toArgb(), isDefault = true))
        list.add(CategoryEntity(name = "Transport", type = "EXPENSE", iconName = "directions_bus", color = Colors[2].toArgb(), isDefault = true))
        list.add(CategoryEntity(name = "Auto/Rickshaw", type = "EXPENSE", iconName = "directions_bus", color = Colors[3].toArgb(), isDefault = true))
        list.add(CategoryEntity(name = "Train/Metro", type = "EXPENSE", iconName = "train", color = Colors[4].toArgb(), isDefault = true))
        list.add(CategoryEntity(name = "Recharge & Bills", type = "EXPENSE", iconName = "phone_android", color = Colors[5].toArgb(), isDefault = true))
        list.add(CategoryEntity(name = "Rent", type = "EXPENSE", iconName = "home", color = Colors[6].toArgb(), isDefault = true))
        list.add(CategoryEntity(name = "Electricity", type = "EXPENSE", iconName = "electric_bolt", color = Colors[7].toArgb(), isDefault = true))
        list.add(CategoryEntity(name = "Fuel/LPG", type = "EXPENSE", iconName = "local_gas_station", color = Colors[8].toArgb(), isDefault = true))
        list.add(CategoryEntity(name = "Shopping", type = "EXPENSE", iconName = "local_mall", color = Colors[9].toArgb(), isDefault = true))
        list.add(CategoryEntity(name = "Health & Medical", type = "EXPENSE", iconName = "local_hospital", color = Colors[10].toArgb(), isDefault = true))
        list.add(CategoryEntity(name = "Education", type = "EXPENSE", iconName = "school", color = Colors[11].toArgb(), isDefault = true))
        list.add(CategoryEntity(name = "Entertainment", type = "EXPENSE", iconName = "movie", color = Colors[12].toArgb(), isDefault = true))
        list.add(CategoryEntity(name = "Chai/Snacks", type = "EXPENSE", iconName = "local_cafe", color = Colors[13].toArgb(), isDefault = true))
        list.add(CategoryEntity(name = "EMI/Loan", type = "EXPENSE", iconName = "credit_card", color = Colors[14].toArgb(), isDefault = true))
        list.add(CategoryEntity(name = "Subscriptions", type = "EXPENSE", iconName = "subscriptions", color = Colors[15].toArgb(), isDefault = true))
        list.add(CategoryEntity(name = "Gifts & Donations", type = "EXPENSE", iconName = "card_giftcard", color = Colors[16].toArgb(), isDefault = true))
        list.add(CategoryEntity(name = "Others", type = "EXPENSE", iconName = "more_horiz", color = Colors[17].toArgb(), isDefault = true))

        // Income
        list.add(CategoryEntity(name = "Salary", type = "INCOME", iconName = "work", color = Colors[9].toArgb(), isDefault = true))
        list.add(CategoryEntity(name = "Freelance", type = "INCOME", iconName = "attach_money", color = Colors[2].toArgb(), isDefault = true))
        list.add(CategoryEntity(name = "Interest", type = "INCOME", iconName = "account_balance", color = Colors[6].toArgb(), isDefault = true))
        list.add(CategoryEntity(name = "Investment Returns", type = "INCOME", iconName = "trending_up", color = Colors[1].toArgb(), isDefault = true))
        list.add(CategoryEntity(name = "Gift Received", type = "INCOME", iconName = "card_giftcard", color = Colors[3].toArgb(), isDefault = true))
        list.add(CategoryEntity(name = "Other Income", type = "INCOME", iconName = "payments", color = Colors[4].toArgb(), isDefault = true))

        return list
    }
}
