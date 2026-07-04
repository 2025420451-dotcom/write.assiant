package com.example.springboot.service;

import com.example.springboot.entity.Poetry;
import com.example.springboot.mapper.PoetryMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class PoetryService {

    @Resource
    PoetryMapper poetryMapper;

    public PageInfo<Poetry> selectPage(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        List<Poetry> list=poetryMapper.selectAll();
        return PageInfo.of(list);
    }

    public PageInfo<Poetry> searchByKeyword(Integer pageNum, Integer pageSize, String keyword) {
        PageHelper.startPage(pageNum, pageSize);
        List<Poetry> list = poetryMapper.selectByKeyword(keyword);
        return new PageInfo<>(list);
    }

    public void insertPoetry(Poetry poetry) {
        poetryMapper.insertPoetry(poetry);
    }

    public void deletePoetry(Integer id) {
        poetryMapper.deletePoetry(id);
    }

    public void updatePoetry(Poetry poetry) {
        poetryMapper.updatePoetry(poetry);
    }
}
