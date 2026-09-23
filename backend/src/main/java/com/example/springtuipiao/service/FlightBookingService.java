package com.example.springtuipiao.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.springtuipiao.dao.BookingMapper;
import com.example.springtuipiao.pojo.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 航班预定管理（数据库版）
 */
@Service
public class FlightBookingService {

	private final BookingMapper bookingMapper;

	public FlightBookingService(BookingMapper bookingMapper) {
		this.bookingMapper = bookingMapper;
	}

	/**
	 * 查询所有订单
	 */
	public List<BookingDetails> getBookings() {
		List<Booking> list = bookingMapper.selectList(null);
		return list.stream().map(this::toBookingDetails).toList();
	}

	/**
	 * 按订单号+姓名查单条
	 */
	private Booking findBooking(String bookingNumber, String name) {
		Booking booking = bookingMapper.selectOne(
				new LambdaQueryWrapper<Booking>()
						.eq(Booking::getBookingNumber, bookingNumber)
						.eq(Booking::getName, name)
		);
		if (booking == null) {
			throw new IllegalArgumentException("Booking not found");
		}
		return booking;
	}

	public BookingDetails getBookingDetails(String bookingNumber, String name) {
		return toBookingDetails(findBooking(bookingNumber, name));
	}

	/**
	 * 改签
	 */
	public void changeBooking(String bookingNumber, String name, String newDate, String from, String to) {
		Booking booking = findBooking(bookingNumber, name);
		if (booking.getDate().isBefore(LocalDate.now().plusDays(1))) {
			throw new IllegalArgumentException("Booking cannot be changed within 24 hours of the start date.");
		}
		booking.setDate(LocalDate.parse(newDate));
		booking.setFrom(from);
		booking.setTo(to);
		bookingMapper.updateById(booking);
	}

	/**
	 * 取消订单
	 */
	public void cancelBooking(String bookingNumber, String name) {
		Booking booking = findBooking(bookingNumber, name);
		if (booking.getDate().isBefore(LocalDate.now().plusDays(2))) {
			throw new IllegalArgumentException("Booking cannot be cancelled within 48 hours of the start date.");
		}
		booking.setBookingStatus("CANCELLED");
		bookingMapper.updateById(booking);
	}

	/**
	 * 创建新订单
	 */
	public String createBooking(String name, String date, String from, String to, BookingClass bookingClass) {
		Booking booking = new Booking();
		booking.setBookingNumber(generateNewBookingNumber());
		booking.setName(name);
		booking.setDate(LocalDate.parse(date));
		booking.setFrom(from);
		booking.setTo(to);
		booking.setBookingStatus("CONFIRMED");
		booking.setBookingClass(bookingClass.name());

		bookingMapper.insert(booking);

		return "预订创建成功！预订编号: " + booking.getBookingNumber() +
				", 姓名: " + name + ", 日期: " + date +
				", 从 " + from + " 到 " + to + ", 舱位: " + bookingClass;
	}

	public String createBooking(String name, String date, String from, String to) {
		return createBooking(name, date, from, to, BookingClass.ECONOMY);
	}

	/**
	 * 生成新订单号
	 */
	private String generateNewBookingNumber() {
		Long count = bookingMapper.selectCount(null);
		return String.valueOf(101 + count);
	}

	/**
	 * 转 DTO（给前端用）
	 */
	private BookingDetails toBookingDetails(Booking booking) {
		return new BookingDetails(
				booking.getBookingNumber(),
				booking.getName(),
				booking.getDate(),
				BookingStatus.valueOf(booking.getBookingStatus()),
				booking.getFrom(),
				booking.getTo(),
				booking.getBookingClass()
		);
	}
}