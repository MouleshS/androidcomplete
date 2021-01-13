package com.androidcomplete.home.domain

import com.androidcomplete.core.domain.network.DataWrapper

/**
 * Created by mouleshs on 04,January,2021
 */
interface MovieRepositoryContract {

    suspend fun getTrendingMovies(apiKey: String): DataWrapper<MovieListResponse>
}