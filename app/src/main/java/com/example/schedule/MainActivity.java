package com.example.schedule;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
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

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        // Перевірка наявності токену при створенні активності
        if (!isTokenAvailable()) {
            // Якщо токен відсутній, перенаправте користувача на екран входу
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        if (savedInstanceState == null) {
            replaceFragment(new RaspisanieFragment());
            navigationView.setCheckedItem(R.id.nav_pari);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.nav_teacher) {
            replaceFragment(new TeacherFragment());
        } else if (itemId == R.id.nav_pari) {
            replaceFragment(new RaspisanieFragment());
        } else if (itemId == R.id.nav_zameni) {
            replaceFragment(new ZameniFragment());
        } else if (itemId == R.id.nav_settings) {
            replaceFragment(new SettingsFragment());
        } else if (itemId == R.id.nav_logout) {
            logout();
            Toast.makeText(this, "Logout!", Toast.LENGTH_SHORT).show();
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
        // Очищення токену при виході
        clearAuthToken();
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
    }

    // Перевірка наявності токену в SharedPreferences
    private boolean isTokenAvailable() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String authToken = sharedPreferences.getString("authToken", "");
        return !TextUtils.isEmpty(authToken);
    }

    // Очищення токену в SharedPreferences
    private void clearAuthToken() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("authToken");
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Перевірка наявності токену при відновленні активності
        if (!isTokenAvailable()) {
            // Якщо токен відсутній, перенаправте користувача на екран входу
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

