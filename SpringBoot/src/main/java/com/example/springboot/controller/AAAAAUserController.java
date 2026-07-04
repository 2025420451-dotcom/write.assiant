package com.example.springboot.controller;

import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.example.springboot.commen.Result;
import com.example.springboot.entity.AAAAAUser;
import com.example.springboot.entity.Poetry;
import com.example.springboot.service.AAAAAUserService;
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
@RequestMapping("/user")
public class AAAAAUserController {

    @Resource
    AAAAAUserService aaaaauserService;

    @PostMapping("/add")
    public Result add(@RequestBody AAAAAUser user) {  // @RequestBody 接收前端传来的 json参数
        aaaaauserService.add(user);
        return Result.success();
    }

    @PutMapping("/update")
    public Result update(@RequestBody AAAAAUser user) {  // @RequestBody 接收前端传来的 json参数
        aaaaauserService.update(user);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {  // @PathVariable 接收前端传来的路径参数
        aaaaauserService.deleteById(id);
        return Result.success();
    }

    @DeleteMapping("/deleteBatch")
    public Result deleteBatch(@RequestBody List<AAAAAUser> list) {  //  @RequestBody 接收前端传来的 json数组
        aaaaauserService.deleteBatch(list);
        return Result.success();
    }

    @GetMapping("/selectAll")  //   完整的请求路径：http://ip:port/admin/selectAll
    public Result selectAll() {
        List<AAAAAUser> aaaaauserList = aaaaauserService.selectAll();
        return Result.success(aaaaauserList);
    }

    @GetMapping("/selectPage")
    public Result selectPage(@RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize,
                             AAAAAUser user) {
        PageInfo<AAAAAUser> pageInfo = aaaaauserService.selectPage(pageNum, pageSize, user);
        return Result.success(pageInfo);  // 返回的是分页的对象
    }

    @GetMapping("/export")
    public void export(HttpServletResponse response) throws Exception {
        List<AAAAAUser> aaaaauserList = aaaaauserService.selectAll();
        ExcelWriter writer = ExcelUtil.getWriter(true);
        writer.addHeaderAlias("username","用户名");
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

        for (AAAAAUser user : list) {
            aaaaauserService.add(user);
        }
        return Result.success();
    }
}