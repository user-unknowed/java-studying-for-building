package com.demo.library;

/** 图书模型（对应 books 表的一行） */
public class Book {
    public long id;
    public String title;
    public String author;
    public int stock;

    public Book() {}

    public Book(long id, String title, String author, int stock) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.stock = stock;
    }
}
