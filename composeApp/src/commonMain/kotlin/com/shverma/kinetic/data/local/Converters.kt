package com.shverma.kinetic.data.local

import androidx.room.TypeConverter
import com.shverma.kinetic.data.local.entity.FoodSource

class Converters {
    @TypeConverter
    fun fromFoodSource(value: FoodSource): String {
        return value.name
    }

    @TypeConverter
    fun toFoodSource(value: String): FoodSource {
        return FoodSource.from(value)
    }
}
