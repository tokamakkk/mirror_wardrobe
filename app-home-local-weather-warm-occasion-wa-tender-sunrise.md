# Plan: WarmthDetails Screen Optimization

## Context
用户希望优化衣橱管理app中的WarmthDetails页面（点击Home页"Local weather"后进入），主要有三个需求：
1. 新增显示带warmValue的"Today outfit suggestion"卡片（如：皮草大衣（12）+打底衫（1））
2. 调整卡片顺序：先显示suggestion卡片，再显示"How warmth target works"说明卡片
3. 将Occasion从文本输入改为下拉选择，选项来自OutfitsScreen中预定义的occasion列表

**Today outfit suggestion逻辑**：
- 未选择occasion（显示"All"）时：根据天气温度推荐（基于衣橱所有单品）
- 选择特定occasion后：从匹配该occasion的saved outfit中推荐（显示outfit及其items的warmValue总和）

## Current Implementation
- **WarmthDetailsScreen.kt**: 当前顺序是"How warmth target works"卡片 → Occasion文本输入 → 推荐outfit列表
- **OutfitRecommendation.kt**: `recommendFromWardrobe`返回`List<String>`，格式是picked.joinToString(" + ") { it.name }，不含warmValue
- **HomeViewModel.kt**: 已有recommendation逻辑，使用`OutfitRecommender.recommendFromWardrobe`
- **OutfitsScreen.kt**: 预定义occasion列表（Business, Office, Dating, Casual, Ceremony, Sport, homewear）

## Implementation Steps

### Step 1: Modify OutfitRecommendation.kt
**File**: `app/src/main/java/com/comp7506/mywardrobe/domain/recommendation/OutfitRecommendation.kt`

修改`recommendFromWardrobe`函数返回格式，让每个suggestion包含warmValue：
```kotlin
fun recommendFromWardrobe(requiredWarmth: Int, items: List<WardrobeItem>): List<String> {
    val usable = items.filter { it.warmthValue > 0 }
    if (usable.isEmpty()) return emptyList()

    val picked = greedyPick(requiredWarmth, usable)
    if (picked.isEmpty()) return emptyList()
    // 改为：itemName(warmthValue) 格式
    return listOf(picked.joinToString(" + ") { "${it.name}（${it.warmthValue}）" })
}
```

### Step 2: Modify WarmthDetailsViewModel.kt
**File**: `app/src/main/java/com/comp7506/mywardrobe/ui/viewmodel/WarmthDetailsViewModel.kt`

1. 添加selectedOccasion state（初始为"All"）
2. 添加weather recommendation state（从HomeViewModel获取或传入）
3. 实现动态的todaySuggestion逻辑：
   - 若occasion="All"，返回weather recommendation（带warmValue）
   - 若occasion为具体值，从matchedOutfits中筛选warmth总和最接近requiredWarmth的outfit
4. 添加requiredWarmth state（用于occasion筛选时的warmth匹配）
5. 更新matchedOutfits筛选逻辑（支持精确匹配occasion）

关键代码：
```kotlin
private val selectedOccasion = MutableStateFlow("All")
private val requiredWarmth = MutableStateFlow(0)

val todaySuggestion: StateFlow<String?> = combine(
    selectedOccasion, matchedOutfits, requiredWarmth, weatherRecommendation
) { occ, outfits, reqWarmth, weatherRec ->
    if (occ == "All") {
        weatherRec // 基于天气的推荐，已带warmValue
    } else {
        // 从matchedOutfits中找warmth最接近reqWarmth的outfit
        outfits.firstOrNull()?.let { outfit ->
            val totalWarmth = outfit.items.sumOf { it.warmthValue }
            "${outfit.outfit.name}（$totalWarmth）"
        }
    }
}
```

### Step 3: Modify WarmthDetailsScreen.kt UI Layout
**File**: `app/src/main/java/com/comp7506/mywardrobe/ui/screens/WarmthDetailsScreen.kt`

1. 新增"Today outfit suggestion"卡片（显示动态推荐，带warmValue）
2. 调整卡片顺序：suggestion卡片 → warmth target works卡片
3. 将Occasion的`OutlinedTextField`改为`ExposedDropdownMenuBox`下拉选择

关键修改：
```kotlin
val occasions = listOf("All", "Business", "Office", "Dating", "Casual", "Ceremony", "Sport", "homewear")

// Today outfit suggestion卡片
Card(...) {
    Text("Today outfit suggestion", style = MaterialTheme.typography.titleMedium)
    Text(todaySuggestion ?: "Waiting for weather...")
}

// How warmth target works卡片（保持现有内容）

// Occasion下拉选择
ExposedDropdownMenuBox(...) {
    occasions.forEach { occ ->
        DropdownMenuItem(text = { Text(occ) }, onClick = { vm.setOccasion(occ) })
    }
}
```

## Critical Files
- `app/src/main/java/com/comp7506/mywardrobe/domain/recommendation/OutfitRecommendation.kt`
- `app/src/main/java/com/comp7506/mywardrobe/ui/screens/WarmthDetailsScreen.kt`
- `app/src/main/java/com/comp7506/mywardrobe/ui/viewmodel/WarmthDetailsViewModel.kt`

## Verification
1. 运行app，点击Home页的"Local weather"卡片
2. 验证WarmthDetails页面：
   - 首先显示"Today outfit suggestion"卡片，内容格式如"皮草大衣（12）+打底衫（1）"
   - 其次显示"How warmth target works"卡片
   - Occasion显示为下拉选择框，包含All, Business, Office, Dating, Casual, Ceremony, Sport, homewear选项
3. 验证动态推荐逻辑：
   - 选择"All"：today outfit suggestion基于天气温度推荐（从衣橱所有单品）
   - 选择具体occasion（如Business）：today outfit suggestion显示该occasion的saved outfit及其warmth总和
4. 选择不同occasion，验证下方outfit列表正确筛选显示