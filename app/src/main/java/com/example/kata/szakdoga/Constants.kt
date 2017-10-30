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
    val CALL_USER = "fi.vtt.nubotest.SHARED_PREFS.CALL_USER"
    val STDBY_SUFFIX = "-stdby"

    val PUB_KEY = "pub-c-9d0d75a5-38db-404f-ac2a-884e18b041d8"
    val SUB_KEY = "sub-c-4e25fb64-37c7-11e5-a477-0619f8945a4f"

    val JSON_CALL_USER = "call_user"
    val JSON_CALL_TIME = "call_time"
    val JSON_OCCUPANCY = "occupancy"
    val JSON_STATUS = "status"

    // JSON for user messages
    val JSON_USER_MSG = "user_message"
    val JSON_MSG_UUID = "msg_uuid"
    val JSON_MSG = "msg_message"
    val JSON_TIME = "msg_timestamp"
    val STATUS_AVAILABLE = "Available"
    val STATUS_OFFLINE = "Offline"
    val STATUS_BUSY = "Busy"
    val SERVER_NAME = "serverName"
    //public static final String DEFAULT_SERVER   = "wss://roomtestbed.kurento.org:8443/room";
    //public static String SERVER_ADDRESS_SET_BY_USER = "wss://roomtestbed.kurento.org:8443/room";
    //    public static final String DEFAULT_SERVER   = "wss://192.168.197.130:8443/room";
    //    public static String SERVER_ADDRESS_SET_BY_USER = "wss://192.168.197.130:8443/room";
    val DEFAULT_SERVER = "wss://vm.ik.bme.hu:3068/room"
    var SERVER_ADDRESS_SET_BY_USER = "wss://vm.ik.bme.hu:3068/room"

    val ROOM_NAME = "fi.vtt.nubotest.SHARED_PREFS.ROOM_NAME"
    var id = 0
}
