package com.example.springtuipiao.pojo;

public class CreateBookingRequest {
    private String name;
    private String date;
    private String from;
    private String to;
    private BookingClass bookingClass;

    // 构造函数
    public CreateBookingRequest() {}

    public CreateBookingRequest(String name, String date, String from, String to, BookingClass bookingClass) {
        this.name = name;
        this.date = date;
        this.from = from;
        this.to = to;
        this.bookingClass = bookingClass;
    }

    // Getter 和 Setter 方法
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public BookingClass getBookingClass() {
        return bookingClass;
    }

    public void setBookingClass(BookingClass bookingClass) {
        this.bookingClass = bookingClass;
    }
}