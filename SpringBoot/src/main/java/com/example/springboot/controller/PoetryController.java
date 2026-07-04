package com.example.springboot.controller;

import com.example.springboot.commen.Result;
import com.example.springboot.entity.Poetry;
import com.example.springboot.service.PoetryService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
public class PoetryController {

    @Resource
    PoetryService poetryService;

    @GetMapping("/poetrylistget")
    public Result selectPoetry(@RequestParam(defaultValue = "1") Integer pageNum,
                               @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Poetry> pageInfo;
        return Result.success(pageInfo=poetryService.selectPage(pageNum,pageSize));
    }

    @GetMapping("/searchpoetrylistget")
    public Result searchselectPoetry(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) { // 添加keyword参数
        PageInfo<Poetry> pageInfo = poetryService.searchByKeyword(pageNum, pageSize, keyword);
        return Result.success(pageInfo);
    }


    @DeleteMapping("/poetrylistdelete/{id}")
    public Result deletePoetry(@PathVariable Integer id) {
        poetryService.deletePoetry(id);
        return Result.success();
    }


    @PostMapping("/poetrylistpost")
    public Result insertPoetry(@RequestBody Poetry poetry) {
        poetryService.insertPoetry(poetry);
        return Result.success();
    }


    @PutMapping("poetrylistput/{id}")
    public Result updatePoetry(@PathVariable Integer id,@RequestBody Poetry poetry) {
        poetry.setId(id);
        poetryService.updatePoetry(poetry);
        return Result.success();
    }

}
