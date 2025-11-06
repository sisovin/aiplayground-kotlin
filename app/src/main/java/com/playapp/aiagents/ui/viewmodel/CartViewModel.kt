package com.playapp.aiagents.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.playapp.aiagents.data.model.Cart
import com.playapp.aiagents.data.model.CartItem
import com.playapp.aiagents.data.model.PricePlan
import com.playapp.aiagents.data.repository.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CartViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CartRepository()

    private val _cart = MutableStateFlow<Cart?>(null)
    val cart: StateFlow<Cart?> = _cart.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    init {
        viewModelScope.launch {
            repository.cart.collect { cart ->
                _cart.value = cart
            }
        }
    }

    fun setCurrentUser(userId: String?) {
        _currentUserId.value = userId
        if (userId != null && _cart.value == null) {
            repository.createCart(userId)
        }
    }

    fun addToCart(pricePlan: PricePlan) {
        val userId = _currentUserId.value
        if (userId == null) {
            _error.value = "Please sign in to add items to cart"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val success = repository.addToCart(userId, pricePlan)
                if (!success) {
                    _error.value = "Failed to add item to cart"
                }
            } catch (e: Exception) {
                _error.value = "Error adding item to cart: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun removeFromCart(itemId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val success = repository.removeFromCart(itemId)
                if (!success) {
                    _error.value = "Failed to remove item from cart"
                }
            } catch (e: Exception) {
                _error.value = "Error removing item from cart: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateQuantity(itemId: String, quantity: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val success = repository.updateQuantity(itemId, quantity)
                if (!success) {
                    _error.value = "Failed to update item quantity"
                }
            } catch (e: Exception) {
                _error.value = "Error updating item quantity: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val success = repository.clearCart()
                if (!success) {
                    _error.value = "Failed to clear cart"
                }
            } catch (e: Exception) {
                _error.value = "Error clearing cart: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getPricePlan(planId: String): PricePlan? {
        return repository.getPricePlan(planId)
    }

    fun getCartItemCount(): Int {
        return repository.getCartItemCount()
    }

    fun clearError() {
        _error.value = null
    }

    fun getPricePlans(): List<PricePlan> {
        return repository.pricePlans
    }
}