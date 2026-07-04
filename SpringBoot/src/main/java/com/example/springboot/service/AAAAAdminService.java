package com.example.springboot.service;


import cn.hutool.core.util.StrUtil;
import com.example.springboot.entity.AAAAAdmin;
import com.example.springboot.exception.CustomerException;
import com.example.springboot.mapper.AAAAAdminMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AAAAAdminService {

    @Resource
    AAAAAdminMapper aaaaadminMapper;

    public void add(AAAAAdmin admin) {
        // 根据新的账号查询数据库  是否存在同样账号的数据
        AAAAAdmin dbAdmin = aaaaadminMapper.selectByAdmin(admin.getAdmin());
        if (dbAdmin != null) {
            throw new CustomerException("账号重复");
        }
        // 默认密码
        if (StrUtil.isBlank(admin.getPassword())) {
            admin.setPassword("admin");
        }
        aaaaadminMapper.insert(admin);
    }

    public void update(AAAAAdmin admin) {
        aaaaadminMapper.updateById(admin);
    }

    public void deleteById(Integer id) {
        aaaaadminMapper.deleteById(id);
    }

    public void deleteBatch(List<AAAAAdmin> list) {
        for (AAAAAdmin admin : list) {
            this.deleteById(admin.getId());
        }
    }

    public List<AAAAAdmin> selectAll() {
        return aaaaadminMapper.selectAll(null);
    }

    public PageInfo<AAAAAdmin> selectPage(Integer pageNum, Integer pageSize, AAAAAdmin admin) {
        // 开启分页查询
        PageHelper.startPage(pageNum, pageSize);
        List<AAAAAdmin> list = aaaaadminMapper.selectAll(admin);
        return PageInfo.of(list);
    }

}