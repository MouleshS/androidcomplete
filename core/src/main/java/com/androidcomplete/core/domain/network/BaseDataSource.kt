package com.androidcomplete.core.domain.network

import retrofit2.Response
import javax.net.ssl.HttpsURLConnection

/**
 * Created by mouleshs on 02,January,2021
 */
abstract class BaseDataSource {

    protected suspend fun <T> getResult(call: suspend () -> Response<T>): DataWrapper<T> {
        try {
            val response = call()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) return DataWrapper.success(body)
            }

            response.errorBody()?.let {
                return when (response.code()) {
                    HttpsURLConnection.HTTP_UNAUTHORIZED -> {
                        error(response.code(), "Access Denied")
                    }
                    else -> {
                        error(response.code(), response.message())
                    }
                }
            }
            return error(response.code(), response.message())
        } catch (e: Exception) {
            return error(0,e.message ?: e.toString())
        }
    }

    private fun <T> error(statusCode: Int, message: String): DataWrapper<T> {
        return DataWrapper.error(statusCode, message)
    }

}