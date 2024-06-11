package com.chat.bot.service;

import com.chat.bot.entity.ConversationNode;
import com.chat.bot.entity.ConversationNode.NodeType;
import com.chat.bot.repository.ChatTransactionRepository;
import com.chat.bot.repository.ConversationNodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ChatbotServiceTest {

    @Mock
    private ConversationNodeRepository nodeRepository;

    @Mock
    private ChatTransactionRepository transactionRepository;

    @InjectMocks
    private ChatbotService chatbotService;

    private ConversationNode firstNode;
    private ConversationNode invalidNode;
    private ConversationNode endNode;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        firstNode = new ConversationNode();
        firstNode.setId(1L);
        firstNode.setMessage("First node message");
        firstNode.setNodeType(NodeType.FIRST_NODE);
        firstNode.setResponses(new HashMap<>());

        invalidNode = new ConversationNode();
        invalidNode.setId(2L);
        invalidNode.setMessage("Invalid response");
        invalidNode.setNodeType(NodeType.INVALID_NODE);

        endNode = new ConversationNode();
        endNode.setId(3L);
        endNode.setMessage("End message");
        endNode.setNodeType(NodeType.END_NODE);

        when(nodeRepository.findByNodeType(NodeType.FIRST_NODE)).thenReturn(Optional.of(firstNode));
        when(nodeRepository.findByNodeType(NodeType.INVALID_NODE)).thenReturn(Optional.of(invalidNode));
        when(nodeRepository.findByNodeType(NodeType.END_NODE)).thenReturn(Optional.of(endNode));
    }

    @Test
    void testInitializeChat() {
        chatbotService.initializeChat();
        verify(nodeRepository).findByNodeType(NodeType.FIRST_NODE);
        assertEquals(firstNode.getMessage(), chatbotService.getConversationHistory().get(0));
    }

    @Test
    void testHandleUserResponseInvalid() {
        chatbotService.initializeChat();  // Set the initial node first
        chatbotService.handleUserResponse("invalid");
        verify(nodeRepository, times(1)).findByNodeType(NodeType.INVALID_NODE);
        assertTrue(chatbotService.getConversationHistory().contains("Invalid response"));
    }

    @Test
    void testHandleEndOfChatNoResponses() {
        when(nodeRepository.findAllByNodeType(NodeType.END_NODE)).thenReturn(new ArrayList<>());
        when(nodeRepository.save(any())).thenReturn(endNode); // Mock the save method to return the end node
        chatbotService.initializeChat();
        firstNode.setResponses(new HashMap<>());  // Simulate no responses in the current node
        chatbotService.handleUserResponse("whatever");
        assertTrue(chatbotService.getConversationHistory().contains("Thanks for chatting!!"));
    }

    @Test
    void testLogTransaction() {
        String message = "Test message";
        String sender = "User";
        chatbotService.initializeChat();
        chatbotService.logTransaction(message, sender);

        verify(transactionRepository).save(argThat(transaction ->
                transaction.getMessage().equals(message) &&
                        transaction.getSender().equals(sender) &&
                        transaction.getSessionId() != null &&
                        transaction.getTimestamp() != null
        ));
    }

    @Test
    void testHandleEndOfChatNoEndNodeFound() {
        when(nodeRepository.findAllByNodeType(NodeType.END_NODE)).thenReturn(new ArrayList<>());
        chatbotService.initializeChat();
        firstNode.setResponses(new HashMap<>());  // Simulate no responses in the current node
        chatbotService.handleUserResponse("whatever");
        verify(nodeRepository).save(any(ConversationNode.class));  // Check if a new end node was created
        assertTrue(chatbotService.getConversationHistory().contains("Thanks for chatting!!"));
    }
}
