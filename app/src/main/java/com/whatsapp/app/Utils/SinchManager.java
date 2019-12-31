package com.whatsapp.app.Utils;

import android.content.Context;
import android.content.Intent;

import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;
import com.whatsapp.app.Activities.IncomingCallActivity;
import com.whatsapp.app.Activities.VideoIncomingCallActivity;
import com.whatsapp.app.ChatActivity;
import com.whatsapp.app.MainActivity;

public class SinchManager {

    public static  Call incomingCallObject;
    public  static SinchClient sinchClient;

    public static void startSinchInstance(Context context , String currentUserID){

        if(sinchClient == null || !sinchClient.isStarted()){
            sinchClient = com.sinch.android.rtc.Sinch.getSinchClientBuilder().context(context)
                    .applicationKey("be90fee6-81d9-4444-8a1b-9fee37d03a23")
                    .applicationSecret("lZdlYPcKnUGqVjsixsnCzA==")
                    .environmentHost("clientapi.sinch.com")
                    .userId(currentUserID)
                    .build();

            sinchClient.setSupportCalling(true);
            sinchClient.startListeningOnActiveConnection();

            sinchClient.getCallClient().addCallClientListener(new CallClientListener() {

                @Override
                public void onIncomingCall(CallClient callClient, final Call incomingCall) {
                    incomingCallObject = incomingCall;

                    if (incomingCall.getDetails().isVideoOffered()) {
                        Intent intent = new Intent(context, VideoIncomingCallActivity.class);
                        context.startActivity(intent);
                        //intent.putExtra(CALL_ID, call.getCallId());
                    } else {
                        Intent intent = new Intent(context, IncomingCallActivity.class);
                        context.startActivity(intent);
                    }


                }
            });

            sinchClient.start();
        }
    }
}

