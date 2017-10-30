package com.example.kata.szakdoga.UI

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.kata.szakdoga.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_three.view.*


class ThreeFragment : Fragment() {


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var view=inflater!!.inflate(R.layout.fragment_three, container, false)
        view.logout_button.setOnClickListener {
            var mAuth = FirebaseAuth.getInstance()
            mAuth.signOut()
            val intent = Intent(context, LoginActivity::class.java)
            startActivity(intent)
        }
        return view
    }

}// Required empty public constructor
