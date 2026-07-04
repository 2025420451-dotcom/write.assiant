package com.example.springboot.controller;

import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.example.springboot.commen.Result;
import com.example.springboot.entity.AAAAAUser;
import com.example.springboot.entity.AAAAAdmin;
import com.example.springboot.service.AAAAAdminService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class AAAAAdminController {

    @Resource
    AAAAAdminService aaaaadminService;

    @PostMapping("/add")
    public Result add(@RequestBody AAAAAdmin admin) {
        aaaaadminService.add(admin);
        return Result.success();
    }

    @PutMapping("/update")
    public Result update(@RequestBody AAAAAdmin admin) {
        aaaaadminService.update(admin);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        aaaaadminService.deleteById(id);
        return Result.success();
    }

    @DeleteMapping("/deleteBatch")
    public Result deleteBatch(@RequestBody List<AAAAAdmin> list) {
        aaaaadminService.deleteBatch(list);
        return Result.success();
    }

    @GetMapping("/selectAll")
    public Result selectAll() {
        List<AAAAAdmin> adminList = aaaaadminService.selectAll();
        return Result.success(adminList);
    }

    @GetMapping("/selectPage")
    public Result selectPage(@RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize,
                             AAAAAdmin admin) {
        PageInfo<AAAAAdmin> pageInfo = aaaaadminService.selectPage(pageNum, pageSize, admin);
        return Result.success(pageInfo);  // 返回的是分页的对象
    }

    @GetMapping("/export")
    public void export(HttpServletResponse response) throws Exception {
        List<AAAAAdmin> aaaaauserList = aaaaadminService.selectAll();
        ExcelWriter writer = ExcelUtil.getWriter(true);
        writer.addHeaderAlias("admin","用户名");
        writer.addHeaderAlias("password","密码");
        writer.addHeaderAlias("phone","电话");
        writer.addHeaderAlias("email","电子邮箱");

        writer.setOnlyAlias(true);

        writer.write(aaaaauserList);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("用户信息", StandardCharsets.UTF_8);
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");
        ServletOutputStream os = response.getOutputStream();
        writer.flush(os);
        writer.close();
        os.close();
    }
    @PostMapping("/import")
    public Result importData(MultipartFile file) throws Exception {

        InputStream inputStream = file.getInputStream();
        ExcelReader reader = ExcelUtil.getReader(inputStream);

        reader.addHeaderAlias("用户名", "username");
        reader.addHeaderAlias("密码", "password");
        reader.addHeaderAlias("电话", "phone");
        reader.addHeaderAlias("电子邮箱", "email");
        List<AAAAAUser> list = reader.readAll(AAAAAUser.class);


        return Result.success();
    }

}
