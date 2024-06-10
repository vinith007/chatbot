package com.chat.bot.entity;

import jakarta.persistence.*;
import java.util.HashMap;
import java.util.Map;

@Entity
public class ConversationNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;

    private String messageName;

    private boolean deletable = true;

    @Enumerated(EnumType.STRING)
    private NodeType nodeType = NodeType.NORMAL_NODE; // Add this line

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "conversation_node_responses", joinColumns = @JoinColumn(name = "conversation_node_id"))
    @MapKeyColumn(name = "response_key")
    @Column(name = "next_node_id")
    private Map<String, Long> responses = new HashMap<>();

    // NodeType enum
    public enum NodeType {
        FIRST_NODE,
        INVALID_NODE,
        NORMAL_NODE,
        END_NODE
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageName() {
        return messageName;
    }

    public void setMessageName(String messageName) {
        this.messageName = messageName;
    }

    public boolean isDeletable() {
        return deletable;
    }

    public void setDeletable(boolean deletable) {
        this.deletable = deletable;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    public Map<String, Long> getResponses() {
        return responses;
    }

    public void setResponses(Map<String, Long> responses) {
        this.responses = responses;
    }
}
