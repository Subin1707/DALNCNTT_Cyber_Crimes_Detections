package com.example.servingwebcontent.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionManager;

/**
 * Resolve ambiguity when both transactionManager and reactiveTransactionManager exist.
 */
@Configuration
public class TransactionConfig {

    @Bean
    @Primary
    public TransactionManager primaryTransactionManager(PlatformTransactionManager txManager) {
        return txManager;
    }
}
