package com.playapp.aiagents.data.model

data class PricePlan(
    val id: String,
    val title: String,
    val price: Double,
    val period: String,
    val features: List<String>,
    val buttonText: String,
    val isPopular: Boolean,
    val trialDays: Int = 0,
    val courseAccess: List<String> = emptyList()
)

data class CartItem(
    val id: String,
    val pricePlan: PricePlan,
    val quantity: Int = 1,
    val addedDate: Long = System.currentTimeMillis(),
    val userId: String? = null
)

data class Cart(
    val id: String,
    val userId: String,
    val items: List<CartItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val createdDate: Long = System.currentTimeMillis(),
    val updatedDate: Long = System.currentTimeMillis()
)