package com.example.kata.szakdoga.UI.VideoListFragment

import android.util.Log
import com.example.kata.szakdoga.UI.Presenter
import com.example.kata.szakdoga.data.User
import com.example.kata.szakdoga.data.Videos
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * Created by Kata on 2017. 03. 12..
 */

class VideoListPresenter : Presenter<VideoListScreen>() {


    private object Holder {
        val INSTANCE = VideoListPresenter()
    }

    companion object {
        val instance: VideoListPresenter by lazy { Holder.INSTANCE }
    }

    val database = FirebaseDatabase.getInstance()
    val myUserRef = database.getReference("users")
    val mAuth = FirebaseAuth.getInstance()

    var users= HashMap<String, User>()
    var friends = HashSet<String>()

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
                    Log.d(VideoListFragment.TAG, "Value is: " + value)
                }
                updateFriends()

            }


            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(VideoListFragment.TAG, "Failed to read value.", error.toException())
            }
        })
    }

    fun updateFriends(){
        friends = HashSet<String>()
        var myUserRef = database.getReference("friends").child(mAuth.currentUser?.uid).child("friends")
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

    var items = ArrayList<Videos>()

    fun updateVideos() {
        val myRef = database.getReference("videos")
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
                                Log.d(VideoListFragment.TAG, "Value is: " + value)
                            }
                        }
                        screen?.updateVideos(items)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Failed to read value
                        Log.w(VideoListFragment.TAG, "Failed to read value.", error.toException())
                    }
                })
    }





}