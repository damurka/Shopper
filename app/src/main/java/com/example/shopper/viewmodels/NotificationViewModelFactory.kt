package com.example.shopper.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


/**
 * Used to generate the NotificationViewModel
 */
class NotificationViewModelFactory (private val userId: String) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>) = NotificationViewModel(userId) as T

}