package com.comp7506.mywardrobe.util

import androidx.navigation.NavController
import com.comp7506.mywardrobe.navigation.AppRoutes

fun NavController.goHome() {
    val popped = popBackStack(AppRoutes.Home.route, false)
    if (!popped) {
        navigate(AppRoutes.Home.route) {
            launchSingleTop = true
        }
    }
}

