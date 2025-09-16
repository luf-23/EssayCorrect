package com.example.essaycorrect.entity;

public class Article {
    private Integer articleId;
    private Integer categoryId;
    private String title;
    private String content;
    private String status;
    private String createTime;
    private String updateTime;
    private String coverImage;

    public Article(Integer categoryId, String title, String content, String status) {
        this.content = content;
        this.categoryId = categoryId;
        this.title = title;
        this.status = status;
    }

    public Integer getArticleId() {
        return articleId;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getStatus() {
        return status;
    }

    public String getCreateTime() {
        return createTime;
    }

    public Article(Integer articleId, Integer categoryId, String title, String content, String status, String createTime, String updateTime, String coverImage) {
        this.articleId = articleId;
        this.categoryId = categoryId;
        this.title = title;
        this.content = content;
        this.status = status;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.coverImage = coverImage;
    }

    public Article() {
    }

    public void setArticleId(Integer articleId) {
        this.articleId = articleId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public String getCoverImage() {
        return coverImage;
    }
}
