package com.example.schedule;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.schedule.databinding.FragmentZameniBinding;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ZameniFragment extends Fragment {

    private ApiService apiService;
    private SharedPreferences sharedPreferences;
    private List<JSONObject> replacementsList;
    private static final String TAG = "ZameniFragment";
    private FragmentZameniBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentZameniBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        apiService = new ApiService();
        sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        String authToken = sharedPreferences.getString("authToken", "");

        if (!authToken.isEmpty()) {
            apiService.getReplacements(5, authToken, new ReplacementsCallback());
        } else {
            Log.e(TAG, "Auth token is empty");
        }

        return view;
    }

    private List<JSONObject> parseResponse(String responseBody) {
        List<JSONObject> jsonObjects = new ArrayList<>();
        Log.d(TAG, "Parsing response");

        try {
            JSONArray jsonArray = new JSONArray(responseBody);

            if (jsonArray.length() > 0) {
                final String name = jsonArray.getJSONObject(0).optString("NAME", "");
                requireActivity().runOnUiThread(() -> binding.textViewName.setText(name));
            }

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.optJSONObject(i);
                if (jsonObject != null) {
                    if (isOldFormat(jsonObject)) {
                        jsonObjects.addAll(parseHtmlTable(jsonObject.optString("PREVIEW_TEXT", "")));
                    } else {
                        jsonObjects.addAll(parseNewFormat(jsonObject.optString("PREVIEW_TEXT", "")));
                    }
                }
            }
            Log.d(TAG, "Parsed response successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error parsing response", e);
        }

        return jsonObjects;
    }

    private boolean isOldFormat(JSONObject jsonObject) {
        return jsonObject.has("PREVIEW_TEXT");
    }

    private List<JSONObject> parseNewFormat(String html) {
        List<JSONObject> jsonObjects = new ArrayList<>();
        org.jsoup.nodes.Document doc = org.jsoup.Jsoup.parse(html);
        org.jsoup.select.Elements rows = doc.select("tr");

        for (int i = 1; i < rows.size(); i++) {
            org.jsoup.select.Elements cells = rows.get(i).select("td");
            if (cells.size() > 0) {
                try {
                    JSONObject parsedObject = new JSONObject();
                    parsedObject.put("GROUP", cells.size() > 4 ? cells.get(4).text() : "");
                    parsedObject.put("PAIR", cells.size() > 1 ? cells.get(1).text() : "");
                    parsedObject.put("SCHEDULE_TEACHER", cells.size() > 2 ? cells.get(2).text() : "");
                    parsedObject.put("AUDIENCE", cells.size() > 3 ? cells.get(3).text() : "");
                    parsedObject.put("SCHEDULE", cells.size() > 6 ? cells.get(6).text() : "");
                    parsedObject.put("REPLACEMENT_TEACHER", cells.size() > 10 ? cells.get(10).text() : "");
                    parsedObject.put("REPLACEMENT", cells.size() > 8 ? cells.get(8).text() : "");
                    jsonObjects.add(parsedObject);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing new format row", e);
                }
            }
        }

        return jsonObjects;
    }

    private List<JSONObject> parseHtmlTable(String html) {
        List<JSONObject> jsonObjects = new ArrayList<>();
        org.jsoup.nodes.Document doc = org.jsoup.Jsoup.parse(html);
        org.jsoup.select.Elements rows = doc.select("tr");

        for (int i = 1; i < rows.size(); i++) {
            org.jsoup.select.Elements cells = rows.get(i).select("td");
            if (cells.size() > 0) {
                try {
                    JSONObject parsedObject = new JSONObject();
                    parsedObject.put("GROUP", cells.size() > 0 ? cells.get(0).text() : "");
                    parsedObject.put("PAIR", cells.size() > 1 ? cells.get(1).text() : "");
                    parsedObject.put("SCHEDULE", cells.size() > 2 ? cells.get(2).text() : "");
                    parsedObject.put("SCHEDULE_TEACHER", cells.size() > 3 ? cells.get(3).text() : "");
                    parsedObject.put("REPLACEMENT", cells.size() > 4 ? cells.get(4).text() : "");
                    parsedObject.put("REPLACEMENT_TEACHER", cells.size() > 5 ? cells.get(5).text() : "");
                    parsedObject.put("AUDIENCE", cells.size() > 6 ? cells.get(6).text() : "");
                    jsonObjects.add(parsedObject);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing row", e);
                }
            } else {
                Log.e(TAG, "Skipping row due to insufficient number of cells: " + cells.size());
            }
        }

        return jsonObjects;
    }

    private void updateRecyclerView() {
        if (binding.recyclerViewReplacements != null) {
            binding.recyclerViewReplacements.setLayoutManager(new LinearLayoutManager(requireContext()));
            if (replacementsList != null) {
                binding.recyclerViewReplacements.setAdapter(new ReplacementsAdapter(replacementsList));
                Log.d(TAG, "RecyclerView updated successfully");
            } else {
                Log.e(TAG, "Replacements list is null");
            }
        } else {
            Log.e(TAG, "RecyclerView is null");
        }
    }

    private class ReplacementsCallback implements Callback {
        @Override
        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                Log.d(TAG, "Response body: " + responseBody);

                replacementsList = parseResponse(responseBody);
                requireActivity().runOnUiThread(ZameniFragment.this::updateRecyclerView);
            } else {
                Log.e(TAG, "Unsuccessful response: " + response.code() + " " + response.message());
            }
        }

        @Override
        public void onFailure(@NotNull Call call, @NotNull IOException e) {
            Log.e(TAG, "Network error: " + e.getMessage());
        }
    }
}