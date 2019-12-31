package com.whatsapp.app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.whatsapp.app.Models.Country;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class VerifyPhoneActivity extends AppCompatActivity
{
    private Button SendVerificationCodeButton, VerifyButton;
    private EditText InputPhoneNumber, InputVerificationCode,countryCodeET;
    TextView carrierChargesTV,verifyTV,smsTV,verifySmsTV;
    Spinner countriesSpinner;
    RelativeLayout phoneRL;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private FirebaseAuth mAuth;

    private ProgressDialog loadingBar;

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private String countriesUrl = "https://restcountries.eu/rest/v2/all";
    List<Country> CountryList;
    List<String> CountriesName;
    ArrayAdapter<String> adapter;
    String phoneNumber;
    String countryCode;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);


        mAuth = FirebaseAuth.getInstance();
        CountryList = new ArrayList<>();
        CountriesName = new ArrayList<>();

        SendVerificationCodeButton = (Button) findViewById(R.id.send_ver_code_button);
        VerifyButton = (Button) findViewById(R.id.verify_button);
        verifyTV = findViewById(R.id.verifyTV);
        smsTV =  findViewById(R.id.smsTV);
        verifySmsTV = findViewById(R.id.verifySmsTV);
        carrierChargesTV = findViewById(R.id.carrierChargesTV);
        InputPhoneNumber = findViewById(R.id.phone_nnumber_input);
        countryCodeET = findViewById(R.id.countryCodeET);
        InputVerificationCode = (EditText) findViewById(R.id.verification_code_input);
        countriesSpinner = findViewById(R.id.countries);
        phoneRL = findViewById(R.id.phoneRL);
        loadingBar = new ProgressDialog(this);

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,CountriesName);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countriesSpinner.setAdapter(adapter);

        countriesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Country country= CountryList.get(i);
                countryCodeET.setText("+" + country.getCode());
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        GetCountriesAndCodes();

        SendVerificationCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                countryCode = countryCodeET.getText().toString();
                phoneNumber = InputPhoneNumber.getText().toString();
                String fullNumber = countryCode + phoneNumber;

                if (TextUtils.isEmpty(countryCode))
                {
                    Toast.makeText(VerifyPhoneActivity.this, "Please enter your country code", Toast.LENGTH_SHORT).show();

                }else if(TextUtils.isEmpty(phoneNumber)){

                    Toast.makeText(VerifyPhoneActivity.this, "Please enter your phone number", Toast.LENGTH_SHORT).show();

                }else
                {
                    loadingBar.setTitle("Phone Verification");
                    loadingBar.setMessage("please wait, while we are authenticating your phone...");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            fullNumber,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            VerifyPhoneActivity.this,               // Activity (for callback binding)
                            callbacks);        // OnVerificationStateChangedCallbacks
                }
            }
        });


        VerifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                SendVerificationCodeButton.setVisibility(View.INVISIBLE);
                InputPhoneNumber.setVisibility(View.INVISIBLE);

                String verificationCode = InputVerificationCode.getText().toString();

                if (TextUtils.isEmpty(verificationCode))
                {
                    Toast.makeText(VerifyPhoneActivity.this, "Please write verification code first...", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    loadingBar.setTitle("Verification Code");
                    loadingBar.setMessage("please wait, while we are verifying verification code...");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });


        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential)
            {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e)
            {
                loadingBar.dismiss();
                Toast.makeText(VerifyPhoneActivity.this, "Invalid Phone Number, Please enter correct phone number with your country code...", Toast.LENGTH_SHORT).show();

                SendVerificationCodeButton.setVisibility(View.VISIBLE);
                phoneRL.setVisibility(View.VISIBLE);
                countriesSpinner.setVisibility(View.VISIBLE);
                carrierChargesTV.setVisibility(View.VISIBLE);
                smsTV.setVisibility(View.VISIBLE);

                VerifyButton.setVisibility(View.INVISIBLE);
                InputVerificationCode.setVisibility(View.INVISIBLE);
                verifySmsTV.setVisibility(View.INVISIBLE);

            }

            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token)
            {
                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                loadingBar.dismiss();
                Toast.makeText(VerifyPhoneActivity.this, "Code has been sent, please check and verify...", Toast.LENGTH_SHORT).show();

                SendVerificationCodeButton.setVisibility(View.INVISIBLE);
                phoneRL.setVisibility(View.INVISIBLE);
                countriesSpinner.setVisibility(View.INVISIBLE);
                carrierChargesTV.setVisibility(View.INVISIBLE);
                smsTV.setVisibility(View.INVISIBLE);

                VerifyButton.setVisibility(View.VISIBLE);
                InputVerificationCode.setVisibility(View.VISIBLE);
                verifySmsTV.setVisibility(View.VISIBLE);

            }
        };
    }

    private void GetCountriesAndCodes() {
        JsonArrayRequest req = new JsonArrayRequest(countriesUrl, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                        //if (progressDialog.isShowing())
                        //    progressDialog.dismiss();

                        // Parsing json response and iterate over each JSON object
                        //Log.d(TAG, "data:" + responseText);
                        try {

                            for (int i = 0; i < response.length(); i++) {

                                JSONObject jsonobject = response.getJSONObject(i);
                                String name = jsonobject.getString("name");
                                CountriesName.add(name);
                                JSONArray codes = jsonobject.getJSONArray("callingCodes");

                                String code="";
                                if(codes.length ()> 0){
                                    for(int j = 0;j<codes.length();j++){

                                        code = codes.getString(j);
                                        break;
                                    }
                                }
                                Country countryObj = new Country(name , code);
                                CountryList.add(countryObj);
                            }
                            adapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(),
                                    "Error: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }

                    }
                },
                new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(VerifyPhoneActivity.this,error.getMessage(),Toast.LENGTH_LONG).show();
                        }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(req);
    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {

                            HashMap<String, Object> profileMap = new HashMap<>();
                            profileMap.put("uid", task.getResult().getUser().getUid());
                            profileMap.put("phone", phoneNumber);
                            profileMap.put("countryCode", countryCode);
                            FirebaseDatabase.getInstance().getReference().child("Users").child(task.getResult().getUser().getUid()).updateChildren(profileMap)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                loadingBar.dismiss();
                                                Toast.makeText(VerifyPhoneActivity.this, "Profile Created Successfully...", Toast.LENGTH_SHORT).show();
                                                SendUserToProfileInfoActivity();
                                            }
                                            else
                                            {
                                                loadingBar.dismiss();
                                                String message = task.getException().toString();
                                                Toast.makeText(VerifyPhoneActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                        else
                        {
                            loadingBar.dismiss();
                            String message = task.getException().toString();
                            Toast.makeText(VerifyPhoneActivity.this, "Error : "  +  message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }




    private void SendUserToProfileInfoActivity()
    {
        Intent mainIntent = new Intent(VerifyPhoneActivity.this, ProfileInfoActivity.class);
        startActivity(mainIntent);
        finish();
    }
}