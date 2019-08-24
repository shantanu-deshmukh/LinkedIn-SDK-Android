package com.shantanudeshmukh.linkedinsdk.HelpClasses;

import android.os.Parcel;
import android.os.Parcelable;

public class LinkedInUser implements Parcelable {
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String profileUrl;
    private String accessToken;
    private long accessTokenExpiry;

    public LinkedInUser() {

    }


    protected LinkedInUser(Parcel in) {
        id = in.readString();
        email = in.readString();
        firstName = in.readString();
        lastName = in.readString();
        profileUrl = in.readString();
        accessToken = in.readString();
        accessTokenExpiry = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(email);
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeString(profileUrl);
        dest.writeString(accessToken);
        dest.writeLong(accessTokenExpiry);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<LinkedInUser> CREATOR = new Creator<LinkedInUser>() {
        @Override
        public LinkedInUser createFromParcel(Parcel in) {
            return new LinkedInUser(in);
        }

        @Override
        public LinkedInUser[] newArray(int size) {
            return new LinkedInUser[size];
        }
    };


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public long getAccessTokenExpiry() {
        return accessTokenExpiry;
    }

    public void setAccessTokenExpiry(long accessTokenExpiry) {
        this.accessTokenExpiry = accessTokenExpiry;
    }
}
