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

package com.example.kata.szakdoga;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.kata.nubomedia.kurentoroomclientandroid.KurentoRoomAPI;
import com.example.kata.nubomedia.kurentoroomclientandroid.RoomError;
import com.example.kata.nubomedia.kurentoroomclientandroid.RoomListener;
import com.example.kata.nubomedia.kurentoroomclientandroid.RoomNotification;
import com.example.kata.nubomedia.kurentoroomclientandroid.RoomResponse;
import com.example.kata.nubomedia.webrtcpeerandroid.NBMMediaConfiguration;
import com.example.kata.nubomedia.webrtcpeerandroid.NBMPeerConnection;
import com.example.kata.nubomedia.webrtcpeerandroid.NBMWebRTCPeer;

import org.webrtc.DataChannel;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.RendererCommon;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceViewRenderer;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.HashMap;
import java.util.Map;

import fi.vtt.nubomedia.utilitiesandroid.LooperExecutor;


/**
 * Activity for receiving the video stream of a peer
 * (based on WatchVideoActivity of Pubnub's video chat tutorial example.
 */
public class WatchVideoActivity extends Activity implements NBMWebRTCPeer.Observer, RoomListener {
    private static final String TAG = "PeerVideoActivity";

    private NBMWebRTCPeer nbmWebRTCPeer;
    private SurfaceViewRenderer masterView;

    private Map<Integer, String> videoRequestUserMapping;
    private int publishVideoRequestId;
    private TextView mCallStatus;

    private int roomId=0;
    public static String username, roomname;
    private Handler mHandler = null;
    private CallState callState;
    private static KurentoRoomAPI kurentoRoomAPI;
    private LooperExecutor executor;
    public static Map<String, Boolean> userPublishList = new HashMap<>();

    private enum CallState{
        IDLE, PUBLISHING, PUBLISHED, WAITING_REMOTE_USER, RECEIVING_REMOTE_USER
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_watch);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mHandler = new Handler();
        masterView = (SurfaceViewRenderer) findViewById(R.id.gl_surface);
        this.mCallStatus   = (TextView) findViewById(R.id.call_status);
        callState = CallState.IDLE;
        Intent intent = getIntent();
        roomname=intent.getStringExtra(Constants.ROOM_NAME);

        username = intent.getStringExtra(Constants.USER_NAME);
        executor = new LooperExecutor();
        executor.requestStart();
        kurentoRoomAPI = new KurentoRoomAPI(executor, Constants.DEFAULT_SERVER, this);

