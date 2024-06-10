package com.example.schedule;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

public class CallScheduleFragment extends Fragment {

    public CallScheduleFragment() {
        // Обязательный конструктор без параметров
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.call_schedule, container, false);
    }
}
