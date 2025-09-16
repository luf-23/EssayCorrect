package com.example.essaycorrect.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.essaycorrect.R;
import com.example.essaycorrect.data.model.ApiResponse;
import com.example.essaycorrect.data.model.Article;
import com.example.essaycorrect.data.network.ApiService;
import com.example.essaycorrect.ui.adapter.ArticleAdapter;
import com.example.essaycorrect.utils.AppStorage;
import com.example.essaycorrect.utils.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SavedArticleActivity extends AppCompatActivity {
    private static final String TAG = "SavedArticleActivity";

    // UI组件
    private RecyclerView recyclerView;
    private ArticleAdapter adapter;
    
    // 数据
    private List<Article> articleList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_article);

        initViews();
        setupRecyclerView();
        setupListeners();
        loadArticles();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        
        articleList = new ArrayList<>();
    }

    private void setupRecyclerView() {
        adapter = new ArticleAdapter(articleList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        
        adapter.setOnItemClickListener(new ArticleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Article article, int position) {
                openArticleForEdit(article);
            }
        });
    }

    private void setupListeners() {
        // 可以添加其他监听器，比如工具栏返回按钮
    }

    private void loadArticles() {
        Integer userId = AppStorage.getInstance(this).getUserInfo().getUserId();
        if (userId == null) {
            Toast.makeText(this, "用户信息异常", Toast.LENGTH_SHORT).show();
            return;
        }

        // 首先获取默认分类ID
        getDefaultCategoryId(userId, new CategoryIdCallback() {
            @Override
            public void onSuccess(Integer categoryId) {
                if (categoryId != null) {
                    loadArticlesByCategory(categoryId);
                } else {
                    showEmptyView();
                }
            }

            @Override
            public void onFailure() {
                showEmptyView();
            }
        });
    }

    private void getDefaultCategoryId(Integer userId, CategoryIdCallback callback) {
        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.getDefaultCategoryId(userId, "默认分类", "用于服务安卓项目")
                .enqueue(new Callback<ApiResponse<Integer>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Integer>> call, Response<ApiResponse<Integer>> response) {
                        if (response.isSuccessful()) {
                            ApiResponse<Integer> apiResponse = response.body();
                            if (apiResponse.getCode() == 0) {
                                callback.onSuccess(apiResponse.getData());
                            } else {
                                Log.e(TAG, "Get category id failed: " + apiResponse.getMessage());
                                callback.onFailure();
                            }
                        } else {
                            callback.onFailure();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Integer>> call, Throwable t) {
                        Log.e(TAG, "Get category id failed", t);
                        callback.onFailure();
                    }
                });
    }

    private void loadArticlesByCategory(Integer categoryId) {
        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.getArticleList(categoryId).enqueue(new Callback<ApiResponse<List<Article>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Article>>> call, Response<ApiResponse<List<Article>>> response) {
                if (response.isSuccessful()) {
                    ApiResponse<List<Article>> apiResponse = response.body();
                    if (apiResponse.getCode() == 0) {
                        List<Article> articles = apiResponse.getData();
                        updateArticleList(articles);
                    } else {
                        Log.e(TAG, "Load articles failed: " + apiResponse.getMessage());
                        showEmptyView();
                    }
                } else {
                    Toast.makeText(SavedArticleActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                    showEmptyView();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Article>>> call, Throwable t) {
                Log.e(TAG, "Load articles failed", t);
                Toast.makeText(SavedArticleActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                showEmptyView();
            }
        });
    }

    private void updateArticleList(List<Article> articles) {
        if (articles == null || articles.isEmpty()) {
            showEmptyView();
        } else {
            articleList.clear();
            articleList.addAll(articles);
            adapter.notifyDataSetChanged();
            hideEmptyView();
        }
    }

    private void showEmptyView() {
        // 当没有数据时，可以显示一个Toast或者隐藏RecyclerView
        Toast.makeText(this, "暂无保存的文章", Toast.LENGTH_SHORT).show();
        recyclerView.setVisibility(android.view.View.GONE);
    }

    private void hideEmptyView() {
        recyclerView.setVisibility(android.view.View.VISIBLE);
    }

    private void openArticleForEdit(Article article) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("title", article.getTitle());
        intent.putExtra("content", article.getContent());
        startActivity(intent);
    }

    private interface CategoryIdCallback {
        void onSuccess(Integer categoryId);
        void onFailure();
    }
}