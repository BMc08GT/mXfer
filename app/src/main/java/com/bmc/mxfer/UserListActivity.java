package com.bmc.mxfer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.bmc.mxfer.adapter.UserListAdapter;
import com.bmc.mxfer.manager.UserListManager;
import com.bmc.mxfer.model.User;
import com.bmc.mxfer.service.GetUsersService;

import java.util.ArrayList;


public class UserListActivity extends ActionBarActivity {

    private static final String TAG = UserListActivity.class.getSimpleName();

    private ArrayList<User> users;

    private RecyclerView mRecyclerView;
    private UserListAdapter mAdapter;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(GetUsersService.ACTION_CHECK_FINISHED)) {
                users = (ArrayList<User>) intent.getSerializableExtra("users");
                setupViews();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.user_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    public void onResume() {
        super.onResume();

        registerReceivers();
        getUsers();
    }

    private void setupViews() {
        if (mAdapter == null) {
            mAdapter = new UserListAdapter(users, R.layout.user_list_item, this);
        }
        mRecyclerView.setAdapter(mAdapter);
    }

    private void registerReceivers() {
        IntentFilter filter = new IntentFilter(GetUsersService.ACTION_CHECK_FINISHED);
        registerReceiver(mReceiver, filter);
    }

    private void getUsers() {
        Intent i = new Intent(this, GetUsersService.class);
        i.setAction(GetUsersService.ACTION_CHECK);
        startService(i);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
