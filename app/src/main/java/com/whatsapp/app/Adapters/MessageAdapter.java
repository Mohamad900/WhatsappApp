package com.whatsapp.app.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.keenfin.audioview.AudioService;
import com.keenfin.audioview.AudioView;
import com.squareup.picasso.Picasso;
import com.whatsapp.app.Activities.ImageViewerActivity;
import com.whatsapp.app.Activities.MainActivity;
import com.whatsapp.app.Models.Messages;
import com.whatsapp.app.R;
import com.whatsapp.app.SmoothSeekBar;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>
{
    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    boolean isStoped = false;
    boolean isPaused = false;
    boolean isPrepared = false;
    Context context;


    public MessageAdapter (Context context, List<Messages> userMessagesList)
    {
        this.userMessagesList = userMessagesList;
        this.context=context;
    }


    public class MessageViewHolder extends RecyclerView.ViewHolder
    {
        public TextView senderMessageText, receiverMessageText,timeReceiverTV,timeSenderTV,voice_duration;
        public RelativeLayout senderMsgRl, receiverMsgRl;
        //public CircleImageView receiverProfileImage;
        public ImageView messageSenderPicture, messageReceiverPicture,messageSenderDocument,messageReceiverDocument;
        RelativeLayout msg_audio;
        SmoothSeekBar seekBar;
        ImageButton imgPlay;
        com.keenfin.audioview.AudioView2 audio_view;


        public MessageViewHolder(@NonNull View itemView)
        {
            super(itemView);

            senderMessageText =  itemView.findViewById(R.id.sender_messsage_text);
            voice_duration =  itemView.findViewById(R.id.voice_duration);
            senderMsgRl =  itemView.findViewById(R.id.sender_msg_rl);
            receiverMsgRl =  itemView.findViewById(R.id.receiver_msg_rl);
            receiverMessageText = (TextView) itemView.findViewById(R.id.receiver_message_text);
            timeReceiverTV = (TextView) itemView.findViewById(R.id.timeReceiverTV);
            timeSenderTV = (TextView) itemView.findViewById(R.id.timeSenderTV);
            msg_audio = itemView.findViewById(R.id.msg_audio);
            seekBar = itemView.findViewById(R.id.seekBar);
            imgPlay = itemView.findViewById(R.id.play);
            audio_view = itemView.findViewById(R.id.audio_view);
            //receiverProfileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_image);
            messageReceiverPicture = itemView.findViewById(R.id.message_receiver_image_view);
            messageSenderPicture = itemView.findViewById(R.id.message_sender_image_view);
            messageSenderDocument = itemView.findViewById(R.id.message_receiver_document_view);
            messageReceiverDocument = itemView.findViewById(R.id.message_sender_document_view);
        }
    }




    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.custom_messages_layout, viewGroup, false);

        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, final int i)
    {
        String messageSenderId = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(i);

        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.hasChild("image"))
                {
                    String receiverImage = dataSnapshot.child("image").getValue().toString();

                    //Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(messageViewHolder.receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        messageViewHolder.receiverMsgRl.setVisibility(View.GONE);
        //messageViewHolder.receiverProfileImage.setVisibility(View.GONE);
        messageViewHolder.senderMsgRl.setVisibility(View.GONE);
        messageViewHolder.messageSenderPicture.setVisibility(View.GONE);
        messageViewHolder.messageReceiverPicture.setVisibility(View.GONE);
        messageViewHolder.messageReceiverDocument.setVisibility(View.GONE);
        messageViewHolder.messageSenderDocument.setVisibility(View.GONE);
        messageViewHolder.msg_audio.setVisibility(View.GONE);


        if (fromMessageType.equals("text"))
        {
            if (fromUserID.equals(messageSenderId))
            {
                messageViewHolder.senderMsgRl.setVisibility(View.VISIBLE);
                messageViewHolder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                messageViewHolder.senderMessageText.setTextColor(Color.BLACK);
                messageViewHolder.senderMessageText.setText(messages.getMessage());
                messageViewHolder.timeSenderTV.setText(messages.getTime());
            }
            else
            {
                //messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.receiverMsgRl.setVisibility(View.VISIBLE);
                messageViewHolder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                messageViewHolder.receiverMessageText.setTextColor(Color.BLACK);
                messageViewHolder.receiverMessageText.setText(messages.getMessage());
                messageViewHolder.timeReceiverTV.setText(messages.getTime());
            }
        }else if (fromMessageType.equals("image")){

            if (fromUserID.equals(messageSenderId)) {

                messageViewHolder.messageSenderPicture.setVisibility((View.VISIBLE));
                Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageSenderPicture);
            }else{
                //messageViewHolder.receiverProfileImage.setVisibility((View.VISIBLE));
                messageViewHolder.messageReceiverPicture.setVisibility((View.VISIBLE));
                Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageReceiverPicture);
            }
        }else if (fromMessageType.equals("pdf") || fromMessageType.equals("docx")) {

            if (fromUserID.equals(messageSenderId)){
                messageViewHolder.messageSenderDocument.setVisibility((View.VISIBLE));
                messageViewHolder.messageSenderDocument.setBackgroundResource(R.drawable.file);

            }else{
                //messageViewHolder.receiverProfileImage.setVisibility((View.VISIBLE));
                messageViewHolder.messageReceiverDocument.setVisibility((View.VISIBLE));
                messageViewHolder.messageReceiverDocument.setBackgroundResource(R.drawable.file);

            }

        }else if (fromMessageType.equals("audio")) {

            if (fromUserID.equals(messageSenderId)){
                messageViewHolder.msg_audio.setVisibility((View.VISIBLE));
               /* RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.ALIGN_PARENT_END);
                params.setMargins(100, 10, 20, 10);
                messageViewHolder.msg_audio.setLayoutParams(params);*/
                messageViewHolder.msg_audio.setBackgroundColor(context.getResources().getColor(R.color.sender_bubble));

            }else{
                messageViewHolder.msg_audio.setVisibility((View.VISIBLE));
              /*  RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.ALIGN_PARENT_START);
                params.setMargins(20, 10, 100, 10);
                messageViewHolder.msg_audio.setLayoutParams(params);*/
                messageViewHolder.msg_audio.setBackgroundColor(context.getResources().getColor(R.color.white));
            }

            messageViewHolder.audio_view.setTag(i);
            if (!messageViewHolder.audio_view.attached())
                messageViewHolder.audio_view.setUpControls();
            try {
                messageViewHolder.audio_view.setDataSource(userMessagesList.get(i).getMessage());
            } catch (IOException ignored) {
            }
            /*
            try {

                    messageViewHolder.audio_view.setDataSource(userMessagesList.get(i).getMessage());
            } catch (IOException e) {
                Log.d("error",e.getMessage());
                e.printStackTrace();
            }*/

/*

            final MediaPlayer mPlayer = new MediaPlayer();
            try {
                mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mPlayer.setDataSource(userMessagesList.get(i).getMessage());
                long duration = userMessagesList.get(i).getDuration();

                String time = String.format("%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(duration),
                        TimeUnit.MILLISECONDS.toSeconds(duration) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));

                messageViewHolder.voice_duration.setText(time);
                messageViewHolder.seekBar.setMax((int)duration);


            } catch (IOException e) {
                e.printStackTrace();
            }

            messageViewHolder.imgPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final Handler mSeekbarUpdateHandler = new Handler();
                    Runnable mUpdateSeekbar = new Runnable() {
                        @Override
                        public void run() {

                            if(mPlayer.isPlaying()) {
                                messageViewHolder.seekBar.setProgress(mPlayer.getCurrentPosition());
                                String time = String.format("%02d:%02d",
                                        TimeUnit.MILLISECONDS.toMinutes((long) mPlayer.getCurrentPosition()),
                                        TimeUnit.MILLISECONDS.toSeconds((long) mPlayer.getCurrentPosition()) -
                                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) mPlayer.getCurrentPosition())));

                                messageViewHolder.voice_duration.setText(time);
                                mSeekbarUpdateHandler.postDelayed(this, 500);
                            }
                        }
                    };

                    if(messageViewHolder.imgPlay.getDrawable().getConstantState() == messageViewHolder.itemView.getContext().getResources().getDrawable(R.drawable.ic_pause_circle_filled_black_24dp).getConstantState()){
                        messageViewHolder.imgPlay.setImageResource(R.drawable.ic_play_circle_filled_black_24dp);
                        mPlayer.pause();
                        isPaused = true;
                        //messageViewHolder.seekBar.setp

                    }else{
                        messageViewHolder.imgPlay.setImageResource(R.drawable.ic_pause_circle_filled_black_24dp);
                        if(!isPaused) {
                            try {
                                mPlayer.prepare();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        mPlayer.start();
                        mSeekbarUpdateHandler.postDelayed(mUpdateSeekbar, 50);
                        //messageViewHolder.seekBar.setEnabled(true);

                    }

                    messageViewHolder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            try {
                                if (mPlayer.isPlaying() && fromUser) {
                                        mPlayer.seekTo(progress);
                                }

                                *//*if (!mPlayer.isPlaying() && isPaused==false) {
                                    messageViewHolder.seekBar.setProgress(0);
                                }*//*
                            } catch (Exception e) {
                                //seekBar.setEnabled(false);
                            }

                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {

                        }
                    });


                    mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            //messageViewHolder.seekBar.setProgress(0);
                            //mp.seekTo(0);
                            mPlayer.stop();
                            isPaused=false;
                            mSeekbarUpdateHandler.removeCallbacks(mUpdateSeekbar);
                            messageViewHolder.imgPlay.setImageResource(R.drawable.ic_play_circle_filled_black_24dp);
                        }
                    });

                }
            });*/

        }


        if(fromUserID.equals(messageSenderId)){

            messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(userMessagesList.get(i).getType().equals("pdf") || userMessagesList.get(i).getType().equals("docx")){

                        CharSequence options[] = new CharSequence[]{

                                "Delete For me",
                                "Download and view this document",
                                "Cancel",
                                "Delete For Every One",

                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int pos) {
                                if(pos == 0){

                                    deleteSentMessage(i,messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }else if(pos == 1){

                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(i).getMessage()));
                                    messageViewHolder.itemView.getContext().startActivity(intent);

                                }else if(pos == 3){
                                    deleteMessageForEveryOne(i,messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(),MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();

                    }

                    else if(userMessagesList.get(i).getType().equals("text")){

                        CharSequence options[] = new CharSequence[]{

                                "Delete For me",
                                "Cancel",
                                "Delete For Every One",

                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int pos) {
                                if(pos == 0){
                                    deleteSentMessage(i,messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(),MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }else if(pos == 2){
                                    deleteMessageForEveryOne(i,messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(),MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();

                    }else if(userMessagesList.get(i).getType().equals("image")){

                        CharSequence options[] = new CharSequence[]{

                                "Delete For me",
                                "View This Image",
                                "Cancel",
                                "Delete For Every One",

                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int pos) {
                                if(pos == 0){
                                    deleteSentMessage(i,messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(),MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }else if(pos == 1){

                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), ImageViewerActivity.class);
                                    intent.putExtra("url",userMessagesList.get(i).getMessage());
                                    messageViewHolder.itemView.getContext().startActivity(intent);

                                }else if(pos == 3){
                                    deleteMessageForEveryOne(i,messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(),MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();

                    }else if(userMessagesList.get(i).getType().equals("audio")){
/*
                        final MediaPlayer mPlayer = new MediaPlayer();
                        try {
                            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            mPlayer.setDataSource(userMessagesList.get(i).getMessage());
                            mPlayer.prepare();
                            mPlayer.start();
                            final Handler mSeekbarUpdateHandler = new Handler();
                            Runnable mUpdateSeekbar = new Runnable() {
                                @Override
                                public void run() {
                                    messageViewHolder.seekBar.setProgress(mPlayer.getCurrentPosition());
                                    mSeekbarUpdateHandler.postDelayed(this, 50);
                                }
                            };
                            mSeekbarUpdateHandler.postDelayed(mUpdateSeekbar, 0);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }*/


                    }

                }
            });
        }else{

            messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(userMessagesList.get(i).getType().equals("pdf") || userMessagesList.get(i).getType().equals("docx")){

                        CharSequence options[] = new CharSequence[]{

                                "Delete For me",
                                "Download and view this document",
                                "Cancel"
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int pos) {
                                if(pos == 0){
                                    deleteReceiveMessage(i,messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(),MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }else if(pos == 1){

                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(),ImageViewerActivity.class);
                                    intent.putExtra("url",userMessagesList.get(i).getMessage());
                                    messageViewHolder.itemView.getContext().startActivity(intent);

                                }
                            }
                        });
                        builder.show();

                    }

                    else if(userMessagesList.get(i).getType().equals("text")){

                        CharSequence options[] = new CharSequence[]{

                                "Delete For me",
                                "Cancel"
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int pos) {
                                if(pos == 0){
                                    deleteReceiveMessage(i,messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(),MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();

                    }else if(userMessagesList.get(i).getType().equals("image")){

                        CharSequence options[] = new CharSequence[]{

                                "Delete For me",
                                "View This Image",
                                "Cancel"
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int pos) {
                                if(pos == 0){
                                    deleteReceiveMessage(i,messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(),MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);

                                }else if(pos == 1){
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(),ImageViewerActivity.class);
                                    intent.putExtra("url",userMessagesList.get(i).getMessage());
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();

                    }
                }
            });
        }

    }


    @Override
    public int getItemCount()
    {
        return userMessagesList.size();
    }

    private void deleteSentMessage(final int position , final MessageViewHolder holder){

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages").child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){
                    Toast.makeText(holder.itemView.getContext(),"Deleted Successfully",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(holder.itemView.getContext(),"Error Deleting Message",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteReceiveMessage(final int position , final MessageViewHolder holder){

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages").child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){
                    Toast.makeText(holder.itemView.getContext(),"Deleted Successfully",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(holder.itemView.getContext(),"Error Deleting Message",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteMessageForEveryOne(final int position , final MessageViewHolder holder){

        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages").child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){

                    rootRef.child("Messages").child(userMessagesList.get(position).getFrom())
                            .child(userMessagesList.get(position).getTo())
                            .child(userMessagesList.get(position).getMessageID())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(holder.itemView.getContext(),"Deleted Successfully",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }else{
                    Toast.makeText(holder.itemView.getContext(),"Error Deleting Message",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


}