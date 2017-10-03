package com.timeandtidestudio.emergencybroadcast;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.timeandtidestudio.emergencybroadcast.Fragment.ContactListFragment;
import com.timeandtidestudio.emergencybroadcast.Fragment.SentMessagesFragment;
import com.timeandtidestudio.emergencybroadcast.Fragment.UserProfileFragment;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.sentMessages:
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.content, SentMessagesFragment.newInstance(), "sentMessagesFragmentTag")
                            .commit();
                    fab.setImageResource(R.drawable.ic_create_white_24dp);
                    fab.setTag("message");
                    fab.setVisibility(View.VISIBLE);
                    return true;
                case R.id.emergencyContactList:
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.content, ContactListFragment.newInstance(), "contactListFragmentTag")
                            .commit();
                    fab.setImageResource(R.drawable.ic_person_add_white_24dp);
                    fab.setTag("contact");
                    fab.setVisibility(View.VISIBLE);
                    return true;
                case R.id.userProfile:
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.content, UserProfileFragment.newInstance(), "userProfileFragmentTag")
                            .commit();
                    fab.setTag("");
                    fab.setVisibility(View.GONE);
                    return true;
            }
            return false;
        }

    };

}
