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

import android.app.Activity
import android.content.Context
import android.graphics.PixelFormat
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.WindowManager
import com.example.kata.nubomedia.kurentoroomclientandroid.*
import com.example.kata.nubomedia.webrtcpeerandroid.NBMMediaConfiguration
import com.example.kata.nubomedia.webrtcpeerandroid.NBMPeerConnection
import com.example.kata.nubomedia.webrtcpeerandroid.NBMWebRTCPeer
import fi.vtt.nubomedia.utilitiesandroid.LooperExecutor
import kotlinx.android.synthetic.main.activity_video_watch.*
import org.webrtc.*
import java.io.BufferedInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.util.*


/**
 * Activity for receiving the video stream of a peer
 * (based on WatchVideoActivity of Pubnub's video chat tutorial example.
 */
class WatchVideoActivity : Activity(), NBMWebRTCPeer.Observer, RoomListener {

    private lateinit var nbmWebRTCPeer: NBMWebRTCPeer

    private var videoRequestUserMapping: MutableMap<Int, String> = HashMap()
    private var publishVideoRequestId: Int = 0

    private var roomId = 0
    private var mHandler: Handler = Handler()
    private var callState: CallState? = null
    private lateinit var executor: LooperExecutor

    private val TAG = "PeerVideoActivity"
    var username: String = ""
    var roomname: String = ""
    private lateinit var kurentoRoomAPI: KurentoRoomAPI
    private var userPublishList: MutableMap<String, Boolean> = HashMap()

    private val offerWhenReady = Runnable {
        // Generate offers to receive video from all peers in the room
        for (entry in userPublishList.entries) {
            if (entry.value) {
                generateOfferForRemote(entry.key)
                Log.i(TAG, "I'm " + username + " DERP: Generating offer for peer " + entry.key)
                // Set value to false so that if this function is called again we won't
                // generate another offer for this user
                entry.setValue(false)
            }
        }
    }

    private enum class CallState {
        IDLE, PUBLISHING, PUBLISHED, WAITING_REMOTE_USER, RECEIVING_REMOTE_USER
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_watch)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        callState = CallState.IDLE
        val intent = intent
        roomname = intent.getStringExtra(Constants.ROOM_NAME)

        username = intent.getStringExtra(Constants.USER_NAME)
        executor = LooperExecutor()
        executor.requestStart()

        hangup_button.setOnClickListener { finish() }

        kurentoRoomAPI = KurentoRoomAPI(executor, Constants.DEFAULT_SERVER, this)

        val cf: CertificateFactory
        try {
            cf = CertificateFactory.getInstance("X.509")
            val caInput = BufferedInputStream(this.assets.open("kurento_room_base64.cer"))
            val ca = cf.generateCertificate(caInput)
            kurentoRoomAPI.addTrustedCertificate("ca", ca)
        } catch (e: CertificateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        kurentoRoomAPI.useSelfSignedCertificate(true)

    }

    override fun onStart() {
        super.onStart()
        if (!kurentoRoomAPI.isWebSocketConnected) {
            kurentoRoomAPI.connectWebSocket()
        }

        Log.i(TAG, "username: " + username)

        val rootEglBase = EglBase.create()
        gl_surface.init(rootEglBase.eglBaseContext, null)
        gl_surface.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)

        val peerConnectionParameters = NBMMediaConfiguration(
                NBMMediaConfiguration.NBMRendererType.OPENGLES,
                NBMMediaConfiguration.NBMAudioCodec.OPUS, 0,
                NBMMediaConfiguration.NBMVideoCodec.VP8, 0,
                NBMMediaConfiguration.NBMVideoFormat(352, 288, PixelFormat.RGB_888, 20.0),
                NBMMediaConfiguration.NBMCameraPosition.FRONT)


        nbmWebRTCPeer = NBMWebRTCPeer(peerConnectionParameters, this, null, this)
        nbmWebRTCPeer.registerMasterRenderer(gl_surface)
        Log.i(TAG, "Initializing nbmWebRTCPeer...")
        nbmWebRTCPeer.initialize()
        callState = CallState.WAITING_REMOTE_USER
        call_status.text = "waiting stream"

        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.mode = AudioManager.MODE_NORMAL
    }

    override fun onStop() {
        endCall()
        super.onStop()
    }




    private fun generateOfferForRemote(remote_name: String) {
        nbmWebRTCPeer.generateOffer(remote_name, false)
        callState = CallState.WAITING_REMOTE_USER
        runOnUiThread { call_status.text = "Watiring remote" }
    }


    /**
     * Terminates the current call and ends activity
     */
    private fun endCall() {
        callState = CallState.IDLE
        try {
            nbmWebRTCPeer.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onInitialize() {
        nbmWebRTCPeer.generateOffer("local", false)
    }

    override fun onLocalSdpOfferGenerated(sessionDescription: SessionDescription, nbmPeerConnection: NBMPeerConnection) {
        // Asking for remote user video
        Log.d(TAG, "Sending " + sessionDescription.type)
        publishVideoRequestId = ++Constants.id
        val username = nbmPeerConnection.connectionId
        videoRequestUserMapping.put(publishVideoRequestId, username)
        kurentoRoomAPI.sendReceiveVideoFrom(username, "webcam", sessionDescription.description, publishVideoRequestId)

    }

    override fun onLocalSdpAnswerGenerated(sessionDescription: SessionDescription, nbmPeerConnection: NBMPeerConnection) {}

    override fun onIceCandidate(iceCandidate: IceCandidate, nbmPeerConnection: NBMPeerConnection) {
        val sendIceCandidateRequestId = ++Constants.id
        kurentoRoomAPI.sendOnIceCandidate(nbmPeerConnection.connectionId, iceCandidate.sdp,
                iceCandidate.sdpMid, Integer.toString(iceCandidate.sdpMLineIndex), sendIceCandidateRequestId)

    }

    override fun onIceStatusChanged(iceConnectionState: PeerConnection.IceConnectionState, nbmPeerConnection: NBMPeerConnection) {
        Log.i(TAG, "onIceStatusChanged")
    }

    override fun onRemoteStreamAdded(mediaStream: MediaStream, nbmPeerConnection: NBMPeerConnection) {
        Log.i(TAG, "onRemoteStreamAdded")
        nbmWebRTCPeer.setActiveMasterStream(mediaStream)
        runOnUiThread { call_status.text = "" }
    }

    override fun onRemoteStreamRemoved(mediaStream: MediaStream, nbmPeerConnection: NBMPeerConnection) {
        Log.i(TAG, "onRemoteStreamRemoved")
    }

    override fun onPeerConnectionError(s: String) {
        Log.e(TAG, "onPeerConnectionError:" + s)
    }

    override fun onDataChannel(dataChannel: DataChannel, connection: NBMPeerConnection) {
        Log.i(TAG, "[datachannel] Peer opened data channel")
    }

    override fun onBufferedAmountChange(l: Long, connection: NBMPeerConnection, channel: DataChannel) {

    }

    private fun sendHelloMessage(channel: DataChannel) {
        val rawMessage = "Hello Peer!".toByteArray(Charset.forName("UTF-8"))
        val directData = ByteBuffer.allocateDirect(rawMessage.size)
        directData.put(rawMessage)
        directData.flip()
        val data = DataChannel.Buffer(directData, false)
        channel.send(data)
    }

    override fun onStateChange(connection: NBMPeerConnection, channel: DataChannel) {
        Log.i(TAG, "[datachannel] DataChannel onStateChange: " + channel.state())
        if (channel.state() == DataChannel.State.OPEN) {
            sendHelloMessage(channel)
            Log.i(TAG, "[datachannel] Datachannel open, sending first hello")
        }
    }

    override fun onMessage(buffer: DataChannel.Buffer, connection: NBMPeerConnection, channel: DataChannel) {
        Log.i(TAG, "[datachannel] Message received: " + buffer.toString())
        sendHelloMessage(channel)
    }


    override fun onRoomResponse(response: RoomResponse) {
        Log.d(TAG, "OnRoomResponse:" + response)
        if (response.method == KurentoRoomAPI.Method.JOIN_ROOM) {
            userPublishList = HashMap(response.users)
        } else {
            val requestId = response.id

            mHandler.postDelayed(offerWhenReady, 1)
            if (requestId == publishVideoRequestId) {

                val sd = SessionDescription(SessionDescription.Type.ANSWER,
                        response.getValue("sdpAnswer")[0])
                // Check if we are waiting for the video publication of the other peer
                if (callState == CallState.WAITING_REMOTE_USER) {
                    //String user_name = Integer.toString(publishVideoRequestId);
                    callState = CallState.RECEIVING_REMOTE_USER
                    val connectionId = videoRequestUserMapping[publishVideoRequestId]
                    nbmWebRTCPeer.processAnswer(sd, connectionId)
                }
            }
        }

    }


    private fun joinRoom() {
        Constants.id++
        roomId = Constants.id
        Log.i(TAG, "Joinroom: User: $username, Room: $roomname id:$roomId")
        if (kurentoRoomAPI.isWebSocketConnected) {
            kurentoRoomAPI.sendJoinRoom(username, roomname, true, roomId)
        }
    }

    public override fun onDestroy() {
        Log.i(TAG, "onDestroy")
        if (kurentoRoomAPI.isWebSocketConnected) {
            kurentoRoomAPI.sendLeaveRoom(roomId)
            kurentoRoomAPI.disconnectWebSocket()
        }
        executor.requestStop()
        super.onDestroy()
    }


    override fun onRoomError(error: RoomError) {
        Log.wtf(TAG, error.toString())
        if (error.code == 104) {
            showFinishingError("Room error", "Username already taken")
        }
    }

    private fun showFinishingError(title: String, message: String) {
        AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok) { _, _ -> finish() }
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
    }

    override fun onRoomNotification(notification: RoomNotification) {
        Log.i(TAG, "OnRoomNotification (state=" + callState.toString() + "):" + notification)
        val map = notification.params

        if (notification.method == RoomListener.METHOD_ICE_CANDIDATE) {
            val sdpMid = map["sdpMid"].toString()
            val sdpMLineIndex = Integer.valueOf(map["sdpMLineIndex"].toString())
            val sdp = map["candidate"].toString()
            val ic = IceCandidate(sdpMid, sdpMLineIndex, sdp)
            nbmWebRTCPeer.addRemoteIceCandidate(ic, notification.getParam("endpointName").toString())
        } else if (notification.method == RoomListener.METHOD_PARTICIPANT_PUBLISHED) {
            val user = map["id"].toString()
            userPublishList.put(user, true)
            mHandler.postDelayed(offerWhenReady, 2000)
        }// Somebody in the room published their video

        // Somebody wrote a message to other users in the room
        /*if (notification.method == RoomListener.METHOD_SEND_MESSAGE) {
            val user = map["user"].toString()
            val message = map["message"].toString()

        } else if (notification.method == RoomListener.METHOD_PARTICIPANT_LEFT) {
            val user = map["name"].toString()

        } else if (notification.method == RoomListener.METHOD_PARTICIPANT_JOINED) {
            val user = map["id"].toString()

        }*/// Somebody joined the room
        // Somebody left the room


    }

    override fun onRoomConnected() {
        if (kurentoRoomAPI.isWebSocketConnected) {
            joinRoom()

        }
    }

    override fun onRoomDisconnected() {
        showFinishingError("Disconnected", "You have been disconnected from room.")
    }


}