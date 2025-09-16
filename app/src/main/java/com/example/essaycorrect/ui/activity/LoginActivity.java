package com.example.essaycorrect.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.essaycorrect.R;
import com.example.essaycorrect.data.model.ApiResponse;
import com.example.essaycorrect.data.model.User;
import com.example.essaycorrect.data.network.ApiService;
import com.example.essaycorrect.utils.AppStorage;
import com.example.essaycorrect.utils.RetrofitClient;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText usernameEditText;
    private TextInputEditText passwordEditText;
    private Button loginButton;
    private TextView registerButton;

    private interface MyCallback{
        void createDefaultCategory();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        initView();
        setListener();
    }

    private void initView() {
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
    }
    
    private void setListener() {
        loginButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            
            if (!isValidInput(username, password)) {
                return;
            }

            performLogin(username, password);
        });
        
        registerButton.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private boolean isValidInput(String username, String password) {
        if (!username.matches("(^\\S{5,16}$)|(^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$)")){
            if (!username.matches("^\\S{5,16}$")){
                Toast.makeText(this, "用户名必须在5~16位", Toast.LENGTH_SHORT).show();
            }else if (!username.matches("^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")){
                Toast.makeText(this, "邮箱格式错误", Toast.LENGTH_SHORT).show();
            }
            return false;
        }
        
        if (!password.matches("^\\S{5,16}$")){
            Toast.makeText(this, "密码必须在6~16位", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        return true;
    }

    private void performLogin(String username, String password) {
        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.login(username, password).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()){
                    ApiResponse apiResponse = response.body();
                    if (apiResponse.getCode()==0){
                        String token = apiResponse.getData().toString();
                        AppStorage.getInstance(LoginActivity.this).saveToken(token);
                        saveUserInfo();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(LoginActivity.this, "登录失败:"+apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserInfo() {
        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.getUserInfo().enqueue(new Callback<ApiResponse<User>>() {

            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (response.isSuccessful()){
                    ApiResponse apiResponse = response.body();
                    if (apiResponse.getCode()==0){
                        AppStorage.getInstance(LoginActivity.this).saveUserInfo((User)apiResponse.getData());

                        Integer userId = AppStorage.getInstance(LoginActivity.this).getUserInfo().getUserId();
                        defaultCategory(userId, () -> {
                            ApiService apiService1 = RetrofitClient.getApiService(LoginActivity.this);
                            apiService1.setDefaultCategory(userId).enqueue(new Callback<ApiResponse>() {
                                @Override
                                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                                    if (response.isSuccessful()){
                                        if (response.body().getCode()!=0){
                                            runOnUiThread(() -> {
                                                Toast.makeText(LoginActivity.this, "设置默认分类失败", Toast.LENGTH_SHORT).show();
                                            });
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<ApiResponse> call, Throwable t) {
                                    runOnUiThread(() -> {
                                        Toast.makeText(LoginActivity.this, "error 500 in 设置默认分类", Toast.LENGTH_SHORT).show();
                                    });
                                }
                            });
                        });
                    }else{
                        Toast.makeText(LoginActivity.this, "获取用户信息失败", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void defaultCategory(Integer userId, MyCallback callback) {
        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.getDefaultCategoryId(userId, "默认分类","用于服务安卓项目")
                .enqueue(new Callback<ApiResponse<Integer>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Integer>> call, Response<ApiResponse<Integer>> response) {
                        if (response.isSuccessful()){
                            ApiResponse<Integer> apiResponse = response.body();
                            if (apiResponse.getCode()==1){
                                callback.createDefaultCategory();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Integer>> call, Throwable t) {
                        // Handle failure
                    }
                });
    }
}