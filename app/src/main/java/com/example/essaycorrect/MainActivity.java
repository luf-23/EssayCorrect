
package com.example.essaycorrect;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.essaycorrect.entity.AIRequest;
import com.example.essaycorrect.entity.ApiResponse;
import com.example.essaycorrect.entity.Article;
import com.example.essaycorrect.util.ApiService;
import com.example.essaycorrect.util.AppStorage;
import com.example.essaycorrect.util.RetrofitClient;
import com.example.essaycorrect.util.SSEStreamHandler;
import com.google.android.material.internal.TextWatcherAdapter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.noties.markwon.Markwon;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.ext.tables.TablePlugin;
import io.noties.markwon.linkify.LinkifyPlugin;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity{
    private SSEStreamHandler sseHandler;
    private EditText titleInput;
    private EditText contentInput;
    private ImageButton submitButton;
    private TextView aiResponseText;
    private ScrollView scrollView;
    private TextView wordCount;

    private ImageButton refreshButton;
    private Markwon markwon;
    private StringBuilder content;
    private ImageButton viewSavedButton;
    private ImageButton saveButton;
    private TextView backButton;
    private ImageButton copyButton;

    private interface GetCategoryIdCallback{
        void onSuccess(Integer categoryId);
        void onFailure();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        markwon.setMarkdown(aiResponseText, "AI的点评将显示在这里...");
        getDataFromIntent();
        setListener();

    }


    private void initView() {
        titleInput = findViewById(R.id.titleInput);
        contentInput = findViewById(R.id.contentInput);
        submitButton = findViewById(R.id.submitButton);
        aiResponseText = findViewById(R.id.aiResponse);
        scrollView = findViewById(R.id.scroll_view);
        wordCount = findViewById(R.id.wordCount);
        refreshButton = findViewById(R.id.refreshButton);
        markwon = Markwon.builder(this)
                .usePlugin(TablePlugin.create(this))
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(LinkifyPlugin.create())
                .build();
        content = new StringBuilder();
        viewSavedButton = findViewById(R.id.viewSavedButton);
        saveButton = findViewById(R.id.saveButton);
        backButton = findViewById(R.id.backButton);
        copyButton = findViewById(R.id.copyButton);
    }

    @SuppressLint("RestrictedApi")
    private void setListener() {
        submitButton.setOnClickListener(v -> {
            String articleTitle = titleInput.getText().toString();
            String articleContent = contentInput.getText().toString();
            if (articleTitle.isEmpty() || articleContent.isEmpty()){
                Toast.makeText(this, "请填写完整的标题和内容", Toast.LENGTH_SHORT).show();
                return;
            }
            if (articleTitle.length()>15){
                Toast.makeText(this, "标题长度不能超过15字", Toast.LENGTH_SHORT).show();
                return;
            }
            if (articleContent.length() > 2000) {
                Toast.makeText(this, "文章长度不能超过2000字", Toast.LENGTH_SHORT).show();
                return;
            }
            String article = "文章标题：\n" + articleTitle + "\n" + "文章内容：\n" + articleContent;
            List<AIRequest.AIMessage> messages = new ArrayList<>();
            messages.add(new AIRequest.AIMessage("user", article));
            String character = getStringValue(MainActivity.this,R.string.character);
            Log.d("flag",character);
            AIRequest request = new AIRequest("deepseek-v3", 0.7, messages, character);
            String token = AppStorage.getInstance(MainActivity.this).getToken();
            content.setLength(0);
            startAIChat(request,token);
        });

        contentInput.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(Editable s) {
                int count = s.toString().length();
                wordCount.setText(count+"/2000");
            }
        });

        refreshButton.setOnClickListener(v -> {
            titleInput.setText("");
            contentInput.setText("");
            markwon.setMarkdown(aiResponseText, "AI的点评将显示在这里...");
            content.setLength(0);
            wordCount.setText("0/2000");
        });
        saveButton.setOnClickListener(v -> {
            String articleTitle = titleInput.getText().toString();
            String articleContent = contentInput.getText().toString();
            if (articleTitle.isEmpty() || articleContent.isEmpty()){
                Toast.makeText(this, "请填写完整的标题和内容", Toast.LENGTH_SHORT).show();
                return;
            }
            if (articleTitle.length()>15){
                Toast.makeText(this, "标题长度不能超过15字", Toast.LENGTH_SHORT).show();
                return;
            }
            if (articleContent.length() > 2000) {
                Toast.makeText(this, "文章长度不能超过2000字", Toast.LENGTH_SHORT).show();
                return;
            }
            save(articleTitle,articleContent,new GetCategoryIdCallback() {

                @Override
                public void onSuccess(Integer categoryId) {
                    ApiService apiService = RetrofitClient.getApiService(MainActivity.this);
                    apiService.add(new Article(categoryId, articleTitle, articleContent,"draft")).enqueue(new Callback<ApiResponse>() {
                        @Override
                        public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                            if (response.isSuccessful()){
                                ApiResponse apiResponse = response.body();
                                if (apiResponse.getCode()==0){
                                    runOnUiThread(() -> {
                                        Toast.makeText(MainActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                                    });
                                }else{
                                    runOnUiThread(() -> {
                                        Toast.makeText(MainActivity.this, "保存失败:"+apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse> call, Throwable t) {
                            runOnUiThread(
                                    () -> {
                                        Toast.makeText(MainActivity.this, "error 500 in 保存", Toast.LENGTH_SHORT).show();
                                    }
                            );
                        }
                    });
                }

                @Override
                public void onFailure() {

                }
            });
        });

        viewSavedButton.setOnClickListener(v->{
            startActivity(new Intent(MainActivity.this, SavedArticleActivity.class));
        });
        backButton.setOnClickListener(v->{
            finish();
        });
        copyButton.setOnClickListener(v->{
            String content = aiResponseText.getText().toString();
            if (content.isEmpty()){
                Toast.makeText(this, "请先点击提交按钮", Toast.LENGTH_SHORT).show();
                return;
            }
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("AI点评", content);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "复制成功", Toast.LENGTH_SHORT).show();
        });
    }

    // 开始AI点评
    private void startAIChat(AIRequest request,String token) {
        markwon.setMarkdown(aiResponseText, "");
        // 创建并启动SSE处理器
        sseHandler = new SSEStreamHandler(new SSEStreamHandler.StreamCallback() {
            @Override
            public void onMessageReceived(String message) {
                runOnUiThread(() -> {

                    // 实时追加AI回复;
                    content.append(message);
                    markwon.setMarkdown(aiResponseText, content.toString());
                    scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
                });
            }

            @Override
            public void onError(Throwable t) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    // 隐藏加载指示器
                    findViewById(R.id.loadingIndicator).setVisibility(View.GONE);
                });
            }

            @Override
            public void onComplete() {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "AI点评完成", Toast.LENGTH_SHORT).show();
                    // 隐藏加载指示器
                    findViewById(R.id.loadingIndicator).setVisibility(View.GONE);
                });
            }
        });

        // 显示加载指示器
        findViewById(R.id.loadingIndicator).setVisibility(View.VISIBLE);

        // 开始流式传输
        sseHandler.startStreaming(request,token);
    }

    public String getStringValue(Context context, int stringResId) {
        return context.getString(stringResId);
    }

    private void save(String title, String content,GetCategoryIdCallback callback){
        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.getDefaultCategoryId(AppStorage.getInstance(this).getUserInfo().getUserId(),"默认分类","用于服务安卓项目").enqueue(new Callback<ApiResponse<Integer>>() {
            @Override
            public void onResponse(Call<ApiResponse<Integer>> call, Response<ApiResponse<Integer>> response) {
                if (response.isSuccessful()){
                    ApiResponse<Integer> apiResponse = response.body();
                    if (apiResponse.getCode()==0){
                        callback.onSuccess(apiResponse.getData());
                    }else{
                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this, "获取默认分类id失败", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Integer>> call, Throwable t) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "error 500 in 获取默认分类id", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void getDataFromIntent(){
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String content = intent.getStringExtra("content");
        if (title != null && content != null) {
            titleInput.setText(title);
            contentInput.setText(content);
            wordCount.setText(content.length() +"/2000");
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
