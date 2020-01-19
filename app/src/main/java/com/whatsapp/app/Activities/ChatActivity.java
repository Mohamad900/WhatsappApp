package com.whatsapp.app.Activities;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.devlomi.record_view.OnRecordClickListener;
import com.devlomi.record_view.OnRecordListener;
import com.devlomi.record_view.RecordButton;
import com.devlomi.record_view.RecordView;
import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.fxn.utility.ImageQuality;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.keenfin.audioview.AudioService;
import com.squareup.picasso.Picasso;
import com.whatsapp.app.Adapters.MessageAdapter;
import com.whatsapp.app.Models.Messages;
import com.whatsapp.app.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class ChatActivity extends AppCompatActivity
{
    private String messageReceiverID, messageReceiverName, messageReceiverImage, messageSenderID;
    private static final String SEND_IMAGE = "send_image";
    private static final String MIC_IMAGE = "mic_image";

    private TextView userName, userLastSeen;
    private CircleImageView userImage;
    private ImageView emojiButton;

    private Toolbar toolbar;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private ImageButton  SendFilesButton;
    RecordButton SendMessageButton;
    private EmojiconEditText MessageInputText;
    LinearLayout chat_linear_layout;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;
    ProgressDialog loadingBar;
    RecordView recordView;
    ImageButton camera;

    private String saveCurrentTime, saveCurrentDate;
    String checker="", myUrl="";
    StorageTask uploadTask;
    Uri fileUri;
    ArrayList<String> selectedImages = new ArrayList<>();


    private MediaRecorder recorder = null;
    private static String fileName = null;
    private static final String LOG_TAG = "AudioRecordTest";
    String messagePushIDAudio = null;

    private View theMenu;
    private View menu1;
    private View menu2;
    private View menu3;
    private View menu4;
    private View menu5;
    private View menu6;

    private boolean menuOpen = false;
    private View overlay;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        fileName =getExternalCacheDir().getAbsolutePath();

        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();

        messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
        messageReceiverImage = getIntent().getExtras().get("visit_image").toString();

        IntializeControllers();
        InializeRecordView();
        userName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile_image).into(userImage);

        theMenu = findViewById(R.id.the_menu);
        menu1 = findViewById(R.id.menu1);
        menu2 = findViewById(R.id.menu2);
        menu3 = findViewById(R.id.menu3);
        menu4 = findViewById(R.id.menu4);
        menu5 = findViewById(R.id.menu5);
        menu6 = findViewById(R.id.menu6);

        View menu_attachment_document = findViewById(R.id.menu_attachment_document);
        View menu_attachment_camera = findViewById(R.id.menu_attachment_camera);
        View menu_attachment_gallery = findViewById(R.id.menu_attachment_gallery);

        overlay = findViewById(R.id.overlay);


        SendMessageButton.setOnRecordClickListener(new OnRecordClickListener() {
            @Override
            public void onClick(View view)
            {
                //if(SendMessageButton.getDrawable()== getResources().getDrawable(R.drawable.input_send)){
                    SendMessage();
                //}

            }
        });


        DisplayLastSeen();

        SendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!menuOpen) {
                    revealMenu();
                } else {
                    hideMenu();
                }
            }
        });

        menu_attachment_document.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideMenu();
                checker = "pdf";
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("application/pdf");
                startActivityForResult(intent.createChooser(intent, "Select PDF File "),438);
            }
        });

        menu_attachment_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideMenu();
                checker = "image";
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent.createChooser(intent, "Select Image "),438);
            }
        });

        menu_attachment_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checker = "image";
                hideMenu();
                Options options = Options.init()
                        .setRequestCode(100)
                        .setCount(30)
                        .setFrontfacing(false)
                        .setImageQuality(ImageQuality.LOW)
                        //.setPreSelectedUrls(selectedImages)
                        .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT)
                        .setPath("/akshay/new");

                Pix.start(ChatActivity.this, options);
            }
        });

        /*SendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[] = new CharSequence[]{

                        "Images",
                        "PDF Files",
                        "Ms Word Files"

                };

                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select the File");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        if(i == 0){
                            checker = "image";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent, "Select Image "),438);
                        }
                        if(i == 1){
                            checker = "pdf";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(intent.createChooser(intent, "Select PDF File "),438);
                        }
                        if(i == 2){
                            checker = "docx";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/msword");
                            startActivityForResult(intent.createChooser(intent, "Select Ms Word File "),438);
                        }
                    }
                });
                builder.show();
            }
        });*/

        RootRef.child("Messages").child(messageSenderID).child(messageReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s)
                    {

                        Messages messages = dataSnapshot.getValue(Messages.class);

                        messagesList.add(messages);

                        messageAdapter.notifyDataSetChanged();

                        userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    public void overlayClick(View v) {
        hideMenu();
    }

    @Override
    public void onBackPressed() {
        if (menuOpen) {
            hideMenu();
        } else {
            finishAfterTransition();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent audioService = new Intent(this, AudioService.class);
        stopService(audioService);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void revealMenu() {
        menuOpen = true;

        theMenu.setVisibility(View.INVISIBLE);
        int cx = theMenu.getRight() - 200;
        int cy = theMenu.getTop();
        int finalRadius = Math.max(theMenu.getWidth(), theMenu.getHeight());
        Animator anim =
                ViewAnimationUtils.createCircularReveal(theMenu, cx, cy, 0, finalRadius);
        anim.setDuration(600);
        theMenu.setVisibility(View.VISIBLE);
        overlay.setVisibility(View.VISIBLE);
        anim.start();


        // Animate The Icons One after the other, I really would like to know if there is any
        // simpler way to do it
        Animation popeup1 = AnimationUtils.loadAnimation(this, R.anim.popeup);
        Animation popeup2 = AnimationUtils.loadAnimation(this, R.anim.popeup);
        Animation popeup3 = AnimationUtils.loadAnimation(this, R.anim.popeup);
        Animation popeup4 = AnimationUtils.loadAnimation(this, R.anim.popeup);
        Animation popeup5 = AnimationUtils.loadAnimation(this, R.anim.popeup);
        Animation popeup6 = AnimationUtils.loadAnimation(this, R.anim.popeup);
        popeup1.setStartOffset(50);
        popeup2.setStartOffset(100);
        popeup3.setStartOffset(150);
        popeup4.setStartOffset(200);
        popeup5.setStartOffset(250);
        popeup6.setStartOffset(300);
        menu1.startAnimation(popeup1);
        menu2.startAnimation(popeup2);
        menu3.startAnimation(popeup3);
        menu4.startAnimation(popeup4);
        menu5.startAnimation(popeup5);
        menu6.startAnimation(popeup6);

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void hideMenu() {
        menuOpen = false;
        int cx = theMenu.getRight() - 200;
        int cy = theMenu.getTop();
        int initialRadius = theMenu.getWidth();
        Animator anim = ViewAnimationUtils.createCircularReveal(theMenu, cx, cy, initialRadius, 0);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                theMenu.setVisibility(View.INVISIBLE);
                theMenu.setVisibility(View.GONE);
                overlay.setVisibility(View.INVISIBLE);
                overlay.setVisibility(View.GONE);
            }
        });
        anim.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 100 && resultCode == Activity.RESULT_OK) {

            selectedImages = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);

            for(int i=0;i<selectedImages.size();i++){

                Uri fileUri = Uri.fromFile(new File(selectedImages.get(i)));

                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");

                final String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                final String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                        .child(messageSenderID).child(messageReceiverID).push();

                final String messagePushID = userMessageKeyRef.getKey();
                final StorageReference filePath = storageReference.child(messagePushID + "." + "jpg");

                filePath.putFile(fileUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()){
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {

                        if(task.isSuccessful()){
                            Uri downloadUrl = task.getResult();
                            myUrl = downloadUrl.toString();

                            Map messageTextBody = new HashMap();
                            messageTextBody.put("message", myUrl);
                            messageTextBody.put("name", fileUri.getLastPathSegment());
                            messageTextBody.put("type", checker);
                            messageTextBody.put("from", messageSenderID);
                            messageTextBody.put("to", messageReceiverID);
                            messageTextBody.put("messageID", messagePushID);
                            messageTextBody.put("time", saveCurrentTime);
                            messageTextBody.put("date", saveCurrentDate);

                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
                            messageBodyDetails.put( messageReceiverRef + "/" + messagePushID, messageTextBody);

                            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task)
                                {
                                    if (task.isSuccessful())
                                    {
                                        loadingBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Message Sent Successfully...", Toast.LENGTH_SHORT).show();
                                    }
                                    else
                                    {
                                        loadingBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                    }
                                    MessageInputText.setText("");
                                }
                            });


                        }

                    }
                });


            }

        }



        if(requestCode ==438 && resultCode == RESULT_OK && data!=null && data.getData() !=null){

            loadingBar.setTitle("Sending File");
            loadingBar.setMessage("Please wait, we are sending that file...");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            fileUri = data.getData();
            if(!checker.equals("image")){

                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");

                final String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                final String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                        .child(messageSenderID).child(messageReceiverID).push();

                final String messagePushID = userMessageKeyRef.getKey();
                final StorageReference filePath = storageReference.child(messagePushID + "." + checker);

                filePath.putFile(fileUri).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double  p = (100.0*taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        loadingBar.setMessage((int)p + " % Uploading....");
                    }
                }).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()){
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }

                }).addOnCompleteListener(new OnCompleteListener<Uri>() {

                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {

                        if(task.isSuccessful()){

                            Map messageTextBody = new HashMap();
                            messageTextBody.put("message", task.getResult().toString());
                            messageTextBody.put("name", fileUri.getLastPathSegment());
                            messageTextBody.put("type", checker);
                            messageTextBody.put("from", messageSenderID);
                            messageTextBody.put("to", messageReceiverID);
                            messageTextBody.put("messageID", messagePushID);
                            messageTextBody.put("time", saveCurrentTime);
                            messageTextBody.put("date", saveCurrentDate);

                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
                            messageBodyDetails.put( messageReceiverRef + "/" + messagePushID, messageTextBody);

                            RootRef.updateChildren(messageBodyDetails);
                            loadingBar.dismiss();

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingBar.dismiss();
                    }
                });



            }else if(checker.equals("image")){

                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");

                final String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                final String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                        .child(messageSenderID).child(messageReceiverID).push();

                final String messagePushID = userMessageKeyRef.getKey();
                final StorageReference filePath = storageReference.child(messagePushID + "." + "jpg");

                filePath.putFile(fileUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()){
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {

                        if(task.isSuccessful()){
                            Uri downloadUrl = task.getResult();
                            myUrl = downloadUrl.toString();

                            Map messageTextBody = new HashMap();
                            messageTextBody.put("message", myUrl);
                            messageTextBody.put("name", fileUri.getLastPathSegment());
                            messageTextBody.put("type", checker);
                            messageTextBody.put("from", messageSenderID);
                            messageTextBody.put("to", messageReceiverID);
                            messageTextBody.put("messageID", messagePushID);
                            messageTextBody.put("time", saveCurrentTime);
                            messageTextBody.put("date", saveCurrentDate);

                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
                            messageBodyDetails.put( messageReceiverRef + "/" + messagePushID, messageTextBody);

                            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task)
                                {
                                    if (task.isSuccessful())
                                    {
                                        loadingBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Message Sent Successfully...", Toast.LENGTH_SHORT).show();
                                    }
                                    else
                                    {
                                        loadingBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                    }
                                    MessageInputText.setText("");
                                }
                            });


                        }

                    }
                });

            }else{
                loadingBar.dismiss();
                Toast.makeText(this,"Nothing Selected, Error.",Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void InializeRecordView() {
        SendMessageButton.setRecordView(recordView);

        //Cancel Bounds is when the Slide To Cancel text gets before the timer . default is 8
        recordView.setCancelBounds(8);

        recordView.setSmallMicColor(Color.parseColor("#c2185b"));

        //prevent recording under one Secondchat_linear_layout
        recordView.setLessThanSecondAllowed(false);

        recordView.setSlideToCancelText("Slide To Cancel");
        recordView.setCustomSounds(R.raw.record_start, R.raw.record_finished, 0);

        recordView.setOnRecordListener(new OnRecordListener() {
            @Override
            public void onStart() {
                chat_linear_layout.setVisibility(View.INVISIBLE);
                startRecording();

            }

            @Override
            public void onCancel() {
                chat_linear_layout.setVisibility(View.VISIBLE);
                Toast.makeText(ChatActivity.this, "onCancel", Toast.LENGTH_SHORT).show();
                stopRecording();
                Log.d("RecordView", "onCancel");

            }

            @Override
            public void onFinish(long recordTime) {
                chat_linear_layout.setVisibility(View.VISIBLE);
                String time = getHumanTimeText(recordTime);
                stopRecording();
                uploadAudio(recordTime);
                Toast.makeText(ChatActivity.this, "onFinishRecord - Recorded Time is: " + time, Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onLessThanSecond() {
                chat_linear_layout.setVisibility(View.VISIBLE);
                Toast.makeText(ChatActivity.this, "OnLessThanSecond", Toast.LENGTH_SHORT).show();
                stopRecording();
            }
        });

    }

    private String getHumanTimeText(long milliseconds) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(milliseconds),
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)));
    }

    private void IntializeControllers()
    {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

       /* ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);*/

        //LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        //actionBar.setCustomView(actionBarView);

        userName = (TextView) findViewById(R.id.custom_profile_name);
        userLastSeen = (TextView) findViewById(R.id.custom_user_last_seen);
        userImage = (CircleImageView) findViewById(R.id.custom_profile_image);
        recordView = (RecordView) findViewById(R.id.record_view);
        emojiButton = findViewById(R.id.emojiButton);
        chat_linear_layout = findViewById(R.id.chat_linear_layout);
        RelativeLayout rootView = findViewById(R.id.rootView);

        SendMessageButton = (RecordButton) findViewById(R.id.send_message_btn);
        SendFilesButton = (ImageButton) findViewById(R.id.send_files_btn);
        MessageInputText = findViewById(R.id.input_message);
        camera = findViewById(R.id.camera);

        messageAdapter = new MessageAdapter(ChatActivity.this, messagesList);
        userMessagesList = (RecyclerView) findViewById(R.id.private_messages_list_of_users);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);
        loadingBar = new ProgressDialog(this);

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());


        Options options = Options.init()
                .setRequestCode(100)
                .setCount(30)
                .setFrontfacing(false)
                .setImageQuality(ImageQuality.LOW)
                //.setPreSelectedUrls(selectedImages)
                .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT)
                .setPath("/akshay/new");

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checker = "image";
                Pix.start(ChatActivity.this, options);
            }
        });


        MessageInputText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            }
        });


        MessageInputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.length() == 0) {
                    showSendButton();
                    SendMessageButton.setListenForRecord(false);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    showAudioButton();
                    SendMessageButton.setListenForRecord(true);
                }
            }
        });

        EmojIconActions emojIcon = new EmojIconActions(this, rootView, MessageInputText, emojiButton);
        //emojIcon.ShowEmojIcon();
        //emojIcon.addEmojiconEditTextList(MessageInputText);

        emojiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emojIcon.ShowEmojIcon();
            }
        });

    }

    private void showSendButton() {
        SendMessageButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.input_send));
        SendMessageButton.setTag(SEND_IMAGE);
    }

    private void showAudioButton() {
        SendMessageButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.input_mic_white));
        SendMessageButton.setTag(MIC_IMAGE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.ic_call){

            FirebaseDatabase.getInstance().getReference().child("PendingCalls").child("1").child(messageReceiverID)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if(dataSnapshot.exists()){
                                Toast.makeText(ChatActivity.this,"The destination user is on another video/voice call",Toast.LENGTH_LONG).show();
                                finish();
                            }else{
                                Intent intent = new Intent(ChatActivity.this, OutgoingCallActivity.class);
                                intent.putExtra("messageReceiverID",messageReceiverID);
                                startActivity(intent);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }

        if(item.getItemId() == R.id.ic_videocall){

            FirebaseDatabase.getInstance().getReference().child("PendingCalls").child("1").child(messageReceiverID)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if(dataSnapshot.exists()){
                                Toast.makeText(ChatActivity.this,"The destination user is on another video/voice call",Toast.LENGTH_LONG).show();
                                finish();
                            }else{
                                Intent intent = new Intent(ChatActivity.this, VideoOutgoingCallActivity.class);
                                intent.putExtra("messageReceiverID",messageReceiverID);
                                startActivity(intent);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


        }


        return super.onOptionsItemSelected(item);
    }

    private void DisplayLastSeen()
    {
        RootRef.child("Users").child(messageReceiverID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.child("userState").hasChild("state"))
                        {
                            String state = dataSnapshot.child("userState").child("state").getValue().toString();
                            String date = dataSnapshot.child("userState").child("date").getValue().toString();
                            String time = dataSnapshot.child("userState").child("time").getValue().toString();

                            if (state.equals("online"))
                            {
                                userLastSeen.setText("online");
                            }
                            else if (state.equals("offline"))
                            {
                                userLastSeen.setText("Last Seen: " + date + " " + time);
                            }
                        }
                        else
                        {
                            userLastSeen.setText("offline");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void SendMessage()
    {
        String messageText = MessageInputText.getText().toString();

        if (TextUtils.isEmpty(messageText))
        {
            Toast.makeText(this, "first write your message...", Toast.LENGTH_SHORT).show();
        }
        else
        {
            String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
            String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

            DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                    .child(messageSenderID).child(messageReceiverID).push();

            String messagePushID = userMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", messageText);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderID);
            messageTextBody.put("to", messageReceiverID);
            messageTextBody.put("messageID", messagePushID);
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("date", saveCurrentDate);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
            messageBodyDetails.put( messageReceiverRef + "/" + messagePushID, messageTextBody);

            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task)
                {
                    if (task.isSuccessful())
                    {

                        Toast.makeText(ChatActivity.this, "Message Sent Successfully...", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                    MessageInputText.setText("");
                }
            });
        }
    }

    //Audio Recording
    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);

        DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                .child(messageSenderID).child(messageReceiverID).push();

        messagePushIDAudio = userMessageKeyRef.getKey();
        fileName += "/"+ messagePushIDAudio + ".mp3";

        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        recorder.start();
    }

    private void stopRecording() {
        if(recorder!=null)recorder.stop();
        if(recorder!=null)recorder.release();
        recorder = null;
    }

    private void uploadAudio(final long time) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Voice Messages");

        final StorageReference filePath = storageReference.child( messagePushIDAudio + "." + "mp3");
        final Uri fileUri = Uri.fromFile(new File(fileName));

        final String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
        final String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("audio/mpeg")
                .build();

        filePath.putFile(fileUri,metadata).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()){
                    throw task.getException();
                }
                return filePath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {

                if(task.isSuccessful()){
                    Uri downloadUrl = task.getResult();
                    myUrl = downloadUrl.toString();

                    Map messageTextBody = new HashMap();
                    messageTextBody.put("message", myUrl);
                    messageTextBody.put("name", fileUri.getLastPathSegment());
                    messageTextBody.put("type", "audio");
                    messageTextBody.put("from", messageSenderID);
                    messageTextBody.put("to", messageReceiverID);
                    messageTextBody.put("messageID", messagePushIDAudio);
                    messageTextBody.put("time", saveCurrentTime);
                    messageTextBody.put("date", saveCurrentDate);
                    messageTextBody.put("duration", time);

                    Map messageBodyDetails = new HashMap();
                    messageBodyDetails.put(messageSenderRef + "/" + messagePushIDAudio, messageTextBody);
                    messageBodyDetails.put( messageReceiverRef + "/" + messagePushIDAudio, messageTextBody);

                    RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task)
                        {
                            if (task.isSuccessful())
                            {
                                loadingBar.dismiss();
                                Toast.makeText(ChatActivity.this, "Message Sent Successfully...", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                loadingBar.dismiss();
                                Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });


                }

            }
        });



    }


}