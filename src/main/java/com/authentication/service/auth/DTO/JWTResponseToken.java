package com.authentication.service.auth.DTO;

import org.springframework.stereotype.Component;

@Component
public class JWTResponseToken {

    private String loginStatus;

    private String token;

    private int statusCode;

    private Long schooldId;

    private String role;

    private String profileID;

    @Override
    public String toString() {
        return "JWTResponseToken{" +
                "loginStatus='" + loginStatus + '\'' +
                ", token='" + token + '\'' +
                ", statusCode=" + statusCode +
                ", schooldId='" + schooldId + '\'' +
                ", role='" + role + '\'' +
                ", profileID='" + profileID + '\'' +
                '}';
    }

    public Long getSchooldId() {
        return schooldId;
    }

    public void setSchooldId(Long schooldId) {
        this.schooldId = schooldId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getProfileID() {
        return profileID;
    }

    public void setProfileID(String profileID) {
        this.profileID = profileID;
    }

    public String getLoginStatus() {
        return loginStatus;
    }

    public void setLoginStatus(String loginStatus) {
        this.loginStatus = loginStatus;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
}
