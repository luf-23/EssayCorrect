package com.example.essaycorrect;

import com.example.essaycorrect.entity.User;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.essaycorrect.entity.ApiResponse;
import com.example.essaycorrect.util.ApiService;
import com.example.essaycorrect.util.RetrofitClient;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    private TextInputEditText username;
    private TextInputEditText email;
    private TextInputEditText verificationCode;
    private TextInputEditText password;
    private TextInputEditText confirmPassword;

    private Button sendCodeButton;
    private Button registerButton;
    private CheckBox agree;
    private CountDownTimer countDownTimer;

    private ApiService apiService;

    private interface VerifyCallback{
        void onSuccess();
        void onFailure();
    }

    private interface RegisterCallback{
        void onSuccess();
        void onFailure();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        initView();
        apiService = RetrofitClient.getApiService(this);
        setListener();
    }

    private void initView() {
        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        verificationCode = findViewById(R.id.verificationCode);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirmPassword);
        sendCodeButton = findViewById(R.id.sendCodeButton);
        registerButton = findViewById(R.id.registerButton);
        agree = findViewById(R.id.agreeTermsCheckbox);
    }

    private void setListener() {
        sendCodeButton.setOnClickListener(v -> {
            if (!email.getText().toString().matches("^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")) {
                Toast.makeText(this, "邮箱格式错误", Toast.LENGTH_SHORT).show();
                return;
            }
            startCountdown();
            ApiService apiService = RetrofitClient.getApiService(this);
            apiService.captcha(email.getText().toString()).enqueue(new retrofit2.Callback<ApiResponse>() {

                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    if (response.isSuccessful()){
                        ApiResponse apiResponse = response.body();
                        if (apiResponse.getCode()==0){
                            Toast.makeText(RegisterActivity.this, "验证码已发送", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(RegisterActivity.this, "失败"+apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    Toast.makeText(RegisterActivity.this, "发送失败", Toast.LENGTH_SHORT).show();
                }
            });

        });

        registerButton.setOnClickListener(v -> {
            if (!agree.isChecked()){
                Toast.makeText(this, "请同意条款", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!username.getText().toString().matches("^[a-zA-Z0-9_]{5,16}$")){
                Toast.makeText(this, "用户名需在5~16位", Toast.LENGTH_SHORT).show();
                return;
            }

            if ( !password.getText().toString().matches("^[a-zA-Z0-9_]{5,16}$")){
                Toast.makeText(this, "密码需在5~16位", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.getText().toString().equals(confirmPassword.getText().toString())){
                Toast.makeText(this, "密码不一致", Toast.LENGTH_SHORT).show();
                return;
            }

            verify(email.getText().toString(),verificationCode.getText().toString(),new VerifyCallback() {
                @Override
                public void onSuccess() {
                    register(username.getText().toString(),password.getText().toString(),email.getText().toString(), new RegisterCallback() {
                        @Override
                        public void onSuccess() {
                            apiService.getUserInfoByName(username.getText().toString()).enqueue(new retrofit2.Callback<ApiResponse<User>>() {

                                @Override
                                public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                                    if (response.isSuccessful()){
                                        ApiResponse apiResponse = response.body();
                                        if (apiResponse.getCode()==0){
                                            User user = (User) apiResponse.getData();
                                            apiService.setDefaultCategory(user.getUserId()).enqueue(new retrofit2.Callback<ApiResponse>() {
                                                @Override
                                                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                                                    if (response.isSuccessful()){
                                                        if (response.body().getCode()!=0){
                                                            runOnUiThread(() -> {
                                                                Toast.makeText(RegisterActivity.this, "设置默认分类失败", Toast.LENGTH_SHORT).show();
                                                            });
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onFailure(Call<ApiResponse> call, Throwable t) {
                                                    runOnUiThread(() -> {
                                                        Toast.makeText(RegisterActivity.this, "error 500 in 设置分类", Toast.LENGTH_SHORT).show();
                                                    });
                                                }
                                            });
                                        }else{
                                            runOnUiThread(() -> {
                                                Toast.makeText(RegisterActivity.this, "获取用户信息失败", Toast.LENGTH_SHORT).show();
                                            });
                                        }
                                    }else{

                                    }
                                }

                                @Override
                                public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                                    Log.d("flag","onFailure"+ t);
                                    runOnUiThread(() -> {
                                        Toast.makeText(RegisterActivity.this, "error 500 in 获取用户信息", Toast.LENGTH_SHORT).show();
                                    });
                                }
                            });
                        }

                        @Override
                        public void onFailure() {
                            Toast.makeText(RegisterActivity.this, "error 500 in 验证码验证", Toast.LENGTH_SHORT).show();
                        }
                    });
                    finish();
                }
                @Override
                public void onFailure() {
                    runOnUiThread(() -> {
                        Toast.makeText(RegisterActivity.this, "验证码错误", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });
    }

    private void register(String username, String password, String email ,RegisterCallback callback) {
        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.register(username,password,email).enqueue(new retrofit2.Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()){
                    ApiResponse apiResponse = response.body();
                    if (apiResponse.getCode()==0){
                        runOnUiThread(() -> {
                            Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                        });
                        callback.onSuccess();
                    }else{
                        Toast.makeText(RegisterActivity.this, "失败"+apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        callback.onFailure();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "注册失败", Toast.LENGTH_SHORT).show();
                callback.onFailure();
            }
        });
    }

    private void startCountdown() {
        // 如果已有倒计时在运行，先取消
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long secondsRemaining = millisUntilFinished / 1000;
                sendCodeButton.setText(secondsRemaining + "s后重新发送");
                if (sendCodeButton.isEnabled()) sendCodeButton.setEnabled(false);
            }

            @Override
            public void onFinish() {
                sendCodeButton.setText("发送验证码");
                sendCodeButton.setEnabled(true);
            }
        };

        countDownTimer.start();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private void verify(String email, String captcha,VerifyCallback verifyCallback) {
        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.verify(email,captcha).enqueue(new retrofit2.Callback<ApiResponse<Boolean>>() {
            @Override
            public void onResponse(Call<ApiResponse<Boolean>> call, Response<ApiResponse<Boolean>> response) {
                if (response.isSuccessful()){
                    ApiResponse apiResponse = response.body();
                    if (apiResponse.getCode()==0){
                        if (apiResponse.getData().equals(true)) verifyCallback.onSuccess();
                        else verifyCallback.onFailure();
                    }else{
                        runOnUiThread(() -> {
                            Toast.makeText(RegisterActivity.this, "验证失败,服务器出错", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Boolean>> call, Throwable t) {
                runOnUiThread(() -> {
                    Toast.makeText(RegisterActivity.this, "验证失败,网络错误", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

}