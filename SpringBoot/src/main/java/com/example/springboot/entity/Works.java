
// src/main/java/com/example/entity/Works.java
package com.example.springboot.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Works {
    private Integer id;
    private String titile;      // 保持与数据库列名一致
    private Integer userId;     // 对应 user_id（需开启驼峰转换）
    private Integer isDraft;    // 对应 is_draft
    private LocalDateTime createdAt; // 对应 created_at


    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getTitile() {
        return titile;
    }
    public void setTitile(String titile) {
        this.titile = titile;
    }
    public Integer getUserId() {
        return userId;
    }
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    public Integer getIsDraft() {
        return isDraft;
    }
    public void setIsDraft(Integer isDraft) {
        this.isDraft = isDraft;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
