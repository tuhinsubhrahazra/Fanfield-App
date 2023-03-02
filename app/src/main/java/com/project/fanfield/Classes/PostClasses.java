package com.project.fanfield.Classes;

import java.util.ArrayList;

public class PostClasses {
    String postId;
    String userImage;
    String userName;
    String postDate;
    String text;
    String postImageUrl;
    String postVideoUrl;
    ArrayList<String> Likes;
    ArrayList<String> Comments;

    public PostClasses(String postId,String userImage, String userName,String postDate,
                       String text, String postImageUrl,String postVideoUrl,
                       ArrayList<String> Likes, ArrayList<String> Comments) {
        this.postId = postId;
        this.userImage = userImage;
        this.userName = userName;
        this.postDate = postDate;
        this.text = text;
        this.postImageUrl = postImageUrl;
        this.postVideoUrl = postVideoUrl;
        this.Likes = Likes;
        this.Comments = Comments;
    }

    public String getUserImageUrl() {
        return userImage;
    }

    public String getUserName() {
        return userName;
    }

    public String getText() {
        return text;
    }

    public String getPostImageUrl() {
        return postImageUrl;
    }

    public String getPostVideoUrl() {
        return postVideoUrl;
    }

    public String getUserImage() {
        return userImage;
    }

    public ArrayList<String> getLikes() {
        return Likes;
    }

    public ArrayList<String> getComments() {
        return Comments;
    }

    public String getPostDate() {
        return postDate;
    }

    public String getPostId() {
        return postId;
    }

    public void setLikes(ArrayList<String> likes) {
        Likes = likes;
    }
}
