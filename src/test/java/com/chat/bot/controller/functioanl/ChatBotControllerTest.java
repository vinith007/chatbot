package com.chat.bot.controller.functional;

import com.chat.bot.service.ChatbotService;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.mockito.Mockito.*;

/**
 * Functional tests for the ChatBotController class.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ChatBotControllerTest {

    @LocalServerPort
    private int port;

    @MockBean
    private ChatbotService chatbotService;

    /**
     * Sets up the test environment before each test.
     */
    @BeforeEach
    public void setup() {
        RestAssured.port = port;
    }

    /**
     * Tests the start of a new chat session.
     */
    @Test
    public void testStartChat() {
        List<String> messages = List.of("Hello, how can I help you?");
        when(chatbotService.getConversationHistory()).thenReturn(messages);

        given()
                .when()
                .get("/chatbot")
                .then()
                .statusCode(HttpStatus.OK.value());

        verify(chatbotService, times(1)).initializeChat();
        verify(chatbotService, times(1)).getConversationHistory();
    }
}
