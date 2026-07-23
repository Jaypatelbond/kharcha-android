package com.kharcha.core.designsystem.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.ElectricBolt
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.FoodBank
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.LocalGasStation
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.LocalMall
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Subscriptions
import androidx.compose.material.icons.rounded.Train
import androidx.compose.material.icons.rounded.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.kharcha.core.model.Category

data class CategoryUi(
    val icon: ImageVector,
    val color: Color
)

fun Category.ui(): CategoryUi {
    val iconVector = getCategoryIcon(this.iconName)
    return CategoryUi(iconVector, Color(this.color))
}

val CategoryIcons = mapOf(
    "restaurant" to Icons.Rounded.Restaurant,
    "shopping_cart" to Icons.Rounded.ShoppingCart,
    "directions_bus" to Icons.Rounded.DirectionsBus,
    "train" to Icons.Rounded.Train,
    "phone_android" to Icons.Rounded.PhoneAndroid,
    "home" to Icons.Rounded.Home,
    "electric_bolt" to Icons.Rounded.ElectricBolt,
    "local_gas_station" to Icons.Rounded.LocalGasStation,
    "local_mall" to Icons.Rounded.LocalMall,
    "local_hospital" to Icons.Rounded.LocalHospital,
    "school" to Icons.Rounded.School,
    "movie" to Icons.Rounded.Movie,
    "local_cafe" to Icons.Rounded.LocalCafe,
    "credit_card" to Icons.Rounded.CreditCard,
    "subscriptions" to Icons.Rounded.Subscriptions,
    "card_giftcard" to Icons.Rounded.CardGiftcard,
    "more_horiz" to Icons.Rounded.MoreHoriz,
    "work" to Icons.Rounded.Work,
    "attach_money" to Icons.Rounded.AttachMoney,
    "account_balance" to Icons.Rounded.AccountBalance,
    "trending_up" to Icons.AutoMirrored.Rounded.TrendingUp,
    "payments" to Icons.Rounded.Payments,
    "fitness_center" to Icons.Rounded.FitnessCenter,
    "food_bank" to Icons.Rounded.FoodBank
)

fun getCategoryIcon(iconName: String): ImageVector {
    return CategoryIcons[iconName] ?: Icons.Rounded.MoreHoriz
}
