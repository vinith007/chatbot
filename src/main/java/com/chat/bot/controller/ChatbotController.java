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

@Controller
@RequestMapping("/chatbot")
public class ChatbotController {

    private static final Logger logger = LoggerFactory.getLogger(ChatbotController.class);

    @Autowired
    private ChatbotService chatbotService;

    @GetMapping
    public String startChat(Model model) {
        logger.info("Starting chat...");
        chatbotService.initializeChat();
        List<String> messages = chatbotService.getConversationHistory();
        model.addAttribute("messages", messages);
        logger.info("Chat started with initial messages.");
        return "chatbot";
    }

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
