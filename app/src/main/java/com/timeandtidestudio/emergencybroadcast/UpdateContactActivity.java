package com.timeandtidestudio.emergencybroadcast;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.timeandtidestudio.emergencybroadcast.Database.ContactsDAO;
import com.timeandtidestudio.emergencybroadcast.Model.EmergencyContact;
import com.timeandtidestudio.emergencybroadcast.Model.SentMessage;

public class UpdateContactActivity extends AppCompatActivity implements View.OnClickListener {

    EmergencyContact contact;
    EditText etName,etPhone;
    Button btnSaveContact;
    int contactId;

    Context mCtx;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_contact);
        mCtx = this;

        try {
            contactId = getIntent().getExtras().getInt("contactId");
        }catch (Exception e){
            e.printStackTrace();
        }
        etName = (EditText) findViewById(R.id.etName);
        etPhone = (EditText) findViewById(R.id.etPhoneNumber);
        btnSaveContact = (Button) findViewById(R.id.btnSave);
        btnSaveContact.setOnClickListener(this);
        try{
            contact = (EmergencyContact) getIntent().getExtras().get("selectedContact");
            etName.setText(contact.name);
            etPhone.setText(contact.phone);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    ContactsDAO contactsDAO;

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnSave:
//                if(contactId!=0){
//
//                }else{
//
//                }
                EmergencyContact emergencyContact =
                        new EmergencyContact(
                                contactId,
                                etName.getText().toString(),
                                etPhone.getText().toString());
                contactsDAO = new ContactsDAO();
                contactsDAO.saveEmergencyContact(mCtx,emergencyContact);
                Toast.makeText(mCtx, getString(R.string.contact_saved), Toast.LENGTH_SHORT).show();
                finish();
                break;
        }
    }
}
