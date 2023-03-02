package com.project.fanfield.Activitys;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.project.fanfield.R;
import com.project.fanfield.Services.Singleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    public EditText userID;
    public EditText password;
    public Button loginBtn;
    public TextView register_txt;
    ProgressBar login_progress_bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppThemeLight);
        setContentView(R.layout.activity_login);

        userID = findViewById(R.id.userID);
        password = findViewById(R.id.password);
        loginBtn = findViewById(R.id.loginBtn);
        register_txt = findViewById(R.id.register_txt);
        login_progress_bar = findViewById(R.id.login_progress_bar);

        register_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this,SignUpActivity.class);
                startActivity(intent);
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(userID.getText()==null || userID.getText().toString().equals("")){
                    userID.setError("Please enter your phone or email");
                }
                if(password.getText()==null || password.getText().toString().equals("")){
                    password.setError("Please enter your phone or email");
                }
                else {
                    loginBtn.setVisibility(View.GONE);
                    login_progress_bar.setVisibility(View.VISIBLE);
                    LoginWithoutToken(userID.getText().toString(),password.getText().toString());
                }
            }
        });

    }

    private void LoginWithoutToken(String id, String password){
        Map<String, String> params = new HashMap<String, String>();
        params.put("id", id);
        params.put("password", password);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                getResources().getString(R.string.API_URL) + "/api/auth/login",
                new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if(response.getString("msg").equals("Log in successfully")||
                                    !response.getString("token").equals("")){

                                loginBtn.setVisibility(View.VISIBLE);
                                login_progress_bar.setVisibility(View.GONE);

                                saveToken(response.getString("token"));

                                Snackbar.make(findViewById(android.R.id.content),"Login Successful",Snackbar.LENGTH_SHORT).show();

                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        } catch (JSONException e) {
                            Snackbar.make(findViewById(android.R.id.content),"Internal error",Snackbar.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volly error", "" + error);
                if (error.networkResponse!=null) {
                    int statusCode = error.networkResponse.statusCode;
                    if (statusCode == 400)
                        Snackbar.make(findViewById(android.R.id.content),"Bad Request",Snackbar.LENGTH_LONG).show();
                    else if (statusCode == 404)
                        Snackbar.make(findViewById(android.R.id.content),"Invalid Username or Password",Snackbar.LENGTH_LONG).show();
                    else if (statusCode == 500)
                        Snackbar.make(findViewById(android.R.id.content),"Internal server error",Snackbar.LENGTH_LONG).show();
                }
                else {
                    Snackbar.make(findViewById(android.R.id.content),"Network error, please check your internet connection",Snackbar.LENGTH_LONG).show();
                }

                loginBtn.setVisibility(View.VISIBLE);
                login_progress_bar.setVisibility(View.GONE);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };

//        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
//                0,
//                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        Singleton.getInstance(LoginActivity.this).addToRequestQueue(jsonObjectRequest);
    }

    private void saveToken(String token){
        SharedPreferences sharedPreferences = getSharedPreferences("LoginData",MODE_PRIVATE);
        SharedPreferences.Editor sEditor = sharedPreferences.edit();
        sEditor.putString("Token",token);
        sEditor.apply();
    }
}