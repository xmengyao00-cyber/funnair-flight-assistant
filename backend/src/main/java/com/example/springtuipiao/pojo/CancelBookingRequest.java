package com.example.springtuipiao.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 取消预订请求DTO
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CancelBookingRequest {

    @NotBlank(message = "预订编号不能为空")
    private String bookingNumber;

    @NotBlank(message = "姓名不能为空")
    private String name;
}
