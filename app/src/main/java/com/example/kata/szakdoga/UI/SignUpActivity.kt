package com.example.kata.szakdoga.UI

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.example.kata.szakdoga.R
import com.example.kata.szakdoga.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_sign_up.*




class SignUpActivity : AppCompatActivity() {

    companion object {
        var TAG="SignUpActivity"
    }
    lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        mAuth = FirebaseAuth.getInstance()
        ok_b.setOnClickListener{
            createUserWithEmailAndPassword(email_et.text.toString(),password_et.text.toString())
        }
    }

    private fun createUserWithEmailAndPassword(email: String, password: String) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success")
                        val user = mAuth.currentUser
                        user?.sendEmailVerification()
                                ?.addOnCompleteListener { task1 ->
                                    if (task1.isSuccessful) {
                                        Log.d(TAG, "Conformation email sent.")
                                        val ref = FirebaseDatabase.getInstance().getReference("users")
                                         ref.child(user.uid).setValue(User(user.uid,user.email))
                                    }
                                }
                        updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        Toast.makeText(this@SignUpActivity, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        updateUI(null)
                    }

                    // ...
                }
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        if (currentUser != null) {
            Toast.makeText(this, "Sign up successful, confirm e-mail address", Toast.LENGTH_LONG).show()
        }else{
            Toast.makeText(this, "not successfull", Toast.LENGTH_LONG).show()
        }

    }
}
