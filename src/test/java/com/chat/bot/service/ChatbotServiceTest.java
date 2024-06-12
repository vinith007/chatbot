package com.chat.bot.service;

import com.chat.bot.constants.ChatMessages;
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

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the ChatbotService class.
 */
class ChatbotServiceTest {

    /**
     * Mocked repository for conversation nodes.
     */
    @Mock
    private ConversationNodeRepository nodeRepository;

    /**
     * Mocked repository for chat transactions.
     */
    @Mock
    private ChatTransactionRepository transactionRepository;

    /**
     * Service instance
     */
    @InjectMocks
    private ChatbotService chatbotService;

    /**
     * Sample conversation node representing the first node.
     */
    private ConversationNode firstNode;

    /**
     * Sample conversation node representing an invalid node.
     */
    private ConversationNode invalidNode;

    /**
     * Sample conversation node representing an end node.
     */
    private ConversationNode endNode;

    /**
     * Sets up the test environment before each test.
     */
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

    /**
     * Tests the initialization of the chat.
     */
    @Test
    void testInitializeChat() {
        chatbotService.initializeChat();
        verify(nodeRepository).findByNodeType(NodeType.FIRST_NODE);
        assertEquals(firstNode.getMessage(), chatbotService.getConversationHistory().get(0));
    }

    /**
     * Tests handling an invalid user response.
     */
    @Test
    void testHandleUserResponseInvalid() {
        chatbotService.initializeChat();
        chatbotService.handleUserResponse("invalid");
        verify(nodeRepository, times(1)).findByNodeType(NodeType.INVALID_NODE);
        assertTrue(chatbotService.getConversationHistory().contains("Invalid response"));
    }

    /**
     * Tests handling the end of the chat when no responses are available.
     */
    @Test
    void testHandleEndOfChatNoResponses() {
        when(nodeRepository.findAllByNodeType(NodeType.END_NODE)).thenReturn(new ArrayList<>());
        when(nodeRepository.save(any())).thenReturn(endNode); // Mock the save method to return the end node
        chatbotService.initializeChat();
        firstNode.setResponses(new HashMap<>());
        chatbotService.handleUserResponse("whatever");
        assertTrue(chatbotService.getConversationHistory().contains("Thanks for chatting!!"));
    }

    /**
     * Tests logging a chat transaction.
     */
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

    /**
     * Tests handling the end of the chat when no end node is found.
     */
    @Test
    void testHandleEndOfChatNoEndNodeFound() {
        when(nodeRepository.findAllByNodeType(NodeType.END_NODE)).thenReturn(new ArrayList<>());
        chatbotService.initializeChat();
        firstNode.setResponses(new HashMap<>());
        chatbotService.handleUserResponse("whatever");
        verify(nodeRepository).save(any(ConversationNode.class));
        assertTrue(chatbotService.getConversationHistory().contains("Thanks for chatting!!"));
    }

    /**
     * Tests handling a user response when the next node is found.
     */
    @Test
    void testHandleUserResponseNextNodeFound() {
        String userResponse = "Hi";
        ConversationNode nextNode = new ConversationNode();
        nextNode.setId(2L);
        nextNode.setMessage("Next node message");
        nextNode.setNodeType(NodeType.NORMAL_NODE);

        firstNode.getResponses().put(userResponse, nextNode.getId());

        chatbotService.initializeChat();
        when(nodeRepository.findById(nextNode.getId())).thenReturn(Optional.of(nextNode));

        chatbotService.handleUserResponse(userResponse);

        verify(nodeRepository).findById(nextNode.getId());
        assertTrue(chatbotService.getConversationHistory().contains("You: " + userResponse));
        assertTrue(chatbotService.getConversationHistory().contains("Next node message"));
    }

    /**
     * Tests handling a user response when the end node is reached.
     */
    @Test
    void testHandleUserResponseEndNodeReached() {
        String userResponse = "Hi";
        endNode.setResponses(new HashMap<>());
        firstNode.getResponses().put(userResponse, endNode.getId());

        chatbotService.initializeChat();
        when(nodeRepository.findById(endNode.getId())).thenReturn(Optional.of(endNode));

        chatbotService.handleUserResponse(userResponse);

        verify(nodeRepository).findById(endNode.getId());
        assertTrue(chatbotService.getConversationHistory().contains(endNode.getMessage()));
    }

    /**
     * Tests handling the end of the chat when an end node with responses is available.
     */
    @Test
    void testHandleEndOfChatWithResponses() {
        ConversationNode endNodeWithResponses = new ConversationNode();
        endNodeWithResponses.setId(4L);
        endNodeWithResponses.setMessage("End node with responses");
        endNodeWithResponses.setNodeType(NodeType.END_NODE);
        endNodeWithResponses.setResponses(Map.of("key", 5L));

        when(nodeRepository.findAllByNodeType(NodeType.END_NODE)).thenReturn(List.of(endNodeWithResponses));

        chatbotService.initializeChat();
        firstNode.setResponses(new HashMap<>());
        chatbotService.handleUserResponse("whatever");
        assertTrue(chatbotService.getConversationHistory().contains(endNodeWithResponses.getMessage()));
    }

    /**
     * Tests handling the end of the chat with the final end node.
     */
    @Test
    void testHandleEndOfChatFinalEndNode() {
        ConversationNode finalEndNode = new ConversationNode();
        finalEndNode.setId(5L);
        finalEndNode.setMessage("Final end node message");
        finalEndNode.setNodeType(NodeType.END_NODE);

        when(nodeRepository.findAllByNodeType(NodeType.END_NODE)).thenReturn(List.of(finalEndNode));

        chatbotService.initializeChat();
        firstNode.setResponses(new HashMap<>());
        chatbotService.handleUserResponse("whatever");

        assertTrue(chatbotService.getConversationHistory().contains(finalEndNode.getMessage()));
    }

    /**
     * Tests creating a default invalid node.
     */
    @Test
    void testCreateDefaultInvalidNode() {
        ConversationNode savedNode = new ConversationNode();
        savedNode.setId(1L);
        when(nodeRepository.save(any(ConversationNode.class))).thenReturn(savedNode);

        ConversationNode invalidNode = chatbotService.createDefaultInvalidNode();

        assertNotNull(invalidNode);
        assertEquals(ChatMessages.INVALID_MESSAGE.getMessage(), invalidNode.getMessage());
        assertEquals(ChatMessages.INVALID_MESSAGE_NAME.getMessage(), invalidNode.getMessageName());
        assertFalse(invalidNode.isDeletable());
        assertEquals(ConversationNode.NodeType.INVALID_NODE, invalidNode.getNodeType());
        verify(nodeRepository, times(1)).save(any(ConversationNode.class));
    }
}
