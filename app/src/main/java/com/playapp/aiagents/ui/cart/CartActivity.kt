package com.playapp.aiagents.ui.cart

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.playapp.aiagents.R
import com.playapp.aiagents.data.model.CartItem
import com.playapp.aiagents.data.model.PricePlan
import com.playapp.aiagents.data.repository.CartRepository
import com.playapp.aiagents.ui.auth.SigninActivity
import com.playapp.aiagents.ui.viewmodel.CartViewModel
import com.playapp.aiagents.ui.playground.PlaygroundActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CartActivity : ComponentActivity() {
    private val viewModel: CartViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(CartViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return CartViewModel(application) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if user is authenticated (simplified check)
        val isAuthenticated = checkUserAuthentication()

        setContent {
            MaterialTheme {
                CartScreen(
                    viewModel = viewModel,
                    isAuthenticated = isAuthenticated,
                    onBackPressed = { finish() },
                    onSignInRequired = {
                        startActivity(Intent(this, SigninActivity::class.java))
                    },
                    onStartTrial = { planId ->
                        startTrial(planId)
                    }
                )
            }
        }
    }

    private fun checkUserAuthentication(): Boolean {
        // TODO: Implement proper authentication check with Firebase Auth
        // For now, return false to show sign-in prompt
        return false
    }

    private fun startTrial(planId: String) {
        val intent = Intent(this, PlaygroundActivity::class.java)
        intent.putExtra("plan_id", planId)
        intent.putExtra("is_trial", true)
        startActivity(intent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    viewModel: CartViewModel = viewModel(),
    isAuthenticated: Boolean = false,
    onBackPressed: () -> Unit = {},
    onSignInRequired: () -> Unit = {},
    onStartTrial: (String) -> Unit = {}
) {
    val cart by viewModel.cart.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Animation states
    var contentVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(200)
        contentVisible = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Shopping Cart",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (cart?.items?.isNotEmpty() == true) {
                        IconButton(onClick = { viewModel.clearCart() }) {
                            Icon(Icons.Filled.DeleteSweep, contentDescription = "Clear Cart")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (!isAuthenticated) {
                // Show sign-in prompt for unauthenticated users
                SignInPromptScreen(onSignInRequired = onSignInRequired)
            } else {
                // Show cart content for authenticated users
                AnimatedVisibility(
                    visible = contentVisible,
                    enter = fadeIn() + slideInVertically()
                ) {
                    if (cart?.items?.isEmpty() != false) {
                        EmptyCartScreen()
                    } else {
                        CartContentScreen(
                            cartItems = cart?.items ?: emptyList(),
                            totalAmount = cart?.totalAmount ?: 0.0,
                            onRemoveItem = { itemId ->
                                viewModel.removeFromCart(itemId)
                            },
                            onUpdateQuantity = { itemId, quantity ->
                                viewModel.updateQuantity(itemId, quantity)
                            },
                            onStartTrial = onStartTrial,
                            isLoading = isLoading
                        )
                    }
                }
            }

            // Error snackbar
            error?.let {
                LaunchedEffect(it) {
                    // Show error and auto-clear after delay
                    delay(3000)
                    viewModel.clearError()
                }

                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(it)
                }
            }

            // Loading overlay
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun SignInPromptScreen(onSignInRequired: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.AccountCircle,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Sign In Required",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Please sign in to access your cart and start your AI learning journey",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onSignInRequired,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.Filled.Login, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign In", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun EmptyCartScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.ShoppingCart,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.outline
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Your cart is empty",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Add some AI learning plans to get started on your journey",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CartContentScreen(
    cartItems: List<CartItem>,
    totalAmount: Double,
    onRemoveItem: (String) -> Unit,
    onUpdateQuantity: (String, Int) -> Unit,
    onStartTrial: (String) -> Unit,
    isLoading: Boolean
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(cartItems) { item ->
                CartItemCard(
                    cartItem = item,
                    onRemove = { onRemoveItem(item.id) },
                    onUpdateQuantity = { quantity -> onUpdateQuantity(item.id, quantity) }
                )
            }
        }

        // Cart summary and checkout
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$${"%.2f".format(totalAmount)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (cartItems.any { it.pricePlan.trialDays > 0 }) {
                    Button(
                        onClick = { onStartTrial(cartItems.first().pricePlan.id) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Free Trial", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { /* TODO: Implement checkout */ },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Filled.Payment, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Proceed to Checkout", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartItemCard(
    cartItem: CartItem,
    onRemove: () -> Unit,
    onUpdateQuantity: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = cartItem.pricePlan.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = cartItem.pricePlan.period,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (cartItem.pricePlan.trialDays > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${cartItem.pricePlan.trialDays} Days Free Trial",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Remove item",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Course access preview
            if (cartItem.pricePlan.courseAccess.isNotEmpty()) {
                Text(
                    text = "Includes access to:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                cartItem.pricePlan.courseAccess.take(3).forEach { course ->
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = course.replace("_", " ").capitalize(),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                if (cartItem.pricePlan.courseAccess.size > 3) {
                    Text(
                        text = "+${cartItem.pricePlan.courseAccess.size - 3} more courses",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quantity and price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { if (cartItem.quantity > 1) onUpdateQuantity(cartItem.quantity - 1) },
                        enabled = cartItem.quantity > 1
                    ) {
                        Icon(Icons.Filled.Remove, contentDescription = "Decrease quantity")
                    }

                    Text(
                        text = cartItem.quantity.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    IconButton(onClick = { onUpdateQuantity(cartItem.quantity + 1) }) {
                        Icon(Icons.Filled.Add, contentDescription = "Increase quantity")
                    }
                }

                Text(
                    text = "$${"%.2f".format(cartItem.pricePlan.price * cartItem.quantity)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}