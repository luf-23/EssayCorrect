package com.example.essaycorrect.entity;

import com.google.gson.*;

import java.time.LocalDateTime;

public class User {
    private Integer userId;
    private String username;
    private String password;
    private String email;
    private String nickname;
    private String avatarImage;
    private String backgroundImage;
    private String signature;
    private String createTime;
    private String updateTime;
    private String lastLoginTime;

    private String lastLoginIp;

    public User() {
    }

    public Integer getUserId() {
        return userId;
    }
}
