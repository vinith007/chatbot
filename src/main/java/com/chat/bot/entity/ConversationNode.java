package com.chat.bot.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Entity class representing a conversation node.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationNode {

    /**
     * Unique identifier for the conversation node.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Message associated with the conversation node.
     */
    private String message;

    /**
     * Name of the message.
     */
    private String messageName;

    /**
     * Indicates whether the node is deletable.
     */
    private boolean deletable = true;

    /**
     * Type of the node.
     */
    @Enumerated(EnumType.STRING)
    private NodeType nodeType = NodeType.NORMAL_NODE;

    /**
     * Responses associated with the conversation node.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "conversation_node_responses", joinColumns = @JoinColumn(name = "conversation_node_id"))
    @MapKeyColumn(name = "response_key")
    @Column(name = "next_node_id")
    private Map<String, Long> responses = new HashMap<>();

    /**
     * Enum representing the type of the node.
     */
    public enum NodeType {
        FIRST_NODE,
        INVALID_NODE,
        NORMAL_NODE,
        END_NODE
    }
}
