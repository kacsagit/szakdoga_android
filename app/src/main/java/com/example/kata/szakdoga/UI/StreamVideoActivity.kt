package com.example.kata.szakdoga.UI

import android.app.Activity
import android.graphics.PixelFormat
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.example.kata.nubomedia.kurentoroomclientandroid.*
import com.example.kata.nubomedia.webrtcpeerandroid.NBMMediaConfiguration
import com.example.kata.nubomedia.webrtcpeerandroid.NBMPeerConnection
import com.example.kata.nubomedia.webrtcpeerandroid.NBMWebRTCPeer
import com.example.kata.szakdoga.Constants
import com.example.kata.szakdoga.R
import fi.vtt.nubomedia.utilitiesandroid.LooperExecutor
import kotlinx.android.synthetic.main.activity_video_stream.*
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
 * (based on PeerVideoActivity of Pubnub's video chat tutorial example.
 */
class StreamVideoActivity : Activity(), NBMWebRTCPeer.Observer, RoomListener {

    private lateinit var nbmWebRTCPeer: NBMWebRTCPeer
    private lateinit var executor: LooperExecutor

    private var videoRequestUserMapping: Map<Int, String>? = null
    private var publishVideoRequestId: Int = 0
    private var backPressed = false
    private var backPressedThread: Thread? = null
    private var roomId = 0

    private var mHandler: Handler? = null
    private var callState: CallState? = null

    private val TAG = "PeerVideoActivity"

    private var username: String = ""
    private var roomname: String = ""
    private lateinit var kurentoRoomAPI: KurentoRoomAPI


    var mirror=true

    private enum class CallState {
        IDLE, PUBLISHING, PUBLISHED, WAITING_REMOTE_USER, RECEIVING_REMOTE_USER
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_stream)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        mHandler = Handler()
        val intent = intent
        roomname = intent.getStringExtra(Constants.ROOM_NAME)
        username = intent.getStringExtra(Constants.USER_NAME)

        executor = LooperExecutor()
        executor.requestStart()
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
        callState = CallState.IDLE


        hangup_button.setOnClickListener {
            kurentoRoomAPI.sendLeaveRoom(roomId)
            kurentoRoomAPI.disconnectWebSocket()
            finish()
        }

        switch_button.setOnClickListener {
            nbmWebRTCPeer.switchCameraPosition()
            mirror=!mirror
            gl_surface_local.setMirror(mirror)
        }
    }

    override fun onStart() {
        super.onStart()
        if (!kurentoRoomAPI.isWebSocketConnected) {
            kurentoRoomAPI.connectWebSocket()
        }
        val extras = intent.extras
        username = extras.getString(Constants.USER_NAME, "")
        Log.i(TAG, "username: " + username)

        val rootEglBase = EglBase.create()
        gl_surface_local.init(rootEglBase.eglBaseContext, null)
        gl_surface_local.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)

        val peerConnectionParameters = NBMMediaConfiguration(
                NBMMediaConfiguration.NBMRendererType.OPENGLES,
                NBMMediaConfiguration.NBMAudioCodec.OPUS, 0,
                NBMMediaConfiguration.NBMVideoCodec.VP8, 0,
                NBMMediaConfiguration.NBMVideoFormat(352, 288, PixelFormat.RGB_888, 20.0),
                NBMMediaConfiguration.NBMCameraPosition.FRONT)

        videoRequestUserMapping = HashMap()

        nbmWebRTCPeer = NBMWebRTCPeer(peerConnectionParameters, this, gl_surface_local, this)
        gl_surface_local.setMirror(mirror)
        Log.i(TAG, "Initializing nbmWebRTCPeer...")
        nbmWebRTCPeer.initialize()
        callState = CallState.PUBLISHING
        call_status.text = "Publishing..."
    }

    override fun onStop() {
        endCall()
        super.onStop()
    }

    override fun onPause() {
        nbmWebRTCPeer.stopLocalMedia()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        nbmWebRTCPeer.startLocalMedia()
    }

    override fun onDestroy() {
        Log.i(TAG, "onDestroy")
        if (kurentoRoomAPI.isWebSocketConnected) {
            kurentoRoomAPI.sendLeaveRoom(roomId)
            kurentoRoomAPI.disconnectWebSocket()
        }
        executor.requestStop()
        super.onDestroy()
    }


    override fun onBackPressed() {
        // If back button has not been pressed in a while then trigger thread and toast notification
        if (!this.backPressed) {
            this.backPressed = true
            Toast.makeText(this, "Press back again to end.", Toast.LENGTH_SHORT).show()
            this.backPressedThread = Thread(Runnable {
                try {
                    Thread.sleep(1000)
                    backPressed = false
                } catch (e: InterruptedException) {
                    Log.d("VCA-oBP", "Successfully interrupted")
                }
            })
            this.backPressedThread!!.start()
        } else {
            if (this.backPressedThread != null)
                this.backPressedThread!!.interrupt()
            kurentoRoomAPI.sendUnpublishVideo(publishVideoRequestId)
            super.onBackPressed()
        }// If button pressed the second time then call super back pressed
        // (eventually calls onDestroy)
    }





    /**
     * Terminates the current call and ends activity
     */
    private fun endCall() {
        callState = CallState.IDLE
        nbmWebRTCPeer.close()


    }

    override fun onInitialize() {
        nbmWebRTCPeer.generateOffer("local", true)
    }

    override fun onLocalSdpOfferGenerated(sessionDescription: SessionDescription, nbmPeerConnection: NBMPeerConnection) {
        if (callState == CallState.PUBLISHING || callState == CallState.PUBLISHED) {
            Log.d(TAG, "Sending " + sessionDescription.type)
            publishVideoRequestId = ++Constants.id
            kurentoRoomAPI.sendPublishVideo(sessionDescription.description, false, publishVideoRequestId)
        }
    }

    override fun onLocalSdpAnswerGenerated(sessionDescription: SessionDescription, nbmPeerConnection: NBMPeerConnection) {}

    override fun onIceCandidate(iceCandidate: IceCandidate, nbmPeerConnection: NBMPeerConnection) {
        val sendIceCandidateRequestId = ++Constants.id
        if (callState == CallState.PUBLISHING || callState == CallState.PUBLISHED) {
            kurentoRoomAPI.sendOnIceCandidate(this.username, iceCandidate.sdp,
                    iceCandidate.sdpMid, Integer.toString(iceCandidate.sdpMLineIndex), sendIceCandidateRequestId)
        }
    }

    override fun onIceStatusChanged(iceConnectionState: PeerConnection.IceConnectionState, nbmPeerConnection: NBMPeerConnection) {
        Log.i(TAG, "onIceStatusChanged")
    }

    override fun onRemoteStreamAdded(mediaStream: MediaStream, nbmPeerConnection: NBMPeerConnection) {
        Log.i(TAG, "onRemoteStreamAdded")
        nbmWebRTCPeer.setActiveMasterStream(mediaStream)
        runOnUiThread{call_status.text = ""}
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
        val requestId = response.id

        if (requestId == publishVideoRequestId) {

            val sd = SessionDescription(SessionDescription.Type.ANSWER,
                    response.getValue("sdpAnswer")[0])

            // Check if we are waiting for publication of our own vide
            if (callState == CallState.PUBLISHING) {
                callState = CallState.PUBLISHED
                nbmWebRTCPeer.processAnswer(sd, "local")
                val handler = Handler()
                handler.postDelayed({
                    kurentoRoomAPI.sendRecordRequest(roomname, true, 1000)
                }, 4000)

            }
        }

    }

    override fun onRoomError(error: RoomError) {
        Log.e(TAG, "OnRoomError:" + error)
    }

    override fun onRoomNotification(notification: RoomNotification) {
        Log.i(TAG, "OnRoomNotification (state=" + callState.toString() + "):" + notification)
        val map = notification.params

        if (notification.method == RoomListener.METHOD_ICE_CANDIDATE) {
            val sdpMid = map["sdpMid"].toString()
            val sdpMLineIndex = Integer.valueOf(map["sdpMLineIndex"].toString())
            val sdp = map["candidate"].toString()
            val ic = IceCandidate(sdpMid, sdpMLineIndex, sdp)

            if (callState == CallState.PUBLISHING || callState == CallState.PUBLISHED) {
                nbmWebRTCPeer.addRemoteIceCandidate(ic, "local")
            }
        } /*else // Somebody wrote a message to other users in the room
            if (notification.method == RoomListener.METHOD_SEND_MESSAGE) {
                val user = map["user"].toString()
                val message = map["message"].toString()

            } else if (notification.method == RoomListener.METHOD_PARTICIPANT_LEFT) {
                val user = map["name"].toString()

            } else if (notification.method == RoomListener.METHOD_PARTICIPANT_JOINED) {
                val user = map["id"].toString()

            }*/
        // Somebody joined the room
        // Somebody left the room

    }

    private fun joinRoom() {
        Constants.id++
        roomId = Constants.id
        Log.i(TAG, "Joinroom: User: " + this.username + ", Room: " + this.roomname + " id:" + roomId)
        if (kurentoRoomAPI.isWebSocketConnected) {
            kurentoRoomAPI.sendJoinRoom(this.username, this.roomname, true, roomId)
        }
    }

    override fun onRoomConnected() {
        if (kurentoRoomAPI.isWebSocketConnected) {
            joinRoom()
        }
    }

    override fun onRoomDisconnected() {
        showFinishingError("Disconnected", "You have been disconnected from room.")
    }


    private fun showFinishingError(title: String, message: String) {
        AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok) { _, _ -> finish() }
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
    }


}