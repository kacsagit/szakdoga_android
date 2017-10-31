package com.example.kata.szakdoga.UI

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.kata.szakdoga.R
import com.example.kata.szakdoga.adapter.OwnVideoListAdapter
import com.example.kata.szakdoga.data.User
import com.example.kata.szakdoga.data.Videos
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.fragment_own_list.view.*
import java.util.ArrayList
import kotlin.collections.HashMap


class OwnListFragment : Fragment() {
    private var mAdapter: OwnVideoListAdapter? = null
    private var mLayoutManager: RecyclerView.LayoutManager? = null

    lateinit var mAuth: FirebaseAuth

    lateinit var mStorageRef: StorageReference
    lateinit var users: HashMap<String, User>
    val link = ""
    lateinit var myRef: Query
    var user: FirebaseUser? = null
    lateinit var database: FirebaseDatabase
    var items = ArrayList<Videos>()

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

        mStorageRef = FirebaseStorage.getInstance().reference


        // Define a layout for RecyclerView
        mLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        view.recycler_view?.layoutManager = mLayoutManager



        user = mAuth.currentUser
        database = FirebaseDatabase.getInstance()

        mAdapter = OwnVideoListAdapter(items)
        view.recycler_view?.adapter = mAdapter

        users = HashMap<String, User>()
        updateVideos()

        return view

    }


    fun updateVideos() {
        myRef = database.getReference("videos").orderByChild("user").equalTo(user?.uid)
        myRef.addValueEventListener(
                object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated.
                        items = ArrayList<Videos>()
                        for (child in dataSnapshot.children) {
                            val value = child.getValue(Videos::class.java)
                            items.add(value!!)
                            Log.d(VideoListFragment.TAG, "Value is: " + value)

                        }
                        mAdapter?.update(items)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Failed to read value
                        Log.w(VideoListFragment.TAG, "Failed to read value.", error.toException())
                    }
                })
    }


}
