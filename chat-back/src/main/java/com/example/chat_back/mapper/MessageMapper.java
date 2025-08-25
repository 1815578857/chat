package com.example.chat_back.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.chat_back.entity.Message;
import org.apache.ibatis.annotations.Mapper;


/**
 * 消息 Mapper 接口
 * BaseMapper已提供基础的CRUD操作，无需额外方法
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
