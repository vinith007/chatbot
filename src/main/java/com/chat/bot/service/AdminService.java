package com.chat.bot.service;

import com.chat.bot.constants.ChatMessages;
import com.chat.bot.entity.ConversationNode;
import com.chat.bot.entity.ConversationNode.NodeType;
import com.chat.bot.repository.ConversationNodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    @Autowired
    private ConversationNodeRepository repository;

    public List<ConversationNode> getAllNodes() {
        logger.info("Fetching all conversation nodes");
        return repository.findAll();
    }

    public ConversationNode getNodeById(Long id) {
        logger.info("Fetching node with ID: {}", id);
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid node Id: " + id));
    }

    public void saveNode(ConversationNode node) {
        validateNodeTypeUniqueness(node);

        if (repository.count() == 0 && node.getNodeType() != NodeType.NORMAL_NODE) {
            node.setNodeType(NodeType.FIRST_NODE);
        }

        if (node.getId() != null) {
            ConversationNode existingNode = repository.findById(node.getId()).orElse(null);
            if (existingNode != null && !existingNode.getResponses().isEmpty()) {
                node.setResponses(existingNode.getResponses());
            }
        }

        repository.save(node);
        logger.info("Saved node with ID: {}", node.getId());
    }

    public void deleteAllNodes() {
        repository.deleteAll();
    }

    public void deleteNode(Long id) {
        logger.info("Deleting node with ID: {}", id);
        if (!repository.existsById(id)) {
            String errorMsg = "Node does not exist.";
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        ConversationNode node = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid node Id: " + id));
        if (!node.isDeletable()) {
            String errorMsg = "This node cannot be deleted.";
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        List<ConversationNode> allNodes = repository.findAll();
        ConversationNode invalidNode = allNodes.stream()
                .filter(n -> NodeType.INVALID_NODE.equals(n.getNodeType()))
                .findFirst()
                .orElseGet(this::createDefaultInvalidNode);
        for (ConversationNode n : allNodes) {
            Map<String, Long> responses = n.getResponses();
            if (responses.containsValue(id)) {
                responses.replaceAll((k, v) -> v.equals(id) ? invalidNode.getId() : v);
                repository.save(n);
                logger.info("Updated node ID: {} to replace response pointing to deleted node ID: {}", n.getId(), id);
            }
        }

        repository.deleteById(id);
        logger.info("Deleted node with ID: {}", id);
    }

    public void saveResponses(Long id, List<String> responseKeys, List<Long> nextNodeIds) {
        if (responseKeys.size() != nextNodeIds.size()) {
            String errorMsg = "Mismatched response keys and next node IDs.";
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        ConversationNode node = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid node Id: " + id));
        Map<String, Long> newResponses = new HashMap<>();

        for (int i = 0; i < responseKeys.size(); i++) {
            newResponses.put(responseKeys.get(i), nextNodeIds.get(i));
            logger.info("Mapping response key: {} to next node ID: {}", responseKeys.get(i), nextNodeIds.get(i));
        }

        node.setResponses(newResponses);
        repository.save(node);
        logger.info("Saved responses for node with ID: {}", id);
    }

    public void addResponses(Long id, List<String> responseKeys, List<Long> nextNodeIds) {
        if (responseKeys.size() != nextNodeIds.size()) {
            String errorMsg = "Mismatched response keys and next node IDs.";
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        ConversationNode node = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid node Id: " + id));
        Map<String, Long> newResponses = node.getResponses();

        for (int i = 0; i < responseKeys.size(); i++) {
            newResponses.put(responseKeys.get(i), nextNodeIds.get(i));
            logger.info("Adding response key: {} to next node ID: {}", responseKeys.get(i), nextNodeIds.get(i));
        }

        node.setResponses(newResponses);
        repository.save(node);
        logger.info("Added new responses for node with ID: {}", id);
    }

    private void validateNodeTypeUniqueness(ConversationNode node) {
        if (node.getNodeType() == NodeType.FIRST_NODE) {
            Optional<ConversationNode> existingFirstNode = repository.findByNodeType(NodeType.FIRST_NODE);
            if (existingFirstNode.isPresent() && !existingFirstNode.get().getId().equals(node.getId())) {
                throw new IllegalArgumentException("A first node already exists with ID: " + existingFirstNode.get().getId());
            }
        }

        if (node.getNodeType() == NodeType.INVALID_NODE) {
            Optional<ConversationNode> existingInvalidNode = repository.findByNodeType(NodeType.INVALID_NODE);
            if (existingInvalidNode.isPresent() && !existingInvalidNode.get().getId().equals(node.getId())) {
                throw new IllegalArgumentException("An invalid node already exists with ID: " + existingInvalidNode.get().getId());
            }
        }
    }

    private ConversationNode createDefaultInvalidNode() {
        ConversationNode invalidNode = new ConversationNode();
        invalidNode.setMessage(ChatMessages.INVALID_RESPONSE.getMessage());
        invalidNode.setMessageName(ChatMessages.INVALID_RESPONSE_NAME.getMessage());
        invalidNode.setDeletable(false);
        invalidNode.setNodeType(NodeType.INVALID_NODE);
        repository.save(invalidNode);
        return invalidNode;
    }
}
