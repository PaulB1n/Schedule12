package com.example.schedule;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class MyTeachersAdapter extends RecyclerView.Adapter<MyTeachersAdapter.ViewHolder> {
    private List<JSONObject> myTeachersList;
    private String BASE_URL;

    public MyTeachersAdapter(List<JSONObject> myTeachersList, String baseUrl) {
        this.myTeachersList = myTeachersList;
        this.BASE_URL = baseUrl;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_teachers, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            JSONObject teacher = myTeachersList.get(position);

            int teacherId = teacher.getInt("id");
            String teacherName = teacher.getString("FIO_prep");

            holder.textMyTeacherId.setText(String.valueOf(teacherId));
            holder.textMyTeacherName.setText(teacherName);

            // Завантаження аватару за допомогою Glide
            if (teacher.has("avatar_id")) {
                int teacherAvatarId = teacher.getInt("avatar_id");
                String avatarUrl = BASE_URL + "/api/teachers/avatar/" + teacherAvatarId;

                Glide.with(holder.teacherAvatar.getContext())
                        .load(avatarUrl)
                        .apply(new RequestOptions().placeholder(R.drawable.my_teachers))
                        .into(holder.teacherAvatar);
            } else {
                holder.teacherAvatar.setImageResource(R.drawable.my_teachers);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return myTeachersList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textMyTeacherId;
        TextView textMyTeacherName;
        ImageView teacherAvatar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textMyTeacherId = itemView.findViewById(R.id.textMyTeacherId);
            textMyTeacherName = itemView.findViewById(R.id.textMyTeacherName);
            teacherAvatar = itemView.findViewById(R.id.teacherAvatar);
        }
    }
}
