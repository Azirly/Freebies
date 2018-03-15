package com.example.justin.freebies;

public class Blog {

    private String Title, Description, Image;

    public Blog(){

    }

    public Blog(String Title, String Description, String Image) {
        this.Title = Title;
        this.Description = Description;
        this.Image = Image;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        this.Title = title;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        this.Description = description;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        this.Image = image;
    }



}