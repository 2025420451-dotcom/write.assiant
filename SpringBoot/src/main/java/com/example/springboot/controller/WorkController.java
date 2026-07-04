package com.example.springboot.controller;

import com.example.springboot.commen.Result;
import com.example.springboot.entity.Works;
import com.example.springboot.service.WorkService;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/works")
public class WorkController {

    @Resource
    WorkService worksService;

    @GetMapping("/selectAll")
    public Result getAllWorks() {
        return Result.success(worksService.getAllWorks());
    }

    @PostMapping("/creatework")
    public Result createWorks(@RequestBody Works works) {return Result.success(worksService.createWorks(works));}

    @DeleteMapping("/{id}")
    public Result deleteWorks(@PathVariable Integer id) {return Result.success(worksService.deleteWorks(id));}
}