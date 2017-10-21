package com.timeandtidestudio.emergencybroadcast.Fragment;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.timeandtidestudio.emergencybroadcast.Adapter.ContactListAdapter;
import com.timeandtidestudio.emergencybroadcast.Adapter.SentMessagesAdapter;
import com.timeandtidestudio.emergencybroadcast.Database.ContactsDAO;
import com.timeandtidestudio.emergencybroadcast.Model.EmergencyContact;
import com.timeandtidestudio.emergencybroadcast.Model.SentMessage;
import com.timeandtidestudio.emergencybroadcast.R;
import com.timeandtidestudio.emergencybroadcast.UpdateContactActivity;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ContactListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ContactListFragment extends Fragment implements AdapterView.OnItemClickListener {

    public ContactListFragment() {
        // Required empty public constructor
    }

    public static ContactListFragment newInstance() {
        ContactListFragment fragment = new ContactListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    Context mCtx;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {}
        mCtx = getActivity();
    }

    ArrayList<EmergencyContact> contactList;
    ContactListAdapter contactListAdapter;
    ListView lvContactList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact_list, container, false);

        mCtx = getActivity();
        lvContactList = (ListView) view.findViewById(R.id.lvContactList);
        refreshList();
        lvContactList.setAdapter(contactListAdapter); //on real case, this will be rplaced with data from sqlite/internet
        lvContactList.setOnItemClickListener(this);
        return view;
    }


    ContactsDAO contactsDAO;
    @Override
    public void onResume() {
        super.onResume();
        refreshList();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent toUpdateContactActivity = new Intent(mCtx, UpdateContactActivity.class);
        toUpdateContactActivity.putExtra("selectedContact",contactList.get(position));
        startActivity(toUpdateContactActivity);
    }

    public void refreshList(){
        contactsDAO = new ContactsDAO();
        contactList = contactsDAO.loadContacts(mCtx);
        contactListAdapter = new ContactListAdapter(mCtx, contactList);
        contactListAdapter.notifyDataSetChanged();
        lvContactList.setAdapter(contactListAdapter);
    }
}
