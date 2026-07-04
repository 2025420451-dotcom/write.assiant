package com.example.springboot.service;

import com.example.springboot.entity.UserInformation;
import com.example.springboot.entity.UserRegister;
import com.example.springboot.exception.CustomerException;
import com.example.springboot.mapper.AdminMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    @Resource
    AdminMapper adminMapper;
    //用户
    //登录校验服务
    public UserInformation login(UserInformation userinformation){
        //验证账号是否存在
        UserInformation dbuserinformation =adminMapper.selectByUsername(userinformation.getUsername());
        if(dbuserinformation==null){
            throw new CustomerException("账号不存在");
        }

        //验证密码是否正确
        if(!dbuserinformation.getPassword().equals(userinformation.getPassword())){
            throw new CustomerException("账号或密码错误");
        }
        return dbuserinformation;
    }

    public void insertUser(UserRegister userregister){
       adminMapper.insertUser(userregister);
    }

    //管理员
    public UserInformation adminlogin(UserInformation userinformation){
        //验证账号是否存在
        UserInformation dbuserinformation =adminMapper.selectByadminUsername(userinformation.getUsername());
        if(dbuserinformation==null){
            throw new CustomerException("账号不存在");
        }

        //验证密码是否正确
        if(!dbuserinformation.getPassword().equals(userinformation.getPassword())){
            throw new CustomerException("账号或密码错误");
        }
        return dbuserinformation;
    }

    public void insertadminUser(UserRegister userregister){
        adminMapper.insertadminUser(userregister);
    }


}
