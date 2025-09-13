package com.example.essaycorrect.interceptor;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.essaycorrect.LoginActivity;
import com.example.essaycorrect.util.AppStorage;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;

public class LoginInterceptor implements Interceptor {
    private Context context;

    public LoginInterceptor(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        // 直接从存储中获取 token
        String token = AppStorage.getInstance(context).getToken();

        Request newRequest;
        if (token != null) {
            newRequest = originalRequest.newBuilder()
                    .header("Authorization", token)
                    .build();
        } else {
            newRequest = originalRequest;
        }


        Response response = chain.proceed(newRequest);

        if (response.code() == 401){
            // 跳转到登录界面
            Intent intent = new Intent(context, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            // 在主线程中执行跳转
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                Toast.makeText(context, "登录已过期，请重新登录", Toast.LENGTH_SHORT).show();
                context.startActivity(intent);
            });

            Log.d("TokenInterceptor", "Token已过期，跳转到登录界面");
        }
        return response;
    }
}