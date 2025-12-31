package com.cmips.integration.framework.support;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for REST API clients.
 *
 * <p>This class provides a builder pattern for configuring REST client connections.
 * It supports various authentication methods including OAuth2, Basic Auth, and API keys.
 *
 * <p>Example usage:
 * <pre>
 * // Basic auth
 * RestConfig basicConfig = RestConfig.builder()
 *     .baseUrl("https://api.example.com")
 *     .basicAuth("user", "password")
 *     .connectTimeout(Duration.ofSeconds(30))
 *     .build();
 *
 * // OAuth2
 * RestConfig oauthConfig = RestConfig.builder()
 *     .baseUrl("https://api.example.com")
 *     .oauth2Config(OAuth2Config.builder()
 *         .tokenUrl("https://auth.example.com/oauth/token")
 *         .clientId("my-client")
 *         .clientSecret("secret")
 *         .build())
 *     .build();
 *
 * // API Key
 * RestConfig apiKeyConfig = RestConfig.builder()
 *     .baseUrl("https://api.example.com")
 *     .apiKey("X-API-Key", "my-api-key")
 *     .build();
 * </pre>
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public final class RestConfig {

    private final String baseUrl;
    private final AuthType authType;
    private final String username;
    private final String password;
    private final String apiKeyHeader;
    private final String apiKeyValue;
    private final OAuth2Config oauth2Config;
    private final Duration connectTimeout;
    private final Duration readTimeout;
    private final Duration writeTimeout;
    private final Map<String, String> defaultHeaders;
    private final int maxRetries;
    private final boolean followRedirects;

    private RestConfig(Builder builder) {
        this.baseUrl = builder.baseUrl;
        this.authType = builder.authType;
        this.username = builder.username;
        this.password = builder.password;
        this.apiKeyHeader = builder.apiKeyHeader;
        this.apiKeyValue = builder.apiKeyValue;
        this.oauth2Config = builder.oauth2Config;
        this.connectTimeout = builder.connectTimeout;
        this.readTimeout = builder.readTimeout;
        this.writeTimeout = builder.writeTimeout;
        this.defaultHeaders = Collections.unmodifiableMap(new HashMap<>(builder.defaultHeaders));
        this.maxRetries = builder.maxRetries;
        this.followRedirects = builder.followRedirects;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public AuthType getAuthType() {
        return authType;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getApiKeyHeader() {
        return apiKeyHeader;
    }

    public String getApiKeyValue() {
        return apiKeyValue;
    }

    public OAuth2Config getOauth2Config() {
        return oauth2Config;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public Duration getWriteTimeout() {
        return writeTimeout;
    }

    public Map<String, String> getDefaultHeaders() {
        return defaultHeaders;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public boolean isFollowRedirects() {
        return followRedirects;
    }

    @Override
    public String toString() {
        return "RestConfig{" +
                "baseUrl='" + baseUrl + '\'' +
                ", authType=" + authType +
                ", connectTimeout=" + connectTimeout +
                ", readTimeout=" + readTimeout +
                '}';
    }

    public enum AuthType {
        NONE,
        BASIC,
        API_KEY,
        OAUTH2,
        BEARER_TOKEN
    }

    public static class Builder {
        private String baseUrl;
        private AuthType authType = AuthType.NONE;
        private String username;
        private String password;
        private String apiKeyHeader;
        private String apiKeyValue;
        private OAuth2Config oauth2Config;
        private Duration connectTimeout = Duration.ofSeconds(30);
        private Duration readTimeout = Duration.ofSeconds(60);
        private Duration writeTimeout = Duration.ofSeconds(60);
        private final Map<String, String> defaultHeaders = new HashMap<>();
        private int maxRetries = 3;
        private boolean followRedirects = true;

        private Builder() {
        }

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder basicAuth(String username, String password) {
            this.authType = AuthType.BASIC;
            this.username = username;
            this.password = password;
            return this;
        }

        public Builder apiKey(String headerName, String apiKey) {
            this.authType = AuthType.API_KEY;
            this.apiKeyHeader = headerName;
            this.apiKeyValue = apiKey;
            return this;
        }

        public Builder oauth2Config(OAuth2Config oauth2Config) {
            this.authType = AuthType.OAUTH2;
            this.oauth2Config = oauth2Config;
            return this;
        }

        public Builder bearerToken(String token) {
            this.authType = AuthType.BEARER_TOKEN;
            this.apiKeyValue = token;
            return this;
        }

        public Builder connectTimeout(Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder readTimeout(Duration readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        public Builder writeTimeout(Duration writeTimeout) {
            this.writeTimeout = writeTimeout;
            return this;
        }

        public Builder header(String name, String value) {
            this.defaultHeaders.put(name, value);
            return this;
        }

        public Builder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public Builder followRedirects(boolean followRedirects) {
            this.followRedirects = followRedirects;
            return this;
        }

        public RestConfig build() {
            if (baseUrl == null || baseUrl.isBlank()) {
                throw new IllegalArgumentException("Base URL is required");
            }
            return new RestConfig(this);
        }
    }
}
