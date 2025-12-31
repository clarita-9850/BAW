package com.cmips.integration.framework.support;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Configuration for OAuth2 authentication.
 *
 * <p>This class provides a builder pattern for configuring OAuth2 client credentials
 * flow and other OAuth2 grant types.
 *
 * <p>Example usage:
 * <pre>
 * // Client credentials flow
 * OAuth2Config config = OAuth2Config.builder()
 *     .tokenUrl("https://auth.example.com/oauth/token")
 *     .clientId("my-client")
 *     .clientSecret("secret")
 *     .scopes(Set.of("read", "write"))
 *     .build();
 *
 * // With additional parameters
 * OAuth2Config configWithParams = OAuth2Config.builder()
 *     .tokenUrl("https://auth.example.com/oauth/token")
 *     .clientId("my-client")
 *     .clientSecret("secret")
 *     .grantType("client_credentials")
 *     .additionalParam("audience", "https://api.example.com")
 *     .build();
 * </pre>
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public final class OAuth2Config {

    private final String tokenUrl;
    private final String clientId;
    private final String clientSecret;
    private final String grantType;
    private final Set<String> scopes;
    private final String username;
    private final String password;
    private final Duration tokenExpiryBuffer;
    private final Map<String, String> additionalParams;

    private OAuth2Config(Builder builder) {
        this.tokenUrl = builder.tokenUrl;
        this.clientId = builder.clientId;
        this.clientSecret = builder.clientSecret;
        this.grantType = builder.grantType;
        this.scopes = Collections.unmodifiableSet(new HashSet<>(builder.scopes));
        this.username = builder.username;
        this.password = builder.password;
        this.tokenExpiryBuffer = builder.tokenExpiryBuffer;
        this.additionalParams = Collections.unmodifiableMap(new HashMap<>(builder.additionalParams));
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getTokenUrl() {
        return tokenUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getGrantType() {
        return grantType;
    }

    public Set<String> getScopes() {
        return scopes;
    }

    public String getScopesAsString() {
        return String.join(" ", scopes);
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Duration getTokenExpiryBuffer() {
        return tokenExpiryBuffer;
    }

    public Map<String, String> getAdditionalParams() {
        return additionalParams;
    }

    @Override
    public String toString() {
        return "OAuth2Config{" +
                "tokenUrl='" + tokenUrl + '\'' +
                ", clientId='" + clientId + '\'' +
                ", grantType='" + grantType + '\'' +
                ", scopes=" + scopes +
                '}';
    }

    public static class Builder {
        private String tokenUrl;
        private String clientId;
        private String clientSecret;
        private String grantType = "client_credentials";
        private Set<String> scopes = new HashSet<>();
        private String username;
        private String password;
        private Duration tokenExpiryBuffer = Duration.ofSeconds(30);
        private Map<String, String> additionalParams = new HashMap<>();

        private Builder() {
        }

        public Builder tokenUrl(String tokenUrl) {
            this.tokenUrl = tokenUrl;
            return this;
        }

        public Builder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder clientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
            return this;
        }

        public Builder grantType(String grantType) {
            this.grantType = grantType;
            return this;
        }

        public Builder scopes(Set<String> scopes) {
            this.scopes = new HashSet<>(scopes);
            return this;
        }

        public Builder scope(String scope) {
            this.scopes.add(scope);
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder tokenExpiryBuffer(Duration tokenExpiryBuffer) {
            this.tokenExpiryBuffer = tokenExpiryBuffer;
            return this;
        }

        public Builder additionalParam(String key, String value) {
            this.additionalParams.put(key, value);
            return this;
        }

        public OAuth2Config build() {
            if (tokenUrl == null || tokenUrl.isBlank()) {
                throw new IllegalArgumentException("Token URL is required");
            }
            if (clientId == null || clientId.isBlank()) {
                throw new IllegalArgumentException("Client ID is required");
            }
            return new OAuth2Config(this);
        }
    }
}
