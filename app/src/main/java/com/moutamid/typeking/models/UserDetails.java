package com.moutamid.typeking.models;

public class UserDetails {
    String name;
    String email;
    int coins;
    boolean vipStatus;

    public UserDetails() {
    }

    public UserDetails(String name, String email, int coins, boolean vipStatus) {
        this.name = name;
        this.email = email;
        this.coins = coins;
        this.vipStatus = vipStatus;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public boolean isVipStatus() {
        return vipStatus;
    }

    public void setVipStatus(boolean vipStatus) {
        this.vipStatus = vipStatus;
    }
}
