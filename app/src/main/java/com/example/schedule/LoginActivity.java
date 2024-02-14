package com.example.schedule;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.Nullable;

import androidx.appcompat.app.AppCompatActivity;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        apiService = new ApiService();
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        emailEditText = findViewById(R.id.email_et);
        passwordEditText = findViewById(R.id.password_et);
        loginButton = findViewById(R.id.login_btn);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });
    }

    private void attemptLogin() {
        apiService.getCsrfCookie(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String csrfCookie = response.header("Set-Cookie");
                    String login = emailEditText.getText().toString();
                    String password = passwordEditText.getText().toString();

                    apiService.login(login, password, csrfCookie, new Callback() {
                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            if (response.isSuccessful()) {
                                handleSuccessfulLogin(response);
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

    private void handleSuccessfulLogin(Response response) {
        try {
            String jsonResponse = response.body().string();
            JSONObject jsonObject = new JSONObject(jsonResponse);
            String authToken = jsonObject.getString("token");

            if (authToken != null && !authToken.isEmpty()) {
                saveTokenAndNavigate(authToken);
            } else {
                handleApiError(response);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
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

    private void saveTokenAndNavigate(String authToken) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("authToken", authToken);
        editor.apply();
        navigateToMain();
    }
    private void navigateToMain() {
        Log.d("Navigation", "Navigating to Main Activity");
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}

