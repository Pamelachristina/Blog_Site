package com.teamtreehouse.blog.model;

import java.util.ArrayList;
import java.util.List;

public class BlogEntry {
    private String title;
    private String body;
    private String createdAt;
    private String slug;
    private List<Comment> comments = new ArrayList<>();

    public BlogEntry(String title, String body, String createdAt) {
        this.title = title;
        this.body = body;
        this.createdAt = createdAt;
    }

    // Getter and Setter for Slug
    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    // Getters and Setters for other fields
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void addComment(Comment comment) {
        this.comments.add(comment);
    }
}


