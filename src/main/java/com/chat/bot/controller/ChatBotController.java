package com.chat.bot.controller;

import com.chat.bot.service.ChatbotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Controller class for managing chatbot interactions.
 */
@Controller
@RequestMapping("/chatbot")
public class ChatBotController {

    private static final Logger logger = LoggerFactory.getLogger(ChatBotController.class);

    @Autowired
    private ChatbotService chatbotService;

    /**
     * Starts a new chat session and initializes the conversation.
     *
     * @param model the model to which attributes are added
     * @return the name of the view to be rendered
     */
    @GetMapping
    public String startChat(Model model) {
        logger.info("Starting chat...");
        chatbotService.initializeChat();
        List<String> messages = chatbotService.getConversationHistory();
        model.addAttribute("messages", messages);
        logger.info("Chat started with initial messages.");
        return "chatbot";
    }

    /**
     * Handles the user's response, updates the conversation, and returns the updated chat messages.
     *
     * @param userResponse the user's response
     * @param model        the model to which attributes are added
     * @return the name of the view to be rendered
     */
    @PostMapping("/respond")
    public String handleResponse(@RequestParam("response") String userResponse, Model model) {
        logger.info("Received user response: {}", userResponse);
        chatbotService.handleUserResponse(userResponse);
        List<String> messages = chatbotService.getConversationHistory();
        model.addAttribute("messages", messages);
        logger.info("Updated chat messages after user response.");
        return "chatbot";
    }
}
