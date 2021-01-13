package com.androidcomplete.home.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.androidcomplete.core.view.BaseActivity
import com.androidcomplete.home.R
import com.androidcomplete.home.ui.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.home_layout.*

/**
 * Created by mouleshs on 05,October,2020
 */

@AndroidEntryPoint
class HomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.home_layout, container, false)
    }

     private val viewModel: HomeViewModel by viewModels()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        proceedBtn.setOnClickListener {
            if (activity is BaseActivity) {
                (activity as BaseActivity).navigate(1)
            }
        }

        viewModel.getTrendingMovies()
        viewModel.trendingMovies.observe(viewLifecycleOwner, Observer {
            it
        })
    }
}