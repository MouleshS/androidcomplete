package com.androidcomplete.home.domain

import com.squareup.moshi.Json

/**
 * Created by mouleshs on 02,January,2021
 */
class MovieListResponse {

    @Json(name = "page")
    var pageIndex: Int = 0

    @Json(name = "results")
    var resultList: List<Movie>? = null
}
