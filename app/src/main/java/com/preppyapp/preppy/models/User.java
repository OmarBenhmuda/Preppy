package com.preppyapp.preppy.models;

public class User {

    private String userEmail, userName, tokenId;

    public User() {

    }

    public User(String userEmail, String userName, String tokenId) {
        this.userEmail = userEmail;
        this.userName = userName;
        this.tokenId = tokenId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public String getTokenId() {
        return tokenId;
    }


}
