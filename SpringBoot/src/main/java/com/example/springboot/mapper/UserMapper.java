package com.example.springboot.mapper;

import com.example.springboot.entity.UserRegister;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Map;

@Mapper
public interface UserMapper {
    UserRegister selectById(Integer id);
    int existsUsername(@Param("username") String username,
                       @Param("excludeId") Integer excludeId);
    int updatePerson(UserRegister user);
}
