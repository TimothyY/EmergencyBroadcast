package com.timeandtidestudio.emergencybroadcast;

import android.app.PendingIntent;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.timeandtidestudio.emergencybroadcast.Controller.utils.PreferencesHelper;
import com.timeandtidestudio.emergencybroadcast.Database.ContactsDAO;
import com.timeandtidestudio.emergencybroadcast.Database.MessagesDAO;
import com.timeandtidestudio.emergencybroadcast.Model.SentMessage;

import java.util.ArrayList;
import java.util.List;

public class CreateMessageActivity extends AppCompatActivity implements View.OnClickListener {

    EditText etCompose;
    Button btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_message);

        etCompose = (EditText)findViewById(R.id.etMessageDraft);
        btnSend = (Button)findViewById(R.id.btnSend);
        btnSend.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        sendManualBroadcast(etCompose.getText().toString());
        Toast.makeText(this, getString(R.string.send_success), Toast.LENGTH_LONG).show();
    }

    public void sendManualBroadcast(String message) {

        ContactsDAO contactsDAO = new ContactsDAO();
        ArrayList<String> phones =contactsDAO.loadNumbers(this);

        SmsManager sms = SmsManager.getDefault();
        for (String phone:phones) {
            // if message length is too long messages are divided
            List<String> messages = sms.divideMessage(message);
            for (String msg : messages) {
                PendingIntent sentIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_SENT"), 0);
                PendingIntent deliveredIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_DELIVERED"), 0);
                sms.sendTextMessage(phone, null, msg, sentIntent, deliveredIntent);
            }
        }

        MessagesDAO messagesDAO = new MessagesDAO();
        messagesDAO.saveSentMessage(getApplicationContext(),new SentMessage(""+System.currentTimeMillis(),PreferencesHelper.getString(PreferencesHelper.USER_MESSAGE)));
    }
}
