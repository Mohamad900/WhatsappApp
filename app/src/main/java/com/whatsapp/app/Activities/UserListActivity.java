package com.whatsapp.app.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.michaelrocks.libphonenumber.android.NumberParseException;
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil;
import io.michaelrocks.libphonenumber.android.Phonenumber;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.whatsapp.app.Adapters.UserListAdapter;
import com.whatsapp.app.Models.User;
import com.whatsapp.app.R;

import java.util.ArrayList;

public class UserListActivity extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView usersRecyclerView;
    ArrayList<User> usersList;
    UserListAdapter mUserListAdapter;
    PhoneNumberUtil phoneNumberUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        initViews();
    }

    private void initViews()
    {
        initToolbar();
        getContactList();
    }

    private void initToolbar() {
        toolbar =findViewById(R.id.toolbar);
        usersRecyclerView =findViewById(R.id.usersRecyclerView);
        usersList= new ArrayList<>();
        mUserListAdapter = new UserListAdapter(this,usersList);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        usersRecyclerView.setAdapter(mUserListAdapter);
        phoneNumberUtil = PhoneNumberUtil.createInstance(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_list, menu);
        return true;
    }

    private void getContactList(){

        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while(phones.moveToNext()){
            //String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            phone = phone.replace(" ", "");
            phone = phone.replace("-", "");
            phone = phone.replace("(", "");
            phone = phone.replace(")", "");

            if(String.valueOf(phone.charAt(0)).equals("+"))
                phone = extractCountryCodeFromPhoneNumber(phone);

            checkAndGetUserDetails(phone);
        }
    }

    private void checkAndGetUserDetails(String phoneNumber) {
        DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference().child("Users");
        Query query = mUserDB.orderByChild("phone").equalTo(phoneNumber);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){

                    String phone="",name="",profileImage="";

                    for(DataSnapshot childSnapshot : dataSnapshot.getChildren()){

                        if(childSnapshot.child("phone").getValue()!= null)
                            phone = childSnapshot.child("phone").getValue().toString();

                        if(childSnapshot.child("name").getValue()!=null)
                            name = childSnapshot.child("name").getValue().toString();

                        if(childSnapshot.child("image").getValue()!=null)
                            profileImage = childSnapshot.child("image").getValue().toString();

                        User mUser = new User(childSnapshot.getKey(), name, phone,profileImage);

                        usersList.add(mUser);
                        mUserListAdapter.notifyDataSetChanged();
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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


}
