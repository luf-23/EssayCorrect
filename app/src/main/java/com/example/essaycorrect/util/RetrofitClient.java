package com.example.essaycorrect.util;

import android.content.Context;

import com.example.essaycorrect.interceptor.LoginInterceptor;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Retrofit retrofit = null;
    private static final String BASE_URL = "http://192.168.171.105:8080/"; // 你的后端基础 URL
    //private static final String BASE_URL = "http://luf.woyioii.cn/api/";

    public static ApiService getApiService(Context context) {
        if (retrofit == null) {
            // 创建日志拦截器（可选，用于调试）
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY); // 记录请求和响应体

            // 创建 OkHttpClient 并添加拦截器
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(new LoginInterceptor(context))
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            // 创建 Retrofit 实例
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client) // 设置自定义的 OkHttpClient
                    .addConverterFactory(GsonConverterFactory.create()) // 添加 Gson 转换器
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}