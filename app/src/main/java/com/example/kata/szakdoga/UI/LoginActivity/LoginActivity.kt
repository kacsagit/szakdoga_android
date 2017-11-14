package com.example.kata.szakdoga.UI.LoginActivity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.example.kata.szakdoga.R
import com.example.kata.szakdoga.UI.IconTabsActivity.IconTabsActivity
import com.example.kata.szakdoga.UI.SignUpActivity.SignUpActivity
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity(), LoginScreen {



    companion object {
        var TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        ok_b.setOnClickListener {
            if (!email_et.text.isEmpty() && !password_et.text.isEmpty()) {
                LoginPresenter.instance.signInWithEmailAndPassword(email_et.text.toString(), password_et.text.toString())
            }
        }
        sign_up_b.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    public override fun onStart() {
        super.onStart()
        LoginPresenter.instance.attachScreen(this)
        // Check if user is signed in and update UI accordingly.
        LoginPresenter.instance.updateUI()
    }



    override fun updateLogin() {
        val intent = Intent(this, IconTabsActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }



    override fun updateLoginFailed() {
        Toast.makeText(this@LoginActivity, "Authentication failed.",
                Toast.LENGTH_SHORT).show()
    }



    override fun onStop() {
        super.onStop()
        LoginPresenter.instance.detachScreen()
    }

}
