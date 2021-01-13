package com.androidcomplete.home.data.remote

import com.androidcomplete.core.domain.network.BaseDataSource
import javax.inject.Inject

/**
 * Created by mouleshs on 02,January,2021
 */
class MovieRemoteDataSource @Inject constructor(private val movieDataService: MovieDataService) :
    BaseDataSource() {
    suspend fun getTrendingMovies(apiKey: String) =
        getResult { movieDataService.getMovieDetail(apiKey) }

}