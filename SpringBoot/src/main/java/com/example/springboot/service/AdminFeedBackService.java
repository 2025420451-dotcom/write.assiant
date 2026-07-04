package com.example.springboot.service;

import com.example.springboot.entity.Feedback;
import com.example.springboot.entity.Poetry;
import com.example.springboot.mapper.AdminFeedBackMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminFeedBackService {

    @Resource
    AdminFeedBackMapper adminFeedBackMapper;

    public PageInfo<Feedback> getallFeedBack(Integer pageNum, Integer pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Feedback> list=adminFeedBackMapper.getallfeedback();
        return PageInfo.of(list);
    }

    public boolean updateFeedbackStatus(Long id, Integer status) {
        return adminFeedBackMapper.updateStatus(id, status) > 0;
    }
}
