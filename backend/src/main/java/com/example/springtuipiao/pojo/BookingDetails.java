package com.example.springtuipiao.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 *
 *   预订详情响应DTO
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingDetails {

    String bookingNumber;
    String name;
    LocalDate date;
    BookingStatus bookingStatus;
    String from;
    String to;
    String bookingClass;
}
