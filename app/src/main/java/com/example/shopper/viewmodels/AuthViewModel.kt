package com.example.shopper.viewmodels

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.shopper.data.FirebaseQueryData
import com.example.shopper.helpers.Constants
import com.example.shopper.helpers.ProfileDeserializer
import com.example.shopper.models.Profile
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId


class AuthViewModel : ViewModel() {
    enum class AuthenticationState {
        AUTHENTICATED,
        UNAUTHENTICATED,
        INVALID_AUTHENTICATION,
        UNVERIFIED_ACCOUNT,

        REGISTRATION_CANCELLED,
        REGISTRATION_COMPLETED,

        INVALID_ACCOUNT_INFO
    }

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val instanceId: FirebaseInstanceId = FirebaseInstanceId.getInstance()

    val authenticationState = MutableLiveData<AuthenticationState>()

    val userId: String get() = auth.currentUser?.uid!!

    val email: String get() = auth.currentUser!!.email as String

    init {
        if (auth.currentUser == null) {
            refuseAuthentication()
        } else {
            acceptAuthentication()
        }

        try {
            auth.currentUser?.reload()
        } catch (e: FirebaseAuthInvalidUserException) {
            refuseAuthentication()
        }

    }

    private fun generateInstanceId() {
        instanceId.instanceId
            .addOnCompleteListener(OnCompleteListener {
                if (!it.isSuccessful) {
                    return@OnCompleteListener
                }

                // Get new Instance ID token
                val token = it.result?.token

                database.child("profiles")
                    .child(userId)
                    .child("instanceId")
                    .setValue(token)
            })
    }

    fun authenticate(activity: Activity, email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    if (auth.currentUser!!.isEmailVerified) {
                        acceptAuthentication()
                    } else {
                        authenticationState.value = AuthenticationState.UNVERIFIED_ACCOUNT
                    }
                } else {
                    authenticationState.value = AuthenticationState.INVALID_AUTHENTICATION
                }
            }
    }

    fun signOut() {
        auth.signOut()
        refuseAuthentication()
    }

    fun createAccount(activity: Activity, email: String, password: String, profile: Profile) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    database.child(Constants.FirebaseProfiles).child(auth.currentUser?.uid!!).setValue(profile)
                    auth.currentUser!!.sendEmailVerification()
                    auth.signOut()

                    authenticationState.value = AuthenticationState.REGISTRATION_COMPLETED
                } else {
                    invalidAccountInfo()
                }
            }
    }

    private fun acceptAuthentication() {
        authenticationState.value = AuthenticationState.AUTHENTICATED
        generateInstanceId()
    }

    fun refuseAuthentication() {
        authenticationState.value = AuthenticationState.UNAUTHENTICATED
    }

    fun userCancelledRegistration() {
        authenticationState.value = AuthenticationState.REGISTRATION_CANCELLED
    }

    fun invalidAccountInfo() {
        authenticationState.value = AuthenticationState.INVALID_ACCOUNT_INFO
    }

}