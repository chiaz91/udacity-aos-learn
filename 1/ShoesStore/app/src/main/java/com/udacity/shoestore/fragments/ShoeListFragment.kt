package com.udacity.shoestore.fragments

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.udacity.shoestore.R
import com.udacity.shoestore.databinding.FragmentShoeListBinding
import com.udacity.shoestore.models.Shoe
import com.udacity.shoestore.models.ShoeViewModel


class ShoeListFragment : Fragment() {
    private lateinit var binding: FragmentShoeListBinding
    private lateinit var viewModel: ShoeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)

        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_shoe_list, container, false)
        binding.fab.setOnClickListener{
            it.findNavController().navigate(ShoeListFragmentDirections.actionShoeListFragmentToShoeDetailsFragment())
        }

        // retrieving view mode
        viewModel = ViewModelProvider(requireActivity()).get(ShoeViewModel::class.java)
        viewModel.shoes?.observe(viewLifecycleOwner, Observer { shoes ->
            // clear previous inflated views
            binding.shoesContainer.removeAllViews()

            // manually create create item views
            if (shoes.size == 0){
                val emptyView = createEmptyView()
                binding.shoesContainer.addView(emptyView)
            } else {
                shoes.forEach{ shoe ->
                    val itemView = createItemViewForShoe(shoe)
                    binding.shoesContainer.addView(itemView)
                }
            }

            binding.invalidateAll()
        })

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.common, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.action_logout -> logout()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun logout(){
        findNavController().navigate(ShoeListFragmentDirections.actionShoeListFragmentToLoginFragment())
    }

    private fun createEmptyView(): View {
        val empty = TextView(requireContext())
        empty.text = "No shoes found"
        return empty
    }

    private fun createItemViewForShoe(shoe: Shoe): View{
        val itemView = layoutInflater.inflate(R.layout.row_shoe, binding.shoesContainer,false)
        with(itemView){
            // todo: extract for string resource
            findViewById<TextView>(com.udacity.shoestore.R.id.name).text = shoe.name
            findViewById<TextView>(com.udacity.shoestore.R.id.size).text = "Size: ${shoe.size}"
            findViewById<TextView>(com.udacity.shoestore.R.id.company).text = "Company: ${shoe.company}"
            findViewById<TextView>(com.udacity.shoestore.R.id.description).text = "Description: ${shoe.description}"
        }
        return itemView
    }
}