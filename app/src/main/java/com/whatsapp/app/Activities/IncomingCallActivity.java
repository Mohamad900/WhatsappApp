package com.whatsapp.app.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallListener;
import com.whatsapp.app.R;
import com.whatsapp.app.Utils.SinchManager;

import java.util.List;

public class IncomingCallActivity extends AppCompatActivity {

    ImageView end_call_icon1,end_call_icon2,answer_call_icon;
    RelativeLayout bottom_call1,bottom_call2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);

        answer_call_icon = findViewById(R.id.answer_call_icon);
        end_call_icon1 = findViewById(R.id.end_call_icon1);
        end_call_icon2 = findViewById(R.id.end_call_icon2);
        bottom_call1 = findViewById(R.id.bottom_call1);
        bottom_call2 = findViewById(R.id.bottom_call2);


        answer_call_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SinchManager.incomingCallObject.answer();
                bottom_call1.setVisibility(View.INVISIBLE);
                bottom_call2.setVisibility(View.VISIBLE);
                end_call_icon2.setVisibility(View.VISIBLE);
                SinchManager.incomingCallObject.addCallListener(new SinchCallListener());
            }
        });

        end_call_icon1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SinchManager.incomingCallObject.hangup();
                finish();
            }
        });

        end_call_icon2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SinchManager.incomingCallObject.hangup();
                finish();
            }
        });
    }

    private class SinchCallListener implements CallListener {

        @Override
        public void onCallProgressing(Call call) {
            //Ringing
        }

        @Override
        public void onCallEstablished(Call call) {
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        }

        @Override
        public void onCallEnded(Call endedCall) {
            SinchManager.incomingCallObject = null;
            setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
            endedCall.hangup();
            finish();
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> list) {

        }
    }

}
