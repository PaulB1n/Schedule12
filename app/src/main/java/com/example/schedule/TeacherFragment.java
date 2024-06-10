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

public class TeacherFragment extends Fragment {

    private static final String TAG = "TeacherFragment";
    private ApiService apiService;
    private SharedPreferences sharedPreferences;
    private List<JSONObject> teachersList;
    private String authToken;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_teacher, container, false);

        apiService = new ApiService();
        sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        authToken = sharedPreferences.getString("authToken", "");

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewTeachers);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        if (!authToken.isEmpty()) {
            fetchTeachersData(recyclerView);
        } else {
            Log.e(TAG, "Auth token is missing. Redirect to login screen.");
            // Implement redirect to login screen if needed
        }

        return view;
    }

    private void fetchTeachersData(RecyclerView recyclerView) {
        apiService.getTeachers(authToken, new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Log.d(TAG, "Response body: " + responseBody);

                    teachersList = parseTeachersResponse(responseBody);

                    requireActivity().runOnUiThread(() -> {
                        TeachersAdapter teachersAdapter = new TeachersAdapter(requireContext(), teachersList, R.drawable.my_teachers, R.drawable.my_teachers);
                        recyclerView.setAdapter(teachersAdapter);
                    });

                } else {
                    Log.e(TAG, "Unsuccessful response: " + response.code() + " " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Network error: " + e.getMessage());
            }
        });
    }

    private List<JSONObject> parseTeachersResponse(String responseBody) {
        List<JSONObject> jsonObjects = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(responseBody);
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObjects.add(jsonArray.getJSONObject(i));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing response", e);
        }
        return jsonObjects;
    }
}
