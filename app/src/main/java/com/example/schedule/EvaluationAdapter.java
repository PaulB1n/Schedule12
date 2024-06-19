package com.example.schedule;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Date;

public class EvaluationAdapter extends RecyclerView.Adapter<EvaluationAdapter.ViewHolder> {

    private List<JSONObject> evaluations;
    private List<JSONObject> filteredEvaluations;
    private Context context;
    private int itemsPerPage = 10;
    private int currentPage = 0;

    public EvaluationAdapter(Context context, List<JSONObject> evaluations) {
        this.context = context;
        this.evaluations = evaluations;
        this.filteredEvaluations = new ArrayList<>();
        loadMore();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_evaluation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            JSONObject evaluation = filteredEvaluations.get(position);
            holder.textViewTitle.setText(evaluation.getString("title"));
            holder.textViewDate.setText("Дата: " + DateUtils.formatDateString(evaluation.getString("date")));
            int mark = getMarkValue(evaluation);
            int maxGrade = evaluation.getInt("max_grade");
            holder.textViewActualGrade.setText(getGradeText(mark, maxGrade));

            if (evaluation.has("subject")) {
                JSONObject subject = evaluation.getJSONObject("subject");
                holder.textViewSubject.setText("Предмет: " + subject.getString("subject_name"));
                Log.d("EvaluationAdapter", "Subject: " + subject.getString("subject_name"));
            } else {
                holder.textViewSubject.setText("Предмет: ");
            }

            String type = evaluation.getString("type_str");
            holder.textViewType.setText(getSpannableTypeText(type));
            holder.textViewType.setTextColor(ContextCompat.getColor(context, R.color.text_color));

        } catch (JSONException | ParseException e) {
            Log.e("EvaluationAdapter", "Error parsing evaluation data", e);
        }
    }

    @Override
    public int getItemCount() {
        return filteredEvaluations.size();
    }

    public void loadMore() {
        int start = currentPage * itemsPerPage;
        int end = Math.min(start + itemsPerPage, evaluations.size());
        if (start < end) {
            for (int i = start; i < end; i++) {
                filteredEvaluations.add(evaluations.get(i));
            }
            currentPage++;
            notifyDataSetChanged();
        }
    }

    private int getMarkValue(JSONObject evaluation) throws JSONException {
        try {
            return evaluation.getInt("mark");
        } catch (JSONException e) {
            String markStr = evaluation.getString("mark");
            if ("-".equals(markStr) || "Зар".equals(markStr)) {
                return 0; // або інше значення за замовчуванням
            } else {
                throw e;
            }
        }
    }

    private SpannableString getSpannableTypeText(String type) {
        int typeColorResId;
        switch (type) {
            case "Поточний":
                typeColorResId = R.color.type_current;
                break;
            case "Модульний":
                typeColorResId = R.color.type_modular;
                break;
            case "Підсумковий":
                typeColorResId = R.color.type_final;
                break;
            default:
                typeColorResId = R.color.text_color;
                break;
        }

        String typeText = "Тип завдання: " + type;
        SpannableString spannableTypeString = new SpannableString(typeText);
        spannableTypeString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, typeColorResId)),
                "Тип завдання: ".length(), typeText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableTypeString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                "Тип завдання: ".length(), typeText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableTypeString;
    }

    private SpannableString getGradeText(int mark, int maxGrade) {
        String gradeText = "Оцінка: " + mark + " / " + maxGrade;
        int gradeColorResId;
        if (mark >= maxGrade * 0.75) {
            gradeColorResId = R.color.grade_good;
        } else if (mark >= maxGrade * 0.5) {
            gradeColorResId = R.color.grade_average;
        } else {
            gradeColorResId = R.color.grade_poor;
        }

        SpannableString spannableGradeString = new SpannableString(gradeText);
        spannableGradeString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, gradeColorResId)),
                "Оцінка: ".length(), ("Оцінка: " + mark).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableGradeString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                "Оцінка: ".length(), ("Оцінка: " + mark).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableGradeString;
    }

    public void filterBySubject(String subjectName) {
        filteredEvaluations.clear();
        currentPage = 0; // Скидання лічильника сторінок
        if ("All".equals(subjectName)) {
            filteredEvaluations.addAll(evaluations.subList(0, Math.min(itemsPerPage, evaluations.size())));
        } else if ("За останій місяць".equals(subjectName)) {
            filterByLastMonth();
        } else {
            filteredEvaluations.addAll(evaluations.stream()
                    .filter(evaluation -> {
                        try {
                            return evaluation.getJSONObject("subject").getString("subject_name").equals(subjectName);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            return false;
                        }
                    })
                    .collect(Collectors.toList()));
        }
        loadMore();
    }

    private void filterByLastMonth() {
        long currentTime = System.currentTimeMillis();
        long oneMonthAgo = currentTime - 30L * 24 * 60 * 60 * 1000;
        filteredEvaluations.addAll(evaluations.stream()
                .filter(evaluation -> {
                    try {
                        String dateStr = evaluation.getString("date");
                        Date date = DateUtils.parseDateString(dateStr);
                        return date != null && date.getTime() >= oneMonthAgo;
                    } catch (JSONException | ParseException e) {
                        e.printStackTrace();
                        return false;
                    }
                })
                .collect(Collectors.toList()));
        loadMore();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle, textViewDate, textViewType, textViewActualGrade, textViewSubject;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewType = itemView.findViewById(R.id.textViewType);
            textViewActualGrade = itemView.findViewById(R.id.textViewActualGrade);
            textViewSubject = itemView.findViewById(R.id.textViewSubject);
        }
    }
}
