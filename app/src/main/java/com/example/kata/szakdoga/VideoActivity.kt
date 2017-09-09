package com.example.kata.szakdoga

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_video.*

class VideoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)
        val str = intent.extras.getString("link")
       // val str = "https://firebasestorage.googleapis.com/v0/b/szakdoga-49405.appspot.com/o/images%2FJuW43Q7SlYalp00JBL8T0K4Sc4D2%2F20170908_134004.mp4?alt=media&token=98982204-0dd8-4af0-a001-495237e534ea"
        val uri = Uri.parse(str)
        video_view.setVideoURI(uri)
        video_view.requestFocus()
        video_view.start()

    }

}
