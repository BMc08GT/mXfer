package com.bmc.mxfer.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class User implements Parcelable, Serializable {

    private String name;
    private String gravatarEmail;
    private String twitterName;
    private String githubUrl;
    private Device[] devices;

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

    /**
     * Get the user's gravatar email address for image retrieval
     * @return gravatarEmail
     */
    public String getGravatarEmail() {
        return gravatarEmail;
    }

    /**
     * Get the user's Twitter username
     * @return twitterUserName
     */
    public String getTwitterName() {
        return twitterName;
    }

    /**
     * Get the user's Github url address
     * @return githubUrl
     */
    public String getGithubUrl() {
        return githubUrl;
    }

    /**
     * Get the devices the user has developed for
     * @return devices
     */
    public Device[] getDevices() {
        return devices;
    }

    @Override
    public String toString() {
        return "User: " + " name=" + name + "\n"
                + "gravatar email=" + gravatarEmail + "\n"
                + "twitter name=" + twitterName + "\n"
                + "github address=" + githubUrl;
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
        out.writeString(gravatarEmail);
        out.writeString(twitterName);
        out.writeString(githubUrl);
        out.writeTypedArray(devices, 0);
    }

    public void readFromParcel(Parcel in) {
        name = in.readString();
        gravatarEmail = in.readString();
        twitterName = in.readString();
        githubUrl = in.readString();
        devices = in.createTypedArray(Device.CREATOR);
    }

    public static class Builder {
        private String name;
        private String gravatarEmail;
        private String twitterName;
        private String githubUrl;
        private Device[] devices;

        public Builder setName(String userName) {
            name = userName;
            return this;
        }

        public Builder setGravatarEmail(String email) {
            gravatarEmail = email;
            return this;
        }

        public Builder setTwitterName(String twitter) {
            twitterName = twitter;
            return this;
        }

        public Builder setGithubUrl(String address) {
            githubUrl = address;
            return this;
        }

        public Builder setDevices(Device[] deviceList) {
            devices = deviceList;
            return this;
        }

        public User build() {
            User user = new User();
            user.name = name;
            user.gravatarEmail = gravatarEmail;
            user.twitterName = twitterName;
            user.githubUrl = githubUrl;
            user.devices = devices;

            return user;
        }
    }
}
