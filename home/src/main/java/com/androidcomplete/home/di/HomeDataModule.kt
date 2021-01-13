package com.androidcomplete.home.di

import com.androidcomplete.home.data.remote.MovieDataService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import retrofit2.Retrofit

/**
 * Created by mouleshs on 02,January,2021
 */
@InstallIn(ActivityComponent::class)
@Module
object HomeDataModule {

    @Provides
    fun providesMovieDataService(retrofit: Retrofit): MovieDataService {
        return retrofit.create(MovieDataService::class.java)
    }
}