package com.shverma.kinetic.utils

import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.shverma.kinetic.R
import com.shverma.kinetic.data.network.OpenAIResponse
import com.shverma.kinetic.utils.GlobalResourceProvider.getGlobalString
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import timber.log.Timber
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException


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

suspend fun <R : Any> safeAiCallWithContent(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    apiCall: suspend () -> OpenAIResponse,
    transform: (String) -> R
): R = withContext(dispatcher) {
    try {
        val apiResponse = apiCall()

        val content = apiResponse.choices
            .firstOrNull()
            ?.message
            ?.content
            ?: throw Exception("Empty AI response")

        Timber.tag("AI").d("Raw response: $content")

        val cleaned = content
            .replace("```json", "")
            .replace("```", "")
            .trim()

        transform(cleaned)

    } catch (e: Exception) {
        if (e is CancellationException) throw e
        throw Exception(getErrorMessage(e), e)
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
