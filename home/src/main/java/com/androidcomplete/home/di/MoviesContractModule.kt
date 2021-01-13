package com.androidcomplete.home.di

import com.androidcomplete.home.data.remote.MovieRepository
import com.androidcomplete.home.domain.MovieRepositoryContract
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

/**
 * Created by mouleshs on 04,January,2021
 */
@InstallIn(ActivityComponent::class)
@Module
interface MoviesContractModule {
    @Binds
    fun bindMyRepository(
        myRepositoryImpl: MovieRepository
    ): MovieRepositoryContract
}