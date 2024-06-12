package com.chat.bot.service;

import com.chat.bot.constants.ChatMessages;
import com.chat.bot.entity.ChatTransaction;
import com.chat.bot.entity.ConversationNode;
import com.chat.bot.entity.ConversationNode.NodeType;
import com.chat.bot.repository.ChatTransactionRepository;
import com.chat.bot.repository.ConversationNodeRepository;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service class for managing chatbot operations.
 */
@Service
public class ChatbotService {


    /** Logger instance for logging. */
    private static final Logger logger = LoggerFactory.getLogger(ChatbotService.class);

    /** Repository for conversation nodes. */
    private final ConversationNodeRepository nodeRepository;

    /** Repository for chat transactions. */
    private final ChatTransactionRepository transactionRepository;

    /** The current node in the conversation. */
    private ConversationNode currentNode;

    /**
     *  Retrieves the conversation history.
     */
    @Getter
    private List<String> conversationHistory;

    /** The last valid node in the conversation. */
    private ConversationNode lastValidNode;

    /** The session ID for the current chat session. */
    private AtomicLong sessionId;

    /**
     * Constructor for ChatbotService.
     *
     * @param nodeRepository       the repository for conversation nodes.
     * @param transactionRepository the repository for chat transactions.
     */
    public ChatbotService(ConversationNodeRepository nodeRepository, ChatTransactionRepository transactionRepository) {
        this.nodeRepository = nodeRepository;
        this.transactionRepository = transactionRepository;
    }
    /**
     * Initializes a new chat session.
     */
    public void initializeChat() {
        sessionId = new AtomicLong(System.currentTimeMillis());
        currentNode = nodeRepository.findByNodeType(NodeType.FIRST_NODE).orElseGet(this::createDefaultInvalidNode);
        lastValidNode = currentNode;
        conversationHistory = new ArrayList<>();
        conversationHistory.add(currentNode.getMessage());
        logger.info("Chat initialized with first node: {}", currentNode.getMessage());
        logTransaction(currentNode.getMessage(), ChatMessages.CHATBOT.getMessage());
    }

    /**
     * Handles the user's response and updates the conversation accordingly.
     *
     * @param userResponse the user's response.
     */
    public void handleUserResponse(String userResponse) {
        logTransaction(userResponse, ChatMessages.USER.getMessage());
        logger.info("User response: {}", userResponse);
        Optional<Long> nextNodeId = currentNode.getResponses().entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(userResponse))
                .map(Map.Entry::getValue)
                .findFirst();

        if (nextNodeId.isPresent()) {
            currentNode = nodeRepository.findById(nextNodeId.get()).orElseThrow();
            logger.info("Next node found: {}", currentNode.getMessage());
            if (currentNode.getNodeType() == NodeType.END_NODE && currentNode.getResponses().isEmpty()) {
                conversationHistory.add(currentNode.getMessage());
                logTransaction(currentNode.getMessage(), ChatMessages.CHATBOT.getMessage());
                logger.info("End node reached: {}", currentNode.getMessage());
                return;
            }

            lastValidNode = currentNode;
            conversationHistory.add(ChatMessages.YOU.getMessage() + userResponse);

        } else {
            handleInvalidResponse(userResponse);
        }

        conversationHistory.add(currentNode.getMessage());
        logTransaction(currentNode.getMessage(), ChatMessages.CHATBOT.getMessage());

        if (currentNode.getResponses().isEmpty()) {
            handleEndOfChat();
        }
    }

    /**
     * Handles the case when the user's response is invalid.
     *
     * @param userResponse the user's response.
     */
    private void handleInvalidResponse(String userResponse) {
        ConversationNode invalidNode = nodeRepository.findByNodeType(NodeType.INVALID_NODE)
                .orElseThrow(() -> new IllegalStateException("No invalid node found"));

        conversationHistory.add(ChatMessages.YOU.getMessage() + userResponse);
        conversationHistory.add(invalidNode.getMessage());
        currentNode = lastValidNode;
        logger.info("Invalid response handled. Returning to last valid node.");
        logTransaction(invalidNode.getMessage(), ChatMessages.CHATBOT.getMessage());
    }

    /**
     * Handles the end of the chat when the current node has no responses.
     */
    private void handleEndOfChat() {
        List<ConversationNode> endNodes = nodeRepository.findAllByNodeType(NodeType.END_NODE);
        Optional<ConversationNode> endNodeWithResponses = endNodes.stream()
                .filter(node -> !node.getResponses().isEmpty())
                .findFirst();

        if (endNodeWithResponses.isPresent()) {
            currentNode = endNodeWithResponses.get();
            lastValidNode = currentNode;
            conversationHistory.add(currentNode.getMessage());
            logger.info("Transitioned to end node with responses: {}", currentNode.getMessage());
            logTransaction(currentNode.getMessage(), ChatMessages.CHATBOT.getMessage());
        } else {
            if (endNodes.isEmpty()) {
                currentNode = createEndNode();
                logger.info("No end node found, created a default end node: {}", currentNode.getMessage());
            } else {
                currentNode = endNodes.stream().findFirst()
                        .orElseThrow(() -> new IllegalStateException("No end node found"));
                logger.info("Transitioned to final end node: {}", currentNode.getMessage());
            }
            conversationHistory.add(currentNode.getMessage());
            logTransaction(currentNode.getMessage(), ChatMessages.CHATBOT.getMessage());
        }
    }

    /**
     * Logs a chat transaction.
     *
     * @param message the message content.
     * @param sender  the sender of the message.
     */
    public void logTransaction(String message, String sender) {
        ChatTransaction transaction = new ChatTransaction();
        transaction.setSessionId(sessionId.get());
        transaction.setMessage(message);
        transaction.setSender(sender);
        transaction.setTimestamp(LocalDateTime.now());
        transactionRepository.save(transaction);
        logger.info("Logged transaction: {} - {}", sender, message);
    }

    /**
     * Creates a default invalid node.
     *
     * @return the created invalid node.
     */
    public ConversationNode createDefaultInvalidNode() {
        ConversationNode invalidNode = new ConversationNode();
        invalidNode.setMessage(ChatMessages.INVALID_MESSAGE.getMessage());
        invalidNode.setMessageName(ChatMessages.INVALID_MESSAGE_NAME.getMessage());
        invalidNode.setDeletable(false);
        invalidNode.setNodeType(NodeType.INVALID_NODE);
        nodeRepository.save(invalidNode);
        logger.info("Created default invalid node.");
        return invalidNode;
    }

    private ConversationNode createEndNode() {
        ConversationNode endNode = new ConversationNode();
        endNode.setMessage(ChatMessages.END_MESSAGE.getMessage());
        endNode.setMessageName(ChatMessages.END_MESSAGE_NAME.getMessage());
        endNode.setDeletable(false);
        endNode.setNodeType(NodeType.END_NODE);
        nodeRepository.save(endNode);
        logger.info("Created default invalid node.");
        return endNode;
    }
}
