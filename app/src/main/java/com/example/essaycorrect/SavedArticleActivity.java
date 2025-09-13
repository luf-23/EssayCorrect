package com.example.essaycorrect;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.essaycorrect.adapter.ArticleAdapter;
import com.example.essaycorrect.entity.ApiResponse;
import com.example.essaycorrect.entity.Article;
import com.example.essaycorrect.util.ApiService;
import com.example.essaycorrect.util.AppStorage;
import com.example.essaycorrect.util.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SavedArticleActivity extends AppCompatActivity {

    private ApiService apiService;
    private List<Article> articleList = new ArrayList<>();
    private ArticleAdapter articleAdapter;
    private RecyclerView articleRecyclerView;

    private interface MyCallback{
        void onSuccess(Integer categoryId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_saved_article);

        articleRecyclerView = findViewById(R.id.recyclerView);
        articleRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        articleAdapter = new ArticleAdapter(articleList);
        articleAdapter.setOnItemClickListener((article, position) -> {
            Bundle bundle = new Bundle();
            bundle.putString("title", article.getTitle());
            bundle.putString("content", article.getContent());
            Intent intent = new Intent(SavedArticleActivity.this, MainActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        });

        articleRecyclerView.setAdapter(articleAdapter);

        apiService = RetrofitClient.getApiService(this);

        getArticles(new MyCallback() {
            @Override
            public void onSuccess(Integer categoryId) {
                apiService.getArticleList(categoryId).enqueue(new Callback<ApiResponse<List<Article>>>() {

                    @Override
                    public void onResponse(Call<ApiResponse<List<Article>>> call, Response<ApiResponse<List<Article>>> response) {
                        if (response.isSuccessful()){
                            ApiResponse<List<Article>> apiResponse = response.body();
                            if (apiResponse.getCode()==0){
                                articleList.addAll(apiResponse.getData());
                                articleAdapter.notifyDataSetChanged();
                            }else{
                                runOnUiThread(() -> {
                                    Toast.makeText(SavedArticleActivity.this, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<Article>>> call, Throwable t) {
                        runOnUiThread(() -> {
                            Toast.makeText(SavedArticleActivity.this, "error 500 in 获取文章列表", Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            }
        });

    }

    private void getArticles(MyCallback myCallback) {
        apiService.getDefaultCategoryId(AppStorage.getInstance(this).getUserInfo().getUserId(), "默认分类", "用于服务安卓项目").enqueue(new Callback<ApiResponse<Integer>>() {
            @Override
            public void onResponse(Call<ApiResponse<Integer>> call, Response<ApiResponse<Integer>> response) {
                if (response.isSuccessful()){
                    ApiResponse<Integer> apiResponse = response.body();
                    if (apiResponse.getCode()==0){
                        myCallback.onSuccess(apiResponse.getData());
                    }else{
                        runOnUiThread(() -> {
                            Toast.makeText(SavedArticleActivity.this, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Integer>> call, Throwable t) {
                runOnUiThread(() -> {
                    Toast.makeText(SavedArticleActivity.this, "error 500 in 获取默认分类id", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

}