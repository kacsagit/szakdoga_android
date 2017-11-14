package com.example.kata.szakdoga.UI.UsersListFragment

import android.util.Log
import com.example.kata.szakdoga.UI.Presenter
import com.example.kata.szakdoga.UI.VideoListFragment.VideoListFragment
import com.example.kata.szakdoga.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * Created by Kata on 2017. 03. 12..
 */

class UsersListPresenter : Presenter<UsersListScreen>() {


    private object Holder {
        val INSTANCE = UsersListPresenter()
    }

    companion object {
        val instance: UsersListPresenter by lazy { Holder.INSTANCE }
    }

    val database = FirebaseDatabase.getInstance()
    val myUserRef = database.getReference("users")
    val mAuth = FirebaseAuth.getInstance()

    var users = ArrayList<User>()
    init{
        val currentUser = mAuth.currentUser!!
        myUserRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                users = ArrayList<User>()
                for (child in dataSnapshot.children) {
                    if (child.key != currentUser.uid) {
                        val value = child.getValue(User::class.java)
                        users.add(value!!)
                        Log.d(VideoListFragment.TAG, "Value is: " + value)
                    }
                }
                screen?.updateUsers(users)

            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(VideoListFragment.TAG, "Failed to read value.", error.toException())
            }
        })
    }

    fun updateFriends(){
        var friends = HashSet<String>()
        var myUserRefFriends = database.getReference("friends").child(mAuth.currentUser?.uid).child("friends")
        myUserRefFriends.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                friends = HashSet<String>()
                for (child in dataSnapshot.children) {
                    val value = child.key
                    friends.add(value.toString())
                    Log.d(VideoListFragment.TAG, "Value is: " + value)
                }
                screen?.updateFriend(friends)
            }


            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(VideoListFragment.TAG, "Failed to read value.", error.toException())
            }
        })
    }



}