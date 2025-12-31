package com.cmips.integration.framework.support;

import com.cmips.integration.framework.exception.ConnectionException;
import com.cmips.integration.framework.exception.SendException;
import com.cmips.integration.framework.exception.ReadException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * REST API client with support for various authentication methods.
 *
 * <p>This class provides a simplified interface for making HTTP requests to REST APIs.
 * It supports Basic Auth, OAuth2, API Key, and Bearer Token authentication.
 *
 * <p>Example usage:
 * <pre>
 * RestConfig config = RestConfig.builder()
 *     .baseUrl("https://api.example.com")
 *     .basicAuth("user", "password")
 *     .build();
 *
 * try (RestClient client = new RestClient(config)) {
 *     PaymentResponse response = client.post("/payments", payment, PaymentResponse.class);
 *     List&lt;Payment&gt; payments = client.get("/payments", PaymentList.class).getPayments();
 * }
 * </pre>
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public class RestClient implements Closeable {

    private static final Logger log = LoggerFactory.getLogger(RestClient.class);

    private final RestConfig config;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Map<String, TokenCache> tokenCache;

    /**
     * Creates a new REST client with the given configuration.
     *
     * @param config the REST configuration
     */
    public RestClient(RestConfig config) {
        this(config, new ObjectMapper());
    }

    /**
     * Creates a new REST client with custom ObjectMapper.
     *
     * @param config the REST configuration
     * @param objectMapper the object mapper for JSON serialization
     */
    public RestClient(RestConfig config, ObjectMapper objectMapper) {
        this.config = config;
        this.objectMapper = objectMapper;
        this.tokenCache = new ConcurrentHashMap<>();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(config.getConnectTimeout())
                .followRedirects(config.isFollowRedirects()
                        ? HttpClient.Redirect.NORMAL
                        : HttpClient.Redirect.NEVER)
                .build();
    }

    /**
     * Performs a GET request.
     *
     * @param <T> the response type
     * @param endpoint the API endpoint
     * @param responseType the response class
     * @return the response object
     * @throws ReadException if the request fails
     */
    public <T> T get(String endpoint, Class<T> responseType) throws ReadException {
        try {
            HttpRequest request = buildRequest("GET", endpoint, null);
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            checkResponse(response);
            return deserialize(response.body(), responseType);

        } catch (Exception e) {
            throw new ReadException("GET request failed: " + endpoint, e);
        }
    }

    /**
     * Performs a POST request.
     *
     * @param <T> the response type
     * @param <R> the request body type
     * @param endpoint the API endpoint
     * @param body the request body
     * @param responseType the response class
     * @return the response object
     * @throws SendException if the request fails
     */
    public <T, R> T post(String endpoint, R body, Class<T> responseType) throws SendException {
        try {
            HttpRequest request = buildRequest("POST", endpoint, body);
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            checkResponse(response);
            return deserialize(response.body(), responseType);

        } catch (Exception e) {
            throw new SendException("POST request failed: " + endpoint, e);
        }
    }

    /**
     * Performs a PUT request.
     *
     * @param <T> the response type
     * @param <R> the request body type
     * @param endpoint the API endpoint
     * @param body the request body
     * @param responseType the response class
     * @return the response object
     * @throws SendException if the request fails
     */
    public <T, R> T put(String endpoint, R body, Class<T> responseType) throws SendException {
        try {
            HttpRequest request = buildRequest("PUT", endpoint, body);
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            checkResponse(response);
            return deserialize(response.body(), responseType);

        } catch (Exception e) {
            throw new SendException("PUT request failed: " + endpoint, e);
        }
    }

    /**
     * Performs a DELETE request.
     *
     * @param endpoint the API endpoint
     * @throws SendException if the request fails
     */
    public void delete(String endpoint) throws SendException {
        try {
            HttpRequest request = buildRequest("DELETE", endpoint, null);
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            checkResponse(response);

        } catch (Exception e) {
            throw new SendException("DELETE request failed: " + endpoint, e);
        }
    }

    /**
     * Performs a PATCH request.
     *
     * @param <T> the response type
     * @param <R> the request body type
     * @param endpoint the API endpoint
     * @param body the request body
     * @param responseType the response class
     * @return the response object
     * @throws SendException if the request fails
     */
    public <T, R> T patch(String endpoint, R body, Class<T> responseType) throws SendException {
        try {
            HttpRequest request = buildRequest("PATCH", endpoint, body);
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            checkResponse(response);
            return deserialize(response.body(), responseType);

        } catch (Exception e) {
            throw new SendException("PATCH request failed: " + endpoint, e);
        }
    }

    private <R> HttpRequest buildRequest(String method, String endpoint, R body) throws Exception {
        String url = config.getBaseUrl() + endpoint;
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(config.getReadTimeout());

        // Add default headers
        for (Map.Entry<String, String> header : config.getDefaultHeaders().entrySet()) {
            builder.header(header.getKey(), header.getValue());
        }

        // Add authentication
        addAuthentication(builder);

        // Set method and body
        if (body != null) {
            String jsonBody = objectMapper.writeValueAsString(body);
            builder.header("Content-Type", "application/json");
            builder.method(method, HttpRequest.BodyPublishers.ofString(jsonBody));
        } else {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        }

        return builder.build();
    }

    private void addAuthentication(HttpRequest.Builder builder) throws ConnectionException {
        switch (config.getAuthType()) {
            case BASIC -> {
                String credentials = config.getUsername() + ":" + config.getPassword();
                String encoded = Base64.getEncoder().encodeToString(
                        credentials.getBytes(StandardCharsets.UTF_8));
                builder.header("Authorization", "Basic " + encoded);
            }
            case API_KEY -> {
                builder.header(config.getApiKeyHeader(), config.getApiKeyValue());
            }
            case BEARER_TOKEN -> {
                builder.header("Authorization", "Bearer " + config.getApiKeyValue());
            }
            case OAUTH2 -> {
                String token = getOAuth2Token();
                builder.header("Authorization", "Bearer " + token);
            }
            case NONE -> {
                // No authentication
            }
        }
    }

    private String getOAuth2Token() throws ConnectionException {
        OAuth2Config oauth2 = config.getOauth2Config();
        String cacheKey = oauth2.getTokenUrl() + ":" + oauth2.getClientId();

        TokenCache cached = tokenCache.get(cacheKey);
        if (cached != null && !cached.isExpired(oauth2.getTokenExpiryBuffer())) {
            return cached.token;
        }

        // Fetch new token
        try {
            String tokenRequestBody = "grant_type=" + oauth2.getGrantType() +
                    "&client_id=" + oauth2.getClientId() +
                    "&client_secret=" + oauth2.getClientSecret();

            if (!oauth2.getScopes().isEmpty()) {
                tokenRequestBody += "&scope=" + oauth2.getScopesAsString();
            }

            for (Map.Entry<String, String> param : oauth2.getAdditionalParams().entrySet()) {
                tokenRequestBody += "&" + param.getKey() + "=" + param.getValue();
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(oauth2.getTokenUrl()))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(tokenRequestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new ConnectionException("OAuth2 token request failed: " + response.body());
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> tokenResponse = objectMapper.readValue(response.body(), Map.class);
            String token = (String) tokenResponse.get("access_token");
            int expiresIn = tokenResponse.containsKey("expires_in")
                    ? ((Number) tokenResponse.get("expires_in")).intValue()
                    : 3600;

            tokenCache.put(cacheKey, new TokenCache(token, expiresIn));
            return token;

        } catch (Exception e) {
            throw new ConnectionException("Failed to obtain OAuth2 token", e);
        }
    }

    private void checkResponse(HttpResponse<String> response) throws SendException {
        if (response.statusCode() >= 400) {
            throw new SendException("HTTP " + response.statusCode() + ": " + response.body());
        }
    }

    private <T> T deserialize(String json, Class<T> type) throws JsonProcessingException {
        if (json == null || json.isBlank()) {
            return null;
        }
        return objectMapper.readValue(json, type);
    }

    @Override
    public void close() {
        tokenCache.clear();
    }

    private static class TokenCache {
        final String token;
        final Instant expiresAt;

        TokenCache(String token, int expiresInSeconds) {
            this.token = token;
            this.expiresAt = Instant.now().plusSeconds(expiresInSeconds);
        }

        boolean isExpired(java.time.Duration buffer) {
            return Instant.now().plus(buffer).isAfter(expiresAt);
        }
    }
}
