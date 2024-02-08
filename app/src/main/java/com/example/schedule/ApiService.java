package com.example.schedule;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class ApiService {

    private static final String BASE_URL = "https://api.college.ks.ua/";
    private static final String TAG = "ApiService"; // Добавлен тег для логирования

    private OkHttpClient client;

    public ApiService() {
        this.client = new OkHttpClient();
    }

    public void getCsrfCookie(Callback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "sanctum/csrf-cookie")
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String csrfCookie = response.header("Set-Cookie");
                    Log.d("CsrfCookie", "CSRF Cookie: " + csrfCookie);
                    //сохранение CSRF Cookie
                    Log.d(TAG, "CSRF Cookie obtained successfully");
                    callback.onResponse(call, response);
                } else {
                    // Обработка неудачного ответа
                    String errorMessage = "Server returned unsuccessful response: " + response.code() + " " + response.message();
                    Log.e(TAG, errorMessage);
                    callback.onFailure(call, new IOException(errorMessage));
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                // Обработка ошибок сети
                Log.e(TAG, "Network error: " + e.getMessage());
                callback.onFailure(call, e);
            }
        });
    }

    public void login(String login, String password, String csrfCookie, Callback callback) {
        Log.d(TAG, "Attempting login with login: " + login + ", password: " + password);

        RequestBody requestBody = new FormBody.Builder()
                .add("login", login)
                .add("password", password)
                .build();

        Request request = new Request.Builder()
                .url(BASE_URL + "api/login")
                .addHeader("Cookie", csrfCookie)
                .addHeader("Accept", "application/json")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Проверка успешности ответа и типа контента
                    String contentType = response.header("Content-Type");
                    callback.onResponse(call, response);
                    if (contentType != null && contentType.startsWith("application/json")) {
                        try {
                            JSONObject jsonObject = new JSONObject(response.body().string());
                            // Обработка JSON
                            Log.d(TAG, "Login successful. JSON response: " + jsonObject.toString());
                            callback.onResponse(call, response);
                        } catch (JSONException e) {
                            // Обработка ошибок парсинга JSON
                            String errorMessage = "Error parsing JSON response: " + e.getMessage();
                            Log.e(TAG, errorMessage);
                            callback.onFailure(call, new IOException(errorMessage));
                        }
                    } else {
                        // Ошибка: ответ не в формате JSON
                        String errorMessage = "Server response is not in JSON format";
                        Log.e(TAG, errorMessage);
                        callback.onFailure(call, new IOException(errorMessage));
                    }
                } else {
                    // Обработка неудачного ответа
                    String errorMessage = "Server returned unsuccessful response: " + response.code() + " " + response.message();
                    Log.e(TAG, errorMessage);
                    callback.onFailure(call, new IOException(errorMessage));
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                // Обработка ошибок сети
                String errorMessage = "Network error: " + e.getMessage();
                Log.e(TAG, errorMessage);
                callback.onFailure(call, e);
            }
        });
    }


    public void checkTokenValidity(String token, Callback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "api/users/profile/my")
                .addHeader("Authorization", "Bearer " + token)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Обработка успешного ответа, например, получение информации о пользователе
                    Log.d(TAG, "Token validity check successful");
                    callback.onResponse(call, response);
                } else {
                    // Обработка неудачного ответа
                    String errorMessage = "Server returned unsuccessful response: " + response.code() + " " + response.message();
                    Log.e(TAG, errorMessage);
                    callback.onFailure(call, new IOException(errorMessage));
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                // Обработка ошибок сети
                String errorMessage = "Network error: " + e.getMessage();
                Log.e(TAG, errorMessage);
                callback.onFailure(call, e);
            }
        });
    }
}



