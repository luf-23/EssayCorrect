package com.example.essaycorrect.ui.activity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.essaycorrect.R;
import com.example.essaycorrect.data.model.ApiResponse;
import com.example.essaycorrect.data.network.ApiService;
import com.example.essaycorrect.utils.RetrofitClient;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";

    // UI组件
    private TextInputEditText username;
    private TextInputEditText email;
    private TextInputEditText verificationCode;
    private TextInputEditText password;
    private TextInputEditText confirmPassword;
    private Button sendCodeButton;
    private Button registerButton;
    private CheckBox agree;
    
    // 其他
    private CountDownTimer countDownTimer;
    private ApiService apiService;

    private interface VerifyCallback {
        void onSuccess();
        void onFailure();
    }

    private interface RegisterCallback {
        void onSuccess();
        void onFailure();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        initViews();
        setupListeners();
    }

    private void initViews() {
        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        verificationCode = findViewById(R.id.verificationCode);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirmPassword);
        sendCodeButton = findViewById(R.id.sendCodeButton);
        registerButton = findViewById(R.id.registerButton);
        agree = findViewById(R.id.agreeTermsCheckbox);
        
        apiService = RetrofitClient.getApiService(this);
    }

    private void setupListeners() {
        sendCodeButton.setOnClickListener(v -> handleSendCode());
        registerButton.setOnClickListener(v -> handleRegister());
    }

    private void handleSendCode() {
        String emailStr = email.getText().toString().trim();
        
        if (!isValidEmail(emailStr)) {
            Toast.makeText(this, "请输入有效的邮箱地址", Toast.LENGTH_SHORT).show();
            return;
        }

        sendVerificationCode(emailStr);
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");
    }

    private void sendVerificationCode(String emailStr) {
        apiService.captcha(emailStr).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse.getCode() == 0) {
                        Toast.makeText(RegisterActivity.this, "验证码已发送", Toast.LENGTH_SHORT).show();
                        startCountDown();
                    } else {
                        Toast.makeText(RegisterActivity.this, "发送失败: " + apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "发送失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e(TAG, "Send code failed", t);
                Toast.makeText(RegisterActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startCountDown() {
        sendCodeButton.setEnabled(false);
        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                sendCodeButton.setText("重新发送(" + millisUntilFinished / 1000 + "s)");
            }

            @Override
            public void onFinish() {
                sendCodeButton.setEnabled(true);
                sendCodeButton.setText("发送验证码");
            }
        }.start();
    }

    private void handleRegister() {
        if (!validateInputs()) {
            return;
        }

        String usernameStr = username.getText().toString().trim();
        String emailStr = email.getText().toString().trim();
        String codeStr = verificationCode.getText().toString().trim();
        String passwordStr = password.getText().toString().trim();

        verifyCode(emailStr, codeStr, new VerifyCallback() {
            @Override
            public void onSuccess() {
                performRegister(usernameStr, passwordStr, emailStr);
            }

            @Override
            public void onFailure() {
                Toast.makeText(RegisterActivity.this, "验证码验证失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateInputs() {
        String usernameStr = username.getText().toString().trim();
        String emailStr = email.getText().toString().trim();
        String codeStr = verificationCode.getText().toString().trim();
        String passwordStr = password.getText().toString().trim();
        String confirmPasswordStr = confirmPassword.getText().toString().trim();

        if (usernameStr.isEmpty() || emailStr.isEmpty() || codeStr.isEmpty() || 
            passwordStr.isEmpty() || confirmPasswordStr.isEmpty()) {
            Toast.makeText(this, "请填写所有字段", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!usernameStr.matches("^\\S{5,16}$")) {
            Toast.makeText(this, "用户名必须在5~16位", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!isValidEmail(emailStr)) {
            Toast.makeText(this, "邮箱格式错误", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!passwordStr.matches("^\\S{6,16}$")) {
            Toast.makeText(this, "密码必须在6~16位", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!passwordStr.equals(confirmPasswordStr)) {
            Toast.makeText(this, "两次密码输入不一致", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!agree.isChecked()) {
            Toast.makeText(this, "请同意用户协议", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void verifyCode(String emailStr, String codeStr, VerifyCallback callback) {
        apiService.verify(emailStr, codeStr).enqueue(new Callback<ApiResponse<Boolean>>() {
            @Override
            public void onResponse(Call<ApiResponse<Boolean>> call, Response<ApiResponse<Boolean>> response) {
                if (response.isSuccessful()) {
                    ApiResponse<Boolean> apiResponse = response.body();
                    if (apiResponse.getCode() == 0 && Boolean.TRUE.equals(apiResponse.getData())) {
                        callback.onSuccess();
                    } else {
                        callback.onFailure();
                    }
                } else {
                    callback.onFailure();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Boolean>> call, Throwable t) {
                Log.e(TAG, "Verify code failed", t);
                callback.onFailure();
            }
        });
    }

    private void performRegister(String usernameStr, String passwordStr, String emailStr) {
        apiService.register(usernameStr, passwordStr, emailStr).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse.getCode() == 0) {
                        Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, "注册失败: " + apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "注册失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e(TAG, "Register failed", t);
                Toast.makeText(RegisterActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}