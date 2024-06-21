package com.example.schedule;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class GroupsFragment extends Fragment {

    private int course;
    private LinearLayout groupsContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);
        groupsContainer = view.findViewById(R.id.groups_container);

        if (getArguments() != null) {
            course = getArguments().getInt("course");
        }

        // Установка Toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Оберіть групу");
        }
        toolbar.setNavigationOnClickListener(v -> getFragmentManager().popBackStack());

        loadGroups(course);

        return view;
    }

    private void loadGroups(int course) {
        try {
            // Загрузить JSON из файла
            String jsonString = loadJSONFromAsset("schedule.json");
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONObject coursesObject = jsonObject.getJSONObject("courses");
            JSONObject courseObject = coursesObject.getJSONObject(String.valueOf(course));
            JSONObject groupsObject = courseObject.getJSONObject("groups");

            // Получение кастомного шрифта
            Typeface typeface = ResourcesCompat.getFont(getContext(), R.font.montserrat_alternates_bold);

            // Итерация по группам и создание кнопок
            Iterator<String> keys = groupsObject.keys();
            while (keys.hasNext()) {
                String group = keys.next();
                Button groupButton = new Button(getContext());
                groupButton.setText("Група " + group);
                groupButton.setTextSize(16);
                groupButton.setTypeface(typeface);
                groupButton.setTextColor(getResources().getColor(R.color.white));
                groupButton.setBackground(getResources().getDrawable(R.drawable.button_background_group));
                groupButton.setElevation(8);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 15, 0, 20);
                groupButton.setLayoutParams(params);

                groupButton.setOnClickListener(v -> navigateToSchedule(group));
                groupsContainer.addView(groupButton);
            }

        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    private void navigateToSchedule(String group) {
        Bundle bundle = new Bundle();
        bundle.putString("group", group);
        ScheduleFragment scheduleFragment = new ScheduleFragment();
        scheduleFragment.setArguments(bundle);

        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, scheduleFragment)
                .addToBackStack(null)
                .commit();
    }

    private String loadJSONFromAsset(String filename) throws IOException {
        InputStream is = getContext().getAssets().open(filename);
        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();
        return new String(buffer, StandardCharsets.UTF_8);
    }
}
