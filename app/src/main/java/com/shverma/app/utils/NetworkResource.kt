package com.shverma.app.utils

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.shverma.androidstarter.R
import com.shverma.app.utils.GlobalResourceProvider.getGlobalString
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.IOException



data class ErrorResponse(
    @SerializedName("code") val code: String,
    @SerializedName("message") val message: String,
)

sealed class Resource<T> {
    data class Success<T>(val data: T?) : Resource<T>()
    data class Error<T>(
        val message: String,
        val errorCode: String? = null,
    ) : Resource<T>()
}

suspend fun <T : Any> safeApiCall(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    apiCall: suspend () -> Response<T>,
): Resource<T> = withContext(dispatcher) {
    try {
        val response = apiCall()
        return@withContext handleApiResponse(
            response = response,
        )
    } catch (e: Exception) {
        Resource.Error(getErrorMessage(e))
    }
}

private suspend fun <T : Any> handleApiResponse(
    response: Response<T>
): Resource<T> {
    val apiName = response.raw().request.url.encodedPath
    return when (response.code()) {
        200, 201 -> {
            response.body()?.let {
                Resource.Success(it)
            } ?: run {
                Resource.Error(getGlobalString(R.string.error_not_found))
            }
        }

        401 -> {
            Resource.Error(
                message = getGlobalString(R.string.error_unauthorized),
                errorCode = response.code().toString()
            )
        }

        400 -> {
            handleBadRequest(response)
        }

        else -> {
            Resource.Error("${response.code()} ${getGlobalString(R.string.error_unknown)}")
        }
    }
}

private fun <T : Any> handleBadRequest(response: Response<T>): Resource<T> {
    val errorBody = response.errorBody()?.string()
    return if (!errorBody.isNullOrEmpty()) {
        val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
        Resource.Error(message = errorResponse.message, errorCode = errorResponse.code)
    } else {
        Resource.Error("${response.code()} ${getGlobalString(R.string.error_unknown)}")
    }
}

private fun getErrorMessage(e: Exception): String {
    return when (e) {
        is IOException -> "${getGlobalString(R.string.error_no_internet)} ${e.message}"
        else -> "${getGlobalString(R.string.error_no_internet)} ${e.message}"
    }
}


fun isNetworkError(errorMessage: String?): Boolean {
    return errorMessage?.contains("network", ignoreCase = true) == true ||
            errorMessage?.contains("timeout", ignoreCase = true) == true ||
            errorMessage?.contains("unable to resolve host", ignoreCase = true) == true
}
