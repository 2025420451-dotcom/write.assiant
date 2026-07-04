package com.example.springboot.service;

import com.example.springboot.entity.Feedback;
import com.example.springboot.mapper.FeedbackMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户反馈服务层，提供反馈相关的业务逻辑。
 */
@Service
public class FeedbackService {

    private final FeedbackMapper feedbackMapper;

    /**
     * 构造方法注入 FeedbackMapper。
     * @param feedbackMapper 反馈数据访问层
     */
    public FeedbackService(FeedbackMapper feedbackMapper) {
        this.feedbackMapper = feedbackMapper;
    }

    /**
     * 创建并保存用户反馈，自动设置当前时间为创建时间。
     * @param feedback 反馈实体
     */
    public void createFeedback(Feedback feedback) {
        feedback.setCreateTime(LocalDateTime.now());
        feedbackMapper.insert(feedback);
    }
}