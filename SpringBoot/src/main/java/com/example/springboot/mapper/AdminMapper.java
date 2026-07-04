package com.example.springboot.mapper;

import com.example.springboot.entity.UserInformation;
import com.example.springboot.entity.UserRegister;

public interface AdminMapper {
    //用户
    UserInformation selectByUsername(String username);
    void insertUser(UserRegister userregister);
    //管理员
    UserInformation selectByadminUsername(String username);
    void insertadminUser(UserRegister userregister);


}

