package com.example.kata.szakdoga.UI.LoginActivity

import android.util.Log
import com.example.kata.szakdoga.UI.Presenter
import com.google.firebase.auth.FirebaseAuth

/**
 * Created by Kata on 2017. 03. 12..
 */

class LoginPresenter : Presenter<LoginScreen>() {

    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private object Holder {
        val INSTANCE = LoginPresenter()
    }

    companion object {
        val instance: LoginPresenter by lazy { Holder.INSTANCE }
    }

    fun signInWithEmailAndPassword(email: String, password: String) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(LoginActivity()) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(LoginActivity.TAG, "signInWithEmail:success")
                        updateUI()
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(LoginActivity.TAG, "signInWithEmail:failure", task.exception)
                        screen?.updateLoginFailed()
                    }
                }
    }

    fun updateUI() {
        val currentUser = mAuth.currentUser
        var emailVerified = false
        currentUser?.let {
            emailVerified = it.isEmailVerified
        }
        //todo email verifacian delete
        emailVerified = true
        if (currentUser != null && emailVerified) {
            screen?.updateLogin()
        }

    }


}
