package com.example.kafkaeventdrivenapp.config;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AwsSecretsConfig {

    @Value("${aws.region:us-east-1}")
    private String awsRegion;

    @Autowired
    private ObjectMapper objectMapper;

    @Bean
    public AWSSecretsManager awsSecretsManager() {
        return AWSSecretsManagerClientBuilder.standard()
                .withRegion(Regions.fromName(awsRegion))
                .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                .build();
    }

    public String getSecretValue(String secretName) {
        try {
            AWSSecretsManager client = awsSecretsManager();
            GetSecretValueRequest request = new GetSecretValueRequest()
                    .withSecretId(secretName);
            
            GetSecretValueResult result = client.getSecretValue(request);
            return result.getSecretString();
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving secret: " + e.getMessage(), e);
        }
    }

    public String getDatabaseUsername(String secretName) {
        try {
            String secretValue = getSecretValue(secretName);
            JsonNode jsonNode = objectMapper.readTree(secretValue);
            return jsonNode.get("username").asText();
        } catch (Exception e) {
            throw new RuntimeException("Error parsing username from secret: " + e.getMessage(), e);
        }
    }

    public String getDatabasePassword(String secretName) {
        try {
            String secretValue = getSecretValue(secretName);
            JsonNode jsonNode = objectMapper.readTree(secretValue);
            return jsonNode.get("password").asText();
        } catch (Exception e) {
            throw new RuntimeException("Error parsing password from secret: " + e.getMessage(), e);
        }
    }

    @Value("${DB_HOST:}")
    private String defaultHost;
    
    @Value("${DB_NAME:}")
    private String defaultDbName;

    public String getDatabaseHost(String secretName) {
        try {
            String secretValue = getSecretValue(secretName);
            JsonNode jsonNode = objectMapper.readTree(secretValue);
            JsonNode hostNode = jsonNode.get("host");
            if (hostNode != null && !hostNode.isNull()) {
                return hostNode.asText();
            }
            // Fallback to environment variable if not in secret
            if (defaultHost != null && !defaultHost.isEmpty()) {
                return defaultHost;
            }
            throw new RuntimeException("host not found in secret and DB_HOST environment variable not set");
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error parsing host from secret: " + e.getMessage(), e);
        }
    }

    public String getDatabaseName(String secretName) {
        try {
            String secretValue = getSecretValue(secretName);
            JsonNode jsonNode = objectMapper.readTree(secretValue);
            JsonNode dbnameNode = jsonNode.get("dbname");
            if (dbnameNode != null && !dbnameNode.isNull()) {
                return dbnameNode.asText();
            }
            // Fallback to environment variable if not in secret
            if (defaultDbName != null && !defaultDbName.isEmpty()) {
                return defaultDbName;
            }
            throw new RuntimeException("dbname not found in secret and DB_NAME environment variable not set");
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error parsing dbname from secret: " + e.getMessage(), e);
        }
    }
}
