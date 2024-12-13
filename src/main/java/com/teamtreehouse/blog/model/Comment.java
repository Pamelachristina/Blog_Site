package com.teamtreehouse.blog.model;



public class Comment {
    private String name;
    private String body;

    public Comment(String name, String body) {
        this.name = name;
        this.body = body;
    }

    public String getName() {
        return name;
    }

    public String getBody() {
        return body;
    }
}
