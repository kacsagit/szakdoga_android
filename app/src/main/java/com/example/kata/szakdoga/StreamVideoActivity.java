

package com.example.kata.szakdoga;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

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
 * (based on PeerVideoActivity of Pubnub's video chat tutorial example.
 */
public class StreamVideoActivity extends Activity implements NBMWebRTCPeer.Observer, RoomListener {
    private static final String TAG = "PeerVideoActivity";

    private SurfaceViewRenderer localView;

    private NBMWebRTCPeer nbmWebRTCPeer;

    public static String username, roomname;
    private static KurentoRoomAPI kurentoRoomAPI;
    private LooperExecutor executor;
    public static Map<String, Boolean> userPublishList = new HashMap<>();

    private Map<Integer, String> videoRequestUserMapping;
    private int publishVideoRequestId;
    private TextView mCallStatus;
    private boolean backPressed = false;
    private Thread backPressedThread = null;
    private int roomId=0;

    private Handler mHandler = null;
    private CallState callState;

    private enum CallState {
        IDLE, PUBLISHING, PUBLISHED, WAITING_REMOTE_USER, RECEIVING_REMOTE_USER
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_stream);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mHandler = new Handler();
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

        localView = (SurfaceViewRenderer) findViewById(R.id.gl_surface_local);
        this.mCallStatus = (TextView) findViewById(R.id.call_status);
        callState = CallState.IDLE;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!kurentoRoomAPI.isWebSocketConnected()) {
            kurentoRoomAPI.connectWebSocket();
        }
        Bundle extras = getIntent().getExtras();
        this.username = extras.getString(Constants.USER_NAME, "");
        Log.i(TAG, "username: " + username);

        EglBase rootEglBase = EglBase.create();
        localView.init(rootEglBase.getEglBaseContext(), null);
        localView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);

        NBMMediaConfiguration peerConnectionParameters = new NBMMediaConfiguration(
                NBMMediaConfiguration.NBMRendererType.OPENGLES,
                NBMMediaConfiguration.NBMAudioCodec.OPUS, 0,
                NBMMediaConfiguration.NBMVideoCodec.VP8, 0,
                new NBMMediaConfiguration.NBMVideoFormat(352, 288, PixelFormat.RGB_888, 20),
                NBMMediaConfiguration.NBMCameraPosition.FRONT);

        videoRequestUserMapping = new HashMap<>();

        nbmWebRTCPeer = new NBMWebRTCPeer(peerConnectionParameters, this, localView, this);
        Log.i(TAG, "Initializing nbmWebRTCPeer...");
        nbmWebRTCPeer.initialize();
        callState = CallState.PUBLISHING;
        mCallStatus.setText("Publishing...");
    }

    @Override
    protected void onStop() {
        endCall();
        super.onStop();
    }

    @Override
    protected void onPause() {
        nbmWebRTCPeer.stopLocalMedia();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        nbmWebRTCPeer.startLocalMedia();
    }

    @Override
    protected void onDestroy()  {
        Log.i(TAG, "onDestroy");
        if (kurentoRoomAPI.isWebSocketConnected()) {
        kurentoRoomAPI.sendLeaveRoom(roomId);
        kurentoRoomAPI.disconnectWebSocket();
    }
        executor.requestStop();
        super.onDestroy();
    }



    @Override
    public void onBackPressed() {
        // If back button has not been pressed in a while then trigger thread and toast notification
        if (!this.backPressed) {
            this.backPressed = true;
            Toast.makeText(this, "Press back again to end.", Toast.LENGTH_SHORT).show();
            this.backPressedThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000);
                        backPressed = false;
                    } catch (InterruptedException e) {
                        Log.d("VCA-oBP", "Successfully interrupted");
                    }
                }
            });
            this.backPressedThread.start();
        }
        // If button pressed the second time then call super back pressed
        // (eventually calls onDestroy)
        else {
            if (this.backPressedThread != null)
                this.backPressedThread.interrupt();
            kurentoRoomAPI.sendUnpublishVideo(publishVideoRequestId);
            super.onBackPressed();
        }
    }

    public void hangup(View view) {
        kurentoRoomAPI.sendLeaveRoom(roomId);
        kurentoRoomAPI.disconnectWebSocket();
        finish();
    }



    public void switchs(View view) {
        nbmWebRTCPeer.switchCameraPosition();
    }



    /**
     * Terminates the current call and ends activity
     */
    private void endCall() {
        callState = CallState.IDLE;
        try {
            if (nbmWebRTCPeer != null) {
                nbmWebRTCPeer.close();
                nbmWebRTCPeer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onInitialize() {
        nbmWebRTCPeer.generateOffer("local", true);
    }

    @Override
    public void onLocalSdpOfferGenerated(final SessionDescription sessionDescription, final NBMPeerConnection nbmPeerConnection) {
        if (callState == CallState.PUBLISHING || callState == CallState.PUBLISHED) {
            Log.d(TAG, "Sending " + sessionDescription.type);
            publishVideoRequestId = ++Constants.id;
            kurentoRoomAPI.sendPublishVideo(sessionDescription.description, false, publishVideoRequestId);
        }
    }

    @Override
    public void onLocalSdpAnswerGenerated(SessionDescription sessionDescription, NBMPeerConnection nbmPeerConnection) {
    }

    @Override
    public void onIceCandidate(IceCandidate iceCandidate, NBMPeerConnection nbmPeerConnection) {
        int sendIceCandidateRequestId = ++Constants.id;
        if (callState == CallState.PUBLISHING || callState == CallState.PUBLISHED) {
            kurentoRoomAPI.sendOnIceCandidate(this.username, iceCandidate.sdp,
                    iceCandidate.sdpMid, Integer.toString(iceCandidate.sdpMLineIndex), sendIceCandidateRequestId);
        }
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


    @Override
    public void onRoomResponse(RoomResponse response) {
        Log.d(TAG, "OnRoomResponse:" + response);
        int requestId = response.getId();

        if (requestId == publishVideoRequestId) {

            SessionDescription sd = new SessionDescription(SessionDescription.Type.ANSWER,
                    response.getValue("sdpAnswer").get(0));

            // Check if we are waiting for publication of our own vide
            if (callState == CallState.PUBLISHING) {
                callState = CallState.PUBLISHED;
                nbmWebRTCPeer.processAnswer(sd, "local");
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //TODO record session
                        kurentoRoomAPI.sendRecordRequest(roomname, true, 1000);
                    }
                }, 4000);

            }
        }

    }

    @Override
    public void onRoomError(RoomError error) {
        Log.e(TAG, "OnRoomError:" + error);
    }

    @Override
    public void onRoomNotification(RoomNotification notification) {
        Log.i(TAG, "OnRoomNotification (state=" + callState.toString() + "):" + notification);
        Map<String, Object> map = notification.getParams();

        if (notification.getMethod().equals(RoomListener.METHOD_ICE_CANDIDATE)) {
            String sdpMid = map.get("sdpMid").toString();
            int sdpMLineIndex = Integer.valueOf(map.get("sdpMLineIndex").toString());
            String sdp = map.get("candidate").toString();
            IceCandidate ic = new IceCandidate(sdpMid, sdpMLineIndex, sdp);

            if (callState == CallState.PUBLISHING || callState == CallState.PUBLISHED) {
                nbmWebRTCPeer.addRemoteIceCandidate(ic, "local");
            }
        }else         // Somebody wrote a message to other users in the room
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

    private void joinRoom () {
        Constants.id++;
        roomId = Constants.id;
        Log.i(TAG, "Joinroom: User: "+this.username+", Room: "+this.roomname+" id:"+roomId);
        if (kurentoRoomAPI.isWebSocketConnected()) {
            kurentoRoomAPI.sendJoinRoom(this.username, this.roomname, true, roomId);
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
}