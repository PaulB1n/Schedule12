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

import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TeacherFragment extends Fragment {

    private static final String TAG = "TeacherFragment";
    private ApiService apiService;
    private SharedPreferences sharedPreferences;
    private List<JSONObject> teachersList;
    private String authToken;
    private RecyclerView recyclerView;
    private ExecutorService executorService;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_teacher, container, false);

        apiService = new ApiService();
        sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        authToken = sharedPreferences.getString("authToken", "");

        recyclerView = view.findViewById(R.id.recyclerViewTeachers);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        executorService = Executors.newSingleThreadExecutor();

        if (!authToken.isEmpty()) {
            fetchTeachersData();
        } else {
            Log.e(TAG, "Auth token is missing. Redirect to login screen.");
            // Implement redirect to login screen if needed
        }

        return view;
    }

    private void fetchTeachersData() {
        executorService.execute(() -> {
            try {
                List<JSONObject> teachers = apiService.getTeachersData(authToken);
                requireActivity().runOnUiThread(() -> {
                    if (teachers != null) {
                        teachersList = teachers;
                        TeachersAdapter teachersAdapter = new TeachersAdapter(requireContext(), teachersList, authToken);
                        recyclerView.setAdapter(teachersAdapter);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
