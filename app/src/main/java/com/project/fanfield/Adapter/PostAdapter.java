package com.project.fanfield.Adapter;

import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.project.fanfield.Activitys.MainActivity;
import com.project.fanfield.Classes.PostClasses;
import com.project.fanfield.R;
import com.project.fanfield.Services.Singleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    Context pContext;
    ArrayList<PostClasses> postList;
    String currentUser;

    public PostAdapter(Context pContext,ArrayList<PostClasses> postList) {
        this.pContext = pContext;
        this.postList = postList;
    }

    @NonNull
    @Override
    public PostAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(pContext).inflate(R.layout.post_adapter,parent,false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull PostAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        String userImageURL = postList.get(position).getUserImageUrl();
        String postImageURL = postList.get(position).getPostImageUrl();

        if(userImageURL.equals("") || userImageURL.equals("NULL")){
            holder.post_user_image.setImageResource(R.drawable.user);
        }else{
            Glide.with(pContext)
                    .load(userImageURL)
                    .placeholder(R.drawable.placeholder)
                    .into(holder.post_user_image);
        }
        if(!postImageURL.equals("") && !postImageURL.equals("NULL")){
            Glide.with(pContext)
                    .load(postImageURL)
                    .placeholder(R.drawable.placeholder)
                    .into(holder.post_image);
        }
        holder.post_user_name.setText(postList.get(position).getUserName());
        holder.post_text.setText(postList.get(position).getText());
        holder.post_time.setText(getPastTime(postList.get(position).getPostDate()));

        holder.like_number.setText(postList.get(position).getLikes().size()+" Likes");
        holder.comment_number.setText(postList.get(position).getComments().size()+" Comments");

//        Log.e(TAG, "onBindViewHolder: "+currentUser+" "+postList.get(position).getLikes());
        if(postList.get(position).getLikes().contains(currentUser)){
            holder.like_text.setText("Liked");
            holder.like_text.setTextColor(pContext.getColor(R.color.blue_violet));
            holder.likeImg.setImageResource(R.drawable.like_fill);
            holder.likeImg.setColorFilter(pContext.getColor(R.color.blue_violet), android.graphics.PorterDuff.Mode.SRC_IN);
        }

        if(postList.get(position).getComments().contains(currentUser)){
            holder.comment_txt.setText("Liked");
            holder.comment_txt.setTextColor(pContext.getColor(R.color.blue_violet));
            holder.comment_img.setImageResource(R.drawable.comment_fill);
            holder.comment_img.setColorFilter(pContext.getColor(R.color.blue_violet), android.graphics.PorterDuff.Mode.SRC_IN);
        }

        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(postList.get(position).getLikes().contains(currentUser)){
                    unLikePost(postList.get(position).getPostId(), holder, position);
                }else {
                    likePost(postList.get(position).getPostId(), holder, position);
                }
            }
        });

        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void unLikePost(String postId, PostAdapter.ViewHolder holder, int pos) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("postid", postId);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                pContext.getResources().getString(R.string.API_URL) + "/api/posts/unlike-post",
                new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if(response.getString("msg").equals("Unliked successfully")){
                                JSONArray likeArray = response.getJSONArray("likes");
                                holder.like_text.setText("Like");
                                holder.like_text.setTextColor(pContext.getColor(R.color.black));
                                holder.likeImg.setImageResource(R.drawable.like);
                                holder.like_number.setText(likeArray.length()+" Likes");
                                holder.likeImg.setColorFilter(pContext.getColor(R.color.blue_violet), android.graphics.PorterDuff.Mode.SRC_IN);

                                ArrayList<String> likeArrayList = new ArrayList<>();
                                for(int i=0;i<likeArray.length();i++) likeArrayList.add(likeArray.getString(i));
                                postList.get(pos).setLikes(likeArrayList);
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "onResponse: "+e);
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Volly error", "" + error);
                        if(error.networkResponse!=null) {
                            int statusCode = error.networkResponse.statusCode;
                            if (statusCode == 400)
                                Toast.makeText(pContext, "Bad Request", Toast.LENGTH_SHORT).show();
                            else if (statusCode == 404)
                                Toast.makeText(pContext, "Invalid Username or Password", Toast.LENGTH_SHORT).show();
                            else if (statusCode == 500)
                                Toast.makeText(pContext, "Internal server error", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(pContext, "Network error !", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("authtoken", getToken());
                return headers;
            }
        };

//        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
//                30000,
//                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        Singleton.getInstance(pContext).addToRequestQueue(jsonObjectRequest);
    }

    private void likePost(String postId,PostAdapter.ViewHolder holder,int pos) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("postid", postId);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                pContext.getResources().getString(R.string.API_URL) + "/api/posts/like-post",
                new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if(response.getString("msg").equals("Liked successfully")){
                                JSONArray likeArray = response.getJSONArray("likes");
                                holder.like_text.setText("Liked");
                                holder.like_text.setTextColor(pContext.getColor(R.color.blue_violet));
                                holder.likeImg.setImageResource(R.drawable.like_fill);
                                holder.like_number.setText(likeArray.length()+" Likes");
                                holder.likeImg.setColorFilter(pContext.getColor(R.color.blue_violet), android.graphics.PorterDuff.Mode.SRC_IN);

                                ArrayList<String> likeArrayList = new ArrayList<>();
                                for(int i=0;i<likeArray.length();i++) likeArrayList.add(likeArray.getString(i));
                                postList.get(pos).setLikes(likeArrayList);
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "onResponse: "+e);
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Volly error", "" + error);
                        if(error.networkResponse!=null) {
                            int statusCode = error.networkResponse.statusCode;
                            if (statusCode == 400)
                                Toast.makeText(pContext, "Bad Request", Toast.LENGTH_SHORT).show();
                            else if (statusCode == 404)
                                Toast.makeText(pContext, "Invalid Username or Password", Toast.LENGTH_SHORT).show();
                            else if (statusCode == 500)
                                Toast.makeText(pContext, "Internal server error", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(pContext, "Network error !", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("authtoken", getToken());
                return headers;
            }
        };

//        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
//                30000,
//                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        Singleton.getInstance(pContext).addToRequestQueue(jsonObjectRequest);
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(ArrayList<PostClasses> newData) {
        this.postList.addAll(newData);
        notifyDataSetChanged();
    }

    public void setCurrentUser(String currentUser){
        this.currentUser = currentUser;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView post_user_image;
        ImageView post_image;
        ImageView likeImg;
        LinearLayout comment;
        TextView post_user_name;
        TextView post_text;
        TextView post_time;
        TextView like_text;
        TextView like_number;
        TextView comment_number;
        LinearLayout like;
        ImageView comment_img;
        TextView comment_txt;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            post_user_image = itemView.findViewById(R.id.post_user_image);
            post_user_name = itemView.findViewById(R.id.post_user_name);
            post_text = itemView.findViewById(R.id.post_text);
            post_time = itemView.findViewById(R.id.post_time);
            post_image = itemView.findViewById(R.id.post_image);
            likeImg = itemView.findViewById(R.id.like_img);
            comment = itemView.findViewById(R.id.comment);
            comment_txt = itemView.findViewById(R.id.comment_txt);
            comment_img = itemView.findViewById(R.id.comment_img);
            comment = itemView.findViewById(R.id.comment);
            like_text = itemView.findViewById(R.id.like_text);
            like = itemView.findViewById(R.id.like);
            like_number = itemView.findViewById(R.id.like_number);
            comment_number = itemView.findViewById(R.id.comment_number);
        }
    }

    public String getPastTime(String dateTime){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalDateTime inputDate = null;
            inputDate = LocalDateTime.parse(dateTime, DateTimeFormatter.ISO_DATE_TIME);
            long daysDifference = ChronoUnit.DAYS.between(inputDate, LocalDateTime.now());
            Duration timeDifference = Duration.between(inputDate, LocalDateTime.now());
            long hours = timeDifference.toHours();
            long minutes = timeDifference.toMinutes() % 60;
            long seconds = timeDifference.getSeconds() % 60;

            if (daysDifference > 0) return daysDifference + " days ago";
            else if (hours > 0) return hours + " hours ago";
            else if (minutes > 0) return minutes + " minutes ago";
            else return seconds + " seconds ago";
        }
        else return "";
    }

    private String getToken(){
        SharedPreferences sharedPreferences = pContext.getSharedPreferences("LoginData",MODE_PRIVATE);
        return sharedPreferences.getString("Token","");
    }
}
