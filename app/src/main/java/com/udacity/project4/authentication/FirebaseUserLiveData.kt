package com.udacity.project4.authentication

import androidx.lifecycle.LiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

/**
 * [FirebaseUserLiveData] is a LiveData subclass that observes changes in Firebase authentication state
 * and provides a current [FirebaseUser] object as its data. The class uses a Firebase authentication
 * state listener to listen for changes in the authentication state and updates the LiveData value
 * accordingly
 */

class FirebaseUserLiveData : LiveData<FirebaseUser?>(){

    private val firebaseAuth = FirebaseAuth.getInstance()

    /**
     * Listener for Firebase authentication state changes.
     * This listener updates the LiveData value whenever the authentication state changes.
     */
    private val authStateListener = FirebaseAuth.AuthStateListener {firebaseAuth ->
        value = firebaseAuth.currentUser

    }

    /**
     * Called when the LiveData becomes active (i.e., has one or more observers).
     * Adds the authentication state listener to start receiving authentication state changes.
     */
    override fun onActive() {
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    /**
     * Called when the LiveData becomes inactive (i.e., has no active observers).
     * Removes the authentication state listener to stop receiving authentication state changes
     * and prevent memory leaks.
     */
    override fun onInactive() {
        firebaseAuth.removeAuthStateListener(authStateListener)
    }
}