package com.bmc.mxfer.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.bmc.mxfer.model.Device;
import com.bmc.mxfer.model.Rom;
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

                for (int i = 0; i < userList.length(); i++) {
                    if (mHttpExecutor.isAborted()) {
                        Log.d(TAG, "ParseJson - HttpExecutor aborted");
                        break;
                    }
                    if (userList.isNull(i)) {
                        continue;
                    }
                    JSONObject item = userList.getJSONObject(i);
                    // Get the devices for this user
                    Device[] devices = getDevicesForUser(item.getJSONArray("devices"));
                    // Create the user object
                    User user = parseUserJSONObject(item, devices);

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

    private User parseUserJSONObject(JSONObject item, Device[] devices) throws JSONException {
        return new User.Builder()
                .setName(item.getString("name"))
                .setGravatarEmail(item.getString("gravatar"))
                .setTwitterName(item.getString("twitter"))
                .setGithubUrl(item.getString("github"))
                .setDevices(devices)
                .build();
    }

    private Device parseDeviceJsonObject(JSONObject item, Rom[] roms) throws JSONException {
        return new Device.Builder()
                .setName(item.getString("codename"))
                .setRoms(roms)
                .build();
    }

    private Rom parseRomJsonObject(JSONObject item) throws JSONException {
        return new Rom.Builder()
                .setUrl(item.getString("url"))
                .setDate(item.getString("date"))
                .setFilename(item.getString("filename"))
                .setSize(item.getLong("size"))
                .build();
    }

    public Device[] getDevicesForUser(JSONArray deviceList) throws JSONException {
        ArrayList<Device> devices = new ArrayList<>();
        for (int i = 0; i < deviceList.length(); i++) {
            if (deviceList.isNull(i)) {
                continue;
            }
            JSONObject item = deviceList.getJSONObject(i);
            // Get the ROMs available for this device
            Rom[] roms = getRomsForDevices(item.getJSONArray("roms"));
            // Create the Device object
            Device device = parseDeviceJsonObject(item, roms);
            if (device != null) {
                devices.add(device);
            }
        }
        return devices.toArray(new Device[devices.size()]);
    }

    private Rom[] getRomsForDevices(JSONArray romList) throws JSONException {
        ArrayList<Rom> roms = new ArrayList<>();
        for (int i = 0; i < romList.length(); i++) {
            if (romList.isNull(i)) {
                continue;
            }
            JSONObject item = romList.getJSONObject(i);
            // Create the ROM object
            Rom rom = parseRomJsonObject(item);
            if (rom != null) {
                roms.add(rom);
            }
        }
        return roms.toArray(new Rom[roms.size()]);
    }
}
