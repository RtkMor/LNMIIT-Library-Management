package com.example.abcd.ui.explore;

public class BookClass {
    private String bookId;
    private String title;
    private String author;
    private String isbn;
    private String accountNumber;
    private String callNumber;
    private Float rating;
    private int ratingNumber;

    // Constructors
    public BookClass(String bookId, String title, String author, String isbn, String accountNumber, String callNumber, Float rating, int ratingNumber) {
        // Constructor to initialize all fields
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.accountNumber = accountNumber;
        this.callNumber = callNumber;
        this.rating = rating;
        this.ratingNumber = ratingNumber;
    }

    public BookClass() {
        // Default constructor with no arguments
    }

    // Getters (without setters)
    public String getBookId() {
        return bookId;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getISBN() {
        return isbn;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getCallNumber() {
        return callNumber;
    }

    public Float getRating() {
        return rating;
    }

    public int getRatingNumber() {
        return ratingNumber;
    }

    public void setId(String bookId) {
        this.bookId = bookId;
    }
}


