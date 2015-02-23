package com.bmc.mxfer.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Rom implements Parcelable, Serializable {

    private String url;
    private String date;
    private String filename;
    private long size;

    private Rom() {
        // Use the builder yo
    }

    private Rom(Parcel in) {
        readFromParcel(in);
    }

    /**
     * Get the URL of the ROM for downloading
     * @return url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Get the date the ROM was compiled/uploaded
     * @return date
     */
    public String getDate() {
        return date;
    }

    /**
     * Get the filename of the ROM
     * @return filename
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Get the size of the ROM package, in bytes
     * @return size
     */
    public long getSize() {
        return size;
    }

    @Override
    public String toString() {
        return "ROM: \n"
                + "filename=" + filename + "\n"
                + "url=" + url + "\n"
                + "date=" + date + "\n"
                + "size=" + size;
    }

    public static final Parcelable.Creator<Rom> CREATOR = new Parcelable.Creator<Rom>() {
        public Rom createFromParcel(Parcel in) {
            return new Rom(in);
        }
        public Rom[] newArray(int size) {
            return new Rom[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(url);
        out.writeString(date);
        out.writeString(filename);
        out.writeLong(size);
    }

    private void readFromParcel(Parcel in) {
        url = in.readString();
        date = in.readString();
        filename = in.readString();
        size = in.readLong();
    }

    public static class Builder {
        private String url;
        private String date;
        private String filename;
        private long size;

        public Builder setUrl(String romUrl) {
            url = romUrl;
            return this;
        }

        public Builder setDate(String romDate) {
            date = romDate;
            return this;
        }

        public Builder setFilename(String zipName) {
            filename = zipName;
            return this;
        }

        public Builder setSize(long zipSize) {
            size = zipSize;
            return this;
        }

        public Rom build() {
            Rom rom = new Rom();
            rom.url = url;
            rom.date = date;
            rom.filename = filename;
            rom.size = size;

            return rom;
        }
    }


}
