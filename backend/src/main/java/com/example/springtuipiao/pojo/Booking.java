package com.example.springtuipiao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("booking")
public class Booking {

    /** 主键，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单号 */
    @TableField("booking_number")
    private String bookingNumber;

    /** 乘客姓名（数据库直存） */
    @TableField("name")
    private String name;

    /** 出发地（保留字，用反引号） */
    @TableField("`from`")
    private String from;

    /** 到达地 */
    @TableField("`to`")
    private String to;

    /** 出发日期 */
    @TableField("`date`")
    private LocalDate date;

    /** 返程日期（可空） */
    @TableField("booking_to")
    private LocalDate bookingTo;

    /** 状态 */
    @TableField("booking_status")
    private String bookingStatus;

    /** 舱位 */
    @TableField("booking_class")
    private String bookingClass;

    /** customer / BookingStatus / BookingClass 是业务层对象，不映射数据库 */
    @TableField(exist = false)
    private Customer customer;

}