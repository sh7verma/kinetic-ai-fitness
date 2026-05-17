package com.shverma.kinetic.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shverma.kinetic.data.model.ai.AIDietPlan
import com.shverma.kinetic.utils.formatGoalName

@Entity(tableName = "diet_plans")
data class DietPlanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String, // "Fat Loss Plan"
    val goal: String, // optional

    val createdAt: Long = System.currentTimeMillis()
)

fun AIDietPlan.toDietPlanEntity(): DietPlanEntity {
    return DietPlanEntity(
        name = "${goal.formatGoalName()} Plan",
        goal = goal
    )
}
