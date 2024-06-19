package com.example.schedule;

import android.widget.TextView;
import androidx.core.content.ContextCompat;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private ApiService apiService;
    private SharedPreferences sharedPreferences;
    private boolean isTransitionPerformed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        apiService = new ApiService();
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        emailEditText = findViewById(R.id.email_et);
        passwordEditText = findViewById(R.id.password_et);
        loginButton = findViewById(R.id.login_btn);

        loginButton.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        isTransitionPerformed = false;

        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showSnackbar("Поля login та password не можуть бути пустими");
            return;
        }
        if (password.length() < 8) {
            showSnackbar("Пароль повинен бути не менше 8 символів");
            return;
        }

        fetchCsrfCookieAndLogin(email, password);
    }

    private void fetchCsrfCookieAndLogin(String email, String password) {
        apiService.getCsrfCookie(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String csrfCookie = response.header("Set-Cookie");
                    if (csrfCookie == null || csrfCookie.isEmpty()) {
                        showSnackbar("Не вдалося отримати CSRF cookie");
                        return;
                    }
                    loginWithCsrfCookie(email, password, csrfCookie);
                } else {
                    showSnackbar("Помилка отримання CSRF cookie");
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                showNetworkError(e);
            }
        });
    }

    private void loginWithCsrfCookie(String email, String password, String csrfCookie) {
        apiService.login(email, password, csrfCookie, new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    handleSuccessfulLogin(response);
                } else {
                    showSnackbar("Логін або пароль не вірний");
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                showNetworkError(e);
            }
        });
    }

    private void handleSuccessfulLogin(Response response) {
        try {
            if (response.body() == null) {
                showSnackbar("Порожня відповідь від сервера");
                return;
            }
            String jsonResponse = response.body().string();
            JSONObject jsonObject = new JSONObject(jsonResponse);
            String authToken = jsonObject.getString("token");

            if (authToken != null && !authToken.isEmpty()) {
                saveTokenAndNavigate(authToken);
            } else {
                showSnackbar("Невірний токен автентифікації");
            }
        } catch (IOException | JSONException e) {
            Log.e("LoginError", "Error handling login response", e);
            showSnackbar("Помилка обробки відповіді сервера");
        }
    }

    private void showSnackbar(String message) {
        View view = findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);

        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(ContextCompat.getColor(this, R.color.snackbar_background_color));

        TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(ContextCompat.getColor(this, R.color.snackbar_text_color));

        snackbar.setActionTextColor(ContextCompat.getColor(this, R.color.snackbar_action_text_color));
        snackbar.show();
    }

    private void showNetworkError(IOException e) {
        showSnackbar("Невірний логін або пароль");
        Log.e("NetworkError", e.getMessage());
    }

    private void saveTokenAndNavigate(String authToken) {
        Log.d("TokenSave", "Token saved: " + authToken);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("authToken", authToken);
        editor.apply();
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        isTransitionPerformed = true;
        finish();
    }
}
