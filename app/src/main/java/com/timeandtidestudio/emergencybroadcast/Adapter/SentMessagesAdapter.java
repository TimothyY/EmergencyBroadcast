package com.timeandtidestudio.emergencybroadcast.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.timeandtidestudio.emergencybroadcast.Model.SentMessage;
import com.timeandtidestudio.emergencybroadcast.R;

import java.util.ArrayList;

/**
 * Created by User on 9/25/2017.
 */

public class SentMessagesAdapter extends BaseAdapter {

    ArrayList<SentMessage> sentMessages;
    Context mCtx; //mengenali adapter dipanggil di Activity

    public SentMessagesAdapter(Context ctx, ArrayList<SentMessage> sentMessages) {
        mCtx = ctx;
        this.sentMessages = sentMessages;
    }

    @Override
    public int getCount() {
        return sentMessages.size();
    }

    @Override
    public SentMessage getItem(int i) {
        return sentMessages.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) mCtx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.row_sent_messages, viewGroup, false);
        TextView tvDate = (TextView) rowView.findViewById(R.id.tvDate);
        tvDate.setText(getItem(i).date);
        TextView tvMessage = (TextView) rowView.findViewById(R.id.tvMessage);
        tvMessage.setText(getItem(i).message);
        return rowView;
    }
}