package com.timeandtidestudio.emergencybroadcast.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.timeandtidestudio.emergencybroadcast.Model.EmergencyContact;
import com.timeandtidestudio.emergencybroadcast.R;

import java.util.ArrayList;

/**
 * Created by User on 9/25/2017.
 */

public class ContactListAdapter extends BaseAdapter {

    public ArrayList<EmergencyContact> contactList;
    Context mCtx; //mengenali adapter dipanggil di Activity

    public ContactListAdapter(Context ctx, ArrayList<EmergencyContact> contactList) {
        mCtx = ctx;
        this.contactList = contactList;
    }

    @Override
    public int getCount() {
        return contactList.size();
    }

    @Override
    public EmergencyContact getItem(int i) {
        return contactList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) mCtx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.row_contact_list, viewGroup, false);

        TextDrawable drawable = TextDrawable.builder()
                .buildRect(getItem(i).initial, Color.RED);

        ImageView image = (ImageView) rowView.findViewById(R.id.ivProfile);
        image.setImageDrawable(drawable);
        TextView tvName = (TextView) rowView.findViewById(R.id.tvName);
        tvName.setText(getItem(i).name);
        TextView tvContent = (TextView) rowView.findViewById(R.id.tvPhoneNumber);
        tvContent.setText(getItem(i).phone);
        return rowView;
    }
}