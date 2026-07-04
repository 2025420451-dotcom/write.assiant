package com.example.springboot.mapper;

import com.example.springboot.entity.AAAAAdmin;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface AAAAAdminMapper {

    List<AAAAAdmin> selectAll(AAAAAdmin admin);

    void insert(AAAAAdmin admin);

    @Select("select * from `admin` where admin = #{admin}")
    AAAAAdmin selectByAdmin(String admin);

    void updateById(AAAAAdmin admin);

    @Delete("delete from `admin` where id = #{id}")
    void deleteById(Integer id);

}
