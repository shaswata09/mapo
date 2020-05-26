package com.example.souravdutta.dutta;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class share_location extends AppCompatActivity {

    private Button sendBtn;
    private EditText txtphoneNo;
    private EditText txtMessage;
    private String phoneNo;
    private String message;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_location);


        sendBtn = findViewById(R.id.share_btn);
        txtphoneNo = findViewById(R.id.ph_num);
        txtMessage = findViewById(R.id.message);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                sendSMSMessage();
            }

        });

    }

    private void sendSMSMessage() {
        GetCurrentGPSLocation gps = new GetCurrentGPSLocation(this);

        phoneNo = txtphoneNo.getText().toString();
        message = txtMessage.getText().toString();

        TextView lati = findViewById(R.id.lat);
        TextView lngi = findViewById(R.id.lng);
        TextView fmsg = findViewById(R.id.msg);

        String lat = Double.toString(gps.getLatitude());
        String lng = Double.toString(gps.getLongitude());
        lati.setText("Your latitude: " + lat);
        lngi.setText("Your longitude: " + lng);


        message = "Mapo@" + lat + "," + lng + ":" + message;

        fmsg.setText("Your Sent Message: "+message);

        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNo, null, message, null, null);
        Toast.makeText(getApplicationContext(), "Location sent.", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

