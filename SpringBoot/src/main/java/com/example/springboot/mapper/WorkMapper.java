package com.example.springboot.mapper;

import com.example.springboot.entity.Works;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

import com.example.springboot.entity.Works;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface WorkMapper {
    int insertWorks(Works works);
    int deleteById(Integer id);
    List<Works> selectAllWorks();
}
