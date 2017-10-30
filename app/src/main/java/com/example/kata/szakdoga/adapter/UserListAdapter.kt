package com.example.kata.szakdoga.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.example.kata.szakdoga.R
import com.example.kata.szakdoga.data.User
import com.example.kata.szakdoga.data.UserId
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*


/**
 * Created by Kata on 2017. 09. 08..
 */
class UserListAdapter(dataset: ArrayList<User>) : RecyclerView.Adapter<UserListAdapter.ViewHolder>() {
    private var mDataSet: ArrayList<User> = dataset
    private var friends: HashSet<String> = HashSet<String>()

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        var currentUser = FirebaseAuth.getInstance().currentUser!!
        val ref = FirebaseDatabase.getInstance().getReference("friends")
        holder?.user?.text = mDataSet[position].email

        if (!friends.contains(mDataSet[position].uid)) {
            holder?.followButton?.visibility = VISIBLE
            holder?.unfollowButton?.visibility = GONE
        } else {
            holder?.followButton?.visibility = GONE
            holder?.unfollowButton?.visibility = VISIBLE
        }


        holder?.followButton?.setOnClickListener {
            holder.followButton.visibility = GONE
            holder.unfollowButton.visibility = VISIBLE
            ref.child(currentUser.uid).child("user").setValue(currentUser.uid)
            ref.child(currentUser.uid).child("friends").child(mDataSet[position].uid).setValue(UserId(mDataSet[position].uid))
        }
        holder?.unfollowButton?.setOnClickListener {
            holder.followButton.visibility = VISIBLE
            holder.unfollowButton.visibility = GONE
            ref.child(currentUser.uid).child(mDataSet[position].uid).setValue(null)


            val queryRef = ref.child(currentUser.uid).child("friends")

            queryRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError?) {

                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (child in dataSnapshot.children) {
                        if (child.key == mDataSet[position].uid) {
                            ref.child(currentUser.uid).child("friends").child(child.key).setValue(null);
                        }
                    }
                }

            })


        }
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }


    fun update(itemsrec: List<User>) {
        mDataSet.clear()
        mDataSet.addAll(itemsrec)
        notifyDataSetChanged()

    }


    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent?.context).inflate(R.layout.user_view, parent, false)
        return ViewHolder(v)
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var followButton: Button = v.findViewById(R.id.follow_button)
        var unfollowButton: Button = v.findViewById(R.id.unfollow_button)
        var user: TextView = v.findViewById(R.id.user_text)

    }


    fun updateFriend(friendlist: HashSet<String>) {
        friends = friendlist
        notifyDataSetChanged()
    }


}