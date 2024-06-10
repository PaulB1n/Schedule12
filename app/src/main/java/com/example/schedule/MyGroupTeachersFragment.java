package com.example.schedule;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MyGroupTeachersFragment extends Fragment {

    private static final String TAG = "MyGroupTeachersFragment";
    private ApiService apiService;
    private SharedPreferences sharedPreferences;
    private List<JSONObject> myTeachersList;
    private View rootView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_my_group_teachers, container, false);

        initializeComponents();
        fetchTeachersData();

        return rootView;
    }

    private void initializeComponents() {
        apiService = new ApiService();
        sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        myTeachersList = new ArrayList<>();
    }

    private void fetchTeachersData() {
        String authToken = sharedPreferences.getString("authToken", "");

        if (!authToken.isEmpty()) {
            apiService.getmyTeachers(authToken, new Callback() {
                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        myTeachersList = parseTeachersResponse(responseBody);
                        rootView.post(MyGroupTeachersFragment.this::updateRecyclerView);
                    } else {
                        Log.e(TAG, "Unsuccessful response: " + response.code() + " " + response.message());
                    }
                }

                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Log.e(TAG, "Network error: " + e.getMessage());
                }
            });
        } else {
            Log.e(TAG, "Auth token is not set");
        }
    }

    private List<JSONObject> parseTeachersResponse(String responseBody) {
        List<JSONObject> jsonObjects = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(responseBody);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.optJSONObject(i);
                if (jsonObject != null) {
                    jsonObjects.add(jsonObject);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing response", e);
        }
        return jsonObjects;
    }

    private void updateRecyclerView() {
        if (rootView != null && myTeachersList != null) {
            RecyclerView recyclerView = rootView.findViewById(R.id.recyclerViewMyGroupTeachers);
            MyTeachersAdapter myTeachersAdapter = new MyTeachersAdapter(myTeachersList, ApiService.getBaseUrl());
            recyclerView.setAdapter(myTeachersAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        }
    }
}
