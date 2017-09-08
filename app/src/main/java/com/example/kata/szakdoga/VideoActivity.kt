package com.example.kata.szakdoga

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_video.*

class VideoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)
        val str = "https://firebasestorage.googleapis.com/v0/b/szakdoga-49405.appspot.com/o/images%2FJuW43Q7SlYalp00JBL8T0K4Sc4D2%2F20170908_110220.mp4?alt=media&token=7819b3be-d775-4091-b9d6-0a8756d03ffb"
        val uri = Uri.parse(str)
        video_view.setVideoURI(uri)
        video_view.requestFocus()
        video_view.start()

    }

}
