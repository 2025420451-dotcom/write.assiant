package com.example.springboot.mapper;

import com.example.springboot.entity.Feedback;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminFeedBackMapper {
    List<Feedback> getallfeedback();
    
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
