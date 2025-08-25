package com.example.chat_back.controller;

import com.example.chat_back.entity.Message;
import com.example.chat_back.service.IMessageService;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;



import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 聊天控制器
 */
@RestController
@RequestMapping("")
@CrossOrigin(origins = "*") // 允许跨域访问
public class MessageController {

    @Autowired
    private IMessageService messageService;
    
    @Autowired
    private OllamaChatModel ollamaChatModel;

    @GetMapping("/messages")
    public List<Message> getMessages() {
        return messageService.list();
    }

    /**
     * 流式聊天接口 - 参考实现
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chat(@RequestParam String prompt) {
        // 保存用户消息
        Message userMsg = new Message("user", prompt);
        messageService.save(userMsg);
        
        StringBuilder aiResponseBuilder = new StringBuilder();
        
        return ollamaChatModel.stream(prompt).map(token -> {
            aiResponseBuilder.append(token);
            System.out.println("Token: '" + token + "'"); // 调试输出每个token
            return ServerSentEvent.builder(token).event("message").build();
        }).concatWith(
            Flux.just(ServerSentEvent.builder("[DONE]").event("message").build())
        ).doOnComplete(() -> {
            // 保存AI回复
            Message aiMsg = new Message("assistant", aiResponseBuilder.toString());
            messageService.save(aiMsg);
            System.out.println("完整响应: " + aiResponseBuilder);
        });
    }

    /**
     * 简单的健康检查接口
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "ok");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }


}
