package com.example.kata.szakdoga.UI.OwnListFragment

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.kata.szakdoga.R
import com.example.kata.szakdoga.UI.LoginActivity.LoginActivity
import com.example.kata.szakdoga.adapter.OwnVideoListAdapter
import com.example.kata.szakdoga.data.Videos
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_own_list.view.*
import java.util.*


class OwnListFragment : Fragment(),OwnScreen {
    private var mAdapter: OwnVideoListAdapter? = null
    private var mLayoutManager: RecyclerView.LayoutManager? = null

    lateinit var mAuth: FirebaseAuth


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater!!.inflate(R.layout.fragment_own_list, container, false)
        mAuth = FirebaseAuth.getInstance()
        view.logout_button.setOnClickListener {
            mAuth.signOut()
            val intent = Intent(context, LoginActivity::class.java)
            startActivity(intent)
        }

        view.user_text.text = mAuth.currentUser?.email

        // Define a layout for RecyclerView
        mLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        view.recycler_view?.layoutManager = mLayoutManager

        val items = ArrayList<Videos>()
        mAdapter = OwnVideoListAdapter(items)
        view.recycler_view?.adapter = mAdapter

        OwnPresenter.instance.updateVideos()

        return view

    }


    override fun updateAdapter(items: ArrayList<Videos>) {
        mAdapter?.update(items)

    }

    override fun onStart() {
        super.onStart()
        OwnPresenter.instance.attachScreen(this)
    }

    override fun onStop() {
        super.onStop()
        OwnPresenter.instance.detachScreen()
    }

}
