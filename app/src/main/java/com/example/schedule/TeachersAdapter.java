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

import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TeachersAdapter extends RecyclerView.Adapter<TeachersAdapter.ViewHolder> {
    private static final String TAG = "TeachersAdapter";
    private Context context;
    private List<JSONObject> teachersList;
    private String authToken;
    private Picasso picasso;

    public TeachersAdapter(Context context, List<JSONObject> teachersList, String authToken) {
        this.context = context;
        this.teachersList = teachersList;
        this.authToken = authToken;

        OkHttpClient picassoClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request request = original.newBuilder()
                            .header("Authorization", "Bearer " + authToken)
                            .header("Accept", "*/*")
                            .header("User-Agent", "PostmanRuntime/7.39.0")
                            .build();
                    Response response = chain.proceed(request);
                    Log.d(TAG, "Response headers: " + response.headers());
                    Log.d(TAG, "Response body: " + response.body().string());
                    return response;
                })
                .build();

        this.picasso = new Picasso.Builder(context)
                .downloader(new OkHttp3Downloader(picassoClient))
                .build();
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

            picasso.load(imageUrl)
                    .placeholder(R.drawable.my_teachers)
                    .error(R.drawable.menu_my_teacher_v2)
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
            holder.imageTeacher.setImageResource(R.drawable.menu_my_teacher_v2); // Error image
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
