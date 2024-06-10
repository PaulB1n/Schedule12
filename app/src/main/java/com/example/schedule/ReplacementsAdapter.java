package com.example.schedule;

import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.TooltipCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schedule.databinding.ItemZameniBinding;

import org.json.JSONObject;

import java.util.List;

public class ReplacementsAdapter extends RecyclerView.Adapter<ReplacementsAdapter.ViewHolder> {

    private List<JSONObject> replacementsList;
    private static final String TAG = "ReplacementsAdapter";

    public ReplacementsAdapter(List<JSONObject> replacementsList) {
        this.replacementsList = replacementsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "Creating ViewHolder");
        ItemZameniBinding binding = ItemZameniBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JSONObject replacement = replacementsList.get(position);

        String group = replacement.optString("GROUP", "");
        String pair = replacement.optString("PAIR", "");
        String schedule = replacement.optString("SCHEDULE", "");
        String scheduleTeacher = replacement.optString("SCHEDULE_TEACHER", "");
        String replacementLesson = replacement.optString("REPLACEMENT", "");
        String replacementTeacher = replacement.optString("REPLACEMENT_TEACHER", "");
        String audience = replacement.optString("AUDIENCE", "");

        Log.d(TAG, "Binding data for position " + position + ": group=" + group + ", pair=" + pair +
                ", schedule=" + schedule + ", scheduleTeacher=" + scheduleTeacher +
                ", replacementLesson=" + replacementLesson + ", replacementTeacher=" + replacementTeacher +
                ", audience=" + audience);

        setTextViewVisibilityAndContent(holder.binding.textViewGroup, group);
        setTextViewVisibilityAndContent(holder.binding.textViewPair, pair);
        setTextViewVisibilityAndContent(holder.binding.textViewSchedule, schedule);
        setTextViewVisibilityAndContent(holder.binding.textViewScheduleTeacher, scheduleTeacher);
        setTextViewVisibilityAndContent(holder.binding.textViewReplacement, replacementLesson);
        setTextViewVisibilityAndContent(holder.binding.textViewReplacementTeacher, replacementTeacher);
        setTextViewVisibilityAndContent(holder.binding.textViewAudience, audience);
    }

    private void setTextViewVisibilityAndContent(TextView textView, String content) {
        if (TextUtils.isEmpty(content)) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.VISIBLE);
            textView.setText(Html.fromHtml(content, Html.FROM_HTML_MODE_COMPACT));
            TooltipCompat.setTooltipText(textView, content);
        }
    }

    @Override
    public int getItemCount() {
        return replacementsList != null ? replacementsList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemZameniBinding binding;

        public ViewHolder(@NonNull ItemZameniBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}