package com.example.chat_back.controller;

import com.example.chat_back.entity.Message;
import com.example.chat_back.service.IMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

/**
 * Chat endpoints: history, streaming chat (SSE) and health check.
 */
@RestController
public class MessageController {

    private static final Logger log = LoggerFactory.getLogger(MessageController.class);

    private final IMessageService messageService;
    private final OllamaChatModel ollamaChatModel;
    private final int maxPromptLength;

    public MessageController(IMessageService messageService,
                             OllamaChatModel ollamaChatModel,
                             @Value("${app.chat.max-prompt-length:4000}") int maxPromptLength) {
        this.messageService = messageService;
        this.ollamaChatModel = ollamaChatModel;
        this.maxPromptLength = maxPromptLength;
    }

    @GetMapping("/messages")
    public List<Message> getMessages() {
        return messageService.list();
    }

    /**
     * Streams the model's answer token by token as Server-Sent Events,
     * then persists both the prompt and the full answer.
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chat(@RequestParam String prompt) {
        if (prompt == null || prompt.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "prompt must not be blank");
        }
        if (prompt.length() > maxPromptLength) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "prompt exceeds " + maxPromptLength + " characters");
        }

        messageService.save(new Message("user", prompt));

        StringBuilder answer = new StringBuilder();

        return ollamaChatModel.stream(prompt)
                .map(token -> {
                    answer.append(token);
                    return ServerSentEvent.builder(token).event("message").build();
                })
                .concatWith(Flux.just(ServerSentEvent.builder("[DONE]").event("message").build()))
                .doOnComplete(() -> {
                    messageService.save(new Message("assistant", answer.toString()));
                    log.debug("Stream finished, {} chars", answer.length());
                })
                .doOnError(e -> log.warn("Stream failed: {}", e.getMessage()));
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "ok", "timestamp", System.currentTimeMillis());
    }
}
