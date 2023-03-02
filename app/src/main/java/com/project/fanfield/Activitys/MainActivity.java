package com.project.fanfield.Activitys;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;
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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.project.fanfield.Adapter.PostAdapter;
import com.project.fanfield.Classes.PostClasses;
import com.project.fanfield.R;
import com.project.fanfield.Services.Singleton;
import com.project.fanfield.Services.VolleyMultipartRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    TextView profile_name;
    RelativeLayout progressView;
    Button upload;
    Button browse;
    ImageView browseImage;
    ImageView profile_image;
    MaterialButton create_post_btn;
    RecyclerView posts_recycle_view;
    LinearLayoutManager postLayoutManager;
    Uri fileUri;
    String userId = "";
    static int page = 0;
    PostAdapter postAdapter;
    private boolean loading = true;
    int pastVisiblesItems, visibleItemCount;
    boolean initFirst = true;
    boolean isScrolling;
    NestedScrollView post_nested_scroll_view;
    ProgressBar post_progress_bar;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppThemeLight);
        setContentView(R.layout.activity_main);
        progressView = findViewById(R.id.progressView);
        profile_name = findViewById(R.id.profile_name);
        profile_image = findViewById(R.id.profile_image);
        create_post_btn = findViewById(R.id.create_post_btn);
        posts_recycle_view = findViewById(R.id.posts_recycle_view);
        post_nested_scroll_view = findViewById(R.id.post_nested_scroll_view);
        post_progress_bar = findViewById(R.id.post_progress_bar);

        create_post_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityOptions options =
                        ActivityOptions.makeSceneTransitionAnimation(
                                MainActivity.this, create_post_btn, "create_post");
                Intent intent = new Intent(MainActivity.this,CreatePost.class);
                intent.putExtra("USER_ID",userId);
                startActivity(intent,options.toBundle());
            }
        });

        progressView.setVisibility(View.VISIBLE);
//        Log.e("Token",""+getToken());
        getUser(getToken());

        ArrayList<PostClasses> list = new ArrayList<>();
        list.add(new PostClasses(
                "000",
                "https://www.google.com/images/branding/googlelogo/2x/googlelogo_light_color_92x30dp.png",
                "Tuhin Subhra Hazra",
                "12/12/12",
                "I love you",
                "https://www.google.com/images/branding/googlelogo/2x/googlelogo_light_color_92x30dp.png",
                "",
                new ArrayList<>(),
                new ArrayList<>()));
        list.add(new PostClasses(
                "000",
                "https://www.google.com/images/branding/googlelogo/2x/googlelogo_light_color_92x30dp.png",
                "Tuhin Subhra Hazra",
                "12/12/12",
                "I love you",
                "https://www.google.com/images/branding/googlelogo/2x/googlelogo_light_color_92x30dp.png",
                "",
                new ArrayList<>(),
                new ArrayList<>()));
        list.add(new PostClasses(
                "000",
                "https://www.google.com/images/branding/googlelogo/2x/googlelogo_light_color_92x30dp.png",
                "Tuhin Subhra Hazra",
                "12/12/12",
                "I love you",
                "https://www.google.com/images/branding/googlelogo/2x/googlelogo_light_color_92x30dp.png",
                "",
                new ArrayList<>(),
                new ArrayList<>()));
        list.add(new PostClasses(
                "000",
                "https://www.google.com/images/branding/googlelogo/2x/googlelogo_light_color_92x30dp.png",
                "Tuhin Subhra Hazra",
                "12/12/12",
                "I love you",
                "https://www.google.com/images/branding/googlelogo/2x/googlelogo_light_color_92x30dp.png",
                "",
                new ArrayList<>(),
                new ArrayList<>()));
        list.add(new PostClasses(
                "000",
                "https://www.google.com/images/branding/googlelogo/2x/googlelogo_light_color_92x30dp.png",
                "Tuhin Subhra Hazra",
                "12/12/12",
                "I love you",
                "https://www.google.com/images/branding/googlelogo/2x/googlelogo_light_color_92x30dp.png",
                "",
                new ArrayList<>(),
                new ArrayList<>()));

        postAdapter = new PostAdapter(getBaseContext(),new ArrayList<>());
        posts_recycle_view.setAdapter(postAdapter);
        postLayoutManager = new LinearLayoutManager(getBaseContext());
        posts_recycle_view.setLayoutManager(postLayoutManager);

        getPosts(getToken(),page);

        post_nested_scroll_view.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(@NonNull NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                    // check if the last visible item has been reached
                    LinearLayoutManager layoutManager = (LinearLayoutManager) posts_recycle_view.getLayoutManager();
                    assert layoutManager != null;
                    int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
                    int totalItemCount = layoutManager.getItemCount();

                    if (lastVisibleItemPosition == totalItemCount - 1) {
                        // trigger new data load
                        Log.e(TAG, "onScrollChange: fetch data");
                        post_progress_bar.setVisibility(View.VISIBLE);
                        getPosts(getToken(),++page);
                    }
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        page = 0;
        finish();
    }

    private void getPosts(String token, int pageNo){
        Log.e(TAG, "getPosts pageno:"+pageNo);
        ArrayList<PostClasses> postArrayList = new ArrayList<>();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                getResources().getString(R.string.API_URL) + "/api/posts/users-posts/" + pageNo,
                null,
                new Response.Listener<JSONObject>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e(TAG, "onResponse1: "+response);
                        try {
                            int totalPost = response.getInt("PostArraySize");
                            int PostLimit = response.getInt("PostLimit");
                            int PageNo = response.getInt("Page");

                            JSONArray postData = response.getJSONArray("PostArray");

                            for(int i=0;i<postData.length();i++){
                                JSONObject object = postData.getJSONObject(i);
                                JSONObject userObject = object.getJSONObject("auther");

                                JSONArray likes = object.getJSONArray("likes");
                                JSONArray comments = object.getJSONArray("comments");

                                ArrayList<String> likeList = new ArrayList<>();
                                ArrayList<String> commentsList = new ArrayList<>();
                                for(int k=0;k<likes.length();k++) likeList.add(likes.getString(k));
                                for(int k=0;k<comments.length();k++) commentsList.add(comments.getString(k));

                                postArrayList.add(new PostClasses(
                                        object.getString("postId"),
                                        userObject.has("profile_pic")?userObject.getString("profile_pic"):"NULL",
                                        userObject.getString("fristname")+" "+userObject.getString("lastname"),
                                        object.getString("post_date"),
                                        object.getString("text"),
                                        !object.has("photo_urls")||object.getString("photo_urls").equals("") ?"NULL":object.getString("photo_urls"),
                                        !object.has("video_urls")||object.getString("video_urls").equals("") ?"NULL":object.getString("video_urls"),
                                        likeList,
                                        commentsList));
                            }
                            progressView.setVisibility(View.GONE);
                            if(postData.length()>0) postAdapter.setData(postArrayList);
                            post_progress_bar.setVisibility(View.GONE);
                            postAdapter.setCurrentUser(userId);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(TAG, "onResponse: "+e);
                            Snackbar.make(findViewById(android.R.id.content),
                                    "Data fetching error",Snackbar.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "onErrorResponse: "+error);
                progressView.setVisibility(View.GONE);
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("authtoken", token);
                return headers;
            }
        };