        CertificateFactory cf;
        try {
            cf = CertificateFactory.getInstance("X.509");
            InputStream caInput = new BufferedInputStream(this.getAssets().open("kurento_room_base64.cer"));
            Certificate ca = cf.generateCertificate(caInput);
            kurentoRoomAPI.addTrustedCertificate("ca", ca);
        } catch (CertificateException |IOException e) {
            e.printStackTrace();
        }
        kurentoRoomAPI.useSelfSignedCertificate(true);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!kurentoRoomAPI.isWebSocketConnected()) {
            kurentoRoomAPI.connectWebSocket();
        }

        Bundle extras = getIntent().getExtras();
        Log.i(TAG, "username: " + username);

        EglBase rootEglBase = EglBase.create();
        masterView.init(rootEglBase.getEglBaseContext(), null);
        masterView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);

        NBMMediaConfiguration peerConnectionParameters = new NBMMediaConfiguration(
                NBMMediaConfiguration.NBMRendererType.OPENGLES,
                NBMMediaConfiguration.NBMAudioCodec.OPUS, 0,
                NBMMediaConfiguration.NBMVideoCodec.VP8, 0,
                new NBMMediaConfiguration.NBMVideoFormat(352, 288, PixelFormat.RGB_888, 20),
                NBMMediaConfiguration.NBMCameraPosition.FRONT);

        videoRequestUserMapping = new HashMap<>();

        nbmWebRTCPeer = new NBMWebRTCPeer(peerConnectionParameters, this, null, this);
        nbmWebRTCPeer.registerMasterRenderer(masterView);
        Log.i(TAG, "Initializing nbmWebRTCPeer...");
        nbmWebRTCPeer.initialize();
        callState = CallState.WAITING_REMOTE_USER;
        mCallStatus.setText("waiting stream");

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_NORMAL);
    }

    @Override
    protected void onStop() {
        endCall();
        super.onStop();
    }


    public void hangup(View view) {
        finish();
    }

    private void GenerateOfferForRemote(String remote_name){
        nbmWebRTCPeer.generateOffer(remote_name, false);
        callState = CallState.WAITING_REMOTE_USER;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCallStatus.setText("Watiring remote");
            }
        });
    }

    public void receiveFromRemote(View view){
        //GenerateOfferForRemote();
    }

    /**
     * Terminates the current call and ends activity
     */
    private void endCall() {
        callState = CallState.IDLE;
        try
        {
            if (nbmWebRTCPeer != null) {
                nbmWebRTCPeer.close();
                nbmWebRTCPeer = null;
            }
        }
        catch (Exception e){e.printStackTrace();}
    }

    @Override
    public void onInitialize() {
        nbmWebRTCPeer.generateOffer("local", false);
    }

    @Override
    public void onLocalSdpOfferGenerated(final SessionDescription sessionDescription, final NBMPeerConnection nbmPeerConnection) {
        // Asking for remote user video
        Log.d(TAG, "Sending " + sessionDescription.type);
        publishVideoRequestId = ++Constants.id;
        String username = nbmPeerConnection.getConnectionId();
        videoRequestUserMapping.put(publishVideoRequestId, username);
        kurentoRoomAPI.sendReceiveVideoFrom(username, "webcam", sessionDescription.description, publishVideoRequestId);

    }

    @Override
    public void onLocalSdpAnswerGenerated(SessionDescription sessionDescription, NBMPeerConnection nbmPeerConnection) {
    }

    @Override
    public void onIceCandidate(IceCandidate iceCandidate, NBMPeerConnection nbmPeerConnection) {
        int sendIceCandidateRequestId = ++Constants.id;
        kurentoRoomAPI.sendOnIceCandidate(nbmPeerConnection.getConnectionId(), iceCandidate.sdp,
                iceCandidate.sdpMid, Integer.toString(iceCandidate.sdpMLineIndex), sendIceCandidateRequestId);

    }

    @Override
    public void onIceStatusChanged(PeerConnection.IceConnectionState iceConnectionState, NBMPeerConnection nbmPeerConnection) {
        Log.i(TAG, "onIceStatusChanged");
    }

    @Override
    public void onRemoteStreamAdded(MediaStream mediaStream, NBMPeerConnection nbmPeerConnection) {
        Log.i(TAG, "onRemoteStreamAdded");
        nbmWebRTCPeer.setActiveMasterStream(mediaStream);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCallStatus.setText("");
            }
        });
    }

    @Override
    public void onRemoteStreamRemoved(MediaStream mediaStream, NBMPeerConnection nbmPeerConnection) {
        Log.i(TAG, "onRemoteStreamRemoved");
    }

    @Override
    public void onPeerConnectionError(String s) {
        Log.e(TAG, "onPeerConnectionError:" + s);
    }

    @Override
    public void onDataChannel(DataChannel dataChannel, NBMPeerConnection connection) {
        Log.i(TAG, "[datachannel] Peer opened data channel");
    }

    @Override
    public void onBufferedAmountChange(long l, NBMPeerConnection connection, DataChannel channel) {

    }

    public void sendHelloMessage(DataChannel channel) {
        byte[] rawMessage = "Hello Peer!".getBytes(Charset.forName("UTF-8"));
        ByteBuffer directData = ByteBuffer.allocateDirect(rawMessage.length);
        directData.put(rawMessage);
        directData.flip();
        DataChannel.Buffer data = new DataChannel.Buffer(directData, false);
        channel.send(data);
    }

    @Override
    public void onStateChange(NBMPeerConnection connection, DataChannel channel) {
        Log.i(TAG, "[datachannel] DataChannel onStateChange: " + channel.state());
        if (channel.state() == DataChannel.State.OPEN) {
            sendHelloMessage(channel);
            Log.i(TAG, "[datachannel] Datachannel open, sending first hello");
        }
    }

    @Override
    public void onMessage(DataChannel.Buffer buffer, NBMPeerConnection connection, DataChannel channel) {
        Log.i(TAG, "[datachannel] Message received: " + buffer.toString());
        sendHelloMessage(channel);
    }

    private Runnable offerWhenReady = new Runnable() {
        @Override
        public void run() {
            // Generate offers to receive video from all peers in the room
            for (Map.Entry<String, Boolean> entry : userPublishList.entrySet()) {
                if (entry.getValue()) {
                    GenerateOfferForRemote(entry.getKey());
                    Log.i(TAG, "I'm " + username + " DERP: Generating offer for peer " + entry.getKey());
                    // Set value to false so that if this function is called again we won't
                    // generate another offer for this user
                    entry.setValue(false);
                }
            }
        }
    };



    @Override
    public void onRoomResponse(RoomResponse response) {
        Log.d(TAG, "OnRoomResponse:" + response);
        if (response.getMethod()==KurentoRoomAPI.Method.JOIN_ROOM) {
            userPublishList = new HashMap<>(response.getUsers());
        }else {
            int requestId = response.getId();

            mHandler.postDelayed(offerWhenReady, 1);
            if (requestId == publishVideoRequestId) {

                SessionDescription sd = new SessionDescription(SessionDescription.Type.ANSWER,
                        response.getValue("sdpAnswer").get(0));
                // Check if we are waiting for the video publication of the other peer
                if (callState == CallState.WAITING_REMOTE_USER) {
                    //String user_name = Integer.toString(publishVideoRequestId);
                    callState = CallState.RECEIVING_REMOTE_USER;
                    String connectionId = videoRequestUserMapping.get(publishVideoRequestId);
                    nbmWebRTCPeer.processAnswer(sd, connectionId);
                }
            }
        }

    }




    private void joinRoom () {
        Constants.id++;
        roomId = Constants.id;
        Log.i(TAG, "Joinroom: User: "+username+", Room: "+roomname+" id:"+roomId);
        if (kurentoRoomAPI.isWebSocketConnected()) {
            kurentoRoomAPI.sendJoinRoom(username, roomname, true, roomId);
        }
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        if (kurentoRoomAPI.isWebSocketConnected()) {
            kurentoRoomAPI.sendLeaveRoom(roomId);
            kurentoRoomAPI.disconnectWebSocket();
        }
        executor.requestStop();
        super.onDestroy();
    }



    @Override
    public void onRoomError(RoomError error) {
        Log.wtf(TAG, error.toString());
        if(error.getCode() == 104) {
            showFinishingError("Room error", "Username already taken");
        }
    }

    private void showFinishingError(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) { finish(); }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public void onRoomNotification(RoomNotification notification) {
        Log.i(TAG, "OnRoomNotification (state=" + callState.toString() + "):" + notification);
        Map<String, Object> map = notification.getParams();

        if(notification.getMethod().equals(RoomListener.METHOD_ICE_CANDIDATE)) {
            String sdpMid = map.get("sdpMid").toString();
            int sdpMLineIndex = Integer.valueOf(map.get("sdpMLineIndex").toString());
            String sdp = map.get("candidate").toString();
            IceCandidate ic = new IceCandidate(sdpMid, sdpMLineIndex, sdp);
            nbmWebRTCPeer.addRemoteIceCandidate(ic, notification.getParam("endpointName").toString());
        }

        // Somebody in the room published their video
        else if(notification.getMethod().equals(RoomListener.METHOD_PARTICIPANT_PUBLISHED)) {
            final String user = map.get("id").toString();
            userPublishList.put(user, true);
            mHandler.postDelayed(offerWhenReady, 2000);
        }

        // Somebody wrote a message to other users in the room
        if(notification.getMethod().equals(RoomListener.METHOD_SEND_MESSAGE)) {
            final String user = map.get("user").toString();
            final String message = map.get("message").toString();

        }

        // Somebody left the room
        else if(notification.getMethod().equals(RoomListener.METHOD_PARTICIPANT_LEFT)) {
            final String user = map.get("name").toString();

        }

        // Somebody joined the room
        else if(notification.getMethod().equals(RoomListener.METHOD_PARTICIPANT_JOINED)) {
            final String user = map.get("id").toString();

        }


    }

    @Override
    public void onRoomConnected() {
        if (kurentoRoomAPI.isWebSocketConnected()) {
            joinRoom();

        }
    }

    @Override
    public void onRoomDisconnected() {
        showFinishingError("Disconnected", "You have been disconnected from room.");
    }


}