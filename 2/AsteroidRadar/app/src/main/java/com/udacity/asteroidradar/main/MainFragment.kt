package com.udacity.asteroidradar.main

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.squareup.picasso.Picasso
import com.udacity.asteroidradar.AsteroidAdapter
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.R
import com.udacity.asteroidradar.databinding.FragmentMainBinding

class MainFragment : Fragment() {
    private lateinit var binding: FragmentMainBinding
    private val adapter = AsteroidAdapter()

    private val viewModel: MainViewModel by lazy {
        val viewModelFactory = MainViewModelFactory(requireActivity().application)
        ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentMainBinding.inflate(inflater)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.asteroidRecycler.adapter = adapter

        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // observe database changes
        viewModel.asteroidList.observe(viewLifecycleOwner, Observer { asteroids ->
            adapter.submitList(asteroids)
        })
        viewModel.pictureOfDay.observe(viewLifecycleOwner, Observer {picture ->
            picture?.let {
                if (picture.mediaType == "image") {
                    loadPictureOfDay(picture)
                }
            }
        })
    }

    private fun loadPictureOfDay(pictureOfDay: PictureOfDay) {
        Picasso.with(binding.activityMainImageOfTheDay.context)
            .load(pictureOfDay.url)
            .placeholder(R.drawable.loading_img)
            .error(R.drawable.ic_broken_image)
            .into(binding.activityMainImageOfTheDay)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_overflow_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return true
    }
}
