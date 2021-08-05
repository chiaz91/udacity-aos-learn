package com.udacity.shoestore.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.udacity.shoestore.R
import com.udacity.shoestore.databinding.FragmentShoeListBinding
import com.udacity.shoestore.models.ShoeViewModel


class ShoeListFragment : Fragment() {
    private lateinit var binding: FragmentShoeListBinding
    private lateinit var viewModel: ShoeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_shoe_list, container, false)
        binding.fab.setOnClickListener{
            it.findNavController().navigate(ShoeListFragmentDirections.actionShoeListFragmentToShoeDetailsFragment())
        }

        // retrieving view mode
        viewModel = ViewModelProvider(requireActivity()).get(ShoeViewModel::class.java)
        binding.shoesContainer.removeAllViews()
        viewModel.shoes.value?.forEachIndexed{ index, shoe ->
            val itemView = inflater.inflate(R.layout.row_shoe, binding.shoesContainer,false)
            with(itemView){
                findViewById<TextView>(R.id.name).text = shoe.name
                findViewById<TextView>(R.id.size).text = "Size: ${shoe.size}"
                findViewById<TextView>(R.id.company).text = "Company: ${shoe.company}"
                findViewById<TextView>(R.id.description).text = "Description: ${shoe.description}"
            }
            binding.shoesContainer.addView(itemView)
        }
        binding.invalidateAll()
        return binding.root
    }
}