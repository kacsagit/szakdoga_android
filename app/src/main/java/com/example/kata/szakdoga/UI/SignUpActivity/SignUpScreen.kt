package com.example.kata.szakdoga.UI.SignUpActivity

import com.google.firebase.auth.FirebaseUser

/**
 * Created by Kata on 2017. 03. 12..
 */

interface SignUpScreen {
    fun updateUI(currentUser: FirebaseUser?)


}