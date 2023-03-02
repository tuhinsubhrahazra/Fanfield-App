package com.project.fanfield.Activitys;

import static android.content.ContentValues.TAG;

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

public class SignUpActivity extends AppCompatActivity {

    RelativeLayout progressView;
    EditText firstName;
    EditText lastName;
    EditText gender;
    EditText dob;
    EditText emailOrMobile;
    EditText password;
    EditText confirmPassword;
    Button singUpBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppThemeLight);
        setContentView(R.layout.activity_sign_up);

        progressView = findViewById(R.id.progressView);
        firstName = findViewById(R.id.firstName);
        lastName = findViewById(R.id.lastName);
        gender = findViewById(R.id.gender);
        dob = findViewById(R.id.dob);
        emailOrMobile = findViewById(R.id.emailOrMobile);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirmPassword);
        singUpBtn = findViewById(R.id.singUpBtn);

        singUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean emptyField = false;
                EditText[] editTexts = new EditText[]{firstName, lastName, gender, dob, emailOrMobile, password, confirmPassword};
                for (EditText editText : editTexts) {
                    if (editText.getText() == null || editText.getText().toString().equals("")) {
                        editText.setError("Please enter this field");
                        emptyField = true;
                    }
                }

                if (!emptyField && !password.getText().toString().equals(confirmPassword.getText().toString())) {
                    confirmPassword.setText("");
                    confirmPassword.setError("password does not mach");
                    emptyField = true;
                }

                if (!emptyField){
                    singUp(firstName.getText().toString(),
                            lastName.getText().toString(),
                            emailOrMobile.getText().toString(),
                            password.getText().toString(),
                            gender.getText().toString(),
                            dob.getText().toString());

                    progressView.setVisibility(View.VISIBLE);
                }

            }
        });

    }

    private void singUp(String firstname,String lastname,String emailormobile, String password, String gender, String DOB){
        Map<String, String> params = new HashMap<>();
        params.put("fristname",firstname);
        params.put("lastname",lastname);
        params.put("emailormobile",emailormobile);
        params.put("password",password);
        params.put("gender",gender);
        params.put("DOB",DOB);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                getResources().getString(R.string.API_URL)+"/api/auth/signup",
                new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if(response.getString("msg").equals("OTP send successfully")){
                                progressView.setVisibility(View.GONE);

                                Intent intent = new Intent(SignUpActivity.this, OtpActivity.class);
                                intent.putExtra("PHONE",emailormobile);
                                startActivity(intent);
//                                finish();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(SignUpActivity.this, "Internal error", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "onResponse: "+e);
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volly error", "" + error);
                if (error.networkResponse!=null) {
                    int statusCode = error.networkResponse.statusCode;
                    if (statusCode == 409)
                        Toast.makeText(SignUpActivity.this, "You already have an account", Toast.LENGTH_SHORT).show();
                    else if (statusCode == 500)
                        Toast.makeText(SignUpActivity.this, "Internal server error", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(SignUpActivity.this, "Network error !", Toast.LENGTH_SHORT).show();
                }
                progressView.setVisibility(View.GONE);
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

        Singleton.getInstance(SignUpActivity.this).addToRequestQueue(jsonObjectRequest);
    }

    private void saveToken(String token){
        SharedPreferences sharedPreferences = getSharedPreferences("LoginData",MODE_PRIVATE);
        SharedPreferences.Editor sEditor = sharedPreferences.edit();
        sEditor.putString("Token",token);
        sEditor.apply();
    }
}