package com.example.springboot.controller;

import com.example.springboot.entity.Feedback;
import com.example.springboot.service.FeedbackService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户反馈控制器，提供反馈提交接口。
 */
@RestController
@RequestMapping("/api/feedback")
public class UserFeedbackController {

    private final FeedbackService feedbackService;

    /**
     * 构造方法注入 FeedbackService。
     * @param feedbackService 反馈服务层 bean
     */
    public UserFeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    /**
     * 提交用户反馈。
     * @param feedback 反馈内容（JSON请求体）
     * @return 操作成功的 HTTP 响应
     */
    @PostMapping
    public ResponseEntity<?> submitFeedback(@RequestBody Feedback feedback) {
        feedbackService.createFeedback(feedback);
        return ResponseEntity.ok().build();
    }
}