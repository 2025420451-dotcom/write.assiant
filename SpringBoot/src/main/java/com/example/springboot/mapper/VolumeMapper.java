package com.example.springboot.mapper;


import com.example.springboot.entity.Volume;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface VolumeMapper {
    List<Volume> selectByWorkId(Integer workId);
    int insert(Volume volume);
    int delete(Integer id);
}