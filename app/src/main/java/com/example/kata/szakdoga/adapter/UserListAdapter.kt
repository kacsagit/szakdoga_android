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
import java.util.*

/**
 * Created by Kata on 2017. 09. 08..
 */
class UserListAdapter(dataset: ArrayList<User>) : RecyclerView.Adapter<UserListAdapter.ViewHolder>() {
    private var mDataSet: ArrayList<User> = dataset


    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {

        holder?.user?.text=mDataSet[position].email
        holder?.followButton?.setOnClickListener{
            holder.followButton.visibility=GONE
            holder.unfollowButton.visibility= VISIBLE
        }
        holder?.unfollowButton?.setOnClickListener{
            holder.followButton.visibility=VISIBLE
            holder.unfollowButton.visibility= GONE
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





}