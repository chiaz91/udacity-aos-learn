package com.udacity.shoestore.fragments

import android.os.Bundle
import android.view.*
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
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_shoe_details, container, false)
        binding.buttonSave.setOnClickListener{
            saveShoe()
        }
        binding.buttonCancel.setOnClickListener{
            cancelShoe()
        }

        // retrieving view mode
        viewModel = ViewModelProvider(requireActivity()).get(ShoeViewModel::class.java)

        return binding.root
    }


    private fun saveShoe(){
        try{
            // save shoe
            val name = binding.shoeName.text.toString()
            val size = binding.shoeSize.text.toString().toDouble()
            val company = binding.company.text.toString()
            val description = binding.description.text.toString()
            viewModel.addShoe( Shoe(name, size,company, description))

            // navigate back
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