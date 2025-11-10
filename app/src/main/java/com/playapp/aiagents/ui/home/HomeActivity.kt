package com.playapp.aiagents.ui.home

import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
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
import com.playapp.aiagents.ui.viewmodel.BannerViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.Image
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.auth.FirebaseAuth
import com.playapp.aiagents.data.service.FirebaseService
import com.playapp.aiagents.ui.notifications.NotificationsActivity
import com.playapp.aiagents.ui.playground.PlaygroundActivity

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

    private val bannerViewModel: BannerViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(BannerViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return BannerViewModel(application) as T
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
                    bannerViewModel = bannerViewModel,
                    activityContext = this,
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
    bannerViewModel: BannerViewModel? = null,
    onGetStarted: () -> Unit = {},
    onSettings: () -> Unit = {},
    onSignUp: () -> Unit = {},
    onCartClick: () -> Unit = {},
    activityContext: Context = LocalContext.current,
    previewAgents: List<com.playapp.aiagents.data.model.Agent> = emptyList()
) {
    val agents by viewModel?.agents?.collectAsState() ?: remember { mutableStateOf(previewAgents) }
    val banners by bannerViewModel?.banners?.collectAsState() ?: remember { mutableStateOf(emptyList<com.playapp.aiagents.data.model.Banner>()) }
    val coroutineScope = rememberCoroutineScope()

    // Navigation state
    var selectedNavItem by remember { mutableStateOf(BottomNavItem.Home) }
    var showMenu by remember { mutableStateOf(false) }

    // User avatar state
    var userAvatarUrl by remember { mutableStateOf<String?>(null) }

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

    // Fetch user avatar URL
    val firebaseService = remember { FirebaseService() }
    val currentUser = FirebaseAuth.getInstance().currentUser
    LaunchedEffect(currentUser?.uid) {
        if (currentUser?.uid != null) {
            firebaseService.getUserProfile(currentUser.uid).collect { profile ->
                userAvatarUrl = profile?.avatarUrl?.takeIf { it.isNotEmpty() }
            }
        } else {
            userAvatarUrl = null
        }
    }

    val context = LocalContext.current

    // Handle navigation
    fun handleNavigation(item: BottomNavItem) {
        selectedNavItem = item
        when (item) {
            BottomNavItem.Home -> {
                // Already on home
            }
            BottomNavItem.Courses -> {
                // Navigate to courses
                onGetStarted()
            }
            BottomNavItem.Playground -> {
                // Handled directly in BottomNavigationBar
            }
            BottomNavItem.Profile -> {
                // Navigate to profile
                val intent = Intent(activityContext, com.playapp.aiagents.ui.profile.ProfileActivity::class.java)
                activityContext.startActivity(intent)
            }
            BottomNavItem.Menu -> {
                // Menu is handled separately in BottomNavigationBar
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                "https://res.cloudinary.com/dwhren8z6/image/upload/v1762566004/ic_aiagents_playapp-nobg_ngwaao.png"
                            ),
                            contentDescription = "AI Agents PlayApp Logo",
                            modifier = Modifier.size(32.dp)
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
        },
        bottomBar = {
            BottomNavigationBar(
                selectedItem = selectedNavItem,
                onItemSelected = ::handleNavigation,
                showMenu = showMenu,
                onShowMenuChange = { showMenu = it },
                agents = agents,
                userAvatarUrl = userAvatarUrl,
                context = context,
                onNavigateToHome = { /* Already on home */ },
                onNavigateToCourses = onGetStarted,
                onNavigateToCart = onCartClick,
                onNavigateToProfile = {
                    val intent = Intent(context, com.playapp.aiagents.ui.profile.ProfileActivity::class.java)
                    context.startActivity(intent)
                },
                onNavigateToSettings = onSettings,
                onNavigateToNotifications = {
                    val intent = Intent(context, com.playapp.aiagents.ui.notifications.NotificationsActivity::class.java)
                    context.startActivity(intent)
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
                    banners = banners,
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
    banners: List<com.playapp.aiagents.data.model.Banner> = emptyList(),
    onGetStarted: () -> Unit
) {
    // Auto-banner animation state
    var currentBannerIndex by remember { mutableStateOf(0) }

    // Auto-cycle through banners every 3 seconds with proper animation
    LaunchedEffect(banners) {
        if (banners.isNotEmpty() && banners.size > 1) {
            while (true) {
                delay(3000) // 3 seconds
                currentBannerIndex = (currentBannerIndex + 1) % banners.size
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Background banner image
        if (banners.isNotEmpty()) {
            val currentBanner = banners[currentBannerIndex]
            // Calculate width based on 996:760 aspect ratio (1.31:1)
            val bannerWidth = 300.dp * 1.31f
            Image(
                painter = rememberAsyncImagePainter(currentBanner.url),
                contentDescription = "Hero Banner",
                modifier = Modifier
                    .width(bannerWidth)
                    .height(300.dp)
                    .align(Alignment.Center),
                contentScale = ContentScale.Crop
            )
        } else {
            // Fallback gradient background if no banners
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
            )
        }

        // Content overlay at bottom
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            // Banner indicators
            if (banners.size > 1) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    banners.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .size(if (index == currentBannerIndex) 12.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    Color.White.copy(
                                        alpha = if (index == currentBannerIndex) 1f else 0.5f
                                    )
                                )
                                .clickable {
                                    currentBannerIndex = index
                                    println("HeroSection: Manual banner switch to index $index")
                                }
                        )
                    }
                }
            }

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

enum class BottomNavItem(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Home("Home", Icons.Filled.Home),
    Courses("Courses", Icons.Filled.School),
    Playground("Playground", Icons.Filled.SmartToy),
    Profile("Profile", Icons.Filled.Person),
    Menu("Menu", Icons.Filled.Menu)
}

@Composable
fun BottomNavigationBar(
    selectedItem: BottomNavItem,
    onItemSelected: (BottomNavItem) -> Unit,
    showMenu: Boolean,
    onShowMenuChange: (Boolean) -> Unit,
    agents: List<com.playapp.aiagents.data.model.Agent>,
    userAvatarUrl: String? = null,
    context: Context = LocalContext.current,
    onNavigateToHome: () -> Unit = {},
    onNavigateToCourses: () -> Unit = {},
    onNavigateToCart: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {}
) {
    Box {
        NavigationBar {
            BottomNavItem.values().forEach { item ->
                NavigationBarItem(
                    icon = {
                        if (item == BottomNavItem.Profile && userAvatarUrl?.isNotEmpty() == true) {
                            // Show user avatar for Profile item
                            Image(
                                painter = rememberAsyncImagePainter(userAvatarUrl),
                                contentDescription = "User Avatar",
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(item.icon, contentDescription = item.title)
                        }
                    },
                    label = null, // Remove text labels
                    selected = selectedItem == item,
                    onClick = {
                        if (item == BottomNavItem.Menu) {
                            onShowMenuChange(true)
                        } else if (item == BottomNavItem.Playground) {
                            // Check if user is authenticated or within trial period
                            val currentUser = FirebaseAuth.getInstance().currentUser
                            val isAuthenticated = currentUser != null
                            val isWithinTrial = currentUser?.metadata?.creationTimestamp?.let { creationTime ->
                                val thirtyDaysInMillis = 30L * 24L * 60L * 60L * 1000L // 30 days in milliseconds
                                val currentTime = System.currentTimeMillis()
                                (currentTime - creationTime) <= thirtyDaysInMillis
                            } ?: false

                            if (isAuthenticated) {
                                // Authenticated user - navigate to Dashboard (MainActivity)
                                val intent = Intent(context, com.playapp.aiagents.ui.main.MainActivity::class.java)
                                context.startActivity(intent)
                            } else if (isWithinTrial) {
                                // Trial user within 30 days - allow Playground access
                                val intent = Intent(context, PlaygroundActivity::class.java)
                                val firstAgent = agents.firstOrNull()
                                if (firstAgent != null) {
                                    intent.putExtra("agent_id", firstAgent.id)
                                }
                                context.startActivity(intent)
                            } else {
                                // Not authenticated and trial expired - redirect to sign in
                                android.widget.Toast.makeText(
                                    context,
                                    "Please sign in to access the AI Playground. Trial users get 30 days free access!",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                                val intent = Intent(context, com.playapp.aiagents.ui.auth.SigninActivity::class.java)
                                context.startActivity(intent)
                            }
                        } else {
                            onItemSelected(item)
                        }
                    }
                )
            }
        }

        // Dropdown Menu for Menu button
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { onShowMenuChange(false) }
        ) {
            DropdownMenuItem(
                text = { Text("Home") },
                onClick = {
                    onShowMenuChange(false)
                    onNavigateToHome()
                },
                leadingIcon = { Icon(Icons.Filled.Home, contentDescription = "Home") }
            )

            DropdownMenuItem(
                text = { Text("Courses") },
                onClick = {
                    onShowMenuChange(false)
                    onNavigateToCourses()
                },
                leadingIcon = { Icon(Icons.Filled.School, contentDescription = "Courses") }
            )

            DropdownMenuItem(
                text = { Text("Cart") },
                onClick = {
                    onShowMenuChange(false)
                    onNavigateToCart()
                },
                leadingIcon = { Icon(Icons.Filled.ShoppingCart, contentDescription = "Cart") }
            )

            DropdownMenuItem(
                text = { Text("Profile") },
                onClick = {
                    onShowMenuChange(false)
                    onNavigateToProfile()
                },
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = "Profile") }
            )

            DropdownMenuItem(
                text = { Text("Notifications") },
                onClick = {
                    onShowMenuChange(false)
                    onNavigateToNotifications()
                },
                leadingIcon = { Icon(Icons.Filled.Notifications, contentDescription = "Notifications") }
            )

            DropdownMenuItem(
                text = { Text("Settings") },
                onClick = {
                    onShowMenuChange(false)
                    onNavigateToSettings()
                },
                leadingIcon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") }
            )
        }
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
            modelType = "LLAMA2",
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
            modelType = "CODELLAMA",
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
            modelType = "MISTRAL",
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