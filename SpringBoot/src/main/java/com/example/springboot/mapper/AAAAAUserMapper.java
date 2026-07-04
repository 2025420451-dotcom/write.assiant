package com.example.springboot.mapper;

import com.example.springboot.entity.AAAAAUser;
import com.example.springboot.entity.AAAAAdmin;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface AAAAAUserMapper {

    List<AAAAAUser> selectAll(AAAAAUser user);

    void insert(AAAAAUser user);

    @Select("select * from `user` where username = #{username}")
    AAAAAdmin selectByAdmin(String admin);

    void updateById(AAAAAUser user);

    @Delete("delete from `user` where id = #{id}")
    void deleteById(Integer id);

}
