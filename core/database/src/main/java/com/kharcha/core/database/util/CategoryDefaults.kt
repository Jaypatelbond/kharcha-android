package com.kharcha.core.database.util

import com.kharcha.core.database.entity.CategoryEntity

object CategoryDefaults {
    // Colors from CategoryUi/Theme
    private val Colors = listOf(
        0xFFFF6B6B.toInt(), // 0: Food (Red)
        0xFF4ECDC4.toInt(), // 1: Groceries (Teal)
        0xFFFFD93D.toInt(), // 2: Transport (Yellow)
        0xFFFF9F43.toInt(), // 3: Auto (Orange)
        0xFF5f27cd.toInt(), // 4: Train (Purple)
        0xFF54a0ff.toInt(), // 5: Bills (Blue)
        0xFF2e86de.toInt(), // 6: Rent (Dark Blue)
        0xFFf368e0.toInt(), // 7: Electricity (Pink)
        0xFF0abde3.toInt(), // 8: Fuel (Cyan)
        0xFF1dd1a1.toInt(), // 9: Shopping (Green)
        0xFFee5253.toInt(), // 10: Health (Red)
        0xFF00d2d3.toInt(), // 11: Education (Cyan)
        0xFF5f27cd.toInt(), // 12: Entertainment (Purple)
        0xFFc8d6e5.toInt(), // 13: Chai (Grey)
        0xFF222f3e.toInt(), // 14: EMI (Dark)
        0xFF8395a7.toInt(), // 15: Subs (Grey)
        0xFFff9ff3.toInt(), // 16: Gifts (Pink)
        0xFF576574.toInt()  // 17: Other (Grey)
    )

    fun getPrepopulatedCategories(): List<CategoryEntity> {
        val list = mutableListOf<CategoryEntity>()
        
        // Expense
        list.add(CategoryEntity(name = "Food & Dining", type = "EXPENSE", iconName = "restaurant", color = Colors[0], isDefault = true))
        list.add(CategoryEntity(name = "Groceries", type = "EXPENSE", iconName = "shopping_cart", color = Colors[1], isDefault = true))
        list.add(CategoryEntity(name = "Transport", type = "EXPENSE", iconName = "directions_bus", color = Colors[2], isDefault = true))
        list.add(CategoryEntity(name = "Auto/Rickshaw", type = "EXPENSE", iconName = "directions_bus", color = Colors[3], isDefault = true))
        list.add(CategoryEntity(name = "Train/Metro", type = "EXPENSE", iconName = "train", color = Colors[4], isDefault = true))
        list.add(CategoryEntity(name = "Recharge & Bills", type = "EXPENSE", iconName = "phone_android", color = Colors[5], isDefault = true))
        list.add(CategoryEntity(name = "Rent", type = "EXPENSE", iconName = "home", color = Colors[6], isDefault = true))
        list.add(CategoryEntity(name = "Electricity", type = "EXPENSE", iconName = "electric_bolt", color = Colors[7], isDefault = true))
        list.add(CategoryEntity(name = "Fuel/LPG", type = "EXPENSE", iconName = "local_gas_station", color = Colors[8], isDefault = true))
        list.add(CategoryEntity(name = "Shopping", type = "EXPENSE", iconName = "local_mall", color = Colors[9], isDefault = true))
        list.add(CategoryEntity(name = "Health & Medical", type = "EXPENSE", iconName = "local_hospital", color = Colors[10], isDefault = true))
        list.add(CategoryEntity(name = "Education", type = "EXPENSE", iconName = "school", color = Colors[11], isDefault = true))
        list.add(CategoryEntity(name = "Entertainment", type = "EXPENSE", iconName = "movie", color = Colors[12], isDefault = true))
        list.add(CategoryEntity(name = "Chai/Snacks", type = "EXPENSE", iconName = "local_cafe", color = Colors[13], isDefault = true))
        list.add(CategoryEntity(name = "EMI/Loan", type = "EXPENSE", iconName = "credit_card", color = Colors[14], isDefault = true))
        list.add(CategoryEntity(name = "Subscriptions", type = "EXPENSE", iconName = "subscriptions", color = Colors[15], isDefault = true))
        list.add(CategoryEntity(name = "Gifts & Donations", type = "EXPENSE", iconName = "card_giftcard", color = Colors[16], isDefault = true))
        list.add(CategoryEntity(name = "Others", type = "EXPENSE", iconName = "more_horiz", color = Colors[17], isDefault = true))

        // Income
        list.add(CategoryEntity(name = "Salary", type = "INCOME", iconName = "work", color = Colors[9], isDefault = true))
        list.add(CategoryEntity(name = "Freelance", type = "INCOME", iconName = "attach_money", color = Colors[2], isDefault = true))
        list.add(CategoryEntity(name = "Interest", type = "INCOME", iconName = "account_balance", color = Colors[6], isDefault = true))
        list.add(CategoryEntity(name = "Investment Returns", type = "INCOME", iconName = "trending_up", color = Colors[1], isDefault = true))
        list.add(CategoryEntity(name = "Gift Received", type = "INCOME", iconName = "card_giftcard", color = Colors[3], isDefault = true))
        list.add(CategoryEntity(name = "Other Income", type = "INCOME", iconName = "payments", color = Colors[4], isDefault = true))

        return list
    }
}
