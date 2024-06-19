package com.example.schedule;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment {

    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_THEME = "theme";
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        Switch themeSwitch = rootView.findViewById(R.id.theme_switch);

        sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isNightMode = getSavedThemePreference();

        themeSwitch.setChecked(isNightMode);

        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateTheme(isChecked);
            saveThemePreference(isChecked);
        });

        return rootView;
    }

    private boolean getSavedThemePreference() {
        return sharedPreferences.getBoolean(KEY_THEME, false);
    }

    private void saveThemePreference(boolean isNightMode) {
        sharedPreferences.edit().putBoolean(KEY_THEME, isNightMode).apply();
    }

    private void updateTheme(boolean isNightMode) {
        AppCompatDelegate.setDefaultNightMode(isNightMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        requireActivity().recreate();
    }
}
