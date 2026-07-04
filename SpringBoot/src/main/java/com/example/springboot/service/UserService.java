package com.example.springboot.service;

import com.example.springboot.commen.Result;
import com.example.springboot.entity.UserRegister;
import com.example.springboot.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserMapper userMapper;

    /**
     * 构造方法注入 UserMapper。
     * @param userMapper 用户数据访问层
     */
    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 根据用户ID检索用户信息。
     * @param id 用户ID
     * @return 用户注册信息
     */
    public UserRegister getPerson(Integer id) {
        return userMapper.selectById(id);
    }

    /**
     * 更新用户资料。
     * 如果用户名已被其他用户使用，则抛出异常；如果用户不存在，也抛出异常。
     * @param user 用户注册信息（包含ID和新的资料）
     * @return 始终返回 null（原设计如此，保持兼容）
     */
    @Transactional
    public Result updatePerson(UserRegister user) {
        if (userMapper.existsUsername(user.getUsername(), user.getId()) > 0) {
            throw new RuntimeException("用户名已存在");
        }
        if (userMapper.updatePerson(user) == 0) {
            throw new RuntimeException("用户不存在");
        }
        return null;
    }
}