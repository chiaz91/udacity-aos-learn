package com.udacity.shoestore.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.udacity.shoestore.R
import com.udacity.shoestore.databinding.FragmentShoeDetailsBinding
import com.udacity.shoestore.models.Shoe
import com.udacity.shoestore.models.ShoeViewModel


class ShoeDetailsFragment : Fragment() {
    private lateinit var binding: FragmentShoeDetailsBinding
    private lateinit var viewModel: ShoeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // retrieving view mode
        viewModel = ViewModelProvider(requireActivity()).get(ShoeViewModel::class.java)

        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_shoe_details, container, false)
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            newShoe = Shoe("",0.0, "","")
            buttonSave.setOnClickListener{
                saveShoe()
            }
            buttonCancel.setOnClickListener{
                cancelShoe()
            }
        }
        return binding.root
    }


    private fun saveShoe(){
        try{
            // add shoes to list
            viewModel.addShoe(binding.newShoe!!)
            
            // navigate back to list shoes
            findNavController().navigate(ShoeDetailsFragmentDirections.actionShoeDetailsFragmentToShoeListFragment())
        } catch (e: Exception){
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun cancelShoe(){
        findNavController().popBackStack()
    }

}