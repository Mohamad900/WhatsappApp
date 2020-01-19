package com.whatsapp.app.Activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.whatsapp.app.R;

import java.io.IOException;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class ProfileInfoActivity extends AppCompatActivity
{
    private Button UpdateAccountSettings;
    private EditText userStatus;
    EmojiconEditText userName;
    private CircleImageView userProfileImage;

    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private static final int GalleryPick = 1;
    private StorageReference UserProfileImagesRef;
    private ProgressDialog loadingBar;

    private Toolbar SettingsToolBar;
    String phoneNumber,countryCode;
    ImageView emojiButton;
    RelativeLayout rootView;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_info);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        UserProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");


        InitializeFields();

        UpdateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                CreateProfileInfo();
            }
        });

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                CheckPermissions();

            }
        });

        EmojIconActions emojIcon = new EmojIconActions(this, rootView, userName, emojiButton);

        emojiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emojIcon.ShowEmojIcon();
            }
        });

    }

    private void CheckPermissions(){

        // Permission is not granted
        if (ContextCompat.checkSelfPermission( this, Manifest.permission.READ_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.READ_EXTERNAL_STORAGE ,android.Manifest.permission.WRITE_EXTERNAL_STORAGE  }, 1);
        }else{
            Intent galleryIntent = new Intent();
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
            galleryIntent.setType("image/*");
            startActivityForResult(galleryIntent, GalleryPick);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {

            // Permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GalleryPick);
            }
            else {
                // Permission is denied
                if(shouldShowRequestPermissionRationale(permissions[0])){
                    Toast.makeText(this, "Please grant the requested permissions to access photos", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(this, "You need to grant the requested permissions at phone settings", Toast.LENGTH_LONG).show();
                }


            }
        }

        if (requestCode == 2) {

            // Permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                    && grantResults[2] == PackageManager.PERMISSION_GRANTED
                    && grantResults[3] == PackageManager.PERMISSION_GRANTED
                    && grantResults[4] == PackageManager.PERMISSION_GRANTED
                    && grantResults[5] == PackageManager.PERMISSION_GRANTED
                    && grantResults[6] == PackageManager.PERMISSION_GRANTED) {

                Intent mainIntent = new Intent(ProfileInfoActivity.this, MainActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mainIntent);
                finish();
            }
            else {
                // Permission is denied
                if(!shouldShowRequestPermissionRationale(permissions[0]) ||
                        !shouldShowRequestPermissionRationale(permissions[1]) ||
                        !shouldShowRequestPermissionRationale(permissions[2]) ||
                        !shouldShowRequestPermissionRationale(permissions[3]) ||
                        !shouldShowRequestPermissionRationale(permissions[4]) ||
                        !shouldShowRequestPermissionRationale(permissions[5]) ||
                        !shouldShowRequestPermissionRationale(permissions[6])){

                    Toast.makeText(this, "You need to grant the requested permissions at phone settings", Toast.LENGTH_LONG).show();

                }else{
                    Toast.makeText(this, "Please grant the requested permissions to access app functionalities", Toast.LENGTH_LONG).show();
                }


            }
        }
    }


    private void InitializeFields()
    {
        UpdateAccountSettings = findViewById(R.id.update_settings_button);
        userName = findViewById(R.id.set_user_name);
        userProfileImage = findViewById(R.id.set_profile_image);
        emojiButton = findViewById(R.id.emojiButton);
        rootView = findViewById(R.id.rootView);
        loadingBar = new ProgressDialog(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==GalleryPick  &&  resultCode==RESULT_OK  &&  data!=null)
        {
            Uri ImageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK)
            {
                loadingBar.setTitle("Set Profile Image");
                loadingBar.setMessage("Please wait, your profile image is updating...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                Uri resultUri = result.getUri();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), resultUri);
                    userProfileImage.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }


                final StorageReference filePath = UserProfileImagesRef.child(currentUserID + ".jpg");

                filePath.putFile(resultUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()){
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task)
                    {
                        if (task.isSuccessful())
                        {
                            Toast.makeText(ProfileInfoActivity.this, "Profile Image uploaded Successfully...", Toast.LENGTH_SHORT).show();

                            final String downloaedUrl = task.getResult().toString();

                            RootRef.child("Users").child(currentUserID).child("image")
                                    .setValue(downloaedUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                loadingBar.dismiss();
                                            }
                                            else
                                            {
                                                String message = task.getException().toString();
                                                Toast.makeText(ProfileInfoActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }
                                        }
                                    });
                        }
                        else
                        {
                            String message = task.getException().toString();
                            Toast.makeText(ProfileInfoActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });
            }
        }
    }

    private void CreateProfileInfo()
    {
        String setUserName = userName.getText().toString();

        if (TextUtils.isEmpty(setUserName))
        {
            Toast.makeText(this, "Please type your name", Toast.LENGTH_SHORT).show();
        }else
        {
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid", currentUserID);
            profileMap.put("name", setUserName);
            RootRef.child("Users").child(currentUserID).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if (task.isSuccessful())
                            {
                                SharedPreferences.Editor editor = getSharedPreferences("UserProfile", MODE_PRIVATE).edit();
                                editor.putString("name", setUserName);
                                editor.apply();

                                Toast.makeText(ProfileInfoActivity.this, "Profile Created Successfully...", Toast.LENGTH_SHORT).show();
                                SendUserToMainActivity();
                            }
                            else
                            {
                                String message = task.getException().toString();
                                Toast.makeText(ProfileInfoActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }


    private void SendUserToMainActivity()
    {

        if (ContextCompat.checkSelfPermission( this, Manifest.permission.READ_CONTACTS ) != PackageManager.PERMISSION_GRANTED  ||
                ContextCompat.checkSelfPermission( this, Manifest.permission.WRITE_CONTACTS ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission( this, Manifest.permission.RECORD_AUDIO ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_NETWORK_STATE ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission( this, Manifest.permission.READ_PHONE_STATE ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission( this, Manifest.permission.MODIFY_AUDIO_SETTINGS ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission( this, Manifest.permission.CAMERA ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions( this, new String[] {

                    android.Manifest.permission.READ_CONTACTS
                    ,android.Manifest.permission.WRITE_CONTACTS
                    ,android.Manifest.permission.RECORD_AUDIO
                    ,android.Manifest.permission.ACCESS_NETWORK_STATE
                    ,android.Manifest.permission.READ_PHONE_STATE
                    ,android.Manifest.permission.MODIFY_AUDIO_SETTINGS
                    , Manifest.permission.CAMERA

                    }, 2);
        }else{
            Intent mainIntent = new Intent(ProfileInfoActivity.this, MainActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(mainIntent);
            finish();
        }


    }
}