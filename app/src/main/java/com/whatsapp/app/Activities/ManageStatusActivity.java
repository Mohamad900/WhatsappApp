package com.whatsapp.app.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.fxn.utility.ImageQuality;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.whatsapp.app.Adapters.ManageStatusAdapter;
import com.whatsapp.app.Adapters.StatusAdapter;
import com.whatsapp.app.Adapters.UserListAdapter;
import com.whatsapp.app.Fragments.StatusFragment;
import com.whatsapp.app.Models.Status;
import com.whatsapp.app.R;

import java.io.File;
import java.util.ArrayList;

public class ManageStatusActivity extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView recyclerView;
    ManageStatusAdapter adapter;
    FirebaseAuth auth;
    ArrayList<Status> currentUserStatus;
    FloatingActionButton camera;
    Options options;
    ArrayList<String> selectedImages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_status);

        toolbar =findViewById(R.id.toolbar);
        recyclerView =findViewById(R.id.recyclerView);
        camera =findViewById(R.id.camera);
        auth = FirebaseAuth.getInstance();
        currentUserStatus = new ArrayList<>();
        selectedImages = new ArrayList<>();

        options = Options.init()
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
                  Pix.start(ManageStatusActivity.this, options);
            }
        });

        initToolbar();
        setAdapter();
        getMyStatus();
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Log.e("val", "requestCode ->  " + requestCode+"  resultCode "+resultCode);
        switch (requestCode) {
            case (100): {
                if (resultCode == Activity.RESULT_OK) {
                    selectedImages = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);
                    SaveStory();
                }
            }
            break;
        }
    }

    private void initToolbar() {

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("My status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void SaveStory(){

        final DatabaseReference userStoryRef = FirebaseDatabase.getInstance().getReference().child("Stories").child(auth.getCurrentUser().getUid());
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Stories");

        for(int i=0;i<selectedImages.size();i++){

            final String key = userStoryRef.push().getKey();
            final StorageReference filePath = storageReference.child(key + "." + "jpg");

            Uri fileUri = Uri.fromFile(new File(selectedImages.get(i)));

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
                        String myUrl = downloadUrl.toString();

                        userStoryRef.child(key).setValue(myUrl);

                    }

                }
            });

        }
    }

    private void getMyStatus() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Stories").child(auth.getCurrentUser().getUid());

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){

                    currentUserStatus.clear();

                    for(DataSnapshot child : dataSnapshot.getChildren()){

                        String image = child.getValue().toString();
                        Status status = new Status(child.getKey(),image);
                        currentUserStatus.add(status);
                    }
                }
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void setAdapter() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ManageStatusAdapter(this, currentUserStatus);
        recyclerView.setAdapter(adapter);
    }

}
