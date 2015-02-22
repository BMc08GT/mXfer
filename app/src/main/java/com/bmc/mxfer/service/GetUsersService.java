package com.bmc.mxfer.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.bmc.mxfer.model.User;
import com.bmc.mxfer.util.HttpRequestExecutor;
import com.bmc.mxfer.util.Utils;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

public class GetUsersService extends IntentService {

    private static final String TAG = GetUsersService.class.getSimpleName();

    public static final String ACTION_CHECK_FINISHED = "com.bmc.mxfer.action.USER_CHECK_FINISHED";
    public static final String ACTION_CHECK = "com.bmc.mxfer.action.CHECK";

    private HttpRequestExecutor mHttpExecutor;

    private SharedPreferences mPrefs;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */
    public GetUsersService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        synchronized (this) {
            mHttpExecutor = new HttpRequestExecutor();
        }

        Intent finishedIntent = new Intent(ACTION_CHECK_FINISHED);
        ArrayList<User> users;
        if (Utils.isConnected(this)) {
            // User is connected so pull users from json
            try {
                users = getCurrentUsersAndFillIntent(finishedIntent);
            } catch (IOException io) {
                Log.d(TAG, "Could not check for users", io);
                users = null;
            }

            if (users == null || mHttpExecutor.isAborted()) {
                sendBroadcast(finishedIntent);
                return;
            }
        }
        sendBroadcast(finishedIntent);
    }

    // HttpRequestExecutor.abort() may cause network activity, which must not happen in the
    // main thread. Spawn off the cleanup into a separate thread to avoid crashing due to
    // NetworkOnMainThreadException.
    private void cleanupHttpExecutor(final HttpRequestExecutor executor) {
        final Thread abortThread = new Thread(new Runnable() {
            @Override
            public void run() {
                executor.abort();
            }
        });
        abortThread.start();
    }

    private void addRequestHeaders(HttpRequestBase request) {
        String userAgent = Utils.getUserAgentString(this);
        if (userAgent != null) {
            request.addHeader("User-Agent", userAgent);
        }
        request.addHeader("Cache-Control", "no-cache");
    }

    private ArrayList<User> getCurrentUsersAndFillIntent(Intent intent) throws IOException {
        URI serverUri = URI.create("http://xfer.aokp.co/romlistings.py");
        HttpPost request = new HttpPost(serverUri);

        addRequestHeaders(request);

        HttpEntity entity = mHttpExecutor.execute(request);
        if (entity == null || mHttpExecutor.isAborted()) {
            return null;
        }

        String json = EntityUtils.toString(entity, "UTF-8");
        ArrayList<User> users = parseJSON(json);

        if (mHttpExecutor.isAborted()) {
            Log.d(TAG, "getUsers - HttpExecutor is aborted");
            return null;
        }

        // Create a bundle to pass back to MainActivity
        Bundle extras = new Bundle();
        extras.putSerializable("users", users);
        extras.putInt("size", users.size());

        intent.putExtras(extras);
        return users;
    }

    private ArrayList<User> parseJSON(String jsonString) {
        ArrayList<User> users = new ArrayList<>();
        try {
            if (jsonString != null) {
                JSONObject result = new JSONObject(jsonString);
                JSONArray userList = result.getJSONArray("users");
                int userSize = userList.length();

                Log.d(TAG, "Got user list with " + userSize + " entries");

                for (int i = 0; i < userSize; i++) {
                    if (mHttpExecutor.isAborted()) {
                        Log.d(TAG, "ParseJson - HttpExecutor aborted");
                        break;
                    }
                    if (userList.isNull(i)) {
                        continue;
                    }
                    JSONObject item = userList.getJSONObject(i);
                    User user = parseUserJSONObject(item);
                    if (user != null) {
                        users.add(user);
                    }
                }
            }
        }  catch (JSONException e) {
            Log.e(TAG, "Error in json result", e);
        }
        return users;
    }

    private User parseUserJSONObject(JSONObject item) throws JSONException {
        return new User.Builder()
                .setName(item.getString("name"))
                .build();
    }
}
