package com.example.springboot.controller;

import com.example.springboot.commen.Result;
import com.example.springboot.entity.Feedback;
import com.example.springboot.service.AdminFeedBackService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
public class AdminFeedBackController {
    @Resource
    AdminFeedBackService adminFeedBackService;

    //采用分页查询的方式获取用户提交的故障反馈信息
    @GetMapping("/GetAllFeedBack")
    //每页分为10个记录，页码从1开始
    public Result getallfeedback(@RequestParam(defaultValue = "1") Integer pageNum,
                                 @RequestParam(defaultValue = "10") Integer pageSize){
        PageInfo<Feedback> pageInfo = adminFeedBackService.getallFeedBack(pageNum,pageSize);
        return Result.success(pageInfo);
    }

    @PutMapping("/UpdateFeedbackStatus")
    public Result updateFeedbackStatus(@RequestParam Long id, @RequestParam Integer status) {
        boolean success = adminFeedBackService.updateFeedbackStatus(id, status);
        return success ? Result.success() : Result.error("更新状态失败");
    }
}
