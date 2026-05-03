package com.shverma.kinetic.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shverma.kinetic.data.local.entity.FoodSource
import com.shverma.kinetic.data.model.MealItem

class Converters {
    @TypeConverter
    fun fromFoodSource(value: FoodSource): String {
        return value.name
    }

    @TypeConverter
    fun toFoodSource(value: String): FoodSource {
        return FoodSource.from(value)
    }

    @TypeConverter
    fun fromMealItemList(value: List<MealItem>?): String? {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toMealItemList(value: String?): List<MealItem>? {
        val listType = object : TypeToken<List<MealItem>>() {}.type
        return Gson().fromJson(value, listType)
    }
}
