package com.example.schedule;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NewsMainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private NewsAdapter newsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_main);

        recyclerView = findViewById(R.id.recyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        swipeRefreshLayout.setOnRefreshListener(this::loadNews);

        loadNews();
    }

    private void loadNews() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            List<NewsItem> newsItems = null;
            try {
                newsItems = NewsScraper.fetchNews();
            } catch (IOException e) {
                e.printStackTrace();
            }

            final List<NewsItem> finalNewsItems = newsItems;
            handler.post(() -> {
                if (finalNewsItems != null) {
                    newsAdapter = new NewsAdapter(NewsMainActivity.this, finalNewsItems);
                    recyclerView.setAdapter(newsAdapter);
                }
                swipeRefreshLayout.setRefreshing(false);
            });
        });
    }
}
