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
    boolean isUpdate = false; //false means it should do insert operation rather than update operation

    Context mCtx;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_contact);
        mCtx = this;

        etName = (EditText) findViewById(R.id.etName);
        etPhone = (EditText) findViewById(R.id.etPhoneNumber);
        btnSaveContact = (Button) findViewById(R.id.btnSave);
        btnSaveContact.setOnClickListener(this);
        try{
            contact = (EmergencyContact) getIntent().getExtras().get("selectedContact");
            etName.setText(contact.name);
            etPhone.setText(contact.phone);
            isUpdate = true;
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    ContactsDAO contactsDAO;

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnSave:
                EmergencyContact emergencyContact =
                        new EmergencyContact(
                                contactId,
                                etName.getText().toString(),
                                etPhone.getText().toString());
                contactsDAO = new ContactsDAO();
                if(isUpdate){
                    contactsDAO.updateEmergencyContact(mCtx,contact.id,etName.getText().toString(),etPhone.getText().toString());
                    Log.v("UpCon","update contact dipanggil");
                }else{
                    contactsDAO.insertEmergencyContact(mCtx,etName.getText().toString(),etPhone.getText().toString());
                    Log.v("UpCon","insert contact dipanggil");
                }
                Toast.makeText(mCtx, getString(R.string.contact_saved), Toast.LENGTH_SHORT).show();
                finish();
                break;
        }
    }
}
