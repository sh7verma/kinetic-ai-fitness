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
    suspend fun insert(food: FoodEntity): Long

    @Query("SELECT * FROM foods WHERE name LIKE '%' || :query || '%' LIMIT 10")
    suspend fun searchFoods(query: String): List<FoodEntity>


    @Query("SELECT * FROM foods WHERE name IN (:names)")
    suspend fun findFoodsByNames(names: List<String>): List<FoodEntity>

    @Query("""
SELECT * FROM (
    SELECT *, ROW_NUMBER() OVER (
        PARTITION BY name 
        ORDER BY 
            CASE source
                WHEN 'user' THEN 4
                WHEN 'usda' THEN 3
                WHEN 'estimated' THEN 2
                WHEN 'ai' THEN 1
                ELSE 0
            END DESC
    ) as rn
    FROM foods
    WHERE name IN (:names)
) WHERE rn = 1
""")
    suspend fun findBestFood(names: List<String>): List<FoodEntity>

    @Query("SELECT * FROM foods WHERE name LIKE '%' || :query || '%' LIMIT 1")
    suspend fun findBestFood(query: String): FoodEntity?

    @Query("SELECT * FROM foods WHERE foodId = :id")
    suspend fun getFoodById(id: Int): FoodEntity?
}
