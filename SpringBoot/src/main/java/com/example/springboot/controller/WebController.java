package com.example.springboot.controller;

import com.example.springboot.commen.Result;
import com.example.springboot.entity.UserInformation;
import com.example.springboot.entity.UserRegister;
import com.example.springboot.service.AdminService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
public class WebController {
    @Resource
    AdminService adminService;
    //用户登录
    @PostMapping("/login")
    public Result login(@RequestBody UserInformation userinformation) {
        UserInformation data=new UserInformation();
        data=adminService.login(userinformation);
        return Result.success(data);
    }
    //用户注册
    @PostMapping("/register")
    public void login(@RequestBody UserRegister userregister) {
        adminService.insertUser(userregister);
    }



    //用户登录
    @PostMapping("/adminlogin")
    public Result adminlogin(@RequestBody UserInformation userinformation) {
        UserInformation data=new UserInformation();
        data=adminService.adminlogin(userinformation);
        return Result.success(data);
    }
    //用户注册
    @PostMapping("/adminregister")
    public void adminlogin(@RequestBody UserRegister userregister) {
        adminService.insertadminUser(userregister);
    }

}
