package com.shverma.kinetic.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shverma.kinetic.data.local.entity.FoodEntity

@Dao
interface FoodDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(foods: List<FoodEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(food: FoodEntity)

    @Query("SELECT * FROM foods WHERE name LIKE '%' || :query || '%' LIMIT 10")
    suspend fun searchFoods(query: String): List<FoodEntity>

    @Query("""
SELECT * FROM foods 
WHERE name LIKE '%' || :query || '%'
ORDER BY 
    CASE source
        WHEN 'USER' THEN 4
        WHEN 'USDA' THEN 3
        WHEN 'FIRESTORE' THEN 2
        WHEN 'AI' THEN 1
    END DESC
LIMIT 1
""")
    suspend fun findBestFood(query: String): FoodEntity?
}
