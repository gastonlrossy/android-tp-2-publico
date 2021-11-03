package com.example.aplicaciong10;

import java.io.Serializable;

public class Hour implements Serializable {

    private String description;

    public Hour(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }


}
