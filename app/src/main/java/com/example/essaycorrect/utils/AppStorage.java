package com.example.essaycorrect.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.essaycorrect.data.model.User;
import com.google.gson.Gson;

public class AppStorage {
    private static final String PREFS_NAME = "app_storage";
    private static final String KEY_TOKEN = "user_token";
    private static final String KEY_USER_INFO = "user_info";

    private static AppStorage instance;
    private SharedPreferences prefs;
    private Gson gson;

    private AppStorage(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public static synchronized AppStorage getInstance(Context context) {
        if (instance == null) {
            instance = new AppStorage(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * 保存用户令牌
     */
    public void saveToken(String token) {
        prefs.edit().putString(KEY_TOKEN, token).apply();
    }

    /**
     * 获取用户令牌
     */
    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    /**
     * 保存用户信息
     */
    public void saveUserInfo(User user) {
        String userJson = gson.toJson(user);
        prefs.edit().putString(KEY_USER_INFO, userJson).apply();
    }

    /**
     * 获取用户信息
     */
    public User getUserInfo() {
        String userJson = prefs.getString(KEY_USER_INFO, null);
        if (userJson != null) {
            return gson.fromJson(userJson, User.class);
        }
        return null;
    }

    /**
     * 清除所有数据（退出登录）
     */
    public void clearAll() {
        prefs.edit().clear().apply();
    }

    /**
     * 检查是否已登录
     */
    public boolean isLoggedIn() {
        return getToken() != null;
    }
}