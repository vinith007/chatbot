package com.chat.bot.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity class representing a chat transaction.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatTransaction {

    /**
     * Unique identifier for the chat transaction.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Session ID associated with the chat transaction.
     */
    private Long sessionId;

    /**
     * Message content of the chat transaction.
     */
    private String message;

    /**
     * Sender of the message in the chat transaction.
     */
    private String sender;

    /**
     * Timestamp of when the chat transaction occurred.
     */
    private LocalDateTime timestamp;
}
