package com.example.schedule;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class EvaluationFragment extends Fragment {

    private static final String TAG = "EvaluationFragment";
    private RecyclerView recyclerView;
    private EvaluationAdapter adapter;
    private List<JSONObject> evaluations;
    private ApiService apiService;
    private SharedPreferences sharedPreferences;

    private static final long CACHE_EXPIRATION_TIME = 24 * 60 * 60 * 1000L; // 1 день в мілісекундах

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        evaluations = new ArrayList<>();
        apiService = new ApiService();
        sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_evaluation, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewevaluations);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EvaluationAdapter(getContext(), evaluations);
        recyclerView.setAdapter(adapter);

        checkCacheValidityAndLoadData();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        apiService.cancelAllRequests();
    }

    private void checkCacheValidityAndLoadData() {
        long lastCacheTime = sharedPreferences.getLong("lastCacheTime", 0);
        long currentTime = System.currentTimeMillis();

        if ((currentTime - lastCacheTime) < CACHE_EXPIRATION_TIME) {
            loadCachedData();
        } else {
            clearCache();
            fetchEvaluations();
        }
    }

    private void loadCachedData() {
        String cachedData = sharedPreferences.getString("cachedEvaluations", null);
        if (cachedData != null) {
            parseResponse(cachedData);
        }
    }

    private void clearCache() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("cachedEvaluations");
        editor.remove("lastCacheTime");
        editor.apply();
    }

    private void fetchEvaluations() {
        String authToken = sharedPreferences.getString("authToken", null);

        if (authToken == null) {
            Log.e(TAG, "Auth token not found in SharedPreferences");
            return;
        }

        Log.d(TAG, "Auth token found: " + authToken);

        apiService.getScheduleAndMarks(authToken, new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Log.d(TAG, "Response from getScheduleAndMarks: " + responseBody);
                    cacheResponse(responseBody);
                    parseResponse(responseBody);
                } else {
                    Log.e(TAG, "Server returned unsuccessful response: " + response.code() + " " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Network error: " + e.getMessage());
            }
        });
    }

    private void cacheResponse(String responseBody) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("cachedEvaluations", responseBody);
        editor.putLong("lastCacheTime", System.currentTimeMillis());
        editor.apply();
    }

    private void parseResponse(String responseBody) {
        try {
            evaluations.clear();
            JSONObject jsonObject = new JSONObject(responseBody);
            JSONArray marksArray = jsonObject.getJSONArray("marks");
            JSONObject subjectObject = jsonObject.getJSONObject("subject");
            for (int i = 0; i < marksArray.length(); i++) {
                JSONObject markObject = marksArray.getJSONObject(i);
                markObject.put("subject", subjectObject);
                evaluations.add(markObject);
            }

            sortEvaluationsByDate();

            getActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing response", e);
        }
    }

    private void sortEvaluationsByDate() {
        Collections.sort(evaluations, new Comparator<JSONObject>() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            @Override
            public int compare(JSONObject o1, JSONObject o2) {
                try {
                    return sdf.parse(o2.getString("date")).compareTo(sdf.parse(o1.getString("date")));
                } catch (JSONException | ParseException e) {
                    Log.e(TAG, "Error comparing dates", e);
                    return 0;
                }
            }
        });
    }
}
