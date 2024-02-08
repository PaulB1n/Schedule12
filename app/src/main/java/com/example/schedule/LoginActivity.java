package com.example.schedule;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Firebase
        FirebaseApp.initializeApp(this);

        //Realtime Database
        databaseReference = FirebaseDatabase.getInstance().getReference();

        emailEditText = findViewById(R.id.email_et);
        passwordEditText = findViewById(R.id.password_et);
        loginButton = findViewById(R.id.login_btn);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // запит CSRF cookie
                ApiService apiService = new ApiService();
                apiService.getCsrfCookie(new Callback() {
                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        if (response.isSuccessful()) {
                            // Запит CSRF cookie успішний
                            String csrfCookie = response.header("Set-Cookie");

                            // Отримання введених даних користувача
                            String login = emailEditText.getText().toString();
                            String password = passwordEditText.getText().toString();

                            //  запиту на вхід із передачею CSRF cookie
                            apiService.login(login, password, csrfCookie, new Callback() {
                                @Override
                                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                    if (response.isSuccessful()) {
                                        String jsonResponse = response.body().string();
                                        try {
                                            JSONObject jsonObject = new JSONObject(jsonResponse);
                                            String authToken = jsonObject.getString("token");

                                            // Перевірка, чи отриманий токен
                                            if (authToken != null && !authToken.isEmpty()) {
                                                saveTokenToFirebase(authToken);

                                                // Збереження токена в SharedPreferences
                                                SharedPreferences preferences = getSharedPreferences("app_prefs", MODE_PRIVATE);
                                                SharedPreferences.Editor editor = preferences.edit();
                                                editor.putString("auth_token", authToken);
                                                editor.apply();

                                                // Перехід в MainActivity
                                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                handleApiError(response);
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        handleApiError(response);
                                    }
                                }

                                @Override
                                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                    handleNetworkError(e);
                                }
                            });
                        } else {
                            handleApiError(response);
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        handleNetworkError(e);
                    }
                });
            }
        });
    }

    private void handleApiError(Response response) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e("AuthError", "Ошибка авторизации: " + response.code() + " " + response.message());
            }
        });
    }

    private void handleNetworkError(IOException e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e("NetworkError", "Ошибка сети: " + e.getMessage());
            }
        });
    }

    private void saveTokenToFirebase(String authToken) {
        // Получение текущего пользователя Firebase
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        String userId = firebaseAuth.getCurrentUser().getUid();

        // Сохранение токена в Realtime Database
        databaseReference.child("users").child(userId).child("token").setValue(authToken);
    }
}
