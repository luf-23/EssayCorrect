package com.example.essaycorrect.ui.activity;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.essaycorrect.R;
import com.example.essaycorrect.data.model.AIRequest;
import com.example.essaycorrect.data.model.ApiResponse;
import com.example.essaycorrect.data.model.Article;
import com.example.essaycorrect.data.network.ApiService;
import com.example.essaycorrect.utils.AppStorage;
import com.example.essaycorrect.utils.RetrofitClient;
import com.example.essaycorrect.utils.SSEStreamHandler;
import com.google.android.material.internal.TextWatcherAdapter;

import java.util.ArrayList;
import java.util.List;

import io.noties.markwon.Markwon;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.ext.tables.TablePlugin;
import io.noties.markwon.linkify.LinkifyPlugin;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    
    // UI组件
    private SSEStreamHandler sseHandler;
    private EditText titleInput;
    private EditText contentInput;
    private ImageButton submitButton;
    private TextView aiResponseText;
    private ScrollView scrollView;
    private TextView wordCount;
    private ImageButton refreshButton;
    private ImageButton viewSavedButton;
    private ImageButton saveButton;
    private TextView backButton;
    private ImageButton copyButton;
    
    // 其他组件
    private Markwon markwon;
    private StringBuilder content;

    private interface GetCategoryIdCallback {
        void onSuccess(Integer categoryId);
        void onFailure();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initMarkdown();
        setupListeners();
        getDataFromIntent();
    }

    private void initViews() {
        titleInput = findViewById(R.id.titleInput);
        contentInput = findViewById(R.id.contentInput);
        submitButton = findViewById(R.id.submitButton);
        aiResponseText = findViewById(R.id.aiResponse);
        scrollView = findViewById(R.id.scroll_view);
        wordCount = findViewById(R.id.wordCount);
        refreshButton = findViewById(R.id.refreshButton);
        viewSavedButton = findViewById(R.id.viewSavedButton);
        saveButton = findViewById(R.id.saveButton);
        backButton = findViewById(R.id.backButton);
        copyButton = findViewById(R.id.copyButton);
        
        content = new StringBuilder();
    }

    private void initMarkdown() {
        markwon = Markwon.builder(this)
                .usePlugin(TablePlugin.create(this))
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(LinkifyPlugin.create())
                .build();
        markwon.setMarkdown(aiResponseText, "AI的点评将显示在这里...");
    }

    @SuppressLint("RestrictedApi")
    private void setupListeners() {
        submitButton.setOnClickListener(v -> handleSubmitClick());
        refreshButton.setOnClickListener(v -> handleRefreshClick());
        saveButton.setOnClickListener(v -> handleSaveClick());
        viewSavedButton.setOnClickListener(v -> navigateToSavedArticles());
        backButton.setOnClickListener(v -> finish());
        copyButton.setOnClickListener(v -> handleCopyClick());
        
        setupWordCountListener();
    }

    private void setupWordCountListener() {
        contentInput.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(Editable s) {
                int count = s.toString().length();
                wordCount.setText(count + "/2000");
            }
        });
    }

    private void handleSubmitClick() {
        String articleTitle = titleInput.getText().toString().trim();
        String articleContent = contentInput.getText().toString().trim();
        
        if (!validateInput(articleTitle, articleContent)) {
            return;
        }

        String article = "文章标题：\n" + articleTitle + "\n" + "文章内容：\n" + articleContent;
        List<AIRequest.AIMessage> messages = new ArrayList<>();
        messages.add(new AIRequest.AIMessage("user", article));
        
        String character = getString(R.string.character);
        AIRequest request = new AIRequest("deepseek-v3", 0.7, messages, character);
        String token = AppStorage.getInstance(this).getToken();
        
        content.setLength(0);
        startAIChat(request, token);
    }

    private boolean validateInput(String title, String content) {
        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "请填写完整的标题和内容", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (title.length() > 15) {
            Toast.makeText(this, "标题长度不能超过15字", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (content.length() > 2000) {
            Toast.makeText(this, "文章长度不能超过2000字", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        return true;
    }

    private void handleRefreshClick() {
        titleInput.setText("");
        contentInput.setText("");
        markwon.setMarkdown(aiResponseText, "AI的点评将显示在这里...");
        content.setLength(0);
        wordCount.setText("0/2000");
    }

    private void handleSaveClick() {
        String articleTitle = titleInput.getText().toString().trim();
        String articleContent = contentInput.getText().toString().trim();
        
        if (!validateInput(articleTitle, articleContent)) {
            return;
        }

        save(articleTitle, articleContent, new GetCategoryIdCallback() {
            @Override
            public void onSuccess(Integer categoryId) {
                saveArticleToServer(categoryId, articleTitle, articleContent);
            }

            @Override
            public void onFailure() {
                // 错误处理已在save方法中处理
            }
        });
    }

    private void saveArticleToServer(Integer categoryId, String title, String content) {
        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.add(new Article(categoryId, title, content, "draft")).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse.getCode() == 0) {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "保存成功", Toast.LENGTH_SHORT).show());
                    } else {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "保存失败:" + apiResponse.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "网络错误，保存失败", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void handleCopyClick() {
        String content = aiResponseText.getText().toString();
        if (content.isEmpty()) {
            Toast.makeText(this, "请先点击提交按钮", Toast.LENGTH_SHORT).show();
            return;
        }
        
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("AI点评", content);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "复制成功", Toast.LENGTH_SHORT).show();
    }

    private void navigateToSavedArticles() {
        startActivity(new Intent(this, SavedArticleActivity.class));
    }

    /**
     * 开始AI点评
     */
    private void startAIChat(AIRequest request, String token) {
        markwon.setMarkdown(aiResponseText, "");
        
        sseHandler = new SSEStreamHandler(new SSEStreamHandler.StreamCallback() {
            @Override
            public void onMessageReceived(String message) {
                runOnUiThread(() -> {
                    content.append(message);
                    markwon.setMarkdown(aiResponseText, content.toString());
                    scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
                });
            }

            @Override
            public void onError(Throwable t) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    hideLoadingIndicator();
                });
            }

            @Override
            public void onComplete() {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "AI点评完成", Toast.LENGTH_SHORT).show();
                    hideLoadingIndicator();
                });
            }
        });

        showLoadingIndicator();
        sseHandler.startStreaming(request, token);
    }

    private void showLoadingIndicator() {
        findViewById(R.id.loadingIndicator).setVisibility(View.VISIBLE);
    }

    private void hideLoadingIndicator() {
        findViewById(R.id.loadingIndicator).setVisibility(View.GONE);
    }

    private void save(String title, String content, GetCategoryIdCallback callback) {
        ApiService apiService = RetrofitClient.getApiService(this);
        Integer userId = AppStorage.getInstance(this).getUserInfo().getUserId();
        
        apiService.getDefaultCategoryId(userId, "默认分类", "用于服务安卓项目").enqueue(new Callback<ApiResponse<Integer>>() {
            @Override
            public void onResponse(Call<ApiResponse<Integer>> call, Response<ApiResponse<Integer>> response) {
                if (response.isSuccessful()) {
                    ApiResponse<Integer> apiResponse = response.body();
                    if (apiResponse.getCode() == 0) {
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "获取默认分类id失败", Toast.LENGTH_SHORT).show());
                        callback.onFailure();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Integer>> call, Throwable t) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "网络错误，无法获取分类信息", Toast.LENGTH_SHORT).show());
                callback.onFailure();
            }
        });
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String content = intent.getStringExtra("content");
        if (title != null && content != null) {
            titleInput.setText(title);
            contentInput.setText(content);
            wordCount.setText(content.length() + "/2000");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sseHandler != null) {
            sseHandler.stopStreaming();
        }
    }
}