package com.example.schedule;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.json.JSONException;
import org.json.JSONObject;

import org.json.JSONArray;

import android.view.View;




public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private boolean isActivityActive;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Инициализация темы
        SharedPreferences sharedPreferences = getSharedPreferences("theme_prefs", Context.MODE_PRIVATE);
        boolean isNightMode = sharedPreferences.getBoolean("theme", false);
        AppCompatDelegate.setDefaultNightMode(isNightMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                com.google.android.gms.base.R.string.common_open_on_phone,
                R.string.close_nav
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        isActivityActive = true;
        apiService = new ApiService();

        // Проверка наличия токена при создании активности
        if (!isTokenAvailable()) {
            // Если токен отсутствует, перенаправьте пользователя на экран входа
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        // Запуск WorkManager для предварительной загрузки данных
        startDataPreloadWorker();

        // Получение и отображение информации о пользователе
        getUserProfile();

        if (savedInstanceState == null) {
            replaceFragment(new ZameniFragment());
            navigationView.setCheckedItem(R.id.nav_zameni);
        }
    }

    private void startDataPreloadWorker() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String authToken = sharedPreferences.getString("authToken", null);

        if (authToken != null) {
            Data inputData = new Data.Builder().putString("authToken", authToken).build();
            OneTimeWorkRequest preloadWorkRequest = new OneTimeWorkRequest.Builder(DataPreloadWorker.class)
                    .setInputData(inputData)
                    .build();
            WorkManager.getInstance(this).enqueue(preloadWorkRequest);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.nav_teacher) {
            replaceFragment(new TeacherFragment());
        } else if (itemId == R.id.nav_my_teacher) {
            replaceFragment(new MyGroupTeachersFragment());
        } else if (itemId == R.id.nav_pari) {
            replaceFragment(new RaspisanieFragment());
        } else if (itemId == R.id.nav_lesson_estimates) {
            replaceFragment(new EvaluationFragment());
        } else if (itemId == R.id.nav_zameni) {
            replaceFragment(new ZameniFragment());
        } else if (itemId == R.id.nav_settings) {
            replaceFragment(new SettingsFragment());
        } else if (itemId == R.id.call_schedule) {
            replaceFragment(new CallScheduleFragment());
        } else if (itemId == R.id.nav_logout) {
            logout();
            Toast.makeText(this, "До побачення! Ви вийшли з системи.", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_schedule_estimates) {
            replaceFragment(new CoursesFragment());
        } else if (itemId == R.id.nav_news) {
            replaceFragment(new NewsFragment());
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);

        if (currentFragment != null) {
            Log.d("FragmentChange", "Replacing fragment from: " + currentFragment.getClass().getSimpleName() +
                    " to: " + fragment.getClass().getSimpleName());
        } else {
            Log.d("FragmentChange", "Initial fragment: " + fragment.getClass().getSimpleName());
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void logout() {
        clearAppData();
        clearAuthToken();
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
    }

    private void clearAppData() {
        try {
            // Clear app cache
            File dir = getCacheDir();
            deleteDir(dir);

            // Clear shared preferences except authToken
            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            String authToken = sharedPreferences.getString("authToken", null);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear().apply();

            if (authToken != null) {
                editor.putString("authToken", authToken);
                editor.apply();
            }

            sharedPreferences = getSharedPreferences("theme_prefs", Context.MODE_PRIVATE);
            sharedPreferences.edit().clear().apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    private boolean isTokenAvailable() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String authToken = sharedPreferences.getString("authToken", "");
        return !TextUtils.isEmpty(authToken);
    }

    private void clearAuthToken() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("authToken");
        editor.apply();
    }

    private void getUserProfile() {
        apiService.getUserData(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        Log.d("MainActivity", "Response body: " + responseBody);

                        JSONObject jsonResponse = new JSONObject(responseBody);
                        JSONObject user = jsonResponse.getJSONObject("user");
                        String fullName = user.getJSONObject("userable").getString("fullname");
                        String roles = user.getString("roles");

                        Log.d("MainActivity", "Full name: " + fullName + ", Roles: " + roles);

                        runOnUiThread(() -> updateNavHeader(fullName, roles));
                    } catch (JSONException e) {
                        Log.e("MainActivity", "Failed to parse JSON", e);
                    }
                } else {
                    Log.e("MainActivity", "Failed to fetch user profile. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("MainActivity", "Network error: " + e.getMessage());
            }
        });
    }




    private void updateNavHeader(String fullName, String roles) {
        Log.d("updateNavHeader", "Full Name: " + fullName + ", Roles: " + roles);

        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0); // Получаем представление заголовка
        TextView fullnameTextView = headerView.findViewById(R.id.fullname_textview);
        TextView roleTextView = headerView.findViewById(R.id.role_textview);

        fullnameTextView.setText(fullName);
        roleTextView.setText(roles);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (isActivityActive) {
            isActivityActive = false;
            return;
        }
        // Проверка наличия токена при восстановлении активности
        if (!isTokenAvailable()) {
            // Если токен отсутствует, перенаправьте пользователя на экран входа
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