//        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
//                0,
//                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        Singleton.getInstance(MainActivity.this).addToRequestQueue(jsonObjectRequest);
    }

    private void getUser(String token){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                getResources().getString(R.string.API_URL) + "/api/aboutuser/getuser",
                null,
                new Response.Listener<JSONObject>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            profile_name.setText(response.getString("fristname"));
                            userId = response.getString("_id");
                            Log.e(TAG, "onBindViewHolder 1: "+userId);
                            if(response.has("profile_pic") && !response.getString("profile_pic").equals("")) {
                                Glide.with(MainActivity.this)
                                        .load("" + response.getString("profile_pic"))
                                        .placeholder(R.drawable.placeholder)
                                        .into(profile_image);
                            }else{
                                profile_image.setImageResource(R.drawable.user);
                            }
                        } catch (JSONException e) {
                            progressView.setVisibility(View.INVISIBLE);
                            Toast.makeText(MainActivity.this, "Internal error", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressView.setVisibility(View.GONE);
                Log.e("Volly error", "" + error);
                if(error.networkResponse!=null) {
                    int statusCode = error.networkResponse.statusCode;
                    if (statusCode == 400)
                        Toast.makeText(MainActivity.this, "Bad Request", Toast.LENGTH_SHORT).show();
                    else if (statusCode == 404)
                        Toast.makeText(MainActivity.this, "Invalid Username or Password", Toast.LENGTH_SHORT).show();
                    else if (statusCode == 500)
                        Toast.makeText(MainActivity.this, "Internal server error", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(MainActivity.this, "Network error !", Toast.LENGTH_SHORT).show();
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
//                0,
//                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        Singleton.getInstance(MainActivity.this).addToRequestQueue(jsonObjectRequest);
    }

    private String getToken(){
        SharedPreferences sharedPreferences = getSharedPreferences("LoginData",MODE_PRIVATE);
        return sharedPreferences.getString("Token","");
    }
}