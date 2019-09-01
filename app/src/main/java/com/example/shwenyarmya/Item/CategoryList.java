package com.example.shwenyarmya.Item;

import java.io.Serializable;

public class CategoryList implements Serializable {

    private String cid, category_name, total_books,cat_image;

    public CategoryList(String cid, String category_name, String total_books,String cat_image) {
        this.cid = cid;
        this.category_name = category_name;
        this.total_books = total_books;
        this.cat_image=cat_image;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }

    public String getTotal_books() {
        return total_books;
    }
     public void setTotal_books(String total_books) {
        this.total_books = total_books;
    }

    public String getcat_image() {
        return cat_image;
    }
    public void setcat_image(String cat_image) {
        this.cat_image = cat_image;
    }
}
