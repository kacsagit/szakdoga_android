package com.example.kata.szakdoga.UI.IconTabsActivity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.example.kata.szakdoga.Constants
import com.example.kata.szakdoga.R
import com.example.kata.szakdoga.UI.LoginActivity.TabsPresenter
import com.example.kata.szakdoga.UI.LoginActivity.TabsScreen
import com.example.kata.szakdoga.UI.OwnListFragment.OwnListFragment
import com.example.kata.szakdoga.UI.StreamVideoActivity
import com.example.kata.szakdoga.UI.UsersListFragment.UsersListFragment
import com.example.kata.szakdoga.UI.VideoListFragment.VideoListFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import kotlinx.android.synthetic.main.activity_icon_tabs.*
import kotlinx.android.synthetic.main.activity_icon_tabs.view.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import java.util.*

class IconTabsActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks, TabsScreen {


    companion object {
        var REQUEST_TAKE_GALLERY_VIDEO = 1
        val TUTORIAL = "tutorial"
        const val RC_CAMERA_PERM = 123
        const val RC_STORAGE_PERM = 124
    }


    lateinit var mAuth: FirebaseAuth
    val link = ""
    lateinit var myRef: DatabaseReference
    var user: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_icon_tabs)
        mAuth = FirebaseAuth.getInstance()
        user = mAuth.currentUser

        setSupportActionBar(toolbar)
        toolbar.add_button.setOnClickListener {
            storageTask()

        }

        supportActionBar?.setDisplayShowTitleEnabled(false)

        setupViewPager(viewpager)

        tabs.setupWithViewPager(viewpager)

        setupTabIcons()
    }


    override fun onStart() {
        super.onStart()
        TabsPresenter.instance.attachScreen(this)
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val tutorial = preferences.getBoolean(TUTORIAL, true)
        if (tutorial) {
            tutorial()
            preferences.edit().putBoolean(TUTORIAL, false).apply()
        }
    }

    private fun tutorial() {
        demo(R.string.add_title, R.string.add_description, R.id.add_button)
                .setPromptStateChangeListener { _, state ->
                    if (state == MaterialTapTargetPrompt.STATE_DISMISSING) {
                        demo(R.string.stream_title, R.string.stream_description, R.id.action_record)
                                .setPromptStateChangeListener { _, state ->
                                    if (state == MaterialTapTargetPrompt.STATE_DISMISSING) {
                                        demo(R.string.home_title, R.string.home_description, (tabs.getChildAt(0) as ViewGroup).getChildAt(0))
                                                .setPromptStateChangeListener { _, state ->
                                                    if (state == MaterialTapTargetPrompt.STATE_DISMISSING) {
                                                        demo(R.string.follow_title, R.string.follow_description, (tabs.getChildAt(0) as ViewGroup).getChildAt(1))
                                                                .setPromptStateChangeListener { _, state ->
                                                                    if (state == MaterialTapTargetPrompt.STATE_DISMISSING) {
                                                                        demo(R.string.own_title, R.string.own_description, (tabs.getChildAt(0) as ViewGroup).getChildAt(2)).show()
                                                                    }
                                                                }
                                                                .show()
                                                    }
                                                }
                                                .show()
                                    }
                                }
                                .show()
                    }
                }
                .show()
    }


    fun demo(title: Int, desc: Int, target: View): MaterialTapTargetPrompt.Builder {
        return MaterialTapTargetPrompt.Builder(this@IconTabsActivity, R.style.MaterialTapTargetPromptTheme_FabTarget)
                .setPrimaryText(title)
                .setSecondaryText(desc)
                .setAnimationInterpolator(FastOutSlowInInterpolator())
                .setMaxTextWidth(R.dimen.tap_target_menu_max_width)
                .setTarget(target)
    }

    fun demo(title: Int, desc: Int, target: Int): MaterialTapTargetPrompt.Builder {
        return MaterialTapTargetPrompt.Builder(this@IconTabsActivity, R.style.MaterialTapTargetPromptTheme_FabTarget)
                .setPrimaryText(title)
                .setSecondaryText(desc)
                .setAnimationInterpolator(FastOutSlowInInterpolator())
                .setMaxTextWidth(R.dimen.tap_target_menu_max_width)
                .setTarget(target)

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
                cameraTask()
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
                data?.data?.let {
                    val selectedImageUri = it
                    // MEDIA GALLERY


                    val selectedImagePath = TabsPresenter.instance.getPath(this,selectedImageUri)
                    if (selectedImagePath != null) {
                        TabsPresenter.instance.uploadFile(selectedImagePath)
                        loading_flayout.visibility = VISIBLE

                    }
                }

            }
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }


    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>?) {

    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        }
    }


    @AfterPermissionGranted(RC_CAMERA_PERM)
    fun cameraTask() {
        var perms = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
        if (EasyPermissions.hasPermissions(this, *perms)) {
            // Have permission, do the thing!
            openStreamingActivity()
        } else {
            // Ask for one permission
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.audio_video_permisson),
                    RC_CAMERA_PERM,
                    *perms)
        }
    }


    @AfterPermissionGranted(RC_STORAGE_PERM)
    fun storageTask() {
        var perms = Manifest.permission.READ_EXTERNAL_STORAGE
        if (EasyPermissions.hasPermissions(this, perms)) {
            // Have permission, do the thing!
            openStorageChoser()
        } else {
            // Ask for one permission
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.storage_permisson),
                    RC_STORAGE_PERM,
                    perms)
        }
    }

    fun openStreamingActivity() {
        val intent = Intent(this, StreamVideoActivity::class.java)
        intent.putExtra(Constants.ROOM_NAME, UUID.randomUUID().toString())
        intent.putExtra(Constants.USER_NAME, user?.email)
        startActivity(intent)
    }

    fun openStorageChoser() {
        val intent = Intent()
        intent.type = "video/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Video"), REQUEST_TAKE_GALLERY_VIDEO)
    }


    override fun onStop() {
        super.onStop()
        TabsPresenter.instance.detachScreen()
    }

    override fun loadingFlayout(gone: Int) {
        loading_flayout.visibility = gone
    }


}
