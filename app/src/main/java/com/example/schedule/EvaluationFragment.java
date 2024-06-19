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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import java.text.ParseException;

public class EvaluationFragment extends Fragment {

    private static final String TAG = "EvaluationFragment";
    private RecyclerView recyclerView;
    private EvaluationAdapter adapter;
    private List<JSONObject> evaluations;
    private ApiService apiService;
    private SharedPreferences sharedPreferences;
    private Spinner subjectSpinner;
    private ProgressBar loadingIndicator;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ExecutorService executorService;

    private static final long CACHE_EXPIRATION_TIME = 24 * 60 * 60 * 1000L; // 1 день в миллисекундах

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        evaluations = Collections.synchronizedList(new ArrayList<>());
        apiService = new ApiService();
        sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        executorService = Executors.newFixedThreadPool(4); // Пул потоков для параллельных задач
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_evaluation, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewevaluations);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EvaluationAdapter(getContext(), evaluations);
        recyclerView.setAdapter(adapter);

        subjectSpinner = view.findViewById(R.id.subjectSpinner);
        loadingIndicator = view.findViewById(R.id.loadingIndicator);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        subjectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedSubject = parent.getItemAtPosition(position).toString();
                adapter.filterBySubject(selectedSubject);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            clearCache();
            fetchEvaluations();
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) { // Check for scroll down
                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == adapter.getItemCount() - 1) {
                        adapter.loadMore();
                    }
                }
            }
        });

        checkCacheValidityAndLoadData();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        apiService.cancelAllRequests();
        executorService.shutdown();
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
            List<JSONObject> tempEvaluations = new ArrayList<>();
            parseResponse(cachedData, tempEvaluations);
            synchronized (evaluations) {
                evaluations.addAll(tempEvaluations);
                sortEvaluationsByDate();
            }
            getActivity().runOnUiThread(() -> {
                adapter.notifyDataSetChanged();
                populateSubjectSpinner();
            });
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

        getActivity().runOnUiThread(() -> loadingIndicator.setVisibility(View.VISIBLE)); // Показать индикатор загрузки

        apiService.getSchedule(authToken, new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Log.d(TAG, "Response from getSchedule: " + responseBody);
                    try {
                        JSONArray journalsArray = new JSONArray(responseBody);
                        if (journalsArray.length() > 0) {
                            List<Future<?>> futures = new ArrayList<>();
                            for (int i = 0; i < journalsArray.length(); i++) {
                                JSONObject journal = journalsArray.getJSONObject(i);
                                int journalId = journal.getInt("id");

                                Log.d(TAG, "Extracted journalId: " + journalId);

                                Future<?> future = executorService.submit(() -> {
                                    apiService.getMarks(authToken, journalId, new Callback() {
                                        @Override
                                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                            if (response.isSuccessful()) {
                                                String marksResponseBody = response.body().string();
                                                Log.d(TAG, "Marks response: " + marksResponseBody);
                                                List<JSONObject> tempEvaluations = new ArrayList<>();
                                                parseResponse(marksResponseBody, tempEvaluations);

                                                synchronized (evaluations) {
                                                    evaluations.addAll(tempEvaluations);
                                                    sortEvaluationsByDate();
                                                }

                                                cacheResponse(marksResponseBody);

                                                getActivity().runOnUiThread(() -> {
                                                    adapter.notifyDataSetChanged();
                                                    populateSubjectSpinner();
                                                });
                                            } else {
                                                Log.e(TAG, "Server returned unsuccessful response: " + response.code() + " " + response.message());
                                            }
                                        }

                                        @Override
                                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                            Log.e(TAG, "Network error: " + e.getMessage());
                                        }
                                    });
                                });

                                futures.add(future);
                            }

                            for (Future<?> future : futures) {
                                try {
                                    future.get();
                                } catch (Exception e) {
                                    Log.e(TAG, "Error waiting for future", e);
                                }
                            }

                            getActivity().runOnUiThread(() -> {
                                loadingIndicator.setVisibility(View.GONE);
                                swipeRefreshLayout.setRefreshing(false);
                            });
                        } else {
                            Log.e(TAG, "No journals found in schedule response");
                            getActivity().runOnUiThread(() -> {
                                loadingIndicator.setVisibility(View.GONE);
                                swipeRefreshLayout.setRefreshing(false);
                            });
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Failed to parse schedule response", e);
                        getActivity().runOnUiThread(() -> {
                            loadingIndicator.setVisibility(View.GONE);
                            swipeRefreshLayout.setRefreshing(false);
                        });
                    }
                } else {
                    Log.e(TAG, "Server returned unsuccessful response: " + response.code() + " " + response.message());
                    getActivity().runOnUiThread(() -> {
                        loadingIndicator.setVisibility(View.GONE);
                        swipeRefreshLayout.setRefreshing(false);
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Network error: " + e.getMessage());
                getActivity().runOnUiThread(() -> {
                    loadingIndicator.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                });
            }
        });
    }

    private void cacheResponse(String responseBody) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("cachedEvaluations", responseBody);
        editor.putLong("lastCacheTime", System.currentTimeMillis());
        editor.apply();
    }

    private void parseResponse(String responseBody, List<JSONObject> tempEvaluations) {
        try {
            JSONObject jsonObject = new JSONObject(responseBody);
            JSONArray marksArray = jsonObject.getJSONArray("marks");
            JSONObject subjectObject = jsonObject.getJSONObject("subject");
            for (int i = 0; i < marksArray.length(); i++) {
                JSONObject markObject = marksArray.getJSONObject(i);
                markObject.put("subject", subjectObject);
                tempEvaluations.add(markObject);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing response", e);
        }
    }

    private void sortEvaluationsByDate() {
        synchronized (evaluations) {
            Collections.sort(evaluations, (o1, o2) -> {
                try {
                    return DateUtils.parseDateString(o2.getString("date"))
                            .compareTo(DateUtils.parseDateString(o1.getString("date")));
                } catch (JSONException | ParseException e) {
                    Log.e(TAG, "Error comparing dates", e);
                    return 0;
                }
            });
        }
    }

    private void populateSubjectSpinner() {
        Set<String> subjects = new HashSet<>();
        subjects.add("All");
        subjects.add("За останій місяць");
        for (JSONObject evaluation : evaluations) {
            try {
                subjects.add(evaluation.getJSONObject("subject").getString("subject_name"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, new ArrayList<>(subjects));
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        subjectSpinner.setAdapter(adapter);
    }
}
