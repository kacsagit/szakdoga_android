/*
 * (C) Copyright 2016 VTT (http://www.vtt.fi)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.example.kata.szakdoga

/**
 * Created by GleasonK on 7/30/15.
 */
object Constants {
    //public static final String SHARED_PREFS = "fi.vtt.nubotest.SHARED_PREFS";
    val USER_NAME = "fi.vtt.nubotest.SHARED_PREFS.USER_NAME"

    //public static final String DEFAULT_SERVER   = "wss://roomtestbed.kurento.org:8443/room";
    //public static String SERVER_ADDRESS_SET_BY_USER = "wss://roomtestbed.kurento.org:8443/room";
    //    public static final String DEFAULT_SERVER   = "wss://192.168.197.130:8443/room";
    //    public static String SERVER_ADDRESS_SET_BY_USER = "wss://192.168.197.130:8443/room";
    val DEFAULT_SERVER = "wss://vm.ik.bme.hu:3068/room"
    var SERVER_ADDRESS_SET_BY_USER = "wss://vm.ik.bme.hu:3068/room"

    val ROOM_NAME = "fi.vtt.nubotest.SHARED_PREFS.ROOM_NAME"
    var id = 0
}
