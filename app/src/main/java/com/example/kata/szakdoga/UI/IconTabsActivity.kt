package com.example.kata.szakdoga.UI

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import com.example.kata.szakdoga.Constants
import com.example.kata.szakdoga.R
import com.example.kata.szakdoga.data.Videos
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_icon_tabs.*
import kotlinx.android.synthetic.main.activity_icon_tabs.view.*
import java.io.File
import java.io.FileOutputStream
import java.util.*

class IconTabsActivity : AppCompatActivity() {
    companion object {
        var REQUEST_TAKE_GALLERY_VIDEO = 1
    }

    lateinit var mStorageRef: StorageReference

    lateinit var mAuth: FirebaseAuth
    val link = ""
    lateinit var myRef: DatabaseReference
    var user: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_icon_tabs)
        mStorageRef = FirebaseStorage.getInstance().reference
        mAuth = FirebaseAuth.getInstance()
        user = mAuth.currentUser
        val database = FirebaseDatabase.getInstance()
        myRef = database.getReference("videos")

        setSupportActionBar(toolbar)
        toolbar.add_button.setOnClickListener {
            val intent = Intent()
            intent.type = "video/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Video"), REQUEST_TAKE_GALLERY_VIDEO)
        }


        supportActionBar?.setDisplayShowTitleEnabled(false)

        setupViewPager(viewpager)

        tabs.setupWithViewPager(viewpager)

        setupTabIcons()
    }

    private fun setupTabIcons() {
        val tabIcons = intArrayOf(R.drawable.icons8_home, R.drawable.icons8_people, R.drawable.ic_tab_contacts)

        tabs.getTabAt(0)?.setIcon(tabIcons[0])
        tabs.getTabAt(1)?.setIcon(tabIcons[1])
        tabs.getTabAt(2)?.setIcon(tabIcons[2])
    }

    private fun setupViewPager(viewPager: ViewPager?) {
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFrag(VideoListFragment(), "ONE")
        adapter.addFrag(UsersListFragment(), "TWO")
        adapter.addFrag(OwnListFragment(), "THREE")
        viewPager?.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_list, menu)
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item!!.itemId
        when (id) {
            R.id.action_record -> {
                //TODO: userid and roomname
                val intent = Intent(this, StreamVideoActivity::class.java)
                intent.putExtra(Constants.ROOM_NAME, "id")
                intent.putExtra(Constants.USER_NAME, "usergfrfgd")
                startActivity(intent)
                return true
            }
        }

        return false
    }


    internal inner class ViewPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager) {
        private val mFragmentList = ArrayList<Fragment>()
        private val mFragmentTitleList = ArrayList<String>()

        override fun getItem(position: Int): Fragment {
            return mFragmentList[position]
        }

        override fun getCount(): Int {
            return mFragmentList.size
        }

        fun addFrag(fragment: Fragment, title: String) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence? {

            // return null to display only the icon
            return null
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {
                data?.let {
                    val selectedImageUri = data.data
                    // MEDIA GALLERY
                    val selectedImagePath = getPath(selectedImageUri)
                    if (selectedImagePath != null) {
                        uploadFile(selectedImagePath)
                        loading_flayout.visibility=VISIBLE

                    }
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
                                loading_flayout.visibility=GONE
                                val downloadUrl = taskSnapshot.downloadUrl
                                val imagedUrl = imageSnapshot.downloadUrl
                                val video = Videos(downloadUrl.toString(), imagedUrl.toString(), user!!.uid, true)
                                val newRef = myRef.push()
                                newRef.setValue(video)
                                Log.d(VideoListFragment.TAG, downloadUrl.toString())
                            })
                            .addOnFailureListener({ e ->
                                loading_flayout.visibility=GONE
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
