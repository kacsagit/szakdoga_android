package com.example.kata.szakdoga

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import java.util.*


/**
 * Created by Kata on 2017. 09. 08..
 */
class ColorAdapter(context: Context, dataset: ArrayList<Videos>) : RecyclerView.Adapter<ColorAdapter.ViewHolder>() {
    private var mDataSet: ArrayList<Videos> = dataset
    private var mContext: Context = context
    private val mRandom = Random()


    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder?.mTextView?.text = mDataSet[position].link
        // Set a random height for TextView
        holder?.mTextView?.layoutParams?.height = getRandomIntInRange(500, 450);
        // Set a random color for TextView background
        holder?.itemView?.setOnClickListener {
            val intent = Intent(mContext, VideoActivity::class.java)
            intent.putExtra("link",mDataSet[position].link)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            mContext.startActivity(intent)
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
        var mTextView: TextView = v.findViewById(R.id.tv)

    }
}