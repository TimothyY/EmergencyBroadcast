package com.timeandtidestudio.emergencybroadcast;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.timeandtidestudio.emergencybroadcast.Model.SentMessage;

import org.joda.time.DateTime;

public class MessageDetailActivity extends AppCompatActivity {

    SentMessage sentMessage;
    TextView tvDate, tvHour, tvMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_message_detail);
        sentMessage = (SentMessage) getIntent().getExtras().get("clickedSentMessage");
        DateTime dt = new DateTime(Long.parseLong(sentMessage.date));
        TextView tvDate = (TextView) findViewById(R.id.tvDate);
        tvDate.setText(dt.toLocalDate().toString());
        TextView tvHour = (TextView) findViewById(R.id.tvHour);
        tvHour.setText(dt.toLocalTime().toString("HH:mm"));
        tvMessage = (TextView) findViewById(R.id.tvMessage);
        tvMessage.setText(sentMessage.message);
    }
}
