package com.shverma.kinetic.data.model

data class AllergyItem(
    val name: String,
    val isCommon: Boolean = false
)

data class DietChip(
    val label: String,
    val key: String
)

val allergyDatabase = listOf(
    AllergyItem("Peanuts", isCommon = true),
    AllergyItem("Tree Nuts", isCommon = true),
    AllergyItem("Milk", isCommon = true),
    AllergyItem("Egg", isCommon = true),
    AllergyItem("Wheat", isCommon = true),
    AllergyItem("Soy", isCommon = true),
    AllergyItem("Fish", isCommon = true),
    AllergyItem("Shellfish", isCommon = true),
    AllergyItem("Sesame", isCommon = false),
    AllergyItem("Gluten", isCommon = true),
    AllergyItem("Lactose", isCommon = true),
    AllergyItem("Fructose", isCommon = false),
    AllergyItem("Mustard", isCommon = false),
    AllergyItem("Celery", isCommon = false),
    AllergyItem("Sulfite", isCommon = false),
    AllergyItem("Lupin", isCommon = false),
    AllergyItem("Molluscs", isCommon = false)
)
