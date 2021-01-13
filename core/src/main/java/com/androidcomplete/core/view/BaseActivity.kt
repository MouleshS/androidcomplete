package com.androidcomplete.core.view

import androidx.appcompat.app.AppCompatActivity

/**
 * Created by mouleshs on 05,October,2020
 */

abstract class BaseActivity : AppCompatActivity() {

    abstract fun navigate(navValue:Int)

}