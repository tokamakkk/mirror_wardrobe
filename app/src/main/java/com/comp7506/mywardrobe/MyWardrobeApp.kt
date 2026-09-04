package com.comp7506.mywardrobe

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Home
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.comp7506.mywardrobe.navigation.AppRoutes
import com.comp7506.mywardrobe.ui.screens.CalendarScreen
import com.comp7506.mywardrobe.ui.screens.HomeScreen
import com.comp7506.mywardrobe.ui.screens.MirrorScreen
import com.comp7506.mywardrobe.ui.screens.OutfitsScreen
import com.comp7506.mywardrobe.ui.screens.PortraitCaptureScreen
import com.comp7506.mywardrobe.ui.screens.WardrobeScreen
import com.comp7506.mywardrobe.ui.screens.LoginScreen
import com.comp7506.mywardrobe.ui.screens.RegisterScreen
import com.comp7506.mywardrobe.ui.screens.WarmthDetailsScreen
import com.comp7506.mywardrobe.ui.screens.MirrorLoadingScreen
import com.comp7506.mywardrobe.ui.screens.AiResultScreen
import com.comp7506.mywardrobe.ui.theme.SoftBackground
import com.comp7506.mywardrobe.ui.theme.UiTokens
import kotlinx.coroutines.flow.map

private data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val bottomNavItems = listOf(
    BottomNavItem(
        route = AppRoutes.Home.route,
        label = "Home",
        selectedIcon = Icons.Outlined.Home,
        unselectedIcon = Icons.Outlined.Home,
    ),
    BottomNavItem(
        route = AppRoutes.Wardrobe.route,
        label = "Wardrobe",
        selectedIcon = Icons.Outlined.Checkroom,
        unselectedIcon = Icons.Outlined.Checkroom,
    ),
    BottomNavItem(
        route = AppRoutes.Mirror.route,
        label = "Mirror",
        selectedIcon = Icons.Outlined.ContentCopy,
        unselectedIcon = Icons.Outlined.ContentCopy,
    ),
    BottomNavItem(
        route = AppRoutes.Outfits.route,
        label = "Outfits",
        selectedIcon = Icons.Outlined.AutoAwesome,
        unselectedIcon = Icons.Outlined.AutoAwesome,
    ),
    BottomNavItem(
        route = AppRoutes.Calendar.route,
        label = "Calendar",
        selectedIcon = Icons.Outlined.CalendarMonth,
        unselectedIcon = Icons.Outlined.CalendarMonth,
    ),
)

