package com.example.schedule;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NewsFragment extends Fragment {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private NewsAdapter newsAdapter;

    private static final String PREFS_NAME = "NewsCache";
    private static final String NEWS_KEY = "news";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        swipeRefreshLayout.setOnRefreshListener(this::loadNews);

        loadNewsFromCache();  // Load news from cache initially
        loadNews();

        return view;
    }

    private void loadNews() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            List<NewsItem> newsItems = null;
            try {
                newsItems = NewsScraper.fetchNews();
                saveNewsToCache(newsItems);  // Save news to cache
            } catch (IOException e) {
                e.printStackTrace();
            }

            final List<NewsItem> finalNewsItems = newsItems;
            handler.post(() -> {
                if (finalNewsItems != null) {
                    newsAdapter = new NewsAdapter(getContext(), finalNewsItems);
                    recyclerView.setAdapter(newsAdapter);
                }
                swipeRefreshLayout.setRefreshing(false);
            });
        });
    }

    private void loadNewsFromCache() {
        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(NEWS_KEY, null);
        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<NewsItem>>() {}.getType();
            List<NewsItem> newsItems = gson.fromJson(json, type);
            newsAdapter = new NewsAdapter(getContext(), newsItems);
            recyclerView.setAdapter(newsAdapter);
        }
    }

    private void saveNewsToCache(List<NewsItem> newsItems) {
        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(newsItems);
        editor.putString(NEWS_KEY, json);
        editor.apply();
    }
}
