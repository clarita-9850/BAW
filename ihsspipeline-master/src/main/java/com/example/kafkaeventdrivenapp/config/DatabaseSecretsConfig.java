package com.example.kafkaeventdrivenapp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@ConditionalOnProperty(name = "spring.datasource.use-secrets-manager", havingValue = "true")
public class DatabaseSecretsConfig {

    @Autowired
    private AwsSecretsConfig awsSecretsConfig;

    @Value("${aws.secrets-manager.secret-name}")
    private String secretName;

    @Value("${DB_PORT:5432}")
    private String dbPort;

    @Bean
    @Primary
    public DataSource dataSource() {
        String host = awsSecretsConfig.getDatabaseHost(secretName);
        String dbName = awsSecretsConfig.getDatabaseName(secretName);
        String username = awsSecretsConfig.getDatabaseUsername(secretName);
        String password = awsSecretsConfig.getDatabasePassword(secretName);
        String jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s?useSSL=false&serverTimezone=UTC", host, dbPort, dbName);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("org.postgresql.Driver");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        return new HikariDataSource(config);
    }
}
