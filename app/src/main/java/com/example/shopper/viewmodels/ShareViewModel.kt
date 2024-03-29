package com.example.shopper.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.shopper.data.FirebaseQueryData
import com.example.shopper.helpers.Constants
import com.example.shopper.helpers.FriendsDeserializer
import com.example.shopper.models.Friend
import com.example.shopper.models.ShoppingList
import com.google.firebase.database.FirebaseDatabase
import kotlin.collections.HashMap
import kotlin.collections.List
import kotlin.collections.find
import kotlin.collections.set


class ShareViewModel(private val userId: String, listId: String) : ViewModel() {

    /**
     * Firebase Database reference
     */
    private val database = FirebaseDatabase.getInstance().reference

    /**
     * Firebase Database friends reference
     */
    private val friendsDatabaseReference = database.child(Constants.FirebaseFriends).child(userId)

    /**
     * Firebase Database shared-with reference
     */
    private val sharedWithDatabaseReference = database.child(Constants.FirebaseSharedWith).child(listId)

    /**
     * Retrieves a list of freinds as LiveData
     */
    val friendsLiveData: LiveData<List<Friend>> = Transformations.map(FirebaseQueryData(friendsDatabaseReference), FriendsDeserializer())

    /**
     * Retrieves a list of freinds shared a shopping list as LiveData
     */
    val sharedWithLiveData: LiveData<List<Friend>> = Transformations.map(FirebaseQueryData(sharedWithDatabaseReference), FriendsDeserializer())

    val result = MediatorLiveData<List<Friend>>()

    init {
        result.addSource(friendsLiveData) { result.value = it }
        result.addSource(sharedWithLiveData) {
            if (friendsLiveData.value != null) {
                for (friend in it) {
                    val found = friendsLiveData.value!!.find { fr -> fr.key == friend.key }
                    found?.shared = !found!!.shared
                }
            }
            result.value = friendsLiveData.value
        }
    }

    /**
     * Adds a user to be your friend
     */
    fun addFriend(key: String, friend: Friend) {
        friendsDatabaseReference.child(key).setValue(friend)
    }

    /**
     * Removes a user from being your friend Unfriend
     */
    fun removeFriend(friend: Friend, shoppingLists: List<ShoppingList>) {
        val childUpdates = HashMap<String, Any?>()
        childUpdates["/${Constants.FirebaseFriends}/$userId/${friend.key}"] = null
        for (shoppingList in shoppingLists) {
            childUpdates["/${Constants.FirebaseSharedWith}/${shoppingList.key}/${friend.key}"] = null
            childUpdates["/${Constants.FirebaseShoppingLists}/${friend.key}/${shoppingList.key}"] = null
        }

        database.updateChildren(childUpdates)
    }

    /**
     * Share or Unshare a list with a friend
     */
    fun shareWith(friend: Friend, shoppingList: ShoppingList) {
        val childUpdates = HashMap<String, Any?>()

        if (friend.shared) {
            childUpdates["/${Constants.FirebaseSharedWith}/${shoppingList.key}/${friend.key}"] = null
            childUpdates["/${Constants.FirebaseShoppingLists}/${friend.key}/${shoppingList.key}"] = null
        } else {
            childUpdates["/${Constants.FirebaseSharedWith}/${shoppingList.key}/${friend.key}"] = friend.toMap()
            childUpdates["/${Constants.FirebaseShoppingLists}/${friend.key}/${shoppingList.key}"] = shoppingList.toMap()
        }

        database.updateChildren(childUpdates)
    }
}