package com.project.fanfield.Activitys;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
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

public class OtpActivity extends AppCompatActivity {

    Button submitOtp;
    EditText otpInput;
    RelativeLayout progressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppThemeLight);
        setContentView(R.layout.activity_otp);

        progressView = findViewById(R.id.otpProgressView);

        Intent intent = getIntent();
        String Phone = intent.getStringExtra("PHONE");

        otpInput = findViewById(R.id.otp_input);
        submitOtp = findViewById(R.id.submitOtp);

        submitOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(otpInput.getText() == null || otpInput.getText().toString().equals(""))
                    otpInput.setError("Please enter OTP");
                else {
                    progressView.setVisibility(View.VISIBLE);
                    setSubmitOtp(Phone,otpInput.getText().toString());
                }
            }
        });
    }

    public void setSubmitOtp(String phone,String otp){
        Map<String, String> params = new HashMap<>();
        params.put("phoneNumber",phone);
        params.put("otp",otp);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                getResources().getString(R.string.API_URL)+"/api/auth/verify-otp",
                new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if(response.getBoolean("success")||
                                    !response.getString("token").equals("")){

                                saveToken(response.getString("token"));

                                Toast.makeText(OtpActivity.this, "Account created successfully", Toast.LENGTH_SHORT).show();

                                progressView.setVisibility(View.INVISIBLE);
                                Intent intent = new Intent(OtpActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(OtpActivity.this, "Internal error", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volly error", "" + error);
                if (error.networkResponse!=null) {
                    int statusCode = error.networkResponse.statusCode;
                    if (statusCode == 401)
                        Toast.makeText(OtpActivity.this, "This OTP is invalid or expired", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(OtpActivity.this, "Network error !", Toast.LENGTH_SHORT).show();
                }
//                progressView.setVisibility(View.GONE);
            }
        }){
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

        Singleton.getInstance(OtpActivity.this).addToRequestQueue(jsonObjectRequest);
    }

    private void saveToken(String token){
        SharedPreferences sharedPreferences = getSharedPreferences("LoginData",MODE_PRIVATE);
        SharedPreferences.Editor sEditor = sharedPreferences.edit();
        sEditor.putString("Token",token);
        sEditor.apply();
    }
}