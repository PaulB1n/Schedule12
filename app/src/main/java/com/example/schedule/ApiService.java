package com.example.schedule;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.ResponseBody;

public class ApiService {

    private static final String BASE_URL = "https://api.college.ks.ua/";
    private static final String TAG = "ApiService";

    private OkHttpClient client;
    private Set<Call> activeCalls;

    public ApiService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)  // Установка таймаута подключения
                .readTimeout(60, TimeUnit.SECONDS)     // Установка таймаута чтения
                .writeTimeout(60, TimeUnit.SECONDS)    // Установка таймаута записи
                .build();
        this.activeCalls = new HashSet<>();
    }

    public static String getBaseUrl() {
        return BASE_URL;
    }

    public void cancelAllRequests() {
        for (Call call : activeCalls) {
            if (!call.isCanceled()) {
                call.cancel();
            }
        }
        activeCalls.clear();
    }

    private void trackCall(Call call) {
        activeCalls.add(call);
    }

    private void untrackCall(Call call) {
        activeCalls.remove(call);
    }

    private void enqueueCall(Call call, Callback callback) {
        trackCall(call);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                untrackCall(call);
                callback.onResponse(call, response);
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                untrackCall(call);
                callback.onFailure(call, e);
            }
        });
    }

    public void getCsrfCookie(Callback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "sanctum/csrf-cookie")
                .get()
                .build();

        Call call = client.newCall(request);
        enqueueCall(call, callback);
    }

    public void login(String login, String password, String csrfCookie, Callback callback) {
        Log.d(TAG, "Attempting login with login: " + login + ", password: " + password);

        RequestBody requestBody = new FormBody.Builder()
                .add("login", login)
                .add("password", password)
                .build();

        Request request = new Request.Builder()
                .url(BASE_URL + "api/login")
                .addHeader("X-Requested-With", "XMLHttpRequest")
                .addHeader("Cookie", csrfCookie)
                .post(requestBody)
                .build();

        Call call = client.newCall(request);
        enqueueCall(call, callback);
    }

    public void checkTokenValidity(String token, Callback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "api/users/profile/my")
                .addHeader("Authorization", "Bearer " + token)
                .get()
                .build();

        Call call = client.newCall(request);
        enqueueCall(call, callback);
    }

    public void getSchedule(String token, Callback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "api/journals")
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("X-Requested-With", "XMLHttpRequest")
                .get()
                .build();

        Call call = client.newCall(request);
        enqueueCall(call, callback);
    }

    public void getMarks(String token, int journalId, Callback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "api/journals/" + journalId + "/marks")
                .addHeader("Authorization", "Bearer " + token)
                .get()
                .build();

        Call call = client.newCall(request);
        enqueueCall(call, callback);
    }

    public void getScheduleAndMarks(String token, Callback callback) {
        getSchedule(token, new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Log.d(TAG, "Response from getSchedule: " + responseBody);
                    try {
                        JSONArray journalsArray = new JSONArray(responseBody);
                        if (journalsArray.length() > 0) {
                            JSONObject firstJournal = journalsArray.getJSONObject(0);
                            int journalId = firstJournal.getInt("id");

                            Log.d(TAG, "Extracted journalId: " + journalId);

                            getMarks(token, journalId, new Callback() {
                                @Override
                                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                    if (response.isSuccessful()) {
                                        String marksResponseBody = response.body().string();
                                        Log.d(TAG, "Marks response: " + marksResponseBody);
                                        callback.onResponse(call, response.newBuilder().body(ResponseBody.create(marksResponseBody, response.body().contentType())).build());
                                    } else {
                                        String errorMessage = "Server returned unsuccessful response: " + response.code() + " " + response.message();
                                        Log.e(TAG, errorMessage);
                                        callback.onFailure(call, new IOException(errorMessage));
                                    }
                                }

                                @Override
                                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                    Log.e(TAG, "Network error: " + e.getMessage());
                                    callback.onFailure(call, e);
                                }
                            });
                        } else {
                            callback.onFailure(call, new IOException("No journals found in schedule response"));
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Failed to parse schedule response", e);
                        callback.onFailure(call, new IOException("Failed to parse schedule response", e));
                    }
                } else {
                    String errorMessage = "Server returned unsuccessful response: " + response.code() + " " + response.message();
                    Log.e(TAG, errorMessage);
                    callback.onFailure(call, new IOException(errorMessage));
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e(TAG, "Network error: " + e.getMessage());
                callback.onFailure(call, e);
            }
        });
    }

    public void getReplacements(int replacementsNumber, String token, Callback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "api/lessons/shedule/replacements:1")
                .addHeader("X-Requested-With", "XMLHttpRequest")
                .addHeader("Authorization", "Bearer " + token)
                .get()
                .build();

        Call call = client.newCall(request);
        enqueueCall(call, callback);
    }

    public void getTeachers(String token, Callback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "api/teachers")
                .addHeader("X-Requested-With", "XMLHttpRequest")
                .addHeader("Authorization", "Bearer " + token)
                .get()
                .build();

        Call call = client.newCall(request);
        enqueueCall(call, callback);
    }

    public void getmyTeachers(String token, Callback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "api/teachers/my")
                .addHeader("Authorization", "Bearer " + token)
                .get()
                .build();

        Call call = client.newCall(request);
        enqueueCall(call, callback);
    }

    public void getTeacherPhoto(String token, String teacherId, Callback callback) {
        String url = BASE_URL + "api/teachers/" + teacherId + "/avatar";
        Log.d(TAG, "Fetching teacher photo with URL: " + url);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("X-Requested-With", "XMLHttpRequest")
                .addHeader("Authorization", "Bearer " + token)
                .get()
                .build();

        Call call = client.newCall(request);
        enqueueCall(call, callback);
    }
}
