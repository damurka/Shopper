package com.example.shopper.services

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Used to generate the Retrofit service for the implementation of the interface API
 */
object ServiceBuilder {

    fun <T> buildService(url: String, serviceType: Class<T>) : T {
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(serviceType)
    }
}