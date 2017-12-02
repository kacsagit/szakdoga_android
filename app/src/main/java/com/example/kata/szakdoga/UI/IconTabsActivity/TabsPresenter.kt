package com.example.kata.szakdoga.UI.LoginActivity

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.support.annotation.RequiresApi
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


   /* fun getPath(context: Context,uri: Uri): String? {
        val projection = arrayOf(MediaStore.Video.Media.DATA)
        val cursor = context.contentResolver.query(uri, projection, null, null, null)
        return if (cursor != null) {
            val column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            cursor.moveToFirst()
            val value = cursor.getString(column_index)
            cursor.close()
            value
        } else
            null
    }*/


    fun getPath(context: Context, uri: Uri): String? {

        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            @RequiresApi(Build.VERSION_CODES.KITKAT)
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }

                // TODO handle non-primary volumes
            } else if (isDownloadsDocument(uri)) {

                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id)!!)

                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }

                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])

                return getDataColumn(context, contentUri, selection, selectionArgs)
            }// MediaProvider
            // DownloadsProvider
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {

            // Return the remote address
            return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(context, uri, null, null)

        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }// File
        // MediaStore (and general)

        return null
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    fun getDataColumn(context: Context, uri: Uri?, selection: String?,
                      selectionArgs: Array<String>?): String? {

        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)

        try {
            cursor = context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            if (cursor != null)
                cursor.close()
        }
        return null
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }



}
