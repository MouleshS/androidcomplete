package com.moulesh.androidcomplete

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.androidcomplete.core.view.BaseActivity
import com.androidcomplete.home.domain.Movie
import com.squareup.moshi.Moshi
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainNavigationActivity : BaseActivity() {
    lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_navigation_layout)
        navController = findNavController(R.id.nav_host_fragment)
    }

    override fun navigate(navValue: Int) {
        navController.navigate(R.id.action_navigation_home_to_detail_navigation)
    }

}