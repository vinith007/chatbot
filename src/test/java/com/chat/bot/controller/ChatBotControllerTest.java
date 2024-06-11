package com.chat.bot.controller;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.chat.bot.service.ChatbotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;

import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for the ChatBotController class.
 */
class ChatBotControllerTest {

    @Mock
    private ChatbotService chatbotService;

    @Mock
    private Model model;

    @InjectMocks
    private ChatBotController chatBotController;

    /**
     * Sets up the test environment before each test.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Tests the startChat method to ensure it initializes the chat session and sets the initial messages in the model.
     */
    @Test
    void testStartChat() {
        List<String> initialMessages = Arrays.asList("Welcome to the chat!", "How can I help you today?");
        when(chatbotService.getConversationHistory()).thenReturn(initialMessages);

        String viewName = chatBotController.startChat(model);

        assertEquals("chatbot", viewName);
        verify(chatbotService).initializeChat();
        verify(chatbotService).getConversationHistory();
        verify(model).addAttribute("messages", initialMessages);
        verifyNoMoreInteractions(chatbotService, model);
    }

    /**
     * Tests the handleResponse method to ensure it processes the user's response and updates the conversation history in the model.
     */
    @Test
    void testHandleResponse() {
        String userResponse = "Hello";
        List<String> updatedMessages = Arrays.asList("You: Hello", "Hello! How can I assist you today?");
        when(chatbotService.getConversationHistory()).thenReturn(updatedMessages);

        String viewName = chatBotController.handleResponse(userResponse, model);

        assertEquals("chatbot", viewName);
        verify(chatbotService).handleUserResponse(userResponse);
        verify(chatbotService).getConversationHistory();
        verify(model).addAttribute("messages", updatedMessages);
        verifyNoMoreInteractions(chatbotService, model);
    }
}
