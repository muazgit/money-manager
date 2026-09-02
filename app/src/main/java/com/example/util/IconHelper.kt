package com.example.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.LaptopMac
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SendToMobile
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.ui.graphics.vector.ImageVector

object IconHelper {
    fun getIcon(name: String): ImageVector {
        return when (name) {
            "restaurant" -> Icons.Default.Restaurant
            "directions_bus" -> Icons.Default.DirectionsBus
            "receipt_long" -> Icons.Default.ReceiptLong
            "shopping_bag" -> Icons.Default.ShoppingBag
            "school" -> Icons.Default.School
            "medical_services" -> Icons.Default.MedicalServices
            "movie" -> Icons.Default.Movie
            "family_restroom" -> Icons.Default.FamilyRestroom
            "volunteer_activism" -> Icons.Default.VolunteerActivism
            "payments" -> Icons.Default.Payments
            "storefront" -> Icons.Default.Storefront
            "laptop_mac" -> Icons.Default.LaptopMac
            "currency_exchange" -> Icons.Default.CurrencyExchange
            "trending_up" -> Icons.Default.TrendingUp
            "card_giftcard" -> Icons.Default.CardGiftcard
            "savings" -> Icons.Default.Savings
            "account_balance_wallet" -> Icons.Default.AccountBalanceWallet
            "send_to_mobile" -> Icons.Default.SendToMobile
            "account_balance" -> Icons.Default.AccountBalance
            "credit_card" -> Icons.Default.CreditCard
            "swap_horiz" -> Icons.Default.SwapHoriz
            else -> Icons.Default.MoreHoriz
        }
    }
}
