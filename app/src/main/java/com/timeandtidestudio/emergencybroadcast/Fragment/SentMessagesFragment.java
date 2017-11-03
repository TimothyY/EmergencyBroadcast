package com.timeandtidestudio.emergencybroadcast.Fragment;


import android.content.Context;
import android.content.Intent;
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
import com.timeandtidestudio.emergencybroadcast.Database.MessagesDAO;
import com.timeandtidestudio.emergencybroadcast.MessageDetailActivity;
import com.timeandtidestudio.emergencybroadcast.Model.SentMessage;
import com.timeandtidestudio.emergencybroadcast.R;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SentMessagesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SentMessagesFragment extends Fragment implements AdapterView.OnItemClickListener {

    Context mCtx;

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
        mCtx = getActivity();
    }

    ListView lvSentMessages;
    SentMessagesAdapter sentMessagesAdapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sent_messages, container, false);

        Context context = view.getContext();
        lvSentMessages = (ListView) view.findViewById(R.id.lvSentMessages);
        refreshList();
        lvSentMessages.setOnItemClickListener(this);
        return view;
    }

    ArrayList<SentMessage> sentMessages;
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent toDetailMessageIntent = new Intent(mCtx, MessageDetailActivity.class);
        toDetailMessageIntent.putExtra("clickedSentMessage",sentMessages.get(position));
        startActivity(toDetailMessageIntent);
    }

    MessagesDAO messagesDAO;
    public void refreshList(){
        messagesDAO = new MessagesDAO();
        sentMessages = messagesDAO.loadMessages(mCtx);
        sentMessagesAdapter = new SentMessagesAdapter(mCtx, sentMessages);
        sentMessagesAdapter.notifyDataSetChanged();
        lvSentMessages.setAdapter(sentMessagesAdapter);
    }
}
