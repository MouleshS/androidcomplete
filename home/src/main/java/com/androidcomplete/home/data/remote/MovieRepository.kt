package com.androidcomplete.home.data.remote

import com.androidcomplete.core.domain.network.DataWrapper
import com.androidcomplete.home.domain.MovieListResponse
import com.androidcomplete.home.domain.MovieRepositoryContract
import javax.inject.Inject

/**
 * Created by mouleshs on 02,January,2021
 */
class MovieRepository @Inject constructor(private val movieRemoteDataSource: MovieRemoteDataSource):MovieRepositoryContract {

    override suspend fun getTrendingMovies(apiKey: String): DataWrapper<MovieListResponse> {
        return movieRemoteDataSource.getTrendingMovies(apiKey)
    }
}