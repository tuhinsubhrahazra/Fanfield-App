package com.project.fanfield.Activitys;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.project.fanfield.R;
import com.project.fanfield.Services.Singleton;
import com.project.fanfield.Services.VolleyMultipartRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CreatePost extends AppCompatActivity {

    ImageView create_post_back;
    MaterialButton postBtn;
    EditText create_post_text;
    ImageView create_post_image;
    Button create_post_browse_btn;
    int IMAGE_CHOOSER_REQUEST_CODE = 200;
    Uri fileUri;
    RelativeLayout progressView;
    String userId;
    String BOUNDARY = String.valueOf(System.currentTimeMillis());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppThemeLight);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);
        create_post_back = findViewById(R.id.create_post_back);
        postBtn = findViewById(R.id.postBtn);
        create_post_text = findViewById(R.id.create_post_text);
        create_post_browse_btn = findViewById(R.id.create_post_browse_btn);
        create_post_image = findViewById(R.id.create_post_image);
        progressView = findViewById(R.id.progressView);

        Intent intent = getIntent();
        userId = intent.getStringExtra("USER_ID");

        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(create_post_text.getText() ==null || create_post_text.getText().toString().equals("")){
                    create_post_text.setError("Please write something");
                }else{
                    progressView.setVisibility(View.VISIBLE);
                    createPost(fileUri);
//                    uploadProfilePicture(fileUri);
                }
            }
        });

        create_post_browse_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageChooser();
            }
        });

        create_post_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    public void imageChooser(){
        Intent i = new Intent();

        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        i.putExtra(Intent.EXTRA_MIME_TYPES, new String[] {"image/*", "video/*"});
        i.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(i, "Select Media"), IMAGE_CHOOSER_REQUEST_CODE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_CHOOSER_REQUEST_CODE) {
                // Get the url of the image from data
                Uri selectedImageUri = data.getData();
                fileUri = selectedImageUri;
                create_post_image.setImageURI(selectedImageUri);
                Log.e(TAG, "onActivityResult0: "+selectedImageUri);
                Log.e(TAG, "onActivityResult: "+ Arrays.toString(getBytes(selectedImageUri)));
            }
        }
    }

    public byte[] getBytes(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];

            int len = 0;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            inputStream.close();
            return byteBuffer.toByteArray();
        }
        catch (Exception e){
            Log.e(TAG, "getBytes: "+e);
            return new byte[0];
        }
    }

    private void createPost(Uri fileUri) {
        String fileType = "NULL";
        String type = "NULL";
        if(fileUri!=null) {
            ContentResolver cR = getContentResolver();
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getExtensionFromMimeType(cR.getType(fileUri));

            if (type.equals("jpg") || type.equals("jpeg") || type.equals("png") || type.equals("webp")) fileType = "image";
            else if (type.equals("mp4") || type.equals("mkv")) fileType = "video";
            else fileType = "text";
        }

        long filename = System.currentTimeMillis();

        String finalFileType = fileType;
        String finalType = type;
        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(
                Request.Method.POST,
                getResources().getString(R.string.API_URL) + "/api/aboutuser/create-post/",
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        if(response.statusCode == 201) {
                            try {
                                JSONObject obj = new JSONObject(new String(response.data));
                                Snackbar.make(findViewById(android.R.id.content),
                                        ""+obj.getString("msg"),
                                        Snackbar.LENGTH_LONG).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            progressView.setVisibility(View.GONE);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Snackbar.make(findViewById(android.R.id.content),
                                "Post failed, please check your internet connection",
                                Snackbar.LENGTH_LONG).show();
                        Log.e("GotError",""+error.getMessage());
                    }
                }) {

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                if(fileUri!=null) {
                    params.put("file", new DataPart(userId + "_" + filename + "." + finalType, getBytes(fileUri),
                            finalFileType + "/" + finalType));
                }
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("authtoken", getToken());
                return headers;
            }

            @NonNull
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("text",create_post_text.getText().toString());
                return params;
            }
        };

        volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        Singleton.getInstance(CreatePost.this).addToRequestQueue(volleyMultipartRequest);











//        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST,
//                getResources().getString(R.string.API_URL) + "/api/aboutuser/create-post/",
//                new Response.Listener<NetworkResponse>() {
//                    @Override
//                    public void onResponse(NetworkResponse response) {
//                        if(response.statusCode==201) {
//                            progressView.setVisibility(View.GONE);
//                            Snackbar.make(findViewById(android.R.id.content),"Post successful",Snackbar.LENGTH_LONG).show();
//                            Log.e(TAG, "onResponse: " + response.statusCode);
//                        }
//                    }
//                },
//                new Response.Listener<JSONObject>() {
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        Log.e(TAG, "onResponse: "+response);
//                    }
//                },
//                new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Snackbar.make(findViewById(android.R.id.content),"Post unsuccessful",Snackbar.LENGTH_LONG).show();
//                        Log.e("GotError",""+error.getMessage());
//                    }
//                }) {
//
//            @Override
//            protected Map<String, DataPart> getByteData() {
//                Map<String, DataPart> params = new HashMap<>();
//
//                if(fileUri!=null && !fileUri.toString().equals("")) {
//                    ContentResolver cR = getContentResolver();
//                    MimeTypeMap mime = MimeTypeMap.getSingleton();
//                    String type = mime.getExtensionFromMimeType(cR.getType(fileUri));
//
//                    String fileType;
//                    if(type.equals("jpg") || type.equals("jpeg") || type.equals("png")) fileType = "image";
//                    else if(type.equals("mp4") || type.equals("mkv")) fileType = "video";
//                    else fileType = "text";
//
//                    long imageName = System.currentTimeMillis();
//                    params.put("file", new DataPart(userId + "_" + imageName + "." + type,
//                            getBytes(fileUri),
//                            fileType + "/" + type));
//                }
//                return params;
//            }
//
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                HashMap<String, String> headers = new HashMap<String, String>();
//                String BOUNDARY = String.valueOf(System.currentTimeMillis());
//                headers.put("Content-Type", "multipart/form-data;boundary=" + BOUNDARY+ "; charset=utf-8");
//                headers.put("Connection", "Keep-Alive");
//                headers.put("Accept", "text/plain , application/json");
//                headers.put("authtoken", getToken());
//                return headers;
//            }
//
//            @Override
//            protected Map<String, String> getParams() throws AuthFailureError {
//                HashMap<String, String> params = new HashMap<String, String>();
//                params.put("text", create_post_text.getText().toString());
//                return params;
//            }
//        };
//
//        volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(
//                30000,
//                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//
//        //adding the request to volley

//        MultipartBody multipartBody = new MultipartBody.Builder(BOUNDARY)
//                .setType(MultipartBody.FORM)
//                .addFormDataPart(paramName, "image.jpg", RequestBody.create(MediaType.parse("image/jpeg"), new File(filePath)))
//                .addFormDataPart("param1", "value1")
//                .addFormDataPart("param2", "value2")
//                .build();
//
//        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(
//                getResources().getString(R.string.API_URL) + "/api/aboutuser/create-post/",
//                getHeaders(),
//                multipartBody.contentType().toString(),
//                multipartBody.toByteArray(),
//                response -> {
//                    // handle the response
//                },
//                error -> {
//                    // handle the error
//                });

//        Singleton.getInstance(CreatePost.this).addToRequestQueue(multipartRequest);
    }

    private Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();

        headers.put("Content-Type", "multipart/form-data;boundary=" + BOUNDARY);
        headers.put("Connection", "Keep-Alive");
        headers.put("authtoken", getToken());
        return headers;
    }

    public void uploadProfilePicture(Uri fileUri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String type = mime.getExtensionFromMimeType(cR.getType(fileUri));

        String fileType;
        if(type.equals("jpg") || type.equals("jpeg") || type.equals("png")) fileType = "image";
        else{
            Snackbar.make(findViewById(android.R.id.content),
                    "Please provide an image file (jpg, jpeg, or png)",
                    Snackbar.LENGTH_LONG).show();
            return;
        }

        long imageName = System.currentTimeMillis();

        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(
                Request.Method.POST,
                getResources().getString(R.string.API_URL) + "/api/aboutuser/upload-profilepic/",
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        if(response.statusCode == 201) {
                            try {
                                JSONObject obj = new JSONObject(new String(response.data));
                                Snackbar.make(findViewById(android.R.id.content),
                                        ""+obj.getString("msg"),
                                        Snackbar.LENGTH_LONG).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            progressView.setVisibility(View.GONE);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Snackbar.make(findViewById(android.R.id.content),
                                "Upload failed, please check your internet connection",
                                Snackbar.LENGTH_LONG).show();
                        Log.e("GotError",""+error.getMessage());
                    }
                }) {

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                params.put("file",new DataPart(userId+"_"+imageName+"."+type,getBytes(fileUri),
                        fileType+"/"+type));
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("authtoken", getToken());
                return headers;
            }

            //            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> headers = new HashMap<>();
//                headers.put("Content-Type", "multipart/form-data;boundary=" + BOUNDARY);
//                headers.put("Connection", "Keep-Alive");
//                Log.e(TAG, "getHeaders: "+getToken());
//                headers.put("authtoken", getToken());
//                return headers;
//            }

            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return super.getParams();
            }
        };

        volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        Singleton.getInstance(CreatePost.this).addToRequestQueue(volleyMultipartRequest);
    }

//    public void uploadProfilePic(Uri fileUri){
//        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST,
//                getResources().getString(R.string.API_URL) + "/api/aboutuser/upload-profilepic/",
//                new Response.Listener<NetworkResponse>() {
//                    @Override
//                    public void onResponse(NetworkResponse response) {
//                        if(response.statusCode==201) {
//                            progressView.setVisibility(View.GONE);
//                            Snackbar.make(findViewById(android.R.id.content),"Post successful",Snackbar.LENGTH_LONG).show();
//                            Log.e(TAG, "onResponse: " + response.statusCode);
//                        }
//                    }
//                },
//                new Response.Listener<JSONObject>() {
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        Log.e(TAG, "onResponse: "+response);
//                    }
//                },
//                new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        progressView.setVisibility(View.GONE);
//                        Snackbar.make(findViewById(android.R.id.content),"Post unsuccessful",Snackbar.LENGTH_LONG).show();
//                        Log.e("GotError",""+error.getMessage());
//                    }
//                }) {
//
//            @Override
//            protected Map<String, DataPart> getByteData() {
//                Map<String, DataPart> params = new HashMap<>();
//
//                if(fileUri!=null && !fileUri.toString().equals("")) {
//                    ContentResolver cR = getContentResolver();
//                    MimeTypeMap mime = MimeTypeMap.getSingleton();
//                    String type = mime.getExtensionFromMimeType(cR.getType(fileUri));
//
//                    String fileType;
//                    if(type.equals("jpg") || type.equals("jpeg") || type.equals("png")) fileType = "image";
//                    else if(type.equals("mp4") || type.equals("mkv")) fileType = "video";
//                    else fileType = "text";
//
//                    long imageName = System.currentTimeMillis();
//                    params.put("file", new DataPart(userId + "_" + imageName + "." + type,
//                            getBytes(fileUri),
//                            fileType + "/" + type));
//                }
//                return params;
//            }
//
//            @Override
//            public String getBodyContentType() {
//                return "multipart/form-data;boundary=" + BOUNDARY;
//            }
//
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                HashMap<String, String> headers = new HashMap<String, String>();
//                headers.put("Content-Type", "multipart/form-data;boundary=" + BOUNDARY);
//                headers.put("Connection", "Keep-Alive");
//                Log.e(TAG, "getHeaders: "+getToken());
//                headers.put("authtoken", getToken());
//                return headers;
//            }
//        };
//
//        volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(
//                30000,
//                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//
//        //adding the request to volley
//        Singleton.getInstance(CreatePost.this).addToRequestQueue(volleyMultipartRequest);
//    }

    private String getToken(){
        SharedPreferences sharedPreferences = getSharedPreferences("LoginData",MODE_PRIVATE);
        return sharedPreferences.getString("Token","");
    }
}