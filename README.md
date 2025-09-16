# EssayCorrect - AI作文批改应用

## 项目简介
EssayCorrect是一个基于Android的AI作文批改应用，支持用户提交作文并获得AI智能点评和建议。

## 功能特性
- 用户注册/登录
- 作文在线提交
- AI实时流式批改
- 文章保存与管理
- Markdown格式显示

## 项目结构

```
app/src/main/java/com/example/essaycorrect/
├── ui/                          # UI相关
│   ├── activity/               # Activity
│   │   ├── LoginActivity.java
│   │   ├── MainActivity.java
│   │   ├── RegisterActivity.java
│   │   └── SavedArticleActivity.java
│   └── adapter/               # 适配器
│       └── ArticleAdapter.java
├── data/                       # 数据层
│   ├── model/                 # 数据模型
│   │   ├── User.java
│   │   ├── Article.java
│   │   ├── ApiResponse.java
│   │   └── AIRequest.java
│   └── network/               # 网络相关
│       ├── ApiService.java
│       └── LoginInterceptor.java
└── utils/                      # 工具类
    ├── AppStorage.java        # 本地存储
    ├── RetrofitClient.java    # 网络客户端
    └── SSEStreamHandler.java  # SSE流处理
```

## 技术栈
- **开发语言**: Java
- **UI框架**: Android原生
- **网络库**: Retrofit + OkHttp
- **数据格式**: JSON (Gson)
- **Markdown渲染**: Markwon
- **实时通信**: SSE (Server-Sent Events)

## 依赖库
- AndroidX系列库
- Retrofit 2.9.0 (网络请求)
- OkHttp 4.11.0 (HTTP客户端)
- Gson 2.10.1 (JSON解析)
- Markwon 4.6.2 (Markdown渲染)
- OkHttp-EventSource 4.1.0 (SSE支持)

## 开发环境
- Android Studio
- JDK 11
- Gradle 8.9.0
- 最低SDK版本: 26 (Android 8.0)
- 目标SDK版本: 35

## 构建说明
1. 确保已安装Android Studio和JDK 11
2. 克隆项目到本地
3. 使用Android Studio打开项目
4. 等待Gradle同步完成
5. 运行项目

## 代码规范
- 采用标准Android项目结构
- 按功能模块分包管理
- 遵循驼峰命名规范
- 添加适当的注释和文档

## 版本信息
- 版本号: 1.0
- 最后更新: 2025年9月16日