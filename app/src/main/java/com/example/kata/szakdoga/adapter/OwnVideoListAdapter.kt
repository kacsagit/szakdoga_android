package com.example.kata.szakdoga.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.view.ViewCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import com.example.kata.szakdoga.R
import com.example.kata.szakdoga.data.Videos
import com.example.kata.szakdoga.UI.videoplayer.PlayerActivity
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.util.*

/**
 * Created by Kata on 2017. 09. 08..
 */
class OwnVideoListAdapter(dataset: ArrayList<Videos>) : RecyclerView.Adapter<OwnVideoListAdapter.ViewHolder>() {
    private var mDataSet: ArrayList<Videos> = dataset
    private lateinit var mContext: Context


    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        Picasso.with(mContext).load(mDataSet[position].thumbnail).placeholder(R.drawable.default_img).into(holder?.image, object : Callback {
            override fun onError() {}

            override fun onSuccess() {
                holder?.progressBar?.visibility = View.GONE
                holder?.playImage?.visibility = View.VISIBLE
            }


        })

        holder?.itemView?.setOnClickListener {
            var preferExtensionDecoders = false
            var uri = mDataSet[position].link
            var extension = ""
            var adTagUri = null
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(mContext as Activity,
                    holder.image,
                    ViewCompat.getTransitionName(holder.image))
            var sample = UriSample(preferExtensionDecoders, uri, extension, adTagUri)
            mContext.startActivity(sample.buildIntent(mContext), options.toBundle())


//            val intent = Intent(mContext, VideoActivity::class.java)
//            intent.putExtra("link",mDataSet[position].link)
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//
//            mContext.startActivity(intent, options.toBundle())
        }
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }


    public fun update(itemsrec: List<Videos>) {
        mDataSet.clear()
        mDataSet.addAll(itemsrec)
        notifyDataSetChanged()

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.own_video_view, parent, false)
        mContext = parent.context
        return ViewHolder(v)
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var image: ImageView = v.findViewById(R.id.image)
        var playImage: ImageView = v.findViewById(R.id.play_image)
        var progressBar: ProgressBar = v.findViewById(R.id.progress_bar)

    }

    private class UriSample(val preferExtensionDecoders: Boolean?, val uri: String?,
                            val extension: String?, val adTagUri: String?) {

        fun buildIntent(context: Context): Intent {
            val intent = Intent(context, PlayerActivity::class.java)
            intent.putExtra(PlayerActivity.PREFER_EXTENSION_DECODERS, preferExtensionDecoders)
                    .setData(Uri.parse(uri))
                    .putExtra(PlayerActivity.EXTENSION_EXTRA, extension)
                    .putExtra(PlayerActivity.AD_TAG_URI_EXTRA, adTagUri)
                    .setAction(PlayerActivity.ACTION_VIEW)

            return intent
        }

    }


}