package com.example.kata.szakdoga.UI.SignUpActivity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.example.kata.szakdoga.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_sign_up.*


class SignUpActivity : AppCompatActivity(),SignUpScreen {

    companion object {
        var TAG = "SignUpActivity"
    }

    lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        mAuth = FirebaseAuth.getInstance()
        ok_b.setOnClickListener {
            if (!email_et.text.isEmpty() && !password_et.text.isEmpty() && (password_et.text.toString()==password_2et.text.toString())) {
                SignUpPresenter.instance.createUserWithEmailAndPassword(email_et.text.toString(), password_et.text.toString())
            }
        }
    }


    override fun updateUI(currentUser: FirebaseUser?) {
        if (currentUser != null) {
            Toast.makeText(this, "Sign up successful, confirm e-mail address", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "not successfull", Toast.LENGTH_LONG).show()
        }

    }

    override fun onStart() {
        super.onStart()
        SignUpPresenter.instance.attachScreen(this)
    }

    override fun onStop() {
        super.onStop()
        SignUpPresenter.instance.detachScreen()
    }
}
