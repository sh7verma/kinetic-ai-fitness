package com.shverma.app.utils


sealed class UiEvent {
    data class NavigateUp<T>(val data: T? = null) : UiEvent()
    data class ShowMessage(val message: String) : UiEvent()
}