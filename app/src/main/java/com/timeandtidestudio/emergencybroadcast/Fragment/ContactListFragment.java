package com.timeandtidestudio.emergencybroadcast.Fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.timeandtidestudio.emergencybroadcast.Adapter.ContactListAdapter;
import com.timeandtidestudio.emergencybroadcast.Adapter.SentMessagesAdapter;
import com.timeandtidestudio.emergencybroadcast.Model.EmergencyContact;
import com.timeandtidestudio.emergencybroadcast.Model.SentMessage;
import com.timeandtidestudio.emergencybroadcast.R;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ContactListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ContactListFragment extends Fragment implements AdapterView.OnItemClickListener {

    ArrayList<EmergencyContact> contactList;

    public ContactListFragment() {
        // Required empty public constructor
    }

    public static ContactListFragment newInstance() {
        ContactListFragment fragment = new ContactListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact_list, container, false);

        contactList = new ArrayList<>();
        contactList.add(new EmergencyContact("Steve Roger","content01"));
        contactList.add(new EmergencyContact("Steve","content02"));

        Context context = view.getContext();
        ListView lvContactList = (ListView) view.findViewById(R.id.lvContactList);
        lvContactList.setAdapter(new ContactListAdapter(getActivity(), contactList)); //on real case, this will be rplaced with data from sqlite/internet
        lvContactList.setOnItemClickListener(this);
        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(getActivity(), contactList.get(position).initial, Toast.LENGTH_SHORT).show();
    }
}
