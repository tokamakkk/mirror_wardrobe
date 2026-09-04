package com.comp7506.mywardrobe.data.model

enum class Category(val label: String) {
    Tops("Tops"),
    Pants("Pants"),
    Outerwear("Outerwear"),
    Shoes("Shoes"),
    Accessories("Accessories"),
    ;

    companion object {
        fun fromLabel(label: String): Category? = entries.firstOrNull { it.label == label }
    }
}
