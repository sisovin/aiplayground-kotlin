package com.playapp.aiagents.data.repository

import com.playapp.aiagents.data.model.Cart
import com.playapp.aiagents.data.model.CartItem
import com.playapp.aiagents.data.model.PricePlan
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow

class CartRepository {
    private val _cart = MutableStateFlow<Cart?>(null)
    val cart: Flow<Cart?> = _cart

    // Predefined price plans
    val pricePlans = listOf(
        PricePlan(
            id = "free",
            title = "Free",
            price = 0.0,
            period = "Forever",
            features = listOf(
                "Access to 3 AI Agents",
                "Basic Chat Functionality",
                "Community Support",
                "Local AI Processing"
            ),
            buttonText = "Get Started",
            isPopular = false,
            trialDays = 0,
            courseAccess = listOf("agent_1", "agent_2", "agent_3")
        ),
        PricePlan(
            id = "pro",
            title = "Pro",
            price = 9.99,
            period = "per month",
            features = listOf(
                "Access to All AI Agents",
                "Advanced Chat Features",
                "Priority Support",
                "Custom Model Training",
                "API Access",
                "Offline Mode"
            ),
            buttonText = "Start Pro Trial",
            isPopular = true,
            trialDays = 14,
            courseAccess = listOf("all_agents")
        ),
        PricePlan(
            id = "enterprise",
            title = "Enterprise",
            price = 29.99,
            period = "per month",
            features = listOf(
                "Everything in Pro",
                "Team Collaboration",
                "Advanced Analytics",
                "Custom Integrations",
                "Dedicated Support",
                "SLA Guarantee",
                "White-label Solution"
            ),
            buttonText = "Contact Sales",
            isPopular = false,
            trialDays = 30,
            courseAccess = listOf("all_agents", "enterprise_features")
        )
    )

    fun getPricePlan(planId: String): PricePlan? {
        return pricePlans.find { it.id == planId }
    }

    fun createCart(userId: String): Cart {
        val newCart = Cart(
            id = "cart_$userId",
            userId = userId,
            items = emptyList(),
            totalAmount = 0.0
        )
        _cart.value = newCart
        return newCart
    }

    fun addToCart(userId: String, pricePlan: PricePlan): Boolean {
        val currentCart = _cart.value ?: createCart(userId)

        // Check if item already exists
        val existingItem = currentCart.items.find { it.pricePlan.id == pricePlan.id }

        val updatedItems = if (existingItem != null) {
            // Update quantity if item exists
            currentCart.items.map { item ->
                if (item.pricePlan.id == pricePlan.id) {
                    item.copy(quantity = item.quantity + 1)
                } else item
            }
        } else {
            // Add new item
            currentCart.items + CartItem(
                id = "${pricePlan.id}_${System.currentTimeMillis()}",
                pricePlan = pricePlan,
                quantity = 1,
                userId = userId
            )
        }

        val totalAmount = updatedItems.sumOf { it.pricePlan.price * it.quantity }

        val updatedCart = currentCart.copy(
            items = updatedItems,
            totalAmount = totalAmount,
            updatedDate = System.currentTimeMillis()
        )

        _cart.value = updatedCart
        return true
    }

    fun removeFromCart(itemId: String): Boolean {
        val currentCart = _cart.value ?: return false

        val updatedItems = currentCart.items.filter { it.id != itemId }
        val totalAmount = updatedItems.sumOf { it.pricePlan.price * it.quantity }

        val updatedCart = currentCart.copy(
            items = updatedItems,
            totalAmount = totalAmount,
            updatedDate = System.currentTimeMillis()
        )

        _cart.value = updatedCart
        return true
    }

    fun updateQuantity(itemId: String, quantity: Int): Boolean {
        if (quantity <= 0) return removeFromCart(itemId)

        val currentCart = _cart.value ?: return false

        val updatedItems = currentCart.items.map { item ->
            if (item.id == itemId) {
                item.copy(quantity = quantity)
            } else item
        }

        val totalAmount = updatedItems.sumOf { it.pricePlan.price * it.quantity }

        val updatedCart = currentCart.copy(
            items = updatedItems,
            totalAmount = totalAmount,
            updatedDate = System.currentTimeMillis()
        )

        _cart.value = updatedCart
        return true
    }

    fun clearCart(): Boolean {
        val currentCart = _cart.value ?: return false
        val clearedCart = currentCart.copy(
            items = emptyList(),
            totalAmount = 0.0,
            updatedDate = System.currentTimeMillis()
        )
        _cart.value = clearedCart
        return true
    }

    fun getCartItemCount(): Int {
        return _cart.value?.items?.sumOf { it.quantity } ?: 0
    }
}