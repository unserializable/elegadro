package org.elegadro.poc.config;

import org.elegadro.persist.file.impl.FilePersisterImpl;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;

/**
 * @author Taimo Peelo
 */
@Configuration
@ComponentScan(basePackageClasses = {
    /* File system persistence */
    FilePersisterImpl.class,
})
public class PersistenceConfiguration {
    // Neo4J
    private @Value("${elegadro.neo4j.uri:bolt://localhost:7687}") String neo4jUri;
    private @Value("${elegadro.neo4j.user.name:elegadro}") String neo4jUserName;
    private @Value("${elegadro.neo4j.user.password:ele}") String neo4jUserPassword;

    @PreDestroy
    public void perstroy() {
        graphDriver().close();
    }

    @Bean
    public Driver graphDriver() {
        return GraphDatabase.driver(neo4jUri, AuthTokens.basic(neo4jUserName, neo4jUserPassword));
    }
}
