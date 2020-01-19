package com.whatsapp.app.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallListener;
import com.whatsapp.app.R;
import com.whatsapp.app.Utils.SinchManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OutgoingCallActivity extends AppCompatActivity {

    ImageView end_call_icon;
    TextView call_caller_time,user_destination_name,call_state;
    Call call;
    String receiverID;
    DatabaseReference rootRef;
    String callerID;
    private String saveCurrentTime, saveCurrentDate;
    Boolean callEstablished = false;
    String messagePushID,callerRef,receiverRef;
    Chronometer chronometer;
    MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outgoing_call);

        end_call_icon = findViewById(R.id.end_call_icon);
        call_state = findViewById(R.id.call_state);
        chronometer = findViewById(R.id.chronometer);
        user_destination_name = findViewById(R.id.user_destination_name);

        callEstablished = false;

        if(getIntent()!=null){
            receiverID = getIntent().getStringExtra("messageReceiverID");
        }

        rootRef = FirebaseDatabase.getInstance().getReference();
        callerID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        callerRef = "Calls/" + callerID + "/" + receiverID;
        receiverRef = "Calls/" + receiverID + "/" + callerID;

        DatabaseReference userMessageKeyRef = rootRef.child("Calls")
                .child(callerID).child(receiverID).push();

        messagePushID = userMessageKeyRef.getKey();

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());


        call = SinchManager.sinchClient.getCallClient().callUser(receiverID);
        call.addCallListener(new OutgoingCallActivity.SinchCallListener());

        end_call_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                call.hangup();
                InsertCallLogs();
                finish();
            }
        });

        FirebaseDatabase.getInstance().getReference().child("Users").child(receiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    String name = dataSnapshot.child("name").getValue().toString();
                    //String imageUrl = dataSnapshot.child("image").getValue().toString();

                    user_destination_name.setText(name);
                    //Picasso.get().load(imageUrl).into(remoteUserProfile);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private class SinchCallListener implements CallListener {

        @Override
        public void onCallProgressing(Call call) {
            //Ringing
            playAudio();
            callEstablished=false;
            call_state.setVisibility(View.VISIBLE);
            FirebaseDatabase.getInstance().getReference().child("PendingCalls").child("1")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(receiverID);

            FirebaseDatabase.getInstance().getReference().child("PendingCalls").child("1")
                    .child(receiverID).setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());
        }

        @Override
        public void onCallEstablished(Call call) {
            callEstablished = true;
            stopAudio();
            call_state.setVisibility(View.INVISIBLE);
            chronometer.setVisibility(View.VISIBLE);
            chronometer.start();
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        }

        @Override
        public void onCallEnded(Call endedCall) {
            call = null;
            stopAudio();
            InsertCallLogs();
            deletePendingCalls();
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

    private void deletePendingCalls() {

        FirebaseDatabase.getInstance().getReference().child("PendingCalls").child("1")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();

        FirebaseDatabase.getInstance().getReference().child("PendingCalls").child("1")
                .child(receiverID).removeValue();

    }

    /*protected  void startTimer() {

        Timer T=new Timer();
        final int[] count = {0};
        long starttime = 0;
        T.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        long millis = System.currentTimeMillis() - starttime;
                        int seconds = (int) (millis / 1000);
                        int minutes = seconds / 60;
                        seconds     = seconds % 60;

                        call_time.setText(String.format("%d:%02d", minutes, seconds));
                        //call_time.setText("count="+ count[0]);
                        //count[0]++;
                    }
                });
            }
        }, 1000, 1000);
    }*/



    private void InsertCallLogs() {

        Map messageTextBody = new HashMap();
        messageTextBody.put("type", "voice call");
        messageTextBody.put("from", callerID);
        messageTextBody.put("to", receiverID);
        messageTextBody.put("messageID", messagePushID);
        messageTextBody.put("time", saveCurrentTime);
        messageTextBody.put("date", saveCurrentDate);
        if(callEstablished) messageTextBody.put("status", "Ended");
        else messageTextBody.put("status", "Not Answered");

        Map messageBodyDetails = new HashMap();
        messageBodyDetails.put(callerRef + "/" + messagePushID, messageTextBody);
        messageBodyDetails.put( receiverRef + "/" + messagePushID, messageTextBody);


        rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task)
            {
                if (task.isSuccessful())
                {
                }
            }
        });
    }

}
