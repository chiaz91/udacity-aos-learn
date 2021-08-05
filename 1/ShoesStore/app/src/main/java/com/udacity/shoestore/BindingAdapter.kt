package com.udacity.shoestore

import android.content.Context
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import com.udacity.shoestore.databinding.RowShoeBinding
import com.udacity.shoestore.models.Shoe


@BindingAdapter("listShoes")
fun bindShoesList(container: LinearLayout, data: List<Shoe>?) {
    val inflater = container.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    container.removeAllViews()
    data?.forEach{ shoe ->
        val itemBinding: RowShoeBinding = DataBindingUtil.inflate(
            inflater, R.layout.row_shoe, container, false)
        itemBinding.curShoe = shoe
        container.addView(itemBinding.root)
    }
}
