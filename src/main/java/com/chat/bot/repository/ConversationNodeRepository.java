package com.chat.bot.repository;

import com.chat.bot.entity.ConversationNode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing conversation nodes.
 */
public interface ConversationNodeRepository extends JpaRepository<ConversationNode, Long> {

    /**
     * Finds a conversation node by its node type.
     *
     * @param nodeType the type of the node
     * @return an Optional containing the found node, or empty if no node found
     */
    Optional<ConversationNode> findByNodeType(ConversationNode.NodeType nodeType);

    /**
     * Finds all conversation nodes by their node type.
     *
     * @param nodeType the type of the nodes
     * @return a list of nodes with the specified type
     */
    List<ConversationNode> findAllByNodeType(ConversationNode.NodeType nodeType);
}
