package com.example.springboot.service;

import com.example.springboot.entity.Chapter;
import com.example.springboot.entity.Volume;
import com.example.springboot.mapper.ChapterMapper;
import com.example.springboot.mapper.VolumeMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 书籍服务层，提供书籍结构、卷、章节的业务逻辑处理。
 */
@Service
public class BookService {

    private final VolumeMapper volumeMapper;
    private final ChapterMapper chapterMapper;

    /**
     * 构造方法注入 Mapper 依赖。
     * @param volumeMapper 卷数据访问层
     * @param chapterMapper 章节数据访问层
     */
    public BookService(VolumeMapper volumeMapper, ChapterMapper chapterMapper) {
        this.volumeMapper = volumeMapper;
        this.chapterMapper = chapterMapper;
    }

    /**
     * 获取指定书籍的完整结构（卷列表及每个卷下的章节列表）。
     * @param workId 书籍ID
     * @return 卷列表，每个卷包含其下属章节
     */
    @Transactional
    public List<Volume> getBookStructure(Integer workId) {
        List<Volume> volumes = volumeMapper.selectByWorkId(workId);
        volumes.forEach(volume -> volume.setChapters(chapterMapper.selectByVolumeId(volume.getId())));
        return volumes;
    }

    /**
     * 创建新的卷。
     * @param volume 卷实体
     */
    @Transactional
    public void createVolume(Volume volume) {
        volumeMapper.insert(volume);
    }

    /**
     * 创建新的章节。
     * @param chapter 章节实体
     */
    @Transactional
    public void createChapter(Chapter chapter) {
        chapterMapper.insert(chapter);
    }

    /**
     * 更新章节信息。
     * @param chapter 章节实体（必须包含ID）
     */
    @Transactional
    public void updateChapter(Chapter chapter) {
        chapterMapper.update(chapter);
    }

    /**
     * 删除指定卷及其下的所有章节。
     * @param id 卷ID
     */
    @Transactional
    public void deleteVolume(Integer id) {
        chapterMapper.deleteByVolumeId(id);
        volumeMapper.delete(id);
    }

    /**
     * 删除指定章节。
     * @param id 章节ID
     */
    @Transactional
    public void deleteChapter(Integer id) {
        chapterMapper.delete(id);
    }

    /**
     * 根据ID获取章节详情。
     * @param id 章节ID
     * @return 章节实体
     */
    public Chapter getChapterById(Integer id) {
        return chapterMapper.selectById(id);
    }

    /**
     * 按日期范围统计每本书的总字数。
     * @param start 开始日期（可为空）
     * @param end 结束日期（可为空）
     * @return 统计结果列表，每项包含书籍ID和总字数
     */
    public List<Map<String, Object>> getBookWords(Date start, Date end) {
        return chapterMapper.countWordsByWork(start, end);
    }
}