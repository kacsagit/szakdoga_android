package com.example.kata.szakdoga

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.fragment_video_list.view.*
import java.util.*


class VideoListFragment : Fragment() {
    companion object {
        var TAG = "VideoListActivity"
    }

    private var mAdapter: VideoListAdapter? = null
    private var mLayoutManager: RecyclerView.LayoutManager? = null

    lateinit var mAuth: FirebaseAuth

    lateinit var mStorageRef: StorageReference

    val link = ""
    lateinit var myRef: DatabaseReference
    var user: FirebaseUser? = null
    lateinit var database: FirebaseDatabase


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
        var items = ArrayList<Videos>()

        mAdapter = VideoListAdapter(activity, items)
        view.recycler_view?.adapter = mAdapter

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                items = ArrayList<Videos>()
                for (child in dataSnapshot.children) {
                    val value = child.getValue(Videos::class.java)
                    items.add(value!!)
                    Log.d(TAG, "Value is: " + value)
                }
                mAdapter?.update(items)
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })

        return view
    }



}
