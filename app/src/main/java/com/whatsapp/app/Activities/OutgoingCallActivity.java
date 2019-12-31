package com.whatsapp.app.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.calling.CallListener;
import com.whatsapp.app.ChatActivity;
import com.whatsapp.app.R;
import com.whatsapp.app.Utils.SinchManager;

import java.util.List;

public class OutgoingCallActivity extends AppCompatActivity {

    ImageView end_call_icon;
    TextView call_caller_time;
    Call call;
    String messageReceiverID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outgoing_call);

        end_call_icon = findViewById(R.id.end_call_icon);

        if(getIntent()!=null){
            messageReceiverID = getIntent().getStringExtra("messageReceiverID");
        }

        call = SinchManager.sinchClient.getCallClient().callUser(messageReceiverID);
        call.addCallListener(new OutgoingCallActivity.SinchCallListener());

        end_call_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                call.hangup();
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
            call = null;
            setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
            endedCall.hangup();
            finish();
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> list) {

        }
    }

}
