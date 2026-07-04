package com.example.springboot.controller;

import com.example.springboot.commen.Result;
import com.example.springboot.entity.UserRegister;
import com.example.springboot.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    /**
     * 构造方法注入 UserService。
     * @param userService 用户服务层 bean
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 获取用户信息。
     * @param id 用户ID
     * @return 包含用户数据的统一响应结果
     */
    @GetMapping("/getperson")
    public Result getPerson(@RequestParam("id") Integer id) {
        return Result.success(userService.getPerson(id));
    }

    /**
     * 更新用户信息。
     * @param user 用户注册信息（JSON请求体）
     * @return 操作成功的结果
     */
    @PostMapping("/updataperson")
    public Result updatePerson(@RequestBody UserRegister user) {
        System.out.println("更新用户信息：" + user);
        return Result.success(userService.updatePerson(user));
    }
}
