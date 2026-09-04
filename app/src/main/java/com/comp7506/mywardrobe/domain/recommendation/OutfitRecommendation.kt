package com.comp7506.mywardrobe.domain.recommendation

import com.comp7506.mywardrobe.data.weather.CurrentWeather
import kotlin.math.roundToInt

data class OutfitRecommendation(
    val requiredWarmth: Int,
    val adjustedForFeel: Int,
    val suggestions: List<String>,
)

object WarmthCalculator {
    fun requiredWarmth(weather: CurrentWeather): Int {
        val base = (weather.apparentTemperatureC ?: weather.temperatureC)
            ?.roundToInt()
            ?: return 0
        return (26 - base).coerceAtLeast(0)
    }

    fun adjustment(weather: CurrentWeather): Int {
        var add = 0
        val wind = weather.windSpeedMps ?: 0.0
        val humidity = weather.relativeHumidityPct ?: 0.0
        val cloud = weather.cloudCoverPct ?: 0.0

        if (wind >= 8.0) add += 1
        if (humidity >= 80.0) add += 1
        if (cloud >= 70.0) add += 1

        return add.coerceAtMost(2)
    }
}

object OutfitRecommender {
    data class WardrobeItem(
        val name: String,
        val category: String,
        val warmthValue: Int,
    )

    private data class Garment(val name: String, val warmth: Int)

    private val garments = listOf(
        Garment("Light T-shirt/Base Layer", 1),
        Garment("Shirt", 2),
        Garment("Sweatshirt/Light Sweater", 3),
        Garment("Thick Sweater/Padded Vest", 4),
        Garment("Trench Coat/Light Down Jacket", 6),
        Garment("Heavy Down Jacket/Wool Coat", 8),
        Garment("Fur Coat", 12),
    )

    fun recommend(requiredWarmth: Int): List<String> {
        if (requiredWarmth <= 1) return listOf("Light T-shirt/Base Layer")
        if (requiredWarmth <= 3) return listOf("Light T-shirt/Base Layer + Shirt")
        if (requiredWarmth <= 6) return listOf("Light T-shirt/Base Layer + Sweatshirt/Light Sweater", "Shirt + Trench Coat/Light Down Jacket")

        val suggestions = mutableListOf<String>()
        suggestions += greedyCombo(requiredWarmth)
        suggestions += greedyCombo((requiredWarmth + 1).coerceAtMost(requiredWarmth + 2))
        return suggestions.distinct().take(3)
    }

    fun recommendFromWardrobe(requiredWarmth: Int, items: List<WardrobeItem>): List<String> {
        val usable = items.filter { it.warmthValue > 0 }
        if (usable.isEmpty()) return emptyList()

        val picked = greedyPick(requiredWarmth, usable)
        if (picked.isEmpty()) return emptyList()
        return listOf(picked.joinToString(" + ") { "${it.name} (${it.warmthValue})" })
    }

    private fun greedyCombo(target: Int): String {
        var remaining = target
        val picked = mutableListOf<Garment>()
        for (g in garments.sortedByDescending { it.warmth }) {
            if (remaining <= 0) break
            if (g.warmth <= remaining || picked.isEmpty()) {
                picked += g
                remaining -= g.warmth
            }
            if (picked.size >= 4) break
        }
        return picked.joinToString(" + ") { it.name }
    }

    private fun greedyPick(target: Int, items: List<WardrobeItem>): List<WardrobeItem> {
        val categoryPriority = listOf("Outerwear", "Tops", "Pants", "Accessories", "Shoes")
        val byCategory = items.groupBy { it.category }
        val picked = mutableListOf<WardrobeItem>()
        var remaining = target

        for (cat in categoryPriority) {
            if (remaining <= 0) break
            val candidates = (byCategory[cat].orEmpty())
                .sortedByDescending { it.warmthValue }
            val best = candidates.firstOrNull { it.warmthValue <= remaining } ?: candidates.firstOrNull()
            if (best != null) {
                picked += best
                remaining -= best.warmthValue
            }
        }

        if (remaining > 0) {
            val rest = items
                .filterNot { already -> picked.any { it.name == already.name } }
                .sortedByDescending { it.warmthValue }
            for (item in rest) {
                if (remaining <= 0) break
                picked += item
                remaining -= item.warmthValue
                if (picked.size >= 5) break
            }
        }

        return picked
    }
}

