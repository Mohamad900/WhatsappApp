package com.whatsapp.app.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OutgoingCallActivity extends AppCompatActivity {

    ImageView end_call_icon;
    TextView call_caller_time;
    Call call;
    String receiverID;
    DatabaseReference rootRef;
    String callerID;
    private String saveCurrentTime, saveCurrentDate;
    Boolean callEstablished = false;
    String messagePushID,callerRef,receiverRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outgoing_call);

        end_call_icon = findViewById(R.id.end_call_icon);

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
    }

    private class SinchCallListener implements CallListener {

        @Override
        public void onCallProgressing(Call call) {
            //Ringing
            callEstablished=false;
        }

        @Override
        public void onCallEstablished(Call call) {
            callEstablished = true;
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        }

        @Override
        public void onCallEnded(Call endedCall) {
            call = null;
            InsertCallLogs();
            setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
            endedCall.hangup();
            finish();
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> list) {

        }
    }

    private void InsertCallLogs() {

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());


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
