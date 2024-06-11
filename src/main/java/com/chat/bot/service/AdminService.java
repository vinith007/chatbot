package com.chat.bot.service;

import com.chat.bot.constants.ChatMessages;
import com.chat.bot.entity.ConversationNode;
import com.chat.bot.entity.ConversationNode.NodeType;
import com.chat.bot.repository.ConversationNodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service class for managing conversation nodes.
 */
@Service
public class AdminService {

    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    /**
     * Repository for conversation nodes.
     */
    private final ConversationNodeRepository repository;

    /**
     * Constructor for injecting the ConversationNodeRepository.
     *
     * @param repository the repository to inject.
     */
    public AdminService(ConversationNodeRepository repository) {
        this.repository = repository;
    }

    /**
     * Fetches all conversation nodes.
     *
     * @return a list of all conversation nodes.
     */
    public List<ConversationNode> getAllNodes() {
        logger.info("Fetching all conversation nodes");
        return repository.findAll();
    }

    /**
     * Fetches a conversation node by its ID.
     *
     * @param id the ID of the node.
     * @return the conversation node.
     */
    public ConversationNode getNodeById(Long id) {
        logger.info("Fetching node with ID: {}", id);
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid node Id: " + id));
    }

    /**
     * Saves a conversation node.
     *
     * @param node the conversation node to save.
     */
    public void saveNode(ConversationNode node) {
        validateNodeTypeUniqueness(node);

        if (repository.count() == 0 && node.getNodeType() != NodeType.NORMAL_NODE) {
            node.setNodeType(NodeType.FIRST_NODE);
        }

        if (node.getId() != null) {
            updateNodeResponses(node);
        }

        repository.save(node);
        logger.info("Saved node with ID: {}", node.getId());
    }

    /**
     * Deletes all conversation nodes.
     */
    public void deleteAllNodes() {
        repository.deleteAll();
    }

    /**
     * Deletes a conversation node by its ID.
     *
     * @param id the ID of the node to delete.
     */
    public void deleteNode(Long id) {
        logger.info("Deleting node with ID: {}", id);
        validateNodeExistence(id);

        ConversationNode node = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid node Id: " + id));

        if (!node.isDeletable()) {
            String errorMsg = "This node cannot be deleted.";
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        updateResponsesForDeletedNode(id);
        repository.deleteById(id);
        logger.info("Deleted node with ID: {}", id);
    }

    /**
     * Saves responses for a conversation node.
     *
     * @param id           the ID of the node.
     * @param responseKeys the response keys.
     * @param nextNodeIds  the IDs of the next nodes.
     */
    public void saveResponses(Long id, List<String> responseKeys, List<Long> nextNodeIds) {
        validateResponseKeyAndNodeIdSizes(responseKeys, nextNodeIds);

        ConversationNode node = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid node Id: " + id));

        Map<String, Long> newResponses = new HashMap<>();
        mapResponseKeysToNodeIds(responseKeys, nextNodeIds, newResponses);

        node.setResponses(newResponses);
        repository.save(node);
        logger.info("Saved responses for node with ID: {}", id);
    }

    /**
     * Adds responses to a conversation node.
     *
     * @param id           the ID of the node.
     * @param responseKeys the response keys.
     * @param nextNodeIds  the IDs of the next nodes.
     */
    public void addResponses(Long id, List<String> responseKeys, List<Long> nextNodeIds) {
        validateResponseKeyAndNodeIdSizes(responseKeys, nextNodeIds);

        ConversationNode node = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid node Id: " + id));

        Map<String, Long> newResponses = node.getResponses();
        mapResponseKeysToNodeIds(responseKeys, nextNodeIds, newResponses);

        node.setResponses(newResponses);
        repository.save(node);
        logger.info("Added new responses for node with ID: {}", id);
    }

    /**
     * Validates the uniqueness of node types.
     *
     * @param node the node to validate.
     */
    private void validateNodeTypeUniqueness(ConversationNode node) {
        if (node.getNodeType() == NodeType.FIRST_NODE) {
            validateNodeTypeUniqueness(NodeType.FIRST_NODE, node.getId());
        }

        if (node.getNodeType() == NodeType.INVALID_NODE) {
            validateNodeTypeUniqueness(NodeType.INVALID_NODE, node.getId());
        }
    }

    /**
     * Validates the uniqueness of a specific node type.
     *
     * @param nodeType the type of node to validate.
     * @param nodeId   the ID of the node to validate against.
     */
    private void validateNodeTypeUniqueness(NodeType nodeType, Long nodeId) {
        Optional<ConversationNode> existingNode = repository.findByNodeType(nodeType);
        if (existingNode.isPresent() && !existingNode.get().getId().equals(nodeId)) {
            throw new IllegalArgumentException("A " + nodeType.name().toLowerCase() + " node already exists with ID: " + existingNode.get().getId());
        }
    }

    /**
     * Updates the responses of a node if it exists.
     *
     * @param node the node to update.
     */
    private void updateNodeResponses(ConversationNode node) {
        ConversationNode existingNode = repository.findById(node.getId()).orElse(null);
        if (existingNode != null && !existingNode.getResponses().isEmpty()) {
            node.setResponses(existingNode.getResponses());
        }
    }

    /**
     * Validates the existence of a node by its ID.
     *
     * @param id the ID of the node.
     */
    private void validateNodeExistence(Long id) {
        if (!repository.existsById(id)) {
            String errorMsg = "Node does not exist.";
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
    }

    /**
     * Updates the responses for a deleted node.
     *
     * @param id the ID of the deleted node.
     */
    private void updateResponsesForDeletedNode(Long id) {
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
    }

    /**
     * Validates that the sizes of the response keys and next node IDs match.
     *
     * @param responseKeys the response keys.
     * @param nextNodeIds  the next node IDs.
     */
    private void validateResponseKeyAndNodeIdSizes(List<String> responseKeys, List<Long> nextNodeIds) {
        if (responseKeys.size() != nextNodeIds.size()) {
            String errorMsg = "Mismatched response keys and next node IDs.";
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
    }

    /**
     * Maps response keys to node IDs.
     *
     * @param responseKeys the response keys.
     * @param nextNodeIds  the next node IDs.
     * @param newResponses the map to store the new responses.
     */
    private void mapResponseKeysToNodeIds(List<String> responseKeys, List<Long> nextNodeIds, Map<String, Long> newResponses) {
        for (int i = 0; i < responseKeys.size(); i++) {
            newResponses.put(responseKeys.get(i), nextNodeIds.get(i));
            logger.info("Mapping response key: {} to next node ID: {}", responseKeys.get(i), nextNodeIds.get(i));
        }
    }

    /**
     * Creates a default invalid node.
     *
     * @return the created invalid node.
     */
    private ConversationNode createDefaultInvalidNode() {
        ConversationNode invalidNode = new ConversationNode();
        invalidNode.setMessage(ChatMessages.INVALID_MESSAGE.getMessage());
        invalidNode.setMessageName(ChatMessages.INVALID_MESSAGE_NAME.getMessage());
        invalidNode.setDeletable(false);
        invalidNode.setNodeType(NodeType.INVALID_NODE);
        repository.save(invalidNode);
        return invalidNode;
    }
}
