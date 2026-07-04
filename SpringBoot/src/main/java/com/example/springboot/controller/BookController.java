package com.example.springboot.controller;

import com.example.springboot.commen.Result;
import com.example.springboot.entity.Chapter;
import com.example.springboot.entity.Volume;
import com.example.springboot.service.BookService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * 书籍管理控制器，提供书籍结构、卷、章节的增删改查以及字数统计接口。
 * 所有接口路径均以 /api/books 为前缀。
 */
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    /**
     * 构造方法注入 BookService。
     * @param bookService 书籍服务层 bean
     */
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    /**
     * 获取指定书籍的完整结构（卷和章节树形数据）。
     * @param workId 书籍ID
     * @return 包含结构数据的统一响应结果
     */
    @GetMapping("/{workId}/structure")
    public Result getStructure(@PathVariable Integer workId) {
        return Result.success(bookService.getBookStructure(workId));
    }

    /**
     * 创建新的卷。
     * @param volume 卷实体（JSON请求体）
     * @return 操作成功的结果
     */
    @PostMapping("/volumes")
    public Result createVolume(@RequestBody Volume volume) {
        bookService.createVolume(volume);
        return Result.success();
    }

    /**
     * 创建新的章节。
     * @param chapter 章节实体（JSON请求体）
     * @return 操作成功的结果
     */
    @PostMapping("/chapters")
    public Result createChapter(@RequestBody Chapter chapter) {
        bookService.createChapter(chapter);
        return Result.success();
    }

    /**
     * 更新章节信息。
     * @param chapter 章节实体（必须包含ID）
     * @return 操作成功的结果
     */
    @PutMapping("/chapters")
    public Result updateChapter(@RequestBody Chapter chapter) {
        bookService.updateChapter(chapter);
        return Result.success();
    }

    /**
     * 删除指定卷。
     * @param id 卷ID
     * @return 操作成功的结果
     */
    @DeleteMapping("/volumes/{id}")
    public Result deleteVolume(@PathVariable Integer id) {
        bookService.deleteVolume(id);
        return Result.success();
    }

    /**
     * 删除指定章节。
     * @param id 章节ID
     * @return 操作成功的结果
     */
    @DeleteMapping("/chapters/{id}")
    public Result deleteChapter(@PathVariable Integer id) {
        bookService.deleteChapter(id);
        return Result.success();
    }

    /**
     * 根据ID获取章节详情。
     * @param id 章节ID
     * @return 包含章节数据的统一响应结果
     */
    @GetMapping("/chapters/{id}")
    public Result getChapter(@PathVariable Integer id) {
        return Result.success(bookService.getChapterById(id));
    }

    /**
     * 统计每本书的总字数，支持按日期范围过滤。
     * @param start 开始日期（可选，格式 yyyy-MM-dd）
     * @param end 结束日期（可选，格式 yyyy-MM-dd）
     * @return 包含字数统计结果的统一响应结果
     */
    @GetMapping("/stats/book-words")
    public Result getBookWords(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date end) {
        return Result.success(bookService.getBookWords(start, end));
    }
}