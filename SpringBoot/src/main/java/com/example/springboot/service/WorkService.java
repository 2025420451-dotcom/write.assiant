package com.example.springboot.service;

import com.example.springboot.entity.Works;
import com.example.springboot.mapper.WorkMapper;
import com.example.springboot.service.WorkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class WorkService {

    WorkMapper worksMapper;

    public WorkService(WorkMapper worksMapper) {
        this.worksMapper = worksMapper;
    }

    public List<Works> getAllWorks() {return worksMapper.selectAllWorks();}
    public int createWorks(Works works) {
        return worksMapper.insertWorks(works);
    }
    public int deleteWorks(Integer id) {
        return worksMapper.deleteById(id);
    }
}