package com.example.kata.szakdoga

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_video_list.*
import java.io.File
import java.io.FileOutputStream
import java.util.*


class VideoListActivity : AppCompatActivity() {


    companion object {
        var REQUEST_TAKE_GALLERY_VIDEO = 1
        var TAG = "VideoListActivity"
    }

    private var mAdapter: RecyclerView.Adapter<*>? = null
    private var mLayoutManager: RecyclerView.LayoutManager? = null

    lateinit var mAuth: FirebaseAuth

    lateinit var mStorageRef: StorageReference

    val link = ""
    lateinit var myRef: DatabaseReference
    var user: FirebaseUser? = null
    lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_list)
        mStorageRef = FirebaseStorage.getInstance().reference
        mAuth = FirebaseAuth.getInstance()
        upload_b.setOnClickListener {
            val intent = Intent()
            intent.type = "video/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Video"), REQUEST_TAKE_GALLERY_VIDEO)
        }


        // Define a layout for RecyclerView
        mLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        recycler_view?.layoutManager = mLayoutManager

        stream.setOnClickListener {
            //TODO: userid and roomname
            val intent = Intent(this, StreamVideoActivity::class.java)
            intent.putExtra(Constants.ROOM_NAME,"id")
            intent.putExtra(Constants.USER_NAME,"usergfrfgd")
            startActivity(intent)
        }

        user = mAuth.currentUser
        database = FirebaseDatabase.getInstance()
        myRef = database.getReference("videos")
        // Read from the database
        var items = ArrayList<Videos>()

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                items = ArrayList<Videos>()
                for (child in dataSnapshot.children) {
                    val value = child.getValue(Videos::class.java)
                    items.add(value!!)
                    Log.d(TAG, "Value is: " + value)
                }
                mAdapter = ColorAdapter(this@VideoListActivity, items)
                recycler_view?.adapter = mAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })
        mAdapter = ColorAdapter(this, items)
        recycler_view?.adapter = mAdapter
    }


    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {
                val selectedImageUri = data.data

                // MEDIA GALLERY
                val selectedImagePath = getPath(selectedImageUri)
                if (selectedImagePath != null) {
                    uploadFile(selectedImagePath)

                }
            }
        }
    }

    private fun uploadFile(selectedImagePath: String) {
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
                                val downloadUrl = taskSnapshot.downloadUrl
                                val imagedUrl = imageSnapshot.downloadUrl
                                var video = Videos(downloadUrl.toString(), imagedUrl.toString(), user!!.uid, true)
                                val newRef = myRef.push()
                                newRef.setValue(video)
                                Log.d(TAG, downloadUrl.toString())
                            })
                            .addOnFailureListener({ e ->
                                Log.d(TAG, "failed")
                                e.printStackTrace()
                                // Handle unsuccessful uploads
                                // ...

                            })
                })
                .addOnFailureListener({ e ->
                    Log.d(TAG, "failed")
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


    private fun getPath(uri: Uri): String? {
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
