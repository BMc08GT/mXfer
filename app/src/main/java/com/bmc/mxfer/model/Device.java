package com.bmc.mxfer.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Device implements Parcelable, Serializable {
    private String codename;

    private Device() {
        // Use builder yo
    }

    private Device(Parcel in) {
        readFromParcel(in);
    }

    /**
     * Get the device codename
     * @return codename
     */
    public String getCodeName() {
        return codename;
    }

    @Override
    public String toString() {
        return "Device: " + codename;
    }

    public static final Parcelable.Creator<Device> CREATOR = new Parcelable.Creator<Device>() {
        public Device createFromParcel(Parcel in) {
            return new Device(in);
        }
        public Device[] newArray(int size) {
            return new Device[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(codename);
    }

    public void readFromParcel(Parcel in) {
        codename = in.readString();
    }

    public static class Builder {
        private String codename;

        public Builder setName(String name) {
            codename = name;
            return this;
        }

        public Device build() {
            Device device = new Device();
            device.codename = codename;

            return device;
        }
    }
}
