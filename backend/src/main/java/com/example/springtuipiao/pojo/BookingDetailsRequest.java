package com.example.springtuipiao.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 预订详情请求DTO
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class BookingDetailsRequest {

    @NotBlank(message = "预订编号不能为空")
    private String bookingNumber;

    @NotBlank(message = "姓名不能为空")
    private String name;

}
