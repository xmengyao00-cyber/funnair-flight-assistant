package com.example.springtuipiao.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 更改预订日期请求DTO
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ChangeBookingDatesRequest {

    @NotBlank(message = "预订编号不能为空")
    private String bookingNumber;

    @NotBlank(message = "姓名不能为空")
    private String name;

    @NotBlank(message = "日期不能为空")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "日期格式应为YYYY-MM-DD")
    private String date;

    @NotBlank(message = "出发地不能为空")
    private String from;

    @NotBlank(message = "目的地不能为空")
    private String to;
}
