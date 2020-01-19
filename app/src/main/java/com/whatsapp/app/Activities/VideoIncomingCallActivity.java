package com.whatsapp.app.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallListener;
import com.sinch.android.rtc.video.VideoCallListener;
import com.sinch.android.rtc.video.VideoController;
import com.squareup.picasso.Picasso;
import com.whatsapp.app.CameraPreview;
import com.whatsapp.app.R;
import com.whatsapp.app.Utils.SinchManager;

import java.util.List;

public class VideoIncomingCallActivity extends AppCompatActivity {


    ImageView endCallButton,answer;
    LinearLayout remoteViewLI;
    LinearLayout localViewRL;
    Chronometer chronometer;
    MediaPlayer mp;
    TextView callState,remoteUserName;
    CircleImageView remoteUserProfile;
    //private Camera mCamera;
    //private CameraPreview mPreview;
    //LinearLayout cameraPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_incoming_call);


        answer =  findViewById(R.id.answerButton);
        callState = findViewById(R.id.callState);
        remoteUserProfile  =findViewById(R.id.remoteUserProfile);
        remoteUserName = findViewById(R.id.remoteUserName);
        //ImageView decline = (Button) findViewById(R.id.declineButton);
        endCallButton = findViewById(R.id.hangupButton);
        localViewRL = findViewById(R.id.localVideo);
        remoteViewLI = findViewById(R.id.remoteVideo);
        chronometer = findViewById(R.id.chronometer);
        //cameraPreview = findViewById(R.id.cPreview);

       /* try {
            //releaseCamera();
            mCamera = Camera.open();
            mCamera.setDisplayOrientation(90);
            mPreview = new CameraPreview(this, mCamera);
            cameraPreview.addView(mPreview);
            mCamera.startPreview();
        } catch (Exception e) {
            Log.e(getString(R.string.app_name), "failed to open Camera");
            e.printStackTrace();
        }*/


        //mCamera.startPreview();

/*
        final Handler handler = new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                releaseCamera();
                chooseCamera();
                mCamera.startPreview();
            }
        };

        handler.postDelayed(r, 1000);*/

        FirebaseDatabase.getInstance().getReference().child("PendingCalls").child("1").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists()){

                            //String key = d.getKey();
                            String value = dataSnapshot.getValue().toString();

                            FirebaseDatabase.getInstance().getReference().child("Users").child(value).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()){

                                        String name = dataSnapshot.child("name").getValue().toString();
                                        String imageUrl = dataSnapshot.child("image").getValue().toString();

                                        remoteUserName.setText(name);
                                        Picasso.get().load(imageUrl).into(remoteUserProfile);

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        answer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //cameraPreview.setVisibility(View.INVISIBLE);
                answer.setVisibility(View.GONE);
                remoteViewLI.setVisibility(View.VISIBLE);
                localViewRL.setVisibility(View.VISIBLE);
                SinchManager.incomingCallObject.answer();
                SinchManager.incomingCallObject.addCallListener(new VideoIncomingCallActivity.SinchCallListener());
            }
        });

       /* decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SinchManager.incomingCallObject.hangup();
                finish();
            }
        });*/

        endCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SinchManager.incomingCallObject != null)SinchManager.incomingCallObject.hangup();
                finish();
            }
        });
    }


/*
    public void chooseCamera() {

        int cameraId = findFrontFacingCamera();
        if (cameraId >= 0) {
            //open the backFacingCamera
            //set a picture callback
            //refresh the preview
            try {
                mCamera = Camera.open(cameraId);
                mCamera.setDisplayOrientation(90);
                mPreview.refreshCamera(mCamera);
            } catch (Exception e) {
                Log.e(getString(R.string.app_name), "failed to open Camera");
                e.printStackTrace();
            }

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

    private class SinchCallListener implements VideoCallListener {

        @Override
        public void onCallProgressing(Call call) {
            //Ringing
            playAudio();
            callState.setVisibility(View.VISIBLE);
        }

        @Override
        public void onCallEstablished(Call call) {
            //cameraPreview.setVisibility(View.INVISIBLE);
            stopAudio();
            callState.setVisibility(View.INVISIBLE);
            chronometer.setVisibility(View.VISIBLE);
            chronometer.start();
            answer.setVisibility(View.GONE);
            remoteViewLI.setVisibility(View.VISIBLE);
            localViewRL.setVisibility(View.VISIBLE);
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        }

        @Override
        public void onCallEnded(Call endedCall) {
            stopAudio();
            SinchManager.incomingCallObject = null;
            setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
            VideoController vc = SinchManager.sinchClient.getVideoController();
            remoteViewLI.removeView(vc.getRemoteView());
            localViewRL.removeView(vc.getLocalView());
            endedCall.hangup();
            finish();
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> list) {

        }

        @Override
        public void onVideoTrackAdded(Call call) {

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

    private void playAudio() {

        mp = MediaPlayer.create(this, R.raw.ring);
        try{
            //mp.prepare();
            mp.start();

        }catch(Exception e){e.printStackTrace();}
    }

    public void stopAudio() {
        if (mp != null) {
            mp.release();
            mp = null;
        }
    }


}
