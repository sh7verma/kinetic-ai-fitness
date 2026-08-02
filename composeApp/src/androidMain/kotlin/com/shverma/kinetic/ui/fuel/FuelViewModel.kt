package com.shverma.kinetic.ui.fuel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shverma.kinetic.data.local.dao.FoodLogDao
import com.shverma.kinetic.data.repository.UserProfileRepository
import kotlinx.coroutines.flow.StateFlow

/** Android lifecycle/date-formatting adapter for the shared Fuel controller. */
class FuelViewModel(
    userProfileRepository: UserProfileRepository,
    foodLogDao: FoodLogDao,
) : ViewModel(), FuelActions {
    private val controller = FuelController(
        userProfileRepository = userProfileRepository,
        foodLogDao = foodLogDao,
        clock = AndroidFuelClock(),
        scope = viewModelScope,
    )

    override val state: StateFlow<FuelState> = controller.state

    override fun repeatMeal(meal: LoggedMealGroup) = controller.repeatMeal(meal)
}
