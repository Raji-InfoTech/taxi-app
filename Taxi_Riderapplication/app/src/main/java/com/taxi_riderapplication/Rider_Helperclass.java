package com.taxi_riderapplication;

public class Rider_Helperclass {
    String name,mobilenumber;

    public Rider_Helperclass() {
    }

    public Rider_Helperclass(String name, String mobilenumber) {
        this.name = name;
        this.mobilenumber = mobilenumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobilenumber() {
        return mobilenumber;
    }

    public void setMobilenumber(String mobilenumber) {
        this.mobilenumber = mobilenumber;
    }
}
