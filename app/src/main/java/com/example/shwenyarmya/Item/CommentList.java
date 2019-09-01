package com.example.shwenyarmya.Item;

import java.io.Serializable;

public class CommentList implements Serializable {

    private String user_id,user_name,user_image, book_id,comment_text,comment_date;

    public CommentList(String user_id, String user_name, String user_image, String book_id, String comment_text, String comment_date) {
        this.user_id = user_id;
        this.user_name = user_name;
        this.user_image = user_image;
        this.book_id = book_id;
        this.comment_text = comment_text;
        this.comment_date = comment_date;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_image() {
        return user_image;
    }

    public void setUser_image(String user_image) {
        this.user_image = user_image;
    }

    public String getBook_id() {
        return book_id;
    }

    public void setBook_id(String book_id) {
        this.book_id = book_id;
    }

    public String getComment_text() {
        return comment_text;
    }

    public void setComment_text(String comment_text) {
        this.comment_text = comment_text;
    }

    public String getComment_date() {
        return comment_date;
    }

    public void setComment_date(String comment_date) {
        this.comment_date = comment_date;
    }
}
