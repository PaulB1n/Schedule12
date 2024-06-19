package com.example.schedule;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.List;

public class TeachersAdapter extends RecyclerView.Adapter<TeachersAdapter.ViewHolder> {
    private static final String TAG = "TeachersAdapter";
    private Context context;
    private List<JSONObject> teachersList;
    private int placeholderResource;
    private int errorResource;

    public TeachersAdapter(Context context, List<JSONObject> teachersList, int placeholderResource, int errorResource) {
        this.context = context;
        this.teachersList = teachersList;
        this.placeholderResource = placeholderResource;
        this.errorResource = errorResource;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_teacher, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            JSONObject teacher = teachersList.get(position);

            int teacherId = teacher.getInt("id");
            String teacherName = teacher.getString("FIO_prep");

            holder.textTeacherId.setText(String.valueOf(teacherId));
            holder.textTeacherName.setText(teacherName);

            String imageUrl = "https://api.college.ks.ua/api/teachers/" + teacherId + "/avatar";
            Log.d(TAG, "Loading Image URL: " + imageUrl);

            Picasso.get()
                    .load(imageUrl)
                    .placeholder(placeholderResource)
                    .error(errorResource)
                    .into(holder.imageTeacher, new Callback() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "Image loaded successfully with Picasso");
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e(TAG, "Image load failed with Picasso for URL: " + imageUrl, e);
                        }
                    });

        } catch (Exception e) {
            Log.e(TAG, "Exception in onBindViewHolder", e);
            holder.imageTeacher.setImageResource(errorResource);
        }
    }

    @Override
    public int getItemCount() {
        return teachersList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textTeacherId;
        TextView textTeacherName;
        ImageView imageTeacher;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textTeacherId = itemView.findViewById(R.id.textTeacherId);
            textTeacherName = itemView.findViewById(R.id.textTeacherName);
            imageTeacher = itemView.findViewById(R.id.imageTeacher);
        }
    }
}
