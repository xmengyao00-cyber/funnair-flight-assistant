package com.example.springtuipiao.service;

import com.example.springtuipiao.pojo.BookingDetails;
import com.example.springtuipiao.pojo.BookingDetailsRequest;
import com.example.springtuipiao.pojo.CancelBookingRequest;
import com.example.springtuipiao.pojo.ChangeBookingDatesRequest;
import com.example.springtuipiao.pojo.CreateBookingRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BookingTools {

	private static final Logger logger = LoggerFactory.getLogger(BookingTools.class);

	@Autowired
	private FlightBookingService flightBookingService;

	/**
	 * 通过 @Bean + Function<...> 注册为可被模型调用的函数
	 * 并与具体业务实现类进行解耦
	 * @return
	 */
	@Tool(description = "获取机票预定详细信息")
	public BookingDetails getBookingDetails(BookingDetailsRequest bookingDetailsRequest) {
		BookingDetails bookingDetails = flightBookingService.getBookingDetails(bookingDetailsRequest.getBookingNumber(), bookingDetailsRequest.getName());

		return bookingDetails;
	}

	@Tool(description = "创建新的机票预定")
	public String createBooking(CreateBookingRequest request) {
		try {
			String result = flightBookingService.createBooking(
					request.getName(),
					request.getDate(),
					request.getFrom(),
					request.getTo(),
					request.getBookingClass()
			);
			return result;
		} catch (Exception e) {
			logger.error("创建预订时出错", e);
			return "创建预订失败: " + e.getMessage();
		}
	}

	@Tool(description = "修改机票预定日期")
	public String changeBooking(ChangeBookingDatesRequest request) {
		flightBookingService.changeBooking(request.getBookingNumber(), request.getName(), request.getDate(), request.getFrom(),
				request.getTo());
		return "修改成功";

	}

	@Tool(description = "取消机票预定")
	public String cancelBooking(CancelBookingRequest request) {
		flightBookingService.cancelBooking(request.getBookingNumber(), request.getName());
		return "取消成功";
	}

}