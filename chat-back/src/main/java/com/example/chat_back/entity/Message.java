package com.example.chat_back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 消息实体类 - 对应 messages 表
 */
@TableName("messages")
public class Message {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 发送者：user 或 assistant
     */
    private String sender;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 创建时间
     */
    @TableField("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;

    // 构造函数
    public Message() {}

    public Message(String sender, String content) {
        this.sender = sender;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }

    public Message(String sender, String content, LocalDateTime createdAt) {
        this.sender = sender;
        this.content = content;
        this.createdAt = createdAt;
    }

    // Getter 和 Setter 方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", sender='" + sender + '\'' +
                ", content='" + content + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
