package com.example.kata.szakdoga.UI

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.kata.szakdoga.R
import com.example.kata.szakdoga.adapter.UserListAdapter
import com.example.kata.szakdoga.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_users_list.view.*


class UsersListFragment : Fragment() {
    var users = ArrayList<User>()
    lateinit var adapter: UserListAdapter

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater!!.inflate(R.layout.fragment_users_list, container, false)
        val database = FirebaseDatabase.getInstance()
        val myUserRef = database.getReference("users")
        val mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser?.uid
        myUserRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                users = ArrayList<User>()
                for (child in dataSnapshot.children) {
                    if (child.key != currentUser) {
                        val value = child.getValue(User::class.java)
                        users.add(value!!)
                        Log.d(VideoListFragment.TAG, "Value is: " + value)
                    }
                }
                adapter.update(users)

            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(VideoListFragment.TAG, "Failed to read value.", error.toException())
            }
        })


        adapter = UserListAdapter(users)
        view.recycler_view.layoutManager = LinearLayoutManager(context)
        view.recycler_view.adapter = adapter

        return view
    }


}// Required empty public constructor
