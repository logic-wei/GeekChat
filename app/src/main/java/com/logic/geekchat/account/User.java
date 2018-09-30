package com.logic.geekchat.account;

public class User {
    private String username = null;
    private String token = null;
    private boolean isTokenVaild = false;
    public User() {
        isTokenVaild = false;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isTokenVaild() {
        return isTokenVaild;
    }

    public void setTokenVaild(boolean tokenVaild) {
        isTokenVaild = tokenVaild;
    }
}
