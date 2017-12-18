package com.example.sapi.advertiser.Models;

/**
 * Created by Intern on 11/7/2017.
 */

public class Advertisment {
    private  String title;
    private String description;
    private String image;
    private String location;
    private String userImage;
    private String uid;

    public class Image{
        public String image;
    }

    public Advertisment(){

    }

    public Advertisment(String title, String description, String image, String userImage, String location, String uid) {
        this.title = title;
        this.description = description;
        this.image = image;
        this.userImage = userImage;
        this.location = location;
        this.uid = uid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }


    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
