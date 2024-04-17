package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthenticationBinding

    private val viewModel : AuthenticationViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_authentication)

        // Implement the create account and sign in using FirebaseUI,
        //  use sign in using email and sign in using Google

        binding.loginBtn.setOnClickListener {
            launchSignInFlow()
        }

        // If the user was authenticated, send him to RemindersActivity
        viewModel.authenticationState.observe(this){ authState ->
            when(authState){
                AuthenticationViewModel.AuthenticationState.AUTHENTICATED -> startReminderActivity()
                AuthenticationViewModel.AuthenticationState.UNAUTHENTICATED -> {
                        Toast.makeText(this, "you need to Login", Toast.LENGTH_SHORT).show()

                }else -> {
                Toast.makeText(this, "There was a Problem. Try Again", Toast.LENGTH_SHORT).show()
                }
            }

        }

        // TODO: a bonus is to customize the sign in flow to look nice using :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout
    }

    private fun startReminderActivity() {
        val intent = Intent(this, RemindersActivity::class.java)
        startActivity(intent)
        finish()
    }



    // New way - onActivityResult is deprecated
    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result ->
        val response = IdpResponse.fromResultIntent(result.data)
        if(result.resultCode == Activity.RESULT_OK){
            Log.i(TAG, "Successfully Signed In UseR: " +"${FirebaseAuth.getInstance().currentUser?.displayName}")
        }
        else{
            Log.i(TAG, "Sign in unsuccessful ${response?.error}")
            Toast.makeText(this, "Unsuccessful Login ${response?.error}", Toast.LENGTH_SHORT).show()
        }
    }
    private fun launchSignInFlow() {

        // Options to login in/ sign up {Email and Google Account}
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        //Create and Launch sign-in intent
        val signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(false)
                .build()

        signInLauncher.launch(signInIntent)


    }

    companion object {
        const val TAG = "AuthenticationActivity"
    }

}