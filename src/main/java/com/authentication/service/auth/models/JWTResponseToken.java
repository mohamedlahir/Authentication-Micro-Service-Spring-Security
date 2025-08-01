package com.authentication.service.auth.models;

import org.springframework.stereotype.Component;

@Component
public class JWTResponseToken {

    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "JWTResponseToken{" +
                "token='" + token + '\'' +
                '}';
    }
}
