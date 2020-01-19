package com.whatsapp.app.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.whatsapp.app.R;

import java.io.IOException;
import java.util.HashMap;

public class SettingsProfileActivity extends AppCompatActivity {

    CircleImageView profile_image;
    String imageUrl;
    EmojiconEditText username;
    ImageView emojiButton;
    Button save;
    RelativeLayout rootView;
    private String currentUserID;
    ProgressDialog progressDialog;
    private static final int GalleryPick = 1;
    private StorageReference UserProfileImagesRef;
    String downloaedUrl;
    StorageReference filePath;
    Uri resultUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_profile);

        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        emojiButton = findViewById(R.id.emojiButton);
        save = findViewById(R.id.save);
        rootView = findViewById(R.id.rootView);

        UserProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Updating..");

        if (getIntent() != null) {

            imageUrl = getIntent().getStringExtra("imageUrl");
        }

        Picasso.get().load(imageUrl).placeholder(R.drawable.pic).into(profile_image);

        EmojIconActions emojIcon = new EmojIconActions(this, rootView, username, emojiButton);


        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                CheckPermissions();

            }
        });

        emojiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emojIcon.ShowEmojIcon();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressDialog.show();
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
                            //Toast.makeText(SettingsProfileActivity.this, "Profile Image uploaded Successfully...", Toast.LENGTH_SHORT).show();
                            downloaedUrl = task.getResult().toString();
                            UpdateProfileInfo();

                        }
                        else
                        {
                            String message = task.getException().toString();
                            Toast.makeText(SettingsProfileActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }
                });



            }
        });

        FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){

                    String name = dataSnapshot.child("name").getValue().toString();

                    username.setText(name);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void UpdateProfileInfo()
    {
        String setUserName = username.getText().toString();

        if (TextUtils.isEmpty(setUserName))
        {
            Toast.makeText(this, "Please type your name", Toast.LENGTH_SHORT).show();
        }else
        {
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid", currentUserID);
            profileMap.put("name", setUserName);
            profileMap.put("image", downloaedUrl);
            FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            progressDialog.dismiss();
                            if (task.isSuccessful())
                            {
                                SharedPreferences.Editor editor = getSharedPreferences("UserProfile", MODE_PRIVATE).edit();
                                editor.putString("name", setUserName);
                                editor.apply();

                                //Toast.makeText(SettingsProfileActivity.this, "Profile Updated Successfully...", Toast.LENGTH_SHORT).show();
                                finishAfterTransition();
                            }
                            else
                            {
                                String message = task.getException().toString();
                                Toast.makeText(SettingsProfileActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {

            // Permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GalleryPick);
            } else {
                // Permission is denied
                if (shouldShowRequestPermissionRationale(permissions[0])) {
                    Toast.makeText(this, "Please grant the requested permissions to access photos", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "You need to grant the requested permissions at phone settings", Toast.LENGTH_LONG).show();
                }


            }
        }
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

                 resultUri = result.getUri();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), resultUri);
                    profile_image.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                filePath = UserProfileImagesRef.child(currentUserID + ".jpg");

            }
        }
    }

}
