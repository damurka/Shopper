package com.example.shopper.models

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class ShoppingList(
    val name: String = "",
    val owner: String = "",
    val userId: String = ""
) {

    @Exclude
    var key: String? = null

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "name" to name,
            "owner" to owner,
            "userId" to userId
        )
    }
}