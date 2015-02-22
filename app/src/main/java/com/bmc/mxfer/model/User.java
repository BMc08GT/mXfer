package com.bmc.mxfer.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class User implements Parcelable, Serializable {

    // TODO: Add other aspects for a 'user', such as g+, twitter, donation, etc.
    private String name;

    private User() {
        // Use builder yo
    }

    private User(Parcel in) {
        readFromParcel(in);
    }

    /**
     * Get the user's name
     * @return name
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "User: " + name;
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        public User createFromParcel(Parcel in) {
            return new User(in);
        }
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(name);
    }

    public void readFromParcel(Parcel in) {
        name = in.readString();
    }

    public static class Builder {
        private String name;

        public Builder setName(String userName) {
            name = userName;
            return this;
        }

        public User build() {
            User user = new User();
            user.name = name;

            return user;
        }
    }
}
