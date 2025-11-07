package com.playapp.aiagents.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.playapp.aiagents.R
import com.playapp.aiagents.ui.main.MainActivity
import com.playapp.aiagents.ui.settings.SettingsActivity
import com.playapp.aiagents.ui.auth.SignupActivity
import com.playapp.aiagents.ui.viewmodel.AgentViewModel
import com.playapp.aiagents.data.repository.AgentRepository
import com.playapp.aiagents.ui.viewmodel.CartViewModel
import com.playapp.aiagents.data.repository.CartRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.tooling.preview.Preview

class HomeActivity : ComponentActivity() {
    private val viewModel: AgentViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(AgentViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return AgentViewModel(application, AgentRepository()) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

    private val cartViewModel: CartViewModel by viewModels {
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
        setContent {
            MaterialTheme {
                HomeScreen(
                    viewModel = viewModel,
                    cartViewModel = cartViewModel,
                    onGetStarted = {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    },
                    onSettings = {
                        startActivity(Intent(this, SettingsActivity::class.java))
                    },
                    onSignUp = {
                        startActivity(Intent(this, com.playapp.aiagents.ui.auth.SignupActivity::class.java))
                    },
                    onCartClick = {
                        startActivity(Intent(this, com.playapp.aiagents.ui.cart.CartActivity::class.java))
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: AgentViewModel? = null,
    cartViewModel: CartViewModel? = null,
    onGetStarted: () -> Unit = {},
    onSettings: () -> Unit = {},
    onSignUp: () -> Unit = {},
    onCartClick: () -> Unit = {},
    previewAgents: List<com.playapp.aiagents.data.model.Agent> = emptyList()
) {
    val agents by viewModel?.agents?.collectAsState() ?: remember { mutableStateOf(previewAgents) }
    val coroutineScope = rememberCoroutineScope()

    // Animation states
    var heroVisible by remember { mutableStateOf(false) }
    var featuresVisible by remember { mutableStateOf(false) }
    var agentsVisible by remember { mutableStateOf(false) }

    // Trigger animations
    LaunchedEffect(Unit) {
        delay(200)
        heroVisible = true
        delay(400)
        featuresVisible = true
        delay(400)
        agentsVisible = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.SmartToy,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "AI Playground",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    val cartItemCount by cartViewModel?.cart?.collectAsState() ?: remember { mutableStateOf(null) }
                    val itemCount = cartItemCount?.items?.sumOf { it.quantity } ?: 0

                    // Cart icon with badge
                    IconButton(onClick = onCartClick) {
                        BadgedBox(
                            badge = {
                                if (itemCount > 0) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = Color.White
                                    ) {
                                        Text(itemCount.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Filled.ShoppingCart, contentDescription = "Shopping Cart")
                        }
                    }

                    IconButton(onClick = onSignUp) {
                        Icon(Icons.Filled.PersonAdd, contentDescription = "Sign Up")
                    }
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hero Section
            item {
                HeroSection(
                    isVisible = heroVisible,
                    onGetStarted = onGetStarted
                )
            }

            // Features Section
            item {
                FeaturesSection(isVisible = featuresVisible)
            }

            // Agents Showcase
            item {
                AgentsShowcaseSection(
                    agents = agents.take(3), // Show first 3 agents
                    isVisible = agentsVisible,
                    onAgentClick = { _ ->
                        // Navigate to playground with specific agent
                        coroutineScope.launch {
                            onGetStarted()
                        }
                    }
                )
            }

            // Price Plan Section
            item {
                PricePlanSection(cartViewModel = cartViewModel)
            }

            // Stats Section
            item {
                StatsSection()
            }

            // CTA Section
            item {
                CallToActionSection(onGetStarted = onGetStarted)
            }

            // Footer
            item {
                FooterSection()
            }
        }
    }
}

@Composable
fun HeroSection(
    isVisible: Boolean,
    onGetStarted: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.surface
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Animated icon
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Psychology,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Welcome to AI Playground",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Explore, learn, and build with AI agents powered by Ollama. Experience the future of conversational AI on your device.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            AnimatedVisibility(visible = isVisible) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onGetStarted,
                        modifier = Modifier
                            .height(56.dp)
                            .padding(horizontal = 24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Get Started", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    }

                    OutlinedButton(
                        onClick = { /* Scroll to features */ },
                        modifier = Modifier.height(56.dp)
                    ) {
                        Text("Learn More")
                    }
                }
            }
        }
    }
}

@Composable
fun FeaturesSection(isVisible: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Powerful Features",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Everything you need to master AI agent development",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FeatureCard(
                icon = Icons.Filled.Message,
                title = "Interactive Chat",
                description = "Converse with AI agents in real-time",
                isVisible = isVisible
            )

            FeatureCard(
                icon = Icons.Filled.Code,
                title = "Code Generation",
                description = "Generate and explore code examples",
                isVisible = isVisible
            )
        }
    }
}

@Composable
fun FeatureCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    isVisible: Boolean
) {
    AnimatedVisibility(visible = isVisible) {
        Card(
            modifier = Modifier
                .width(160.dp)
                .padding(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AgentsShowcaseSection(
    agents: List<com.playapp.aiagents.data.model.Agent>,
    isVisible: Boolean,
    onAgentClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp) // Fixed height to take up remaining white space
            .padding(24.dp)
    ) {
        Text(
            text = "Featured AI Agents",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Meet our intelligent AI companions",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Group agents into rows of 2 for grid layout
            val agentRows = agents.chunked(2)
            items(agentRows) { rowAgents ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    rowAgents.forEach { agent ->
                        AnimatedVisibility(
                            visible = isVisible,
                            modifier = Modifier.weight(1f)
                        ) {
                            AgentCard(
                                agent = agent,
                                onClick = { onAgentClick(agent.id) }
                            )
                        }
                    }
                    // Fill empty space if odd number of agents
                    if (rowAgents.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun AgentCard(
    agent: com.playapp.aiagents.data.model.Agent,
    onClick: () -> Unit
) {
    val backgroundColor = when (agent.id % 5) {
        0 -> Color(0xFF4CAF50) // Green
        1 -> Color(0xFF2196F3) // Blue
        2 -> Color(0xFFFF9800) // Orange
        3 -> Color(0xFFE91E63) // Pink
        4 -> Color(0xFF9C27B0) // Purple
        else -> Color(0xFF607D8B) // Grey
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Agent ${agent.id}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = agent.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = agent.description.take(100) + if (agent.description.length > 100) "..." else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = "Rating",
                        tint = Color.Yellow,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "4.${agent.id % 5 + 5}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                }

                Text(
                    text = agent.duration,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun StatsSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem(
            value = "10+",
            label = "AI Agents",
            icon = Icons.Filled.SmartToy
        )

        StatItem(
            value = "24/7",
            label = "Available",
            icon = Icons.Filled.AccessTime
        )

        StatItem(
            value = "Local",
            label = "Privacy",
            icon = Icons.Filled.Security
        )
    }
}

@Composable
fun StatItem(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CallToActionSection(onGetStarted: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Ready to Start Your AI Journey?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Join thousands of developers exploring the future of AI",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onGetStarted,
                modifier = Modifier
                    .height(56.dp)
                    .padding(horizontal = 32.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Start Exploring", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }
    }
}

@Composable
fun PricePlanSection(cartViewModel: CartViewModel?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = "Choose Your Plan",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Select the perfect plan for your AI learning journey",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            modifier = Modifier.height(400.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                PricingCard(
                    title = "Free",
                    price = "$0",
                    period = "Forever",
                    features = listOf(
                        "Access to 3 AI Agents",
                        "Basic Chat Functionality",
                        "Community Support",
                        "Local AI Processing"
                    ),
                    buttonText = "Get Started",
                    isPopular = false,
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    cartViewModel = cartViewModel,
                    planId = "free"
                )
            }

            item {
                PricingCard(
                    title = "Pro",
                    price = "$9.99",
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
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                    cartViewModel = cartViewModel,
                    planId = "pro"
                )
            }

            item {
                PricingCard(
                    title = "Enterprise",
                    price = "$29.99",
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
                    backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                    cartViewModel = cartViewModel,
                    planId = "enterprise"
                )
            }
        }
    }
}

@Composable
fun PricingCard(
    title: String,
    price: String,
    period: String,
    features: List<String>,
    buttonText: String,
    isPopular: Boolean,
    backgroundColor: Color,
    cartViewModel: CartViewModel?,
    planId: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isPopular) {
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Most Popular",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = price,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = period,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            features.forEach { feature ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = feature,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val pricePlan = cartViewModel!!.getPricePlan(planId)
                    if (pricePlan != null) {
                        cartViewModel.addToCart(pricePlan)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPopular) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(Icons.Filled.AddShoppingCart, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add to Cart", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun FooterSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Built with ❤️ for AI enthusiasts",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Powered by Ollama • Local AI • Privacy First",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val sampleAgents = listOf(
        com.playapp.aiagents.data.model.Agent(
            id = 1,
            title = "Conversational AI Assistant",
            provider = "Ollama",
            instructor = "AI Team",
            duration = "5 min",
            description = "A helpful AI assistant for general conversations and queries.",
            color = "#2196F3",
            topics = listOf("Chat", "General"),
            ollamaPrompt = "You are a helpful assistant.",
            model = "llama2",
            modelType = com.playapp.aiagents.data.model.OllamaModel.LLAMA2,
            samplePrompts = listOf("Hello!", "How are you?"),
            setupInstructions = "Just start chatting.",
            supportsStreaming = true
        ),
        com.playapp.aiagents.data.model.Agent(
            id = 2,
            title = "Code Generator",
            provider = "Ollama",
            instructor = "Dev Team",
            duration = "10 min",
            description = "Generate code snippets and explain programming concepts.",
            color = "#4CAF50",
            topics = listOf("Coding", "Programming"),
            ollamaPrompt = "You are a code expert.",
            model = "codellama",
            modelType = com.playapp.aiagents.data.model.OllamaModel.CODELLAMA,
            samplePrompts = listOf("Write a function to sort an array.", "Explain recursion."),
            setupInstructions = "Provide code examples.",
            supportsStreaming = true
        ),
        com.playapp.aiagents.data.model.Agent(
            id = 3,
            title = "Creative Writer",
            provider = "Ollama",
            instructor = "Creative Team",
            duration = "15 min",
            description = "Assist with creative writing and storytelling.",
            color = "#FF9800",
            topics = listOf("Writing", "Creativity"),
            ollamaPrompt = "You are a creative writer.",
            model = "mistral",
            modelType = com.playapp.aiagents.data.model.OllamaModel.MISTRAL,
            samplePrompts = listOf("Write a short story.", "Brainstorm plot ideas."),
            setupInstructions = "Focus on narrative.",
            supportsStreaming = true
        )
    )
    MaterialTheme {
        HomeScreen(
            onGetStarted = {},
            onSettings = {},
            onSignUp = {},
            onCartClick = {},
            previewAgents = sampleAgents
        )
    }
}