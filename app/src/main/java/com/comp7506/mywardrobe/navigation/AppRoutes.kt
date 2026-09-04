package com.comp7506.mywardrobe.navigation

import android.util.Log
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink

sealed class AppRoutes(val route: String) {
    data object Login : AppRoutes("login")
    data object Register : AppRoutes("register")
    data object Home : AppRoutes("home")
    data object Wardrobe : AppRoutes("wardrobe")
    data object AddItem : AppRoutes("add_item")
    data object Outfits : AppRoutes("outfits")
    data object Calendar : AppRoutes("calendar")
    data object CreateOutfit : AppRoutes("create_outfit")
    data object EditOutfit : AppRoutes("edit_outfit/{outfitId}") {
        fun route(outfitId: Long) = "edit_outfit/$outfitId"
    }
    data object Stats : AppRoutes("stats")
    data object WarmthDetails : AppRoutes("warmth_details")
    data object Mirror : AppRoutes("mirror")
    data object PortraitCapture : AppRoutes("portrait_capture")
    data object MirrorLoading : AppRoutes("mirror_loading?clothingIds={clothingIds}&useImageUpload={useImageUpload}") {
        fun passClothingIds(ids: List<Long>): String {
            val result = "mirror_loading?clothingIds=${ids.joinToString(",")}&useImageUpload=true"
            Log.d("AppRoutes", "Generated route: $result")
            return result
        }

        fun passClothingIdsAndMode(ids: List<Long>, useImageUpload: Boolean): String {
            val result = "mirror_loading?clothingIds=${ids.joinToString(",")}&useImageUpload=$useImageUpload"
            Log.d("AppRoutes", "Generated route: $result")
            return result
        }
    }
    data object AiResult : AppRoutes("ai_result?imageUrl={imageUrl}&clothingIds={clothingIds}") {
        fun passImageUrl(imageUrl: String) = "ai_result?imageUrl=$imageUrl"
        fun passClothingIds(ids: List<Long>) = "ai_result?clothingIds=${ids.joinToString(",")}"
        fun route(imageUrl: String, clothingIds: String) = "ai_result?imageUrl=$imageUrl&clothingIds=$clothingIds"
    }
}
