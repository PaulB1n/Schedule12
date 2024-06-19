package com.example.schedule;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {

    private List<JSONObject> scheduleList;

    public ScheduleAdapter(List<JSONObject> scheduleList) {
        this.scheduleList = scheduleList;
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule, parent, false);
        return new ScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {

        JSONObject scheduleItem = scheduleList.get(position);


        try {
            JSONObject subjectObject = scheduleItem.getJSONObject("subject");
            String subjectName = scheduleItem.getJSONObject("subject").getString("subject_name");
            String teacherName = scheduleItem.getJSONObject("subject").getString("short_title");
            String description = "";

            holder.textViewSubjectName.setText(subjectName);
            holder.textViewTeacherName.setText(teacherName);

            // Проверка наличия описания перед установкой
            if (!description.isEmpty()) {
                holder.textViewDescription.setText(description);

            } else {
                holder.textViewDescription.setVisibility(View.GONE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public int getItemCount() {
        return scheduleList.size();
    }

    static class ScheduleViewHolder extends RecyclerView.ViewHolder {

        TextView textViewSubjectName;
        TextView textViewTeacherName;
        TextView textViewDescription;

        public ScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewSubjectName = itemView.findViewById(R.id.textViewSubjectName);
            textViewTeacherName = itemView.findViewById(R.id.textViewTeacherName);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
        }
    }
}
