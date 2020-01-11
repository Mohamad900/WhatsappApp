package com.whatsapp.app.Models;

import java.util.List;

public class Country {

    public String name;
    public String code;
    public String image;


    public Country(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public Country(String name, String code, String image) {
        this.name = name;
        this.code = code;
        this.image = image;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
