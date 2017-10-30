package com.example.kata.szakdoga.UI

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.example.kata.szakdoga.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {


    companion object {
        var TAG = "LoginActivity"
    }

    lateinit var mAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        mAuth = FirebaseAuth.getInstance()

        ok_b.setOnClickListener {
            signInWithEmailAndPassword(email_et.text.toString(), password_et.text.toString())
        }
        sign_up_b.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = mAuth.currentUser
        updateUI(currentUser)
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        val user = mAuth.currentUser
        var emailVerified = false
        user?.let{
            emailVerified = it.isEmailVerified
        }
        //todo email verifacian delete
        emailVerified = true
        if (currentUser != null && emailVerified) {
            Toast.makeText(this, "logd in", Toast.LENGTH_LONG).show()
            val intent = Intent(this, IconTabsActivity::class.java)
            startActivity(intent)
        } else {
            Toast.makeText(this, "not successfull", Toast.LENGTH_LONG).show()
        }

    }

    private fun signInWithEmailAndPassword(email: String, password: String) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success")
                        val user = mAuth.currentUser
                        updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        Toast.makeText(this@LoginActivity, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        updateUI(null)
                    }
                }
    }

}
