package com.example.springboot.mapper;

import com.example.springboot.entity.Feedback;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface FeedbackMapper {
    @Insert("INSERT INTO feedback(type, description, contact, create_time,status) " +
            "VALUES(#{type}, #{description}, #{contact}, #{createTime}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Feedback feedback);
}
