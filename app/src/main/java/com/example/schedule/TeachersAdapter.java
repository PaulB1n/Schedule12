package com.example.schedule;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.RequestListener;

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

            Glide.with(context)
                    .load(imageUrl)
                    .apply(new RequestOptions()
                            .placeholder(placeholderResource)
                            .error(errorResource))
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            Log.e(TAG, "Image load failed for URL: " + imageUrl, e);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .into(holder.imageTeacher);

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
