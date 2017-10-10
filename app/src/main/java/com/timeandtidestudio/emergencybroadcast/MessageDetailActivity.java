package com.timeandtidestudio.emergencybroadcast;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.timeandtidestudio.emergencybroadcast.Model.SentMessage;

public class MessageDetailActivity extends AppCompatActivity {

    SentMessage sentMessage;
    TextView tvDate, tvHour, tvMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_detail);
        sentMessage = (SentMessage) getIntent().getExtras().get("clickedSentMessage");
        tvDate = (TextView) findViewById(R.id.tvDate);
        tvDate.setText(sentMessage.date);
        tvHour = (TextView) findViewById(R.id.tvHour);
        tvHour.setText(sentMessage.hour);
        tvMessage = (TextView) findViewById(R.id.tvMessage);
        tvMessage.setText(sentMessage.message);
    }
}
