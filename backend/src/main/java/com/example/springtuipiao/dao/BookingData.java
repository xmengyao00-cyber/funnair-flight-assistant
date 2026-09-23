package com.example.springtuipiao.dao;

import com.example.springtuipiao.pojo.Booking;
import com.example.springtuipiao.pojo.Customer;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description TODO
 * @Author lijiar
 * @Date 2025/11/25 1:09
 */
public class BookingData {


    private List<Customer> customers = new ArrayList<>();

    private List<Booking> bookings = new ArrayList<>();

    public List<Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(List<Customer> customers) {
        this.customers = customers;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
    }


}
