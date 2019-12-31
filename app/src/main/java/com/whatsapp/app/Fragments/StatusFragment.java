package com.whatsapp.app.Fragments;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import io.michaelrocks.libphonenumber.android.NumberParseException;
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil;
import io.michaelrocks.libphonenumber.android.Phonenumber;

import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.fxn.utility.ImageQuality;
import com.fxn.utility.PermUtil;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.whatsapp.app.Activities.StoryActivity;
import com.whatsapp.app.Adapters.StatusAdapter;
import com.whatsapp.app.ChatActivity;
import com.whatsapp.app.Models.Status;
import com.whatsapp.app.Models.User;
import com.whatsapp.app.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class StatusFragment extends Fragment {

    private RecyclerView rvStatus;
    private StatusAdapter adapter;
    private ArrayList<Status> contactsStatusList;
    RelativeLayout chat_row_container;
    Options options;
    CircleImageView profile_image;
    ArrayList<String> selectedImages = new ArrayList<>();
    Map stories ;
    FirebaseAuth auth;
    ArrayList<String> currentUserStatus;
    PhoneNumberUtil phoneNumberUtil;
    ArrayList<String> usersIdsList;
    ArrayList<String> contactStatusImages;
    HashSet<String> contactsPhoneNumbers;

    public StatusFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_status, container, false);

        initialize(view);
        getMyStatus();
        getContactsStatus();
        setAdapter();

        return view;
    }

    private void initialize(View view) {

        contactsStatusList = new ArrayList<>();
        selectedImages = new ArrayList<>();
        usersIdsList =new ArrayList<>();
        contactStatusImages  = new ArrayList<>();
        contactsPhoneNumbers = new HashSet<>();
        stories = new HashMap();
        phoneNumberUtil = PhoneNumberUtil.createInstance(getContext());
        rvStatus = view.findViewById(R.id.rvStatus);
        profile_image = view.findViewById(R.id.profile_image);
        chat_row_container = view.findViewById(R.id.chat_row_container);
        auth = FirebaseAuth.getInstance();
        currentUserStatus = new ArrayList<>();

        options = Options.init()
                .setRequestCode(100)
                .setCount(30)
                .setFrontfacing(false)
                .setImageQuality(ImageQuality.LOW)
                //.setPreSelectedUrls(selectedImages)
                .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT)
                .setPath("/akshay/new");

        chat_row_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(currentUserStatus.size()>0){
                    Intent intent = new Intent(getContext(), StoryActivity.class);
                    intent.putStringArrayListExtra("currentUserStatus",currentUserStatus);
                    startActivity(intent);
                }else{
                    Pix.start(StatusFragment.this, options);

                }

                //options.setPreSelectedUrls(selectedImages);
            }
        });
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PermUtil.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Pix.start(StatusFragment.this, options);
                } else {
                    Toast.makeText(getContext(), "Approve permissions to open Pix ImagePicker", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    private void getMyStatus() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Stories").child(auth.getCurrentUser().getUid());

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){

                    int count=0;
                    currentUserStatus.clear();

                    for(DataSnapshot child : dataSnapshot.getChildren()){

                        if(count ==0) Picasso.get().load(child.getValue().toString()).placeholder(R.drawable.pic).into(profile_image);
                        count=count+1;
                        currentUserStatus.add(child.getValue().toString());
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

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

    private void getContactsStatus() {
        getContactList();



    }

    private void getContactList(){

        Cursor phones = getContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while(phones.moveToNext()){
            //String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            phone = phone.replace(" ", "");
            phone = phone.replace("-", "");
            phone = phone.replace("(", "");
            phone = phone.replace(")", "");

            if(String.valueOf(phone.charAt(0)).equals("+"))
                phone = extractCountryCodeFromPhoneNumber(phone);

            contactsPhoneNumbers.add(phone);
        }

        for (String contactsPhoneNumber : contactsPhoneNumbers) {
            checkAndGetUserDetails(contactsPhoneNumber);
        }

    }

    private String extractCountryCodeFromPhoneNumber(String phoneNumber){

        long nationalNumber = 0;

        try {
            // phone must begin with '+'
            Phonenumber.PhoneNumber numberProto = phoneNumberUtil.parse(phoneNumber, "");
            int countryCode = numberProto.getCountryCode();
            nationalNumber = numberProto.getNationalNumber();
            Log.i("code", "code " + countryCode);
            Log.i("code", "national number " + nationalNumber);
        } catch (NumberParseException e) {
            System.err.println("NumberParseException was thrown: " + e.toString());
        }

        return String.valueOf(nationalNumber);
    }

    private void checkAndGetUserDetails(String phoneNumber) {
        contactStatusImages.clear();
        DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference().child("Users");
        Query query = mUserDB.orderByChild("phone").equalTo(phoneNumber);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){

                    for(DataSnapshot childSnapshot : dataSnapshot.getChildren()) {

                        String userId = "";
                        String d = childSnapshot.getKey();
                        String e = auth.getCurrentUser().getUid();

                        userId = childSnapshot.getKey();
                        final String name = childSnapshot.child("name").getValue().toString();

                        if (!childSnapshot.getKey().equals(auth.getCurrentUser().getUid())) {

                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Stories").child(userId);

                            ref.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.exists()) {


                                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                                            contactStatusImages.add(child.getValue().toString());
                                        }

                                        Status status = new Status(contactStatusImages, name);
                                        contactsStatusList.add(status);
                                        adapter.notifyDataSetChanged();
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setAdapter() {

        rvStatus.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new StatusAdapter(getContext(), contactsStatusList);
        rvStatus.setAdapter(adapter);

    }

}
