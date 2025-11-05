package com.playapp.aiagents.ui.cart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

class CartActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                CartScreen()
            }
        }
    }
}

@Composable
fun CartScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Cart", style = MaterialTheme.typography.headlineMedium)
        // Add more UI here
    }
}

@Preview(showBackground = true)
@Composable
fun CartPreview() {
    MaterialTheme {
        CartScreen()
    }
}