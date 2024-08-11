package com.example.whatapp.model;

public class User {

    private String name;
    private String number;
    private String email;
    private String about;
    private boolean isSelected;
    private String imageLink;

    public void select(boolean select){
        isSelected = select;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

}
