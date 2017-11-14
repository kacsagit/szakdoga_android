package com.example.kata.szakdoga.UI.UsersListFragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.kata.szakdoga.R
import com.example.kata.szakdoga.adapter.UserListAdapter
import com.example.kata.szakdoga.data.User
import kotlinx.android.synthetic.main.fragment_users_list.view.*


class UsersListFragment : Fragment(),UsersListScreen {


    lateinit var adapter: UserListAdapter

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater!!.inflate(R.layout.fragment_users_list, container, false)

        adapter = UserListAdapter(ArrayList<User>())
        view.recycler_view.layoutManager = LinearLayoutManager(context)
        view.recycler_view.adapter = adapter
        UsersListPresenter.instance.updateFriends()
        return view
    }
    override fun updateUsers(users: ArrayList<User>) {
        adapter.update(users)
    }

    override fun updateFriend(friends: HashSet<String>) {
        adapter.updateFriend(friends)
    }


    override fun onStart() {
        super.onStart()
        UsersListPresenter.instance.attachScreen(this)
    }

    override fun onStop() {
        super.onStop()
        UsersListPresenter.instance.detachScreen()
    }


}// Required empty public constructor
