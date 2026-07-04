package com.example.springboot.service;

import cn.hutool.core.util.StrUtil;
import com.example.springboot.entity.AAAAAUser;
import com.example.springboot.entity.AAAAAdmin;
import com.example.springboot.entity.Poetry;
import com.example.springboot.exception.CustomerException;
import com.example.springboot.mapper.AAAAAUserMapper;
import com.example.springboot.mapper.AAAAAdminMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AAAAAUserService {

    @Resource
    AAAAAUserMapper aaaaaduserMapper;

    public void add(AAAAAUser user) {
        // 根据新的账号查询数据库  是否存在同样账号的数据
        AAAAAdmin dbAdmin = aaaaaduserMapper.selectByAdmin(user.getUsername());
        if (dbAdmin != null) {
            throw new CustomerException("账号重复");
        }
        // 默认密码
        if (StrUtil.isBlank(user.getPassword())) {
            user.setPassword("admin");
        }
        aaaaaduserMapper.insert(user);
    }

    public void update(AAAAAUser user) {
        aaaaaduserMapper.updateById(user);
    }

    public void deleteById(Integer id) {
        aaaaaduserMapper.deleteById(id);
    }

    public void deleteBatch(List<AAAAAUser> list) {
        for (AAAAAUser user : list) {
            this.deleteById(user.getId());
        }
    }

    public List<AAAAAUser> selectAll() {
        return aaaaaduserMapper.selectAll(null);
    }

    public PageInfo<AAAAAUser> selectPage(Integer pageNum, Integer pageSize, AAAAAUser user) {
        PageHelper.startPage(pageNum, pageSize);
        List<AAAAAUser> list = aaaaaduserMapper.selectAll(user);
        return PageInfo.of(list);
    }

}
