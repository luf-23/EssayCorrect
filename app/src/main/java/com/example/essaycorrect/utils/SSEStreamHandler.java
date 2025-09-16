package com.example.essaycorrect.utils;

import android.util.Log;

import com.example.essaycorrect.data.model.AIRequest;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SSEStreamHandler {
    private static final String TAG = "SSEStreamHandler";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String STREAM_URL = "http://43.142.2.253/api/ai/chat-stream";

    private OkHttpClient client;
    private Call call;
    private StreamCallback callback;

    public interface StreamCallback {
        void onMessageReceived(String message);
        void onError(Throwable t);
        void onComplete();
    }

    public SSEStreamHandler(StreamCallback callback) {
        this.callback = callback;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(0, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public void startStreaming(AIRequest request, String token) {
        try {
            String json = new Gson().toJson(request);
            Log.d(TAG, "发送的JSON: " + json);

            RequestBody body = RequestBody.create(json, JSON);
            Request okHttpRequest = buildRequest(body, token);

            call = client.newCall(okHttpRequest);
            call.enqueue(createCallback());

        } catch (Exception e) {
            Log.e(TAG, "启动SSE流失败", e);
            if (callback != null) {
                callback.onError(e);
            }
        }
    }

    private Request buildRequest(RequestBody body, String token) {
        Request.Builder requestBuilder = new Request.Builder()
                .url(STREAM_URL)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "text/event-stream")
                .addHeader("Cache-Control", "no-cache");

        if (token != null && !token.trim().isEmpty()) {
            requestBuilder.addHeader("Authorization", token.trim());
        }

        return requestBuilder.build();
    }

    private Callback createCallback() {
        return new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "SSE请求失败", e);
                if (callback != null) {
                    callback.onError(e);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    handleErrorResponse(response);
                    return;
                }

                Log.d(TAG, "SSE连接成功建立，开始读取流");
                processSSEStream(response);
            }
        };
    }

    private void handleErrorResponse(Response response) throws IOException {
        String errorBody = response.body() != null ? response.body().string() : "null";
        Log.e(TAG, "SSE响应失败: " + response.code() + ", body: " + errorBody);

        if (callback != null) {
            if (response.code() != 401) {
                callback.onError(new IOException("HTTP " + response.code() + ": " + errorBody));
            }
        }
    }

    private void processSSEStream(Response response) {
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(response.body().byteStream()))) {

                String line;
                while ((line = reader.readLine()) != null && !call.isCanceled()) {
                    Log.d(TAG, "原始行: " + line);
                    processSSELine(line);
                }

                Log.d(TAG, "SSE流正常结束");
                if (callback != null) {
                    callback.onComplete();
                }

            } catch (IOException e) {
                if (!call.isCanceled()) {
                    Log.e(TAG, "读取SSE流失败", e);
                    if (callback != null) {
                        callback.onError(e);
                    }
                }
            }
        }).start();
    }

    private void processSSELine(String line) {
        if (line.trim().startsWith("data:")) {
            String data = line.trim().substring(5).trim();

            if (data.equals("[DONE]")) {
                Log.d(TAG, "收到结束标记[DONE]");
                return;
            }

            if (!data.isEmpty()) {
                processData(data);
            }
        }
    }

    private void processData(String data) {
        try {
            Log.d(TAG, "处理数据: " + data);

            JSONObject jsonData = new JSONObject(data);

            // DeepSeek-v3 格式解析
            if (jsonData.has("choices")) {
                JSONObject firstChoice = jsonData.getJSONArray("choices").getJSONObject(0);
                if (firstChoice.has("delta")) {
                    JSONObject delta = firstChoice.getJSONObject("delta");
                    if (delta.has("content")) {
                        String content = delta.getString("content");
                        if (content != null && !content.isEmpty()) {
                            Log.d(TAG, "提取到内容: '" + content + "'");
                            if (callback != null) {
                                callback.onMessageReceived(content);
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSON解析失败: " + data, e);
        } catch (Exception e) {
            Log.e(TAG, "处理数据时发生错误", e);
        }
    }

    public void stopStreaming() {
        if (call != null && !call.isCanceled()) {
            call.cancel();
            Log.d(TAG, "SSE流已停止");
        }
    }

    public boolean isRunning() {
        return call != null && !call.isCanceled();
    }
}