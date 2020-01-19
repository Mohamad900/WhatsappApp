package com.whatsapp.app.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.hololo.library.otpview.OTPListener;
import com.hololo.library.otpview.OTPView;
import com.whatsapp.app.R;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class VerifyPhoneActivity extends AppCompatActivity
{
    private Button SendVerificationCodeButton;
    private EditText InputPhoneNumber,countryCodeET;
    OTPView InputVerificationCode;
    TextView carrierChargesTV,verifyTV,smsTV,verifySmsTV,resend_timer;
    TextView countriesSpinner;
    LinearLayout phoneRL;
    TextView digit_code;
    RelativeLayout resend;
    LinearLayout resendCall;
    String fullNumber;
    ImageView imgSMS;
    TextView resend_text;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private FirebaseAuth mAuth;

    private ProgressDialog loadingBar;

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    String phoneNumber;
    String countryCode;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        mAuth = FirebaseAuth.getInstance();

        SendVerificationCodeButton = (Button) findViewById(R.id.send_ver_code_button);
        verifyTV = findViewById(R.id.verifyTV);
        imgSMS = findViewById(R.id.imgSMS);
        resend_text = findViewById(R.id.resend_text);
        resend_timer =findViewById(R.id.resend_timer);
        resendCall = findViewById(R.id.resendCall);
        resend = findViewById(R.id.resend);
        digit_code = findViewById(R.id.digit_code);
        smsTV =  findViewById(R.id.smsTV);
        verifySmsTV = findViewById(R.id.verifySmsTV);
        carrierChargesTV = findViewById(R.id.carrierChargesTV);
        InputPhoneNumber = findViewById(R.id.phone_nnumber_input);
        countryCodeET = findViewById(R.id.countryCodeET);
        InputVerificationCode = findViewById(R.id.verification_code_input);
        countriesSpinner = findViewById(R.id.countries);
        phoneRL = findViewById(R.id.phoneRL);
        loadingBar = new ProgressDialog(this);

        resend.setEnabled(false);


        InputVerificationCode.setListener(new OTPListener() {
            @Override
            public void otpFinished(String otp) {
                loadingBar.setTitle("");
                loadingBar.setMessage("Verifying...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, otp);
                signInWithPhoneAuthCredential(credential);
            }
        });

        countriesSpinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VerifyPhoneActivity.this, CountryActivity.class);
                startActivityForResult(intent, 1);
            }
        });


        resend.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                //Toast.makeText(VerifyPhoneActivity.this,"p",Toast.LENGTH_SHORT).show();
                resend.setEnabled(false);
                resend_text.setTextColor(getResources().getColor(R.color.grey));
                imgSMS.setBackgroundTintList(getResources().getColorStateList(R.color.grey));
                loadingBar.setMessage("Sending SMS...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        fullNumber,        // Phone number to verify
                        60,                 // Timeout duration
                        TimeUnit.SECONDS,   // Unit of timeout
                        VerifyPhoneActivity.this,               // Activity (for callback binding)
                        callbacks);        // OnVerificationStateChangedCallbacks
            }
        });

        SendVerificationCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                countryCode = countryCodeET.getText().toString();
                phoneNumber = InputPhoneNumber.getText().toString();
                fullNumber = countryCode + phoneNumber;

                if (TextUtils.isEmpty(countryCode))
                {
                    Toast.makeText(VerifyPhoneActivity.this, "Please enter your country code", Toast.LENGTH_SHORT).show();

                }else if(TextUtils.isEmpty(phoneNumber)){

                    Toast.makeText(VerifyPhoneActivity.this, "Please enter your phone number", Toast.LENGTH_SHORT).show();

                }else
                {
                    loadingBar.setMessage("Connecting...");
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

                InputVerificationCode.setVisibility(View.INVISIBLE);
                verifySmsTV.setVisibility(View.INVISIBLE);
                digit_code.setVisibility(View.INVISIBLE);
                resendCall.setVisibility(View.INVISIBLE);

            }

            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token)
            {
                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                loadingBar.dismiss();
                //Toast.makeText(VerifyPhoneActivity.this, "Code has been sent, please check and verify...", Toast.LENGTH_SHORT).show();

                SendVerificationCodeButton.setVisibility(View.INVISIBLE);
                phoneRL.setVisibility(View.INVISIBLE);
                countriesSpinner.setVisibility(View.INVISIBLE);
                carrierChargesTV.setVisibility(View.INVISIBLE);
                smsTV.setVisibility(View.INVISIBLE);

                InputVerificationCode.setVisibility(View.VISIBLE);
                verifySmsTV.setVisibility(View.VISIBLE);
                digit_code.setVisibility(View.VISIBLE);
                resendCall.setVisibility(View.VISIBLE);
                verifySmsTV.setText("Waiting to automatically detect an SMS sent to "+countryCode+ phoneNumber +".");
                startCountDownTimer();
            }
        };
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == RESULT_OK) {
                String code = data.getStringExtra("countryCode");
                String name = data.getStringExtra("countryName");

                countriesSpinner.setText(name);
                countryCodeET.setText("+ "+code);

            }
        }
    }

    private void startCountDownTimer(){

        new CountDownTimer(60000, 1000) {

            public void onTick(long millisUntilFinished) {
                resend_timer.setText("00:"+millisUntilFinished / 1000);
                //here you can have your logic to set text to edittext
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            public void onFinish() {
                //mTextField.setText("done!");
                resend.setEnabled(true);
                resend_text.setTextColor(getResources().getColor(R.color.colorPrimary));
                imgSMS.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));

            }

        }.start();

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
                                                Toast.makeText(VerifyPhoneActivity.this, "User Created Successfully...", Toast.LENGTH_SHORT).show();
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

        SharedPreferences.Editor editor = getSharedPreferences("UserProfile", MODE_PRIVATE).edit();
        editor.putString("phoneNumber", phoneNumber);
        editor.apply();

        Intent mainIntent = new Intent(VerifyPhoneActivity.this, ProfileInfoActivity.class);
        startActivity(mainIntent);
        finish();
    }
}