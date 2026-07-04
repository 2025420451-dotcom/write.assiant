package com.example.springboot.mapper;

import com.example.springboot.entity.Poetry;

import java.util.List;

public interface PoetryMapper {

    void insertPoetry(Poetry poetry) ;

    List<Poetry> selectAll();

    void deletePoetry(Integer id);

    void updatePoetry(Poetry poetry);


    List<Poetry> selectByKeyword(String keyword);
}
