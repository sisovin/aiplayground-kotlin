package com.playapp.aiagents.ui.main

import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.filled.MenuBook

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
                        // TODO: Implement sign out functionality
                        // This could include clearing user session, Firebase sign out, etc.
                        // For now, just show a toast message
                        android.widget.Toast.makeText(
                            this@MainActivity,
                            "Sign out functionality will be implemented",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
        }
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

    val navItems = listOf(
        NavItem("Home", Icons.Filled.Home),
        NavItem("Courses", Icons.AutoMirrored.Filled.MenuBook),
        NavItem("Cart", Icons.Filled.ShoppingCart),
        NavItem("Notifications", Icons.Filled.Notifications),
        NavItem("Profile", Icons.Filled.Person),
        NavItem("Menu", Icons.Filled.Menu)
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Bar with Bell Icon
        TopAppBar(
            title = { Text("AI Agents", fontWeight = FontWeight.Bold) },
            actions = {
                IconButton(onClick = { /* Handle notifications */ }) {
                    Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                }
            }
        )

        // Top Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
        ) {
            HorizontalPager(
                state = rememberPagerState(pageCount = { 5 }),
                modifier = Modifier.height(200.dp)
            ) { page ->
                val backgroundColor = when (page) {
                    0 -> colorResource(R.color.agent_1)
                    1 -> colorResource(R.color.agent_2)
                    2 -> colorResource(R.color.agent_3)
                    3 -> colorResource(R.color.agent_4)
                    4 -> colorResource(R.color.agent_5)
                    else -> colorResource(R.color.primary)
                }
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = backgroundColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Learn How to Build AI Agent",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Start With These 9 Free Courses",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Chip("9 Agent Types")
                            Chip("Ollama Powered")
                            Chip("Interactive Learning")
                        }
                    }
                }
            }
        }

        // Course Slider
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
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

        // Bottom Navigation (Icon only, no labels)
        Box {
            NavigationBar {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
                            Icon(item.icon, contentDescription = item.label)
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

                Divider()

                DropdownMenuItem(
                    text = { Text("Sign In") },
                    onClick = {
                        showMenu = false
                        onSignIn()
                    },
                    leadingIcon = { Icon(Icons.Filled.Login, contentDescription = "Sign In") }
                )

                DropdownMenuItem(
                    text = { Text("Sign Out") },
                    onClick = {
                        showMenu = false
                        onSignOut()
                    },
                    leadingIcon = { Icon(Icons.Filled.Logout, contentDescription = "Sign Out") }
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