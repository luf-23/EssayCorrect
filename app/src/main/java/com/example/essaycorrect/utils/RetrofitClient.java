package com.example.essaycorrect.utils;

import android.content.Context;

import com.example.essaycorrect.data.network.ApiService;
import com.example.essaycorrect.data.network.LoginInterceptor;

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
            retrofit = createRetrofitInstance(context);
        }
        return retrofit.create(ApiService.class);
    }

    private static Retrofit createRetrofitInstance(Context context) {
        // 创建日志拦截器（用于调试）
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        // 创建 OkHttpClient 并添加拦截器
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(new LoginInterceptor(context))
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        // 创建 Retrofit 实例
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    /**
     * 重置客户端实例（用于测试或配置更改）
     */
    public static void resetInstance() {
        retrofit = null;
    }
}