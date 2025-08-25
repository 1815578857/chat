package com.example.chat_back.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.chat_back.entity.Message;



/**
 * 消息服务接口
 * 继承 IService 获得基础的 CRUD 操作方法，满足当前需求
 */
public interface IMessageService extends IService<Message> {
    // IService已提供save()、list()等基础操作，无需额外方法
}
