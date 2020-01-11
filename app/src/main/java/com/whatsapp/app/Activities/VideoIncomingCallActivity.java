package com.whatsapp.app.Activities;

import androidx.appcompat.app.AppCompatActivity;

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

import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallListener;
import com.sinch.android.rtc.video.VideoCallListener;
import com.sinch.android.rtc.video.VideoController;
import com.whatsapp.app.CameraPreview;
import com.whatsapp.app.R;
import com.whatsapp.app.Utils.SinchManager;

import java.util.List;

public class VideoIncomingCallActivity extends AppCompatActivity {


    ImageView endCallButton,answer;
    LinearLayout remoteViewLI;
    LinearLayout localViewRL;
    //private Camera mCamera;
    //private CameraPreview mPreview;
    //LinearLayout cameraPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_incoming_call);


        answer =  findViewById(R.id.answerButton);
        //ImageView decline = (Button) findViewById(R.id.declineButton);
        endCallButton = findViewById(R.id.hangupButton);
        localViewRL = findViewById(R.id.localVideo);
        remoteViewLI = findViewById(R.id.remoteVideo);
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
        }

        @Override
        public void onCallEstablished(Call call) {
            //cameraPreview.setVisibility(View.INVISIBLE);
            answer.setVisibility(View.GONE);
            remoteViewLI.setVisibility(View.VISIBLE);
            localViewRL.setVisibility(View.VISIBLE);
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        }

        @Override
        public void onCallEnded(Call endedCall) {
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

}
