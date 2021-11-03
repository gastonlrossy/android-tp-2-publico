package com.example.aplicaciong10;

import java.io.Serializable;

public class Library implements Serializable {

    private int image;
    private String title;
    private String address;
    private String days;
    private String hoursRange;

    public Library(int image, String title, String address, String days, String hoursRange) {
        this.image = image;
        this.title = title;
        this.address = address;
        this.days = days;
        this.hoursRange = hoursRange;
    }

    public int getImage() {
        return image;
    }

    public String getTitle() {
        return title;
    }

    public String getAddress() { return address; }

    public String getDays() {
        return days;
    }

    public String getHoursRange() {
        return hoursRange;
    }
}
