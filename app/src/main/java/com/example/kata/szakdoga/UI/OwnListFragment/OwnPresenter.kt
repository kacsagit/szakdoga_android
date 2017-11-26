package com.example.kata.szakdoga.UI.OwnListFragment

import android.util.Log
import com.example.kata.szakdoga.UI.Presenter
import com.example.kata.szakdoga.UI.VideoListFragment.VideoListFragment
import com.example.kata.szakdoga.data.Videos
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

/**
 * Created by Kata on 2017. 03. 12..
 */

class OwnPresenter : Presenter<OwnScreen>() {

  val mAuth = FirebaseAuth.getInstance()
  val user = mAuth.currentUser
  val database = FirebaseDatabase.getInstance()
  val myRef = database.getReference("videos").orderByChild("user").equalTo(user?.uid)
  var items = ArrayList<Videos>()

  private object Holder {
    val INSTANCE = OwnPresenter()
  }

  companion object {
    val instance: OwnPresenter by lazy { Holder.INSTANCE }
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
                  items.add(value!!)
                  Log.d(VideoListFragment.TAG, "Value is: " + value)

                }
                screen?.updateAdapter(items)
              }

              override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(VideoListFragment.TAG, "Failed to read value.", error.toException())
              }
            })
  }


}