package com.example.caslogin.data.model;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
public class LoggedInUser {

    private Long userId;
    private String token;
    private String displayName;

    public LoggedInUser(Long userId, String token, String displayName) {
        this.userId = userId;
        this.token = token;
        this.displayName = displayName;
    }

    public Long getUserId() {
        return userId;
    }

    public String getToken() {
        return token;
    }

    public String getDisplayName() {
        return displayName;
    }
}
