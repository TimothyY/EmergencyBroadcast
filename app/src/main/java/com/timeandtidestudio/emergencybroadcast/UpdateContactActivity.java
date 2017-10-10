package com.timeandtidestudio.emergencybroadcast;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.timeandtidestudio.emergencybroadcast.Model.EmergencyContact;

public class UpdateContactActivity extends AppCompatActivity implements View.OnClickListener {

    EmergencyContact contact;
    EditText etName,etPhone;
    Button btnSaveContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_contact);
        etName = (EditText) findViewById(R.id.etName);
        etPhone = (EditText) findViewById(R.id.etPhoneNumber);
        btnSaveContact = (Button) findViewById(R.id.btnSave);
        btnSaveContact.setOnClickListener(this);
        try{
            contact = (EmergencyContact) getIntent().getExtras().get("selectedContact");
            etName.setText(contact.name);
            Log.v("kyaa",contact.name);
            etPhone.setText(contact.phone);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {

    }
}
