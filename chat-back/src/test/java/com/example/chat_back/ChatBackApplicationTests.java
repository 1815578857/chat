package com.example.chat_back;

import com.example.chat_back.config.CorsConfig;
import com.example.chat_back.controller.MessageController;
import com.example.chat_back.entity.Message;
import com.example.chat_back.service.IMessageService;
import org.junit.jupiter.api.Test;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer tests: no MySQL or Ollama needed, so they run in CI.
 */
@WebMvcTest(MessageController.class)
@Import(CorsConfig.class)
class ChatBackApplicationTests {

	@Autowired
	private MockMvc mvc;

	@MockitoBean
	private IMessageService messageService;

	@MockitoBean
	private OllamaChatModel ollamaChatModel;

	@Test
	void healthReturnsOk() throws Exception {
		mvc.perform(get("/health"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ok"));
	}

	@Test
	void messagesReturnsHistory() throws Exception {
		when(messageService.list()).thenReturn(List.of(new Message("user", "hello")));

		mvc.perform(get("/messages"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].sender").value("user"))
				.andExpect(jsonPath("$[0].content").value("hello"));
	}

	@Test
	void streamSendsTokensThenDoneAndPersistsBothMessages() throws Exception {
		when(ollamaChatModel.stream("hi")).thenReturn(Flux.just("Hel", "lo"));

		MvcResult result = mvc.perform(get("/stream").param("prompt", "hi"))
				.andExpect(request().asyncStarted())
				.andReturn();

		mvc.perform(asyncDispatch(result))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("data:Hel")))
				.andExpect(content().string(containsString("data:[DONE]")));

		verify(messageService).save(argThat((Message m) -> m != null && "user".equals(m.getSender()) && "hi".equals(m.getContent())));
		verify(messageService).save(argThat((Message m) -> m != null && "assistant".equals(m.getSender()) && "Hello".equals(m.getContent())));
	}

	@Test
	void streamRejectsBlankPrompt() throws Exception {
		mvc.perform(get("/stream").param("prompt", "   "))
				.andExpect(status().isBadRequest());
		verify(messageService, never()).save(any());
	}

	@Test
	void streamRejectsOverlongPrompt() throws Exception {
		mvc.perform(get("/stream").param("prompt", "x".repeat(4001)))
				.andExpect(status().isBadRequest());
		verify(messageService, never()).save(any());
	}

	@Test
	void corsOnlyAllowsConfiguredOrigin() throws Exception {
		mvc.perform(get("/health").header("Origin", "http://evil.example"))
				.andExpect(status().isForbidden());
		mvc.perform(get("/health").header("Origin", "http://localhost:5173"))
				.andExpect(status().isOk());
	}
}
