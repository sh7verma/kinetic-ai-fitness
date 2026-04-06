package com.shverma.app.data.repository

import com.shverma.app.data.network.ApiService
import javax.inject.Inject

interface HomeRepository {
    suspend fun getItems(): List<String>
    suspend fun deleteItem(id: Int)
}

class HomeRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : HomeRepository {

    override suspend fun getItems(): List<String> {
        // Simulate network/database fetch
        return listOf("Item 1", "Item 2", "Item 3")
    }

    override suspend fun deleteItem(id: Int) {
        // Simulate deleting item
    }
}