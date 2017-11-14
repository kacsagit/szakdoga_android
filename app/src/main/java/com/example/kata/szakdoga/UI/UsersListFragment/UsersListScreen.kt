package com.example.kata.szakdoga.UI.UsersListFragment

import com.example.kata.szakdoga.data.User

/**
 * Created by Kata on 2017. 03. 12..
 */

interface UsersListScreen {
    fun updateFriend(friends: HashSet<String>)

    fun updateUsers(users: ArrayList<User>)

}