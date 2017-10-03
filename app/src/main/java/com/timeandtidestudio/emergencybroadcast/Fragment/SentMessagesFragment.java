package com.timeandtidestudio.emergencybroadcast.Fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.timeandtidestudio.emergencybroadcast.Adapter.SentMessagesAdapter;
import com.timeandtidestudio.emergencybroadcast.Model.SentMessage;
import com.timeandtidestudio.emergencybroadcast.R;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SentMessagesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SentMessagesFragment extends Fragment implements AdapterView.OnItemClickListener {

    ArrayList<SentMessage> sentMessages;

    public SentMessagesFragment() {
        // Required empty public constructor
    }

    public static SentMessagesFragment newInstance() {
        SentMessagesFragment fragment = new SentMessagesFragment();
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
        View view = inflater.inflate(R.layout.fragment_sent_messages, container, false);

        sentMessages = new ArrayList<>();
        sentMessages.add(new SentMessage("TITLE_SENT_MESSAGE_01",getString(R.string.lorem_ipsum),"xxx"));
        sentMessages.add(new SentMessage("TITLE_SENT_MESSAGE_02",getString(R.string.lorem_ipsum),"yyy"));

        Context context = view.getContext();
        ListView lvSentMessages = (ListView) view.findViewById(R.id.lvSentMessages);
        lvSentMessages.setAdapter(new SentMessagesAdapter(getActivity(),sentMessages)); //on real case, this will be rplaced with data from sqlite/internet
        lvSentMessages.setOnItemClickListener(this);
        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(getActivity(), ""+position, Toast.LENGTH_SHORT).show();
    }
}
