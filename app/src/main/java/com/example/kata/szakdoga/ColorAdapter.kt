package com.example.kata.szakdoga

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.view.ViewCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.squareup.picasso.Picasso
import java.util.*




/**
 * Created by Kata on 2017. 09. 08..
 */
class ColorAdapter(activity: Activity, dataset: ArrayList<Videos>) : RecyclerView.Adapter<ColorAdapter.ViewHolder>() {
    private var mDataSet: ArrayList<Videos> = dataset
    private var mContext: Context = activity
    private var act: Activity = activity
    private val mRandom = Random()


    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        Picasso.with(mContext).load(mDataSet[position].tumbnail).placeholder(R.mipmap.ic_launcher).into(holder?.image)
        // Set a random height for TextView
    //    holder?.image?.layoutParams?.height = getRandomIntInRange(500, 450);
        // Set a random color for TextView background
        holder?.itemView?.setOnClickListener {
            val intent = Intent(mContext, VideoActivity::class.java)
            intent.putExtra("link",mDataSet[position].link)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(act,
                    holder.image,
                    ViewCompat.getTransitionName(holder.image))
            mContext.startActivity(intent, options.toBundle())
        }
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }


    protected fun getRandomIntInRange(max: Int, min: Int): Int {
        return mRandom.nextInt(max - min + min) + min
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(mContext).inflate(R.layout.custom_view, parent, false)
        return ViewHolder(v)
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var image: ImageView = v.findViewById(R.id.image)

    }
}