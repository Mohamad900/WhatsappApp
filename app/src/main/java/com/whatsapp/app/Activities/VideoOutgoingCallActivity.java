package com.whatsapp.app.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import android.hardware.Camera;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallEndCause;
import com.sinch.android.rtc.calling.CallListener;
import com.sinch.android.rtc.video.VideoCallListener;
import com.sinch.android.rtc.video.VideoController;
import com.squareup.picasso.Picasso;
import com.whatsapp.app.CameraPreview;
import com.whatsapp.app.R;
import com.whatsapp.app.Utils.SinchManager;

import java.util.List;

public class VideoOutgoingCallActivity extends AppCompatActivity {

    String messageReceiverID;
    Call call;
    private TextView mCallDuration;
    private TextView mCallState;
    private TextView mCallerName;
    ImageView endCallButton;
    LinearLayout remoteViewLI;
    LinearLayout localViewRL;

    private Camera mCamera;
    private CameraPreview mPreview;
    LinearLayout cameraPreview;
    CircleImageView remoteUserProfile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_outgoing_call);

        //mCallDuration = (TextView) findViewById(R.id.callDuration);
        mCallerName = (TextView) findViewById(R.id.remoteUserName);
        mCallState = (TextView) findViewById(R.id.callState);
        remoteUserProfile =  findViewById(R.id.remoteUserProfile);
        endCallButton = findViewById(R.id.hangupButton);
        localViewRL = findViewById(R.id.localVideo);
        remoteViewLI = findViewById(R.id.remoteVideo);

        //mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
        /*//mCamera.setDisplayOrientation(90);
        cameraPreview = findViewById(R.id.cPreview);
        mPreview = new CameraPreview(this, mCamera);
        cameraPreview.addView(mPreview);*/
        //releaseCamera();
        //chooseCamera();
        //mCamera.startPreview();


            //releaseCamera();
            //chooseCamera();



        if(getIntent()!=null){
            messageReceiverID = getIntent().getStringExtra("messageReceiverID");
        }

        SinchManager.sinchClient.getCallClient().callUserVideo(messageReceiverID).addCallListener(new VideoOutgoingCallActivity.SinchVideoCallListener());

        FirebaseDatabase.getInstance().getReference().child("Users").child(messageReceiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    String name = dataSnapshot.child("name").getValue().toString();
                    String imageUrl = dataSnapshot.child("image").getValue().toString();

                    mCallerName.setText(name);
                    Picasso.get().load(imageUrl).into(remoteUserProfile);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        endCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                call.hangup();
                finish();
            }
        });

   /*     final Handler handler = new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                releaseCamera();
                chooseCamera();
                //mCamera.startPreview();
            }
        };

        handler.postDelayed(r, 1000);*/

    }

    /*public void chooseCamera() {

        int cameraId = findFrontFacingCamera();
        if (cameraId >= 0) {
            //open the backFacingCamera
            //set a picture callback
            //refresh the preview
            mCamera = Camera.open(cameraId);
            mCamera.setDisplayOrientation(90);
            mPreview.refreshCamera(mCamera);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        //when on Pause, release camera in order to be used from other applications
        releaseCamera();
    }

    private void releaseCamera() {
        // stop and release camera
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }
    private int findFrontFacingCamera() {

        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                break;
            }
        }
        return cameraId;

    }*/

    public class SinchVideoCallListener implements VideoCallListener {

        @Override
        public void onCallProgressing(Call call) {
            //Ringing
            mCallState.setText("Ringing");
        }

        @Override
        public void onCallEstablished(Call call) {
            mCallState.setText("");
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        }

        @Override
        public void onCallEnded(Call endedCall) {
            //CallEndCause cause = call.getDetails().getEndCause();
            //Log.d(TAG, "Call ended. Reason: " + cause.toString());
            call = null;
            setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
            endedCall.hangup();
            VideoController vc = SinchManager.sinchClient.getVideoController();
            remoteViewLI.removeView(vc.getRemoteView());
            localViewRL.removeView(vc.getLocalView());
            finish();
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> list) {

        }

        @Override
        public void onVideoTrackAdded(Call call) {
            //Log.d(TAG, "Video track added");
            VideoController vc = SinchManager.sinchClient.getVideoController();
            View myPreview = vc.getLocalView();
            View remoteView = vc.getRemoteView();

            // Add the views to your view hierarchy
            remoteViewLI.addView(remoteView);
            localViewRL.addView(myPreview);

        }

        @Override
        public void onVideoTrackPaused(Call call) {
        }

        @Override
        public void onVideoTrackResumed(Call call) {
        }

    }


}
