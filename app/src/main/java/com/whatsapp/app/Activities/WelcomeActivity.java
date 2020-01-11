package com.whatsapp.app.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;
import com.whatsapp.app.MainActivity;
import com.whatsapp.app.ProfileInfoActivity;
import com.whatsapp.app.R;
import com.whatsapp.app.VerifyPhoneActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by Mohamad Abdallah.
 */

public class WelcomeActivity extends AppCompatActivity{

    TextView agreeNContinueTVBtn;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        init();
    }

    private void init()
    {
        mAuth = FirebaseAuth.getInstance();
        agreeNContinueTVBtn = findViewById(R.id.agreeNContinueTVBtn);
        agreeNContinueTVBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeActivity.this, VerifyPhoneActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user already signed in
        if (mAuth.getCurrentUser() != null) {

            SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
            String name = prefs.getString("name", null);//"No name defined" is the default value.

            if(name !=null){

                if(!name.trim().isEmpty()){
                    startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                    finish();
                }else{
                    startActivity(new Intent(WelcomeActivity.this, ProfileInfoActivity.class));
                    finish();
                }
            }else{
                startActivity(new Intent(WelcomeActivity.this, ProfileInfoActivity.class));
                finish();
            }

        }
    }
}
