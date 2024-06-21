package com.example.schedule;

import android.graphics.Typeface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class ScheduleFragment extends Fragment {

    private String group;
    private final String[] daysOrder = {"Понеділок", "Вівторок", "Середа", "Четвер", "П'ятниця"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);

        if (getArguments() != null) {
            group = getArguments().getString("group");
        }

        // Установка Toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Розклад");
        }
        toolbar.setNavigationOnClickListener(v -> getFragmentManager().popBackStack());

        LinearLayout scheduleContainer = view.findViewById(R.id.schedule_container);
        try {
            // Загрузить расписание для выбранной группы и отобразить его
            loadScheduleForGroup(group, scheduleContainer);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            TextView textView = new TextView(getContext());
            textView.setText("Не удалось загрузить расписание");
            scheduleContainer.addView(textView);
        }

        return view;
    }

    private void loadScheduleForGroup(String group, LinearLayout scheduleContainer) throws IOException, JSONException {
        String jsonString = loadJSONFromAsset("schedule.json");
        JSONObject jsonObject = new JSONObject(jsonString);
        JSONObject coursesObject = jsonObject.getJSONObject("courses");

        // LinkedHashMap для хранения расписания по дням недели в правильном порядке
        Map<String, JSONArray> scheduleByDay = new LinkedHashMap<>();
        for (String day : daysOrder) {
            scheduleByDay.put(day, new JSONArray());
        }

        // Поиск группы во всех курсах
        for (Iterator<String> it = coursesObject.keys(); it.hasNext(); ) {
            String courseKey = it.next();
            JSONObject courseObject = coursesObject.getJSONObject(courseKey);
            if (courseObject.has("groups")) {
                JSONObject groupsObject = courseObject.getJSONObject("groups");
                if (groupsObject.has(group)) {
                    JSONArray scheduleArray = groupsObject.getJSONArray(group);
                    for (int i = 0; i < scheduleArray.length(); i++) {
                        JSONObject lesson = scheduleArray.getJSONObject(i);
                        String day = lesson.getString("day");
                        if (scheduleByDay.containsKey(day)) {
                            scheduleByDay.get(day).put(lesson);
                        }
                    }
                    break;
                }
            }
        }

        // Получение кастомного шрифта
        Typeface typeface = ResourcesCompat.getFont(getContext(), R.font.montserrat_alternates_bold);

        // Отображение расписания по дням недели
        for (Map.Entry<String, JSONArray> entry : scheduleByDay.entrySet()) {
            String day = entry.getKey();
            JSONArray lessons = entry.getValue();

            if (lessons.length() > 0) {
                TextView dayTextView = new TextView(getContext());
                dayTextView.setText(day);
                dayTextView.setTextSize(24);
                dayTextView.setTypeface(typeface);
                dayTextView.setPadding(0, 16, 0, 16);
                scheduleContainer.addView(dayTextView);

                for (int i = 0; i < lessons.length(); i++) {
                    JSONObject lesson = lessons.getJSONObject(i);

                    CardView cardView = new CardView(getContext());
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    layoutParams.setMargins(0, 8, 0, 8);
                    cardView.setLayoutParams(layoutParams);
                    cardView.setRadius(8);
                    cardView.setCardBackgroundColor(Color.parseColor("#FAFAFA"));
                    cardView.setCardElevation(8);

                    LinearLayout cardLayout = new LinearLayout(getContext());
                    cardLayout.setOrientation(LinearLayout.HORIZONTAL);
                    cardLayout.setPadding(16, 16, 16, 16);

                    // Создание TextView для номера урока в виде круга
                    TextView numberTextView = new TextView(getContext());
                    numberTextView.setText(lesson.getString("time"));
                    numberTextView.setTextSize(18);
                    numberTextView.setTypeface(typeface);
                    numberTextView.setGravity(Gravity.CENTER);
                    numberTextView.setTextColor(Color.WHITE);
                    numberTextView.setBackgroundResource(R.drawable.circle_background);
                    LinearLayout.LayoutParams numberLayoutParams = new LinearLayout.LayoutParams(
                            64, 64
                    );
                    numberLayoutParams.setMargins(0, 0, 16, 0);
                    numberTextView.setLayoutParams(numberLayoutParams);

                    // Создание фона для номера урока
                    GradientDrawable numberBackground = new GradientDrawable();
                    numberBackground.setShape(GradientDrawable.OVAL);
                    numberBackground.setColor(Color.parseColor("#3F51B5"));
                    numberTextView.setBackground(numberBackground);

                    cardLayout.addView(numberTextView);

                    LinearLayout textLayout = new LinearLayout(getContext());
                    textLayout.setOrientation(LinearLayout.VERTICAL);

                    TextView subjectTextView = new TextView(getContext());
                    subjectTextView.setText(lesson.getString("subject"));
                    subjectTextView.setTextSize(16);
                    subjectTextView.setTypeface(typeface);
                    subjectTextView.setTextColor(Color.parseColor("#555555"));
                    textLayout.addView(subjectTextView);

                    TextView teacherTextView = new TextView(getContext());
                    teacherTextView.setText(lesson.getString("teacher"));
                    teacherTextView.setTextSize(16);
                    teacherTextView.setTypeface(typeface);
                    teacherTextView.setTextColor(Color.parseColor("#777777"));
                    textLayout.addView(teacherTextView);

                    cardLayout.addView(textLayout);
                    cardView.addView(cardLayout);
                    scheduleContainer.addView(cardView);
                }
            }
        }
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
