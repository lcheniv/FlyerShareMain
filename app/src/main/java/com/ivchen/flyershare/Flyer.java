package com.ivchen.flyershare;

/**
 * Created by Daniel on 4/12/2016.
 */
public class Flyer {
    private String image, date, school, title;

    public Flyer(){
     //empty default constructor, needed for firebase to be able to deserialize database entries
    }

    public String getImage(){
        return image;
    }

    public String getDate(){
        return date;
    }

    public String getSchool(){
        return school;
    }

    public String getTitle(){
        return title;
    }
}
