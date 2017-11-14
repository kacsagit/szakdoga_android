package com.example.kata.szakdoga.UI.LoginActivity

import android.content.ContentResolver
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import com.example.kata.szakdoga.UI.Presenter
import com.example.kata.szakdoga.UI.VideoListFragment.VideoListFragment
import com.example.kata.szakdoga.data.Videos
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileOutputStream
import java.util.*

/**
 * Created by Kata on 2017. 03. 12..
 */

class TabsPresenter : Presenter<TabsScreen>() {

    val mStorageRef = FirebaseStorage.getInstance().reference
    val mAuth = FirebaseAuth.getInstance()
    val user = mAuth.currentUser
    val database = FirebaseDatabase.getInstance()
    val myRef = database.getReference("videos")

    private object Holder {
        val INSTANCE = TabsPresenter()
    }

    companion object {
        val instance: TabsPresenter by lazy { Holder.INSTANCE }
    }

    fun uploadFile(selectedImagePath: String) {
        val file = Uri.fromFile(File(selectedImagePath))

        val name = UUID.randomUUID().toString()

        val imageRef = mStorageRef.child("images/" + user!!.uid + "/" + name + "image")
        val riversRef = mStorageRef.child("images/" + user!!.uid + "/" + name)

        val bMap = ThumbnailUtils.createVideoThumbnail(file.path, MediaStore.Video.Thumbnails.MINI_KIND)

        imageRef.putFile(saveImage(bMap, name))
                .addOnSuccessListener({ imageSnapshot ->
                    // Get a URL to the uploaded content
                    riversRef.putFile(file)
                            .addOnSuccessListener({ taskSnapshot ->
                                // Get a URL to the uploaded content
                                screen?.loadingFlayout(View.GONE)
                                val downloadUrl = taskSnapshot.downloadUrl
                                val imagedUrl = imageSnapshot.downloadUrl
                                val video = Videos(downloadUrl.toString(), imagedUrl.toString(), user!!.uid, true)
                                val newRef = myRef.push()
                                newRef.setValue(video)
                                Log.d(VideoListFragment.TAG, downloadUrl.toString())
                            })
                            .addOnFailureListener({ e ->
                                screen?.loadingFlayout(View.GONE)
                                Log.d(VideoListFragment.TAG, "failed")
                                e.printStackTrace()
                                // Handle unsuccessful uploads
                                // ...

                            })
                })
                .addOnFailureListener({ e ->
                    Log.d(VideoListFragment.TAG, "failed")
                    e.printStackTrace()
                    // Handle unsuccessful uploads
                    // ...

                })

    }

    private fun saveImage(finalBitmap: Bitmap, filename: String): Uri {

        val root = Environment.getExternalStorageDirectory().absolutePath
        val myDir = File(root + "/saved_images")
        myDir.mkdirs()

        val file = File(myDir, filename)
        if (file.exists()) file.delete()

        FileOutputStream(file).use {
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)
            it.flush()
        }
        return Uri.fromFile(file)
    }


    fun getPath(uri: Uri,contentResolver: ContentResolver): String? {
        val projection = arrayOf(MediaStore.Video.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        return if (cursor != null) {
            val column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            cursor.moveToFirst()
            val value = cursor.getString(column_index)
            cursor.close()
            value
        } else
            null
    }



}
