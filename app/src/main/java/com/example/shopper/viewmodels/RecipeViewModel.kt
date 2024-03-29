package com.example.shopper.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.shopper.models.RecipeCover
import com.example.shopper.models.RecipeResponse
import com.example.shopper.services.RecipeService
import com.example.shopper.services.ServiceBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class RecipeViewModel : ViewModel() {

    /**
     * Recipes as MutableLiveDate
     */
    private var recipes = MutableLiveData<List<RecipeCover>>()

    /**
     * Service for http call to the Edamam API
     */
    private val recipeService = ServiceBuilder.buildService(URL, RecipeService::class.java)

    /**
     * Recipes as LiveDate
     */
    val recipeLiveData: LiveData<List<RecipeCover>> = recipes

    /**
     * Gets the recipes from then EDAMAAM API
     */
    fun getRecipes(query: String) {
        // TODO: Get the API KEY from https://developer.edamam.com
        // current key have limitation since they are free
        recipeService.searchRecipe(query, "e6f5f143", "3782c50113ad97726f12a0f702e800a3")
            .enqueue(object: Callback<RecipeResponse> {
                override fun onFailure(call: Call<RecipeResponse>, t: Throwable) {

                }

                override fun onResponse(call: Call<RecipeResponse>, response: Response<RecipeResponse>) {
                    if (response.isSuccessful) {
                        recipes.value = response.body()?.recipes
                    }
                }

            })
    }

    companion object {
        private const val URL = "https://api.edamam.com"
    }

}