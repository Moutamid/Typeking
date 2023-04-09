package com.moutamid.typeking.models;

public class ViewersModel {
    String user, date;

    public ViewersModel() {
    }

    public ViewersModel(String user, String date) {
        this.user = user;
        this.date = date;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
