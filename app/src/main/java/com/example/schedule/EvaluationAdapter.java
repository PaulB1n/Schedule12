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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EvaluationAdapter extends RecyclerView.Adapter<EvaluationAdapter.ViewHolder> {

    private List<JSONObject> evaluations;
    private Context context;
    private SimpleDateFormat inputDateFormat;
    private SimpleDateFormat outputDateFormat;

    public EvaluationAdapter(Context context, List<JSONObject> evaluations) {
        this.context = context;
        this.evaluations = evaluations;
        this.inputDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        this.outputDateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
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
            JSONObject evaluation = evaluations.get(position);
            holder.textViewTitle.setText(evaluation.getString("title"));

            // Преобразування формату дати
            String dateStr = evaluation.getString("date");
            holder.textViewDate.setText("Дата: " + formatDateString(dateStr));

            int mark = evaluation.getInt("mark");
            int maxGrade = evaluation.getInt("max_grade");
            holder.textViewActualGrade.setText(getGradeText(mark, maxGrade));

            // Отримуємо назву предмету з об'єкту subject
            if (evaluation.has("subject")) {
                JSONObject subject = evaluation.getJSONObject("subject");
                holder.textViewSubject.setText("Предмет: " + subject.getString("subject_name"));
                Log.d("EvaluationAdapter", "Subject: " + subject.getString("subject_name"));
            } else {
                holder.textViewSubject.setText("Предмет: ");
            }

            // Встановлюємо колір тексту в залежності від типу завдання
            String type = evaluation.getString("type_str");
            holder.textViewType.setText(getSpannableTypeText(type));
            holder.textViewType.setTextColor(ContextCompat.getColor(context, R.color.text_color));

        } catch (JSONException | ParseException e) {
            Log.e("EvaluationAdapter", "Error parsing evaluation data", e);
        }
    }

    @Override
    public int getItemCount() {
        return evaluations.size();
    }

    private String formatDateString(String dateStr) throws ParseException {
        Date date = inputDateFormat.parse(dateStr);
        return outputDateFormat.format(date);
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
            gradeColorResId = R.color.grade_good; // Високі оцінки
        } else if (mark >= maxGrade * 0.5) {
            gradeColorResId = R.color.grade_average; // Середні оцінки
        } else {
            gradeColorResId = R.color.grade_poor; // Низькі оцінки
        }

        SpannableString spannableGradeString = new SpannableString(gradeText);
        spannableGradeString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, gradeColorResId)),
                "Оцінка: ".length(), ("Оцінка: " + mark).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableGradeString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                "Оцінка: ".length(), ("Оцінка: " + mark).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableGradeString;
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
