package com.example.springtuipiao.pojo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description TODO
 * @Author lijiar
 * @Date 2025/11/25 1:14
 */
@Data
public class Customer {
    private String name;

    private List<Booking> bookings = new ArrayList<>();
}

