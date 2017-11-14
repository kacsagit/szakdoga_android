package com.example.kata.szakdoga.UI.VideoListFragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.kata.szakdoga.R
import com.example.kata.szakdoga.adapter.VideoListAdapter
import com.example.kata.szakdoga.data.Videos
import kotlinx.android.synthetic.main.fragment_video_list.view.*


class VideoListFragment : Fragment(),VideoListScreen {


    companion object {
        var TAG = "VideoListActivity"
    }

    private var mAdapter: VideoListAdapter? = null
    private var mLayoutManager: RecyclerView.LayoutManager? = null


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater!!.inflate(R.layout.fragment_video_list, container, false)

        // Define a layout for RecyclerView
        mLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        view.recycler_view?.layoutManager = mLayoutManager


        mAdapter = VideoListAdapter(ArrayList<Videos>())
        view.recycler_view?.adapter = mAdapter


        VideoListPresenter.instance.updateUser()

        return view

    }





    override fun updateVideos(items: ArrayList<Videos>) {
        mAdapter?.update(items)
    }


    override fun onStart() {
        super.onStart()
        VideoListPresenter.instance.attachScreen(this)
    }

    override fun onStop() {
        super.onStop()
        VideoListPresenter.instance.detachScreen()
    }


}
