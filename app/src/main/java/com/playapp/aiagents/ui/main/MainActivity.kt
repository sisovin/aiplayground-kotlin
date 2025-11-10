package com.playapp.aiagents.ui.main

import android.os.Bundle
import android.content.Intent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.*
import androidx.compose.animation.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.lifecycleScope
import com.playapp.aiagents.R
import com.playapp.aiagents.data.model.Agent
import com.playapp.aiagents.data.repository.AgentRepository
import com.playapp.aiagents.ui.viewmodel.AgentViewModel
import com.playapp.aiagents.ui.detail.CourseDetailActivity
import com.playapp.aiagents.ui.home.HomeActivity
import com.playapp.aiagents.ui.courses.CoursesActivity
import com.playapp.aiagents.ui.cart.CartActivity
import com.playapp.aiagents.ui.notifications.NotificationsActivity
import com.playapp.aiagents.ui.profile.ProfileActivity
import com.playapp.aiagents.ui.settings.SettingsActivity
import com.playapp.aiagents.ui.auth.SigninActivity
import com.playapp.aiagents.ui.auth.SignupActivity
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.rememberAsyncImagePainter
import com.playapp.aiagents.data.service.FirebaseService
import com.playapp.aiagents.data.model.UserProfile
import com.playapp.aiagents.data.model.UserCourseProgress
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import com.playapp.aiagents.data.service.FirebaseAuthService

data class NavItem(val label: String, val icon: ImageVector)

@Composable
fun Chip(text: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun CourseDetailScreen(agent: Agent, index: Int, onClick: () -> Unit = {}) {
     Box(
         modifier = Modifier
             .fillMaxWidth()
             .wrapContentHeight()
             .padding(horizontal = 16.dp, vertical = 5.dp)
     ) {
         Column(
             modifier = Modifier.fillMaxSize(),
             verticalArrangement = Arrangement.Center,
             horizontalAlignment = Alignment.CenterHorizontally
         ) {
             Box {
                 Card(
                     modifier = Modifier.fillMaxWidth(),
                     colors = CardDefaults.cardColors(containerColor = Color(AndroidColor.parseColor(agent.color))),
                     onClick = onClick
                 ) {
                     Column(
                         modifier = Modifier.padding(16.dp),
                         horizontalAlignment = Alignment.CenterHorizontally
                     ) {
                         Text(
                             text = agent.title,
                             style = MaterialTheme.typography.headlineMedium,
                             color = Color.White,
                             fontWeight = FontWeight.Bold,
                             textAlign = TextAlign.Center
                         )
                         Spacer(modifier = Modifier.height(8.dp))
                         Text(
                             text = "By ${agent.instructor}",
                             style = MaterialTheme.typography.bodyLarge,
                             color = Color.White.copy(alpha = 0.8f)
                         )
                         Spacer(modifier = Modifier.height(8.dp))
                         Text(
                             text = agent.provider,
                             style = MaterialTheme.typography.bodyMedium,
                             color = Color.White
                         )
                         Spacer(modifier = Modifier.height(8.dp))
                         Text(
                             text = agent.duration,
                             style = MaterialTheme.typography.bodySmall,
                             color = Color.White.copy(alpha = 0.8f)
                         )
                         Spacer(modifier = Modifier.height(16.dp))
                         Text(
                             text = agent.description,
                             style = MaterialTheme.typography.bodyMedium,
                             color = Color.White,
                             textAlign = TextAlign.Center
                         )
                        Spacer(modifier = Modifier.height(16.dp))
                         Text(
                             text = "Topics: ${agent.topics.joinToString(", ")}",
                             style = MaterialTheme.typography.bodySmall,
                             color = Color.White.copy(alpha = 0.8f),
                             textAlign = TextAlign.Center
                         )
                     }
                 }

                 Box(
                     modifier = Modifier
                         .align(Alignment.TopEnd)
                         .padding(10.dp)
                         .size(32.dp)
                         .background(Color.Blue, CircleShape),
                     contentAlignment = Alignment.Center
                 ) {
                     Text(text = index.toString(), color = Color.White)
                 }
             }
         }
     }
 }

class MainActivity : ComponentActivity() {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Handle email link authentication
        handleEmailLinkSignIn()

        // Check authentication status
        val authService = FirebaseAuthService()
        val isAuthenticated = runBlocking {
            authService.isUserLoggedIn
        }

        if (!isAuthenticated) {
            // Redirect to sign-in if not authenticated
            startActivity(Intent(this, SigninActivity::class.java))
            finish()
            return
        }

        setContent {
            MaterialTheme {
                DashboardScreen(
                    viewModel = viewModel,
                    activityContext = this@MainActivity,
                    onNavigateToCourseDetail = { courseId ->
                        val intent = Intent(this@MainActivity, CourseDetailActivity::class.java)
                        intent.putExtra("course_id", courseId)
                        startActivity(intent)
                    },
                    onNavigateToHome = {
                        val intent = Intent(this@MainActivity, HomeActivity::class.java)
                        startActivity(intent)
                    },
                    onNavigateToCourses = {
                        val intent = Intent(this@MainActivity, CoursesActivity::class.java)
                        startActivity(intent)
                    },
                    onNavigateToCart = {
                        val intent = Intent(this@MainActivity, CartActivity::class.java)
                        startActivity(intent)
                    },
                    onNavigateToProfile = {
                        val intent = Intent(this@MainActivity, ProfileActivity::class.java)
                        startActivity(intent)
                    },
                    onNavigateToSettings = {
                        val intent = Intent(this@MainActivity, SettingsActivity::class.java)
                        startActivity(intent)
                    },
                    onSignIn = {
                        val intent = Intent(this@MainActivity, SigninActivity::class.java)
                        startActivity(intent)
                    },
                    onSignOut = {
                        // Implement Firebase sign out
                        val authService = FirebaseAuthService()
                        lifecycleScope.launch {
                            try {
                                authService.signOut()
                                // Redirect to sign-in activity
                                startActivity(Intent(this@MainActivity, SigninActivity::class.java))
                                finish()
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(
                                    this@MainActivity,
                                    "Failed to sign out: ${e.message}",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                )
            }
        }
    }

    private fun handleEmailLinkSignIn() {
        val authService = FirebaseAuthService()

        // Check if the intent contains an email link
        val emailLink = intent.data?.toString()
        if (emailLink != null && authService.isSignInWithEmailLink(emailLink)) {
            // Show a dialog to get the user's email
            showEmailInputDialog { email ->
                lifecycleScope.launch {
                    try {
                        val result = authService.signInWithEmailLink(email, emailLink)
                        result.fold(
                            onSuccess = {
                                // Successfully signed in, continue with normal flow
                                Toast.makeText(this@MainActivity, "Successfully signed in!", Toast.LENGTH_SHORT).show()
                            },
                            onFailure = { exception ->
                                Toast.makeText(this@MainActivity, "Sign in failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                                // Redirect to sign-in activity
                                startActivity(Intent(this@MainActivity, SigninActivity::class.java))
                                finish()
                            }
                        )
                    } catch (e: Exception) {
                        Toast.makeText(this@MainActivity, "Sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@MainActivity, SigninActivity::class.java))
                        finish()
                    }
                }
            }
        }
    }

    private fun showEmailInputDialog(onEmailEntered: (String) -> Unit) {
        val editText = android.widget.EditText(this)
        editText.hint = "Enter your email"

        android.app.AlertDialog.Builder(this)
            .setTitle("Complete Sign In")
            .setMessage("Please enter your email address to complete the sign in process.")
            .setView(editText)
            .setPositiveButton("Sign In") { _, _ ->
                val email = editText.text.toString().trim()
                if (email.isNotEmpty()) {
                    onEmailEntered(email)
                } else {
                    Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, SigninActivity::class.java))
                    finish()
                }
            }
            .setNegativeButton("Cancel") { _, _ ->
                startActivity(Intent(this, SigninActivity::class.java))
                finish()
            }
            .setCancelable(false)
            .show()
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: AgentViewModel,
    activityContext: android.content.Context? = null,
    onNavigateToCourseDetail: (Int) -> Unit = {},
    onNavigateToCourses: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToCart: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onSignIn: () -> Unit = {},
    onSignOut: () -> Unit = {}
) {
    val agents by viewModel.agents.collectAsState()
    var selectedItem by remember { mutableStateOf(0) }
    var showMenu by remember { mutableStateOf(false) }

    // Authentication state
    val authService = remember { FirebaseAuthService() }
    var currentUser by remember { mutableStateOf<com.google.firebase.auth.FirebaseUser?>(null) }
    var isUserLoggedIn by remember { mutableStateOf(false) }

    // User profile state
    val firebaseService = remember { FirebaseService() }
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }

    // Course progress state
    var courseProgress by remember { mutableStateOf<List<UserCourseProgress>>(emptyList()) }

    // Load auth state
    LaunchedEffect(Unit) {
        authService.getAuthStateFlow().collect { user ->
            currentUser = user
            isUserLoggedIn = user != null

            // Load user profile when user is authenticated
            if (user != null) {
                firebaseService.getUserProfile(user.uid).collect { profile ->
                    userProfile = profile
                }

                // Load course progress for authenticated user
                firebaseService.getUserCourseProgress(user.uid).collect { progress ->
                    courseProgress = progress
                }
            } else {
                userProfile = null
                courseProgress = emptyList()
            }
        }
    }

    val navItems = listOf(
        NavItem("Home", Icons.Filled.Home),
        NavItem("Courses", Icons.AutoMirrored.Filled.MenuBook),
        NavItem("Cart", Icons.Filled.ShoppingCart),
        NavItem("Notifications", Icons.Filled.Notifications),
        NavItem("Profile", Icons.Filled.Person),
        NavItem("Menu", Icons.Filled.Menu)
    )

    Scaffold(
        bottomBar = {
            // Bottom Navigation (Icon only, no labels)
            Box {
                NavigationBar {
                    navItems.forEachIndexed { index, item ->
                        NavigationBarItem(
                            icon = {
                                if (index == 4 && isUserLoggedIn && userProfile?.avatarUrl?.isNotEmpty() == true) {
                                    // Show user avatar for profile tab
                                    Image(
                                        painter = rememberAsyncImagePainter(userProfile!!.avatarUrl),
                                        contentDescription = "User Avatar",
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else if (index == 4 && isUserLoggedIn) {
                                    // Show initial placeholder for profile tab
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(
                                                brush = Brush.linearGradient(
                                                    colors = listOf(
                                                        MaterialTheme.colorScheme.primary,
                                                        MaterialTheme.colorScheme.secondary
                                                    )
                                                ),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = userProfile?.fullName?.firstOrNull()?.toString()
                                                ?: currentUser?.displayName?.firstOrNull()?.toString()
                                                ?: currentUser?.email?.firstOrNull()?.toString()?.uppercase()
                                                ?: "U",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                } else {
                                    // Show default icon for other tabs
                                    Icon(item.icon, contentDescription = item.label)
                                }
                            },
                            label = null, // Remove text labels
                            selected = selectedItem == index,
                            onClick = {
                                selectedItem = index
                                when (index) {
                                    0 -> { // Home
                                        val intent = Intent(activityContext, HomeActivity::class.java)
                                        activityContext?.startActivity(intent)
                                    }
                                    1 -> { // Courses
                                        val intent = Intent(activityContext, CoursesActivity::class.java)
                                        activityContext?.startActivity(intent)
                                    }
                                    2 -> { // Cart
                                        val intent = Intent(activityContext, CartActivity::class.java)
                                        activityContext?.startActivity(intent)
                                    }
                                    3 -> { // Notifications
                                        val intent = Intent(activityContext, NotificationsActivity::class.java)
                                        activityContext?.startActivity(intent)
                                    }
                                    4 -> { // Profile
                                        val intent = Intent(activityContext, ProfileActivity::class.java)
                                        activityContext?.startActivity(intent)
                                    }
                                    5 -> showMenu = true // Menu tab shows dropdown
                                }
                            }
                        )
                    }
                }

                // Dropdown Menu for Menu button
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Home") },
                        onClick = {
                            showMenu = false
                            onNavigateToHome()
                        },
                        leadingIcon = { Icon(Icons.Filled.Home, contentDescription = "Home") }
                    )

                    DropdownMenuItem(
                        text = { Text("Courses") },
                        onClick = {
                            showMenu = false
                            onNavigateToCourses()
                        },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Courses") }
                    )

                    DropdownMenuItem(
                        text = { Text("Cart") },
                        onClick = {
                            showMenu = false
                            onNavigateToCart()
                        },
                        leadingIcon = { Icon(Icons.Filled.ShoppingCart, contentDescription = "Cart") }
                    )

                    DropdownMenuItem(
                        text = { Text("Profile") },
                        onClick = {
                            showMenu = false
                            onNavigateToProfile()
                        },
                        leadingIcon = { Icon(Icons.Filled.Person, contentDescription = "Profile") }
                    )

                    DropdownMenuItem(
                        text = { Text("Notifications") },
                        onClick = {
                            showMenu = false
                            val intent = Intent(activityContext, NotificationsActivity::class.java)
                            activityContext?.startActivity(intent)
                        },
                        leadingIcon = { Icon(Icons.Filled.Notifications, contentDescription = "Notifications") }
                    )

                    DropdownMenuItem(
                        text = { Text("Settings") },
                        onClick = {
                            showMenu = false
                            onNavigateToSettings()
                        },
                        leadingIcon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") }
                    )

                    HorizontalDivider()

                    DropdownMenuItem(
                        text = { Text("Sign In") },
                        onClick = {
                            showMenu = false
                            onSignIn()
                        },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.Login, contentDescription = "Sign In") }
                    )

                    DropdownMenuItem(
                        text = { Text("Sign Out") },
                        onClick = {
                            showMenu = false
                            onSignOut()
                        },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Sign Out") }
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // User Profile Header
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // User Avatar
                            if (userProfile?.avatarUrl?.isNotEmpty() == true) {
                                // Display actual avatar image
                                Image(
                                    painter = rememberAsyncImagePainter(userProfile!!.avatarUrl),
                                    contentDescription = "User Avatar",
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                // Fallback to placeholder with initial
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(
                                            brush = Brush.linearGradient(
                                                colors = listOf(
                                                    MaterialTheme.colorScheme.primary,
                                                    MaterialTheme.colorScheme.secondary
                                                )
                                            ),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = userProfile?.fullName?.firstOrNull()?.toString()
                                            ?: currentUser?.displayName?.firstOrNull()?.toString()
                                            ?: currentUser?.email?.firstOrNull()?.toString()?.uppercase()
                                            ?: "U",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = userProfile?.fullName?.takeIf { it.isNotEmpty() }
                                        ?: currentUser?.displayName
                                        ?: "AI Explorer",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = currentUser?.email ?: "user@example.com",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }

                        // Sign Out Button
                        IconButton(onClick = onSignOut) {
                            Icon(
                                Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Sign Out",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Stats Overview
            item {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        StatsCard(
                            title = "Courses Enrolled",
                            value = agents.size.toString(),
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    item {
                        StatsCard(
                            title = "Hours Learned",
                            value = "24", // Mock data
                            icon = Icons.Filled.Schedule,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    item {
                        StatsCard(
                            title = "Completed Courses",
                            value = "3", // Mock data
                            icon = Icons.Filled.CheckCircle,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    item {
                        StatsCard(
                            title = "Achievements",
                            value = "7", // Mock data
                            icon = Icons.Filled.Star,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Continue Learning Section
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Continue Learning",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        if (courseProgress.isEmpty()) {
                            // Show message when no progress data
                            Text(
                                text = "No courses in progress yet. Start learning to see your progress here!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            // Display real course progress data
                            courseProgress.take(3).forEach { progress ->
                                ContinueLearningItem(
                                    courseTitle = progress.courseTitle,
                                    progress = progress.progress,
                                    lastAccessed = formatLastAccessed(progress.lastAccessed)
                                )
                                if (progress != courseProgress.take(3).last()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Book Icon with Top Bar
            item {
                TopAppBar(
                    title = { Text("AI Playground", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { /* Handle back navigation */ }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* Handle notifications */ }) {
                            Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                        }
                        IconButton(onClick = { /* Handle search */ }) {
                            Icon(Icons.Filled.Search, contentDescription = "Search")
                        }
                    }
                )
            }
            
            // Course Grid Access - Full course browsing
            itemsIndexed(agents.take(kotlin.math.min(9, agents.size))) { index, agent ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn()
                ) {
                    CourseDetailScreen(agent, index + 1) {
                        onNavigateToCourseDetail(agent.id)
                    }
                }
            }
        }
    }
}

// @Preview(showBackground = true)
// @Composable
// fun AnimatedComposablePreview() {
//     var expanded by remember { mutableStateOf(false) }
//     val animatedSize by animateDpAsState(
//         targetValue = if (expanded) 300.dp else 200.dp,
//         animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
//     )

//     Column(
//         modifier = Modifier
//             .fillMaxSize()
//             .padding(16.dp),
//         horizontalAlignment = Alignment.CenterHorizontally,
//         verticalArrangement = Arrangement.Center
//     ) {
//         Button(onClick = { expanded = !expanded }) {
//             Text("Toggle Animation")
//         }
//         Spacer(modifier = Modifier.height(16.dp))
//         Image(
//             painter = painterResource(id = R.drawable.main_screen_banner),
//             contentDescription = "Animated banner",
//             modifier = Modifier.size(animatedSize),
//             contentScale = ContentScale.Fit
//         )
//     }
// @Preview(showBackground = true)
// @Composable
// fun DashboardPreview() {
//     MaterialTheme {
//         // Create a mock ViewModel for preview
//         val mockViewModel = object : AgentViewModel(
//             application = androidx.compose.ui.platform.LocalContext.current as android.app.Application,
//             repository = AgentRepository()
//         ) {}
//         DashboardScreen(mockViewModel)
//     }
// }

@Composable
fun StatsCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = color.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ContinueLearningItem(
    courseTitle: String,
    progress: Float,
    lastAccessed: String
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = courseTitle,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Last accessed: $lastAccessed",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Utility function to format last accessed time
fun formatLastAccessed(timestamp: String): String {
    return try {
        val time = timestamp.toLongOrNull() ?: return "Recently"
        val now = System.currentTimeMillis()
        val diff = now - time

        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        when {
            seconds < 60 -> "Just now"
            minutes < 60 -> "$minutes minutes ago"
            hours < 24 -> "$hours hours ago"
            days < 7 -> "$days days ago"
            else -> "A week ago"
        }
    } catch (e: Exception) {
        "Recently"
    }
}