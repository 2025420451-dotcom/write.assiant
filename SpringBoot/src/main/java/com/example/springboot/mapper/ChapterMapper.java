package com.example.springboot.mapper;

import com.example.springboot.entity.Chapter;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface ChapterMapper {
    List<Chapter> selectByVolumeId(Integer volumeId);
    int insert(Chapter chapter);
    int update(Chapter chapter);
    int delete(Integer id);
    int deleteByVolumeId(Integer volumeId);
    Chapter selectById(Integer id);
    List<Map<String, Object>> countWordsByWork(Date start, Date end);
}