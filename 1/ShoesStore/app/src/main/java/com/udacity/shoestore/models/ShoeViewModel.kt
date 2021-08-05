package com.udacity.shoestore.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ShoeViewModel: ViewModel() {
    private var _shoes = MutableLiveData<MutableList<Shoe>>()
    val shoes: LiveData<MutableList<Shoe>>
        get() = _shoes

    init{
        _shoes.value = mutableListOf<Shoe>(
            // dummy data
            Shoe("Nike Revolution 5", 7.5, "Nike", "The Nike Revolution 5 cushions your stride with soft foam to keep you running in comfort. Lightweight knit material wraps your foot in breathable support, while a minimalist design fits in just about anywhere your day takes you."),
            Shoe("NMD_R1 SHOES", 8.5, "Adidas", "Continue the NMD legacy in these adidas NMD_R1 Shoes. Plush Boost cushioning keeps your city adventure feeling like you're walking on clouds. And since your head is sometimes up there, it's not a bad place for your feet to be too. Great for those sunup to sundown days, the soft knit textile upper doubles down on the comfort."),
            Shoe("574 Un-N-Ding", 6.0, "New Balance", "Introduced in 1988, the 574 was an instant favorite, but not just among runners. A simple design experiment combining a road and trail runner was the idea: a shoe meant to cover more terrain, and it did. Blurring the lines between performance and everyday wear, the 574 made its mark as one of the first true go-anywhere sneakers. Today, the 574 is known worldwide for its versatility and classic design synonymous with our brand heritage. This version celebrates Grey Day and the iconic color that's embedded in New Balance DNA.")
        )
    }

    fun addShoe(shoe: Shoe){
        val shoeList = _shoes.value
        shoeList?.add(shoe)
//        _shoes.value = shoeList
    }
}