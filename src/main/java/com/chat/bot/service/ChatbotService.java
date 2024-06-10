package com.chat.bot.service;

import com.chat.bot.constants.ChatMessages;
import com.chat.bot.entity.ChatTransaction;
import com.chat.bot.entity.ConversationNode;
import com.chat.bot.entity.ConversationNode.NodeType;
import com.chat.bot.repository.ChatTransactionRepository;
import com.chat.bot.repository.ConversationNodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ChatbotService {

    @Autowired
    private ConversationNodeRepository nodeRepository;

    @Autowired
    private ChatTransactionRepository transactionRepository;

    private ConversationNode currentNode;
    private List<String> conversationHistory;
    private ConversationNode lastValidNode;
    private AtomicLong sessionId;

    public void initializeChat() {
        sessionId = new AtomicLong(System.currentTimeMillis());
        currentNode = nodeRepository.findByNodeType(NodeType.FIRST_NODE).orElseGet(this::createDefaultInvalidNode);
        lastValidNode = currentNode;
        conversationHistory = new ArrayList<>();
        conversationHistory.add(currentNode.getMessage());
        logTransaction(currentNode.getMessage(), ChatMessages.CHATBOT.getMessage());
    }

    public List<String> getConversationHistory() {
        return conversationHistory;
    }

    public void handleUserResponse(String userResponse) {

        logTransaction(userResponse, ChatMessages.USER.getMessage());

        Optional<Long> nextNodeId = currentNode.getResponses().entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(userResponse))
                .map(Map.Entry::getValue)
                .findFirst();

        if (nextNodeId.isPresent()) {
            currentNode = nodeRepository.findById(nextNodeId.get()).orElseThrow();

            if (currentNode.getNodeType() == NodeType.END_NODE && currentNode.getResponses().isEmpty()) {
                conversationHistory.add(currentNode.getMessage());
                logTransaction(currentNode.getMessage(), ChatMessages.CHATBOT.getMessage());
                return;
            }

            lastValidNode = currentNode;
        } else {
            ConversationNode invalidNode = nodeRepository.findByNodeType(NodeType.INVALID_NODE)
                    .orElseThrow(() -> new IllegalStateException("No invalid node found"));
            conversationHistory.add(ChatMessages.YOU.getMessage() + userResponse);
            conversationHistory.add(invalidNode.getMessage());
            conversationHistory.add(lastValidNode.getMessage());
            currentNode = lastValidNode;
            logTransaction(invalidNode.getMessage(), ChatMessages.CHATBOT.getMessage());
            return;
        }

        conversationHistory.add(ChatMessages.YOU.getMessage() + userResponse);
        conversationHistory.add(currentNode.getMessage());
        logTransaction(currentNode.getMessage(), ChatMessages.CHATBOT.getMessage());

        if (currentNode.getResponses().isEmpty()) {
            List<ConversationNode> endNodes = nodeRepository.findAllByNodeType(NodeType.END_NODE);
            Optional<ConversationNode> endNodeWithResponses = endNodes.stream()
                    .filter(node -> !node.getResponses().isEmpty())
                    .findFirst();

            if (endNodeWithResponses.isPresent()) {
                currentNode = endNodeWithResponses.get();
                lastValidNode = currentNode;
                conversationHistory.add(currentNode.getMessage());
                logTransaction(currentNode.getMessage(), ChatMessages.CHATBOT.getMessage());
            } else {
                currentNode = endNodes.stream().findFirst().orElseThrow(() -> new IllegalStateException("No end node found"));
                conversationHistory.add(currentNode.getMessage());
                logTransaction(currentNode.getMessage(), ChatMessages.CHATBOT.getMessage());
            }
        }
    }

    private void logTransaction(String message, String sender) {
        ChatTransaction transaction = new ChatTransaction();
        transaction.setSessionId(sessionId.get());
        transaction.setMessage(message);
        transaction.setSender(sender);
        transaction.setTimestamp(LocalDateTime.now());
        transactionRepository.save(transaction);
    }

    private ConversationNode createDefaultInvalidNode() {
        ConversationNode invalidNode = new ConversationNode();
        invalidNode.setMessage(ChatMessages.INVALID_RESPONSE.getMessage());
        invalidNode.setMessageName(ChatMessages.INVALID_RESPONSE_NAME.getMessage());
        invalidNode.setDeletable(false);
        invalidNode.setNodeType(NodeType.INVALID_NODE);
        nodeRepository.save(invalidNode);
        return invalidNode;
    }

}
