package com.example.chat_back.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.chat_back.entity.Message;
import com.example.chat_back.mapper.MessageMapper;
import org.springframework.stereotype.Service;

/**
 * 消息服务实现类
 * 继承 ServiceImpl 获得基础的 CRUD 操作实现，满足当前需求
 */
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements IMessageService {
    // ServiceImpl已提供save()、list()等基础操作，无需额外实现
}
