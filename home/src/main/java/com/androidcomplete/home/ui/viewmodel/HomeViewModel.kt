package com.androidcomplete.home.ui.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidcomplete.core.BuildConfig
import com.androidcomplete.core.domain.network.DataWrapper
import com.androidcomplete.home.domain.MovieListResponse
import com.androidcomplete.home.domain.MovieRepositoryContract
import kotlinx.coroutines.launch

/**
 * Created by mouleshs on 02,January,2021
 */

class HomeViewModel @ViewModelInject constructor(
    private val movieRepository: MovieRepositoryContract
) : ViewModel() {
    var trendingMovies = MutableLiveData<MovieListResponse>()

    fun getTrendingMovies() {
        viewModelScope.launch {
            val result = movieRepository.getTrendingMovies(BuildConfig.API_KEY)
            if (result.status == DataWrapper.Status.SUCCESS) {
                trendingMovies.postValue(result.data)
            }
        }
    }
}