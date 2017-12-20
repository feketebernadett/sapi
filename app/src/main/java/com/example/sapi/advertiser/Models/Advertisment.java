package com.example.sapi.advertiser.Models;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Intern on 11/7/2017.
 */

public class Advertisment {
    public  String title;
    public String description;
    public String image;
    public String location;
    public double locationLat;
    public double locationLng;
    public String userImage;
    public String uid;
    public Map<String, String> adImages = new HashMap<>();

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
}
