package com.example.essaycorrect.util;

import androidx.annotation.RequiresApi;

import com.example.essaycorrect.entity.ApiResponse;
import com.example.essaycorrect.entity.Article;
import com.example.essaycorrect.entity.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    @POST("user/login")
    Call<ApiResponse> login(@Query("usernameOrEmail") String usernameOrEmail, @Query("password") String password);

    @GET("user/getInfo")
    Call<ApiResponse<User>> getUserInfo();

    @POST("user/captcha")
    Call<ApiResponse> captcha(@Query("email") String email);

    @POST("user/verify")
    Call<ApiResponse<Boolean>> verify (@Query("email") String email, @Query("captcha") String captcha);

    @POST("user/register")
    Call<ApiResponse> register(@Query("username") String username, @Query("password") String password,@Query("email") String email);

    @POST("category/default")
    Call<ApiResponse> setDefaultCategory(@Query("userId") Integer userId);

    @GET("user/getUserInfoByName")
    Call<ApiResponse<User>> getUserInfoByName(@Query("username") String username);

    @GET("category/defaultId")
    Call<ApiResponse<Integer>> getDefaultCategoryId(@Query("userId") Integer userId, @Query("categoryName") String categoryName,@Query("categoryDescription") String categoryDescription);

    @POST("article/add")
    Call<ApiResponse> add(@Body Article article);

    @GET("article/draftList")
    Call<ApiResponse<List< Article>>> getArticleList(@Query("categoryId") Integer categoryId);


}