package com.example.kata.szakdoga.UI

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.kata.szakdoga.R
import com.example.kata.szakdoga.adapter.VideoListAdapter
import com.example.kata.szakdoga.data.User
import com.example.kata.szakdoga.data.Videos
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.fragment_video_list.view.*
import java.util.*
import kotlin.collections.HashMap


class VideoListFragment : Fragment() {
    companion object {
        var TAG = "VideoListActivity"
    }

    private var mAdapter: VideoListAdapter? = null
    private var mLayoutManager: RecyclerView.LayoutManager? = null

    lateinit var mAuth: FirebaseAuth

    lateinit var mStorageRef: StorageReference
    lateinit var users: HashMap<String, User>
    val link = ""
    lateinit var myRef: DatabaseReference
    var user: FirebaseUser? = null
    lateinit var database: FirebaseDatabase
    var items = ArrayList<Videos>()

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater!!.inflate(R.layout.fragment_video_list, container, false)
        mStorageRef = FirebaseStorage.getInstance().reference
        mAuth = FirebaseAuth.getInstance()


        // Define a layout for RecyclerView
        mLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        view.recycler_view?.layoutManager = mLayoutManager



        user = mAuth.currentUser
        database = FirebaseDatabase.getInstance()
        myRef = database.getReference("videos")
        // Read from the database


        mAdapter = VideoListAdapter(items)
        view.recycler_view?.adapter = mAdapter



        users = HashMap<String, User>()
        updateUser()

        return view

    }

    fun updateUser() {
        var myUserRef = database.getReference("users")
        myUserRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                users = HashMap<String, User>()
                for (child in dataSnapshot.children) {
                    val value = child.getValue(User::class.java)
                    users.put(child.key, value!!)
                    Log.d(TAG, "Value is: " + value)
                }
                updateFriends()

            }


            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })
    }


    fun updateVideos() {
        myRef.addValueEventListener(
                object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated.
                        items = ArrayList<Videos>()
                        for (child in dataSnapshot.children) {
                            val value = child.getValue(Videos::class.java)
                            if (friends.contains(value?.user)) {
                                var user = users[value?.user]?.email
                                user?.let { value?.user = it }
                                items.add(value!!)
                                Log.d(TAG, "Value is: " + value)
                            }
                        }
                        mAdapter?.update(items)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Failed to read value
                        Log.w(TAG, "Failed to read value.", error.toException())
                    }
                })
    }

    var friends = HashSet<String>()

    fun updateFriends() {

        var myUserRef = database.getReference("friends").child(user?.uid).child("friends")
        myUserRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                friends = HashSet<String>()
                for (child in dataSnapshot.children) {
                    val value = child.key
                    friends.add(value.toString())
                    Log.d(VideoListFragment.TAG, "Value is: " + value)
                }
                updateVideos()
            }


            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(VideoListFragment.TAG, "Failed to read value.", error.toException())
            }
        })
    }


}
