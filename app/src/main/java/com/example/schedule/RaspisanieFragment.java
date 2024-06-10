package com.example.schedule;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class RaspisanieFragment extends Fragment {

    private ApiService apiService;
    private SharedPreferences sharedPreferences;
    private List<JSONObject> scheduleItemsList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        apiService = new ApiService();
        sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        String authToken = sharedPreferences.getString("authToken", "");

        if (!authToken.isEmpty()) {
            apiService.getSchedule(authToken, new Callback() {
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        scheduleItemsList = parseResponse(responseBody);
                        updateRecyclerView();
                    } else {
                        Log.e("RaspisanieFragment", "Unsuccessful response: " + response.code() + " " + response.message());
                    }
                }

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e("RaspisanieFragment", "Network error: " + e.getMessage());
                }
            });
        }

        return inflater.inflate(R.layout.fragment_raspisanie, container, false);
    }

    private List<JSONObject> parseResponse(String responseBody) {
        List<JSONObject> jsonObjects = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(responseBody);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                jsonObjects.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObjects;
    }

    private void updateRecyclerView() {
        if (getView() != null) {
            requireActivity().runOnUiThread(() -> {
                RecyclerView recyclerView = getView().findViewById(R.id.recyclerViewSchedule);
                ScheduleAdapter scheduleAdapter = new ScheduleAdapter(scheduleItemsList);
                recyclerView.setAdapter(scheduleAdapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            });
        }
    }
}
