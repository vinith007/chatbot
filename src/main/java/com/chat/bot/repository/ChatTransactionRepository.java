package com.chat.bot.repository;

import com.chat.bot.entity.ChatTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for managing chat transactions.
 */
public interface ChatTransactionRepository extends JpaRepository<ChatTransaction, Long> {
}
