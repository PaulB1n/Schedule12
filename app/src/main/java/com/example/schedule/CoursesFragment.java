package com.example.schedule;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class CoursesFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_courses, container, false);

        Button course1 = view.findViewById(R.id.button_course_1);
        course1.setOnClickListener(v -> navigateToGroups(1));

        Button course2 = view.findViewById(R.id.button_course_2);
        course2.setOnClickListener(v -> navigateToGroups(2));

        Button course3 = view.findViewById(R.id.button_course_3);
        course3.setOnClickListener(v -> navigateToGroups(3));

        Button course4 = view.findViewById(R.id.button_course_4);
        course4.setOnClickListener(v -> navigateToGroups(4));

        return view;
    }

    private void navigateToGroups(int course) {
        Bundle bundle = new Bundle();
        bundle.putInt("course", course);
        GroupsFragment groupsFragment = new GroupsFragment();
        groupsFragment.setArguments(bundle);

        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, groupsFragment)
                .addToBackStack(null)
                .commit();
    }
}
