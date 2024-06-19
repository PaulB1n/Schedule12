package com.example.schedule;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import java.io.IOException;


public class DataPreloadWorker extends Worker {

    private ApiService apiService;

    public DataPreloadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        apiService = new ApiService();
    }

    @NonNull
    @Override
    public Result doWork() {
        String authToken = getInputData().getString("authToken");
        if (authToken == null) {
            return Result.failure();
        }

        apiService.getSchedule(authToken, new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Handle response, save data to cache or database
                    String responseBody = response.body().string();
                    // Save the data to cache (SharedPreferences or database)
                    SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("cachedEvaluations", responseBody);
                    editor.putLong("lastCacheTime", System.currentTimeMillis());
                    editor.apply();
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // Handle failure
            }
        });

        return Result.success();
    }
}
