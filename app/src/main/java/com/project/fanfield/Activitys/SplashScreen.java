package com.project.fanfield.Activitys;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.project.fanfield.R;
import com.project.fanfield.Services.Singleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

@SuppressLint("CustomSplashScreen")
public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppThemeLight);
        setContentView(R.layout.activity_splash_screen);

        String LToken = getToken();
        Log.e("Token",LToken);
        new Handler().postDelayed(() -> {
            if (!LToken.equals("")) LoginWithToken(LToken);
            else {
                Intent intent = new Intent(SplashScreen.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }, 2000);
    }

    private void LoginWithToken(String token){
        Log.d(TAG, "LoginWithToken: login performed");
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                getResources().getString(R.string.API_URL) + "/api/auth/login",
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if(response.getString("msg").equals("Log in successfully")){

                                Toast.makeText(SplashScreen.this, "Login Successful", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(SplashScreen.this, "Internal error", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volly error", "" + error);
                if(error.networkResponse!=null) {
                    int statusCode = error.networkResponse.statusCode;

                    if (statusCode == 400)
                        Toast.makeText(SplashScreen.this, "Bad Request", Toast.LENGTH_SHORT).show();

                    else if (statusCode == 500)
                        Toast.makeText(SplashScreen.this, "Internal server error", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(SplashScreen.this,LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
                else {
                    Toast.makeText(SplashScreen.this,
                            "Network error : Please check your internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("authtoken", token);
                return headers;
            }
        };

//        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
//                30000,
//                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        Singleton.getInstance(SplashScreen.this).addToRequestQueue(jsonObjectRequest);
    }

    private String getToken(){
        SharedPreferences sharedPreferences = getSharedPreferences("LoginData",MODE_PRIVATE);
        return sharedPreferences.getString("Token","");
    }
}