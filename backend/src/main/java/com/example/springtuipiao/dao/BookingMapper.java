package com.example.springtuipiao.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springtuipiao.pojo.Booking;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BookingMapper extends BaseMapper<Booking> {
}