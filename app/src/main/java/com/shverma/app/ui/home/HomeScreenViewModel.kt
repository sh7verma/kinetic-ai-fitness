package com.shverma.app.ui.home
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shverma.app.utils.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    // Inject repository or use cases here if needed
) : ViewModel() {

    private val _state = MutableStateFlow(HomeScreenState())
    val state: StateFlow<HomeScreenState> = _state.asStateFlow()

    var uiEvent = Channel<UiEvent>()
        private set

    init {
        onEvent(HomeScreenEvents.LoadItems)
    }

    fun onEvent(event: HomeScreenEvents) {
        when (event) {
            is HomeScreenEvents.LoadItems -> {
                fetchItems()
            }

            is HomeScreenEvents.Refresh -> {
                refreshItems()
            }

            is HomeScreenEvents.DeleteItem -> {
                viewModelScope.launch {
                    // repository.deleteItemById(event.id)
                    uiEvent.send(UiEvent.ShowMessage("Item deleted"))
                }
            }
        }
    }

    private fun fetchItems() {
        viewModelScope.launch {
            // Simulate loading data
            _state.value = _state.value.copy(
                itemList = listOf("Item 1", "Item 2", "Item 3"),
                isLoading = false
            )
        }
    }

    private fun refreshItems() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            fetchItems()
            uiEvent.send(UiEvent.ShowMessage("Items refreshed"))
        }
    }
}

sealed class HomeScreenEvents {
    data object LoadItems : HomeScreenEvents()
    data object Refresh : HomeScreenEvents()
    data class DeleteItem(val id: Int) : HomeScreenEvents()
}

data class HomeScreenState(
    val itemList: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
