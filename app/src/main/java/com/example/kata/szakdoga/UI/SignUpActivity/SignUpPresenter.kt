package com.example.kata.szakdoga.UI.SignUpActivity

import android.util.Log
import com.example.kata.szakdoga.UI.Presenter
import com.example.kata.szakdoga.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

/**
 * Created by Kata on 2017. 03. 12..
 */

class SignUpPresenter : Presenter<SignUpScreen>() {

    var   mAuth = FirebaseAuth.getInstance()

    private object Holder {
        val INSTANCE = SignUpPresenter()
    }

    companion object {
        val instance: SignUpPresenter by lazy { Holder.INSTANCE }
    }


    fun createUserWithEmailAndPassword(email: String, password: String) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(SignUpActivity()) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(SignUpActivity.TAG, "createUserWithEmail:success")
                        val user = mAuth.currentUser
                        user?.sendEmailVerification()
                                ?.addOnCompleteListener { task1 ->
                                    if (task1.isSuccessful) {
                                        Log.d(SignUpActivity.TAG, "Conformation email sent.")
                                        val ref = FirebaseDatabase.getInstance().getReference("users")
                                        ref.child(user.uid).setValue(User(user.uid, user.email))
                                    }
                                }
                        screen?.updateUI(user)
                    } else {
                        screen?.updateUI(null)
                    }

                }
    }



}