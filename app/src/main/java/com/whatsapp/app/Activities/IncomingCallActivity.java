package com.whatsapp.app.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
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
import com.squareup.picasso.Picasso;
import com.whatsapp.app.R;
import com.whatsapp.app.Utils.SinchManager;

import java.util.List;

public class IncomingCallActivity extends AppCompatActivity {

    ImageView end_call_icon1,end_call_icon2,answer_call_icon;
    RelativeLayout bottom_call1,bottom_call2;
    TextView call_state,caller_name;
    Chronometer chronometer;
    MediaPlayer mp;
    CircleImageView caller_image;
    ImageView user_caller_image;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);

        answer_call_icon = findViewById(R.id.answer_call_icon);
        end_call_icon1 = findViewById(R.id.end_call_icon1);
        end_call_icon2 = findViewById(R.id.end_call_icon2);
        bottom_call1 = findViewById(R.id.bottom_call1);
        bottom_call2 = findViewById(R.id.bottom_call2);
        call_state = findViewById(R.id.call_state);
        caller_name = findViewById(R.id.caller_name);
        chronometer = findViewById(R.id.chronometer);
        caller_image = findViewById(R.id.caller_image);
        user_caller_image = findViewById(R.id.user_caller_image);

        SinchManager.incomingCallObject.addCallListener(new SinchCallListener());


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

                                    caller_name.setText(name);
                                    Picasso.get().load(imageUrl).into(caller_image);
                                    Picasso.get().load(imageUrl).into(user_caller_image);
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

        answer_call_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SinchManager.incomingCallObject.answer();
                bottom_call1.setVisibility(View.INVISIBLE);
                bottom_call2.setVisibility(View.VISIBLE);
                end_call_icon2.setVisibility(View.VISIBLE);
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
            playAudio();
            call_state.setVisibility(View.VISIBLE);
        }

        @Override
        public void onCallEstablished(Call call) {
            stopAudio();
            call_state.setVisibility(View.INVISIBLE);
            chronometer.setVisibility(View.VISIBLE);
            chronometer.start();
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        }

        @Override
        public void onCallEnded(Call endedCall) {
            stopAudio();
            SinchManager.incomingCallObject = null;
            setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
            endedCall.hangup();
            finish();
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> list) {

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
