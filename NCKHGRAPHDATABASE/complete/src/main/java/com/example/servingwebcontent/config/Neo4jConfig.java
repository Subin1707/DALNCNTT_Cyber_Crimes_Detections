package com.example.servingwebcontent.config;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.boot.autoconfigure.neo4j.Neo4jProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.core.DatabaseSelectionProvider;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.core.transaction.Neo4jTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class Neo4jConfig {

    @Bean
    public Driver neo4jDriver(Neo4jProperties props) {
        return GraphDatabase.driver(
                props.getUri(),
                AuthTokens.basic(
                        props.getAuthentication().getUsername(),
                        props.getAuthentication().getPassword()
                )
        );
    }

    @Bean
    public Neo4jClient neo4jClient(Driver driver,
                                   DatabaseSelectionProvider databaseSelectionProvider) {
        return Neo4jClient.create(driver, databaseSelectionProvider);
    }

    @Bean
    public PlatformTransactionManager transactionManager(
            Driver driver,
            DatabaseSelectionProvider databaseSelectionProvider) {
        return new Neo4jTransactionManager(driver, databaseSelectionProvider);
    }
}