@Composable
fun MyWardrobeApp() {
    val app = LocalContext.current.applicationContext as MyWardrobeApplication
    val isLoggedIn by app.authManager.currentUserFlow
        .map { it != null }
        .collectAsStateWithLifecycle(initialValue = app.authManager.currentUser() != null)
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    LaunchedEffect(isLoggedIn, currentDestination?.route) {
        val route = currentDestination?.route
        if (!isLoggedIn && route !in setOf(AppRoutes.Login.route, AppRoutes.Register.route)) {
            navController.navigate(AppRoutes.Login.route) {
                popUpTo(AppRoutes.Home.route) { inclusive = true }
            }
        }
        if (isLoggedIn && route in setOf(AppRoutes.Login.route, AppRoutes.Register.route)) {
            navController.navigate(AppRoutes.Home.route) {
                popUpTo(AppRoutes.Login.route) { inclusive = true }
            }
        }
    }

    val showBottomBar = isLoggedIn && currentDestination?.route in bottomNavItems.map { it.route }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                // Mesh-like diffuse light spots that stay stable across navigation.
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x66E6E6FA),
                            Color(0x00E6E6FA),
                        ),
                        center = Offset(x = size.width * 0.92f, y = size.height * 0.10f),
                        radius = size.minDimension * 0.62f,
                    ),
                    radius = size.minDimension * 0.62f,
                    center = Offset(x = size.width * 0.92f, y = size.height * 0.10f),
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x66FFF5EE),
                            Color(0x00FFF5EE),
                        ),
                        center = Offset(x = size.width * 0.08f, y = size.height * 0.94f),
                        radius = size.minDimension * 0.70f,
                    ),
                    radius = size.minDimension * 0.70f,
                    center = Offset(x = size.width * 0.08f, y = size.height * 0.94f),
                )
            }
            .background(SoftBackground),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = UiTokens.navBarContainer,
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy
                            ?.any { it.route == item.route } == true
                        val mirrorSelected = selected && item.route == AppRoutes.Mirror.route
                        NavigationBarItem(
                            modifier = Modifier.weight(1f),
                            selected = selected,
                            onClick = { navController.navigate(item.route) },
                            icon = {
                                if (mirrorSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .background(UiTokens.navMirrorGlow, CircleShape)
                                            .border(1.dp, Color.White.copy(alpha = 0.65f), CircleShape),
                                        contentAlignment = androidx.compose.ui.Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector = item.selectedIcon,
                                            contentDescription = item.label,
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label,
                                    )
                                }
                            },
                            label = { Text(item.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = UiTokens.navSelected,
                                selectedTextColor = UiTokens.navSelected,
                                unselectedIconColor = UiTokens.navUnselected,
                                unselectedTextColor = UiTokens.navUnselected,
                                indicatorColor = Color.Transparent,
                            ),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (isLoggedIn) AppRoutes.Home.route else AppRoutes.Login.route,
            modifier = Modifier.padding(
                if (currentDestination?.route in setOf(
                        AppRoutes.Mirror.route,
                        AppRoutes.MirrorLoading.route,
                        AppRoutes.Wardrobe.route,
                        AppRoutes.Outfits.route,
                        AppRoutes.Calendar.route
                    )) {
                    androidx.compose.foundation.layout.PaddingValues(0.dp)
                } else {
                    innerPadding
                },
            ),
        ) {
            composable(AppRoutes.Login.route) { LoginScreen(navController = navController) }
            composable(AppRoutes.Register.route) { RegisterScreen(navController = navController) }
            composable(AppRoutes.Home.route) { HomeScreen(navController = navController) }
            composable(AppRoutes.Wardrobe.route) { WardrobeScreen(navController = navController) }
            composable(AppRoutes.Mirror.route) { MirrorScreen(navController = navController) }
            composable(AppRoutes.PortraitCapture.route) { PortraitCaptureScreen(navController = navController) }
            composable(AppRoutes.AddItem.route) { com.comp7506.mywardrobe.ui.screens.AddItemScreen(navController = navController) }
            composable(AppRoutes.Outfits.route) { OutfitsScreen(navController = navController) }
            composable(AppRoutes.Calendar.route) { CalendarScreen() }
            composable(AppRoutes.CreateOutfit.route) { com.comp7506.mywardrobe.ui.screens.CreateOutfitScreen(navController = navController) }
            composable(
                route = AppRoutes.EditOutfit.route,
                arguments = listOf(navArgument("outfitId") { type = NavType.LongType }),
            ) { entry ->
                val outfitId = entry.arguments?.getLong("outfitId") ?: return@composable
                com.comp7506.mywardrobe.ui.screens.EditOutfitScreen(outfitId = outfitId, navController = navController)
            }
            composable(AppRoutes.Stats.route) { com.comp7506.mywardrobe.ui.screens.StatsScreen(navController = navController) }
            composable(AppRoutes.WarmthDetails.route) { WarmthDetailsScreen(navController = navController) }
            composable(AppRoutes.MirrorLoading.route) { MirrorLoadingScreen(navController = navController) }
            composable(
                route = AppRoutes.AiResult.route,
                arguments = listOf(
                    navArgument("imageUrl") { type = NavType.StringType },
                    navArgument("clothingIds") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val imageUrl = backStackEntry.arguments?.getString("imageUrl")
                val clothingIdsString = backStackEntry.arguments?.getString("clothingIds")
                val clothingIds = clothingIdsString?.split(",")?.mapNotNull { it.toLongOrNull() } ?: emptyList()

                com.comp7506.mywardrobe.ui.screens.AiResultScreen(
                    navController = navController,
                    imageUrl = imageUrl,
                    selectedClothingIds = clothingIds
                )
            }
        }
    }
}
