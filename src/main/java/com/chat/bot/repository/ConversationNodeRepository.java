package com.chat.bot.repository;

import com.chat.bot.entity.ConversationNode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConversationNodeRepository extends JpaRepository<ConversationNode, Long> {
    Optional<ConversationNode> findByNodeType(ConversationNode.NodeType nodeType);

    List<ConversationNode> findAllByNodeType(ConversationNode.NodeType nodeType);
}
