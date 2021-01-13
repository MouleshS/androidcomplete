package com.androidcomplete.home.data.remote

import com.androidcomplete.home.domain.MovieListResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by mouleshs on 02,January,2021
 */
interface MovieDataService {

    @GET("movie/popular")
    suspend fun getMovieDetail(@Query("api_key") apiKey: String): Response<MovieListResponse>
}