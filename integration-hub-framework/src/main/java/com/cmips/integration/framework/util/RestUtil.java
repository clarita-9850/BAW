package com.cmips.integration.framework.util;

import com.cmips.integration.framework.exception.ConnectionException;
import com.cmips.integration.framework.exception.ReadException;
import com.cmips.integration.framework.exception.SendException;
import com.cmips.integration.framework.support.OAuth2Config;
import com.cmips.integration.framework.support.RestClient;
import com.cmips.integration.framework.support.RestConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;

/**
 * Utility class for REST API operations.
 *
 * <p>This class provides static utility methods for common REST API operations
 * including GET, POST, PUT, DELETE, and OAuth2 token retrieval.
 *
 * <p>Example usage:
 * <pre>
 * RestConfig config = RestConfig.builder()
 *     .baseUrl("https://api.example.com")
 *     .basicAuth("user", "password")
 *     .build();
 *
 * // Create a reusable client
 * RestClient client = RestUtil.createClient(config);
 *
 * // GET request
 * PaymentList payments = RestUtil.get(client, "/payments", PaymentList.class);
 *
 * // POST request
 * PaymentResponse response = RestUtil.post(client, "/payments", payment, PaymentResponse.class);
 *
 * // Get OAuth2 token
 * String token = RestUtil.getOAuth2Token(oauth2Config);
 * </pre>
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public final class RestUtil {

    private static final Logger log = LoggerFactory.getLogger(RestUtil.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    private RestUtil() {
        // Utility class - prevent instantiation
    }

    /**
     * Creates a new REST client with the given configuration.
     *
     * @param config the REST configuration
     * @return a new RestClient instance
     */
    public static RestClient createClient(RestConfig config) {
        return new RestClient(config);
    }

    /**
     * Creates a new REST client with custom ObjectMapper.
     *
     * @param config the REST configuration
     * @param objectMapper the object mapper for JSON serialization
     * @return a new RestClient instance
     */
    public static RestClient createClient(RestConfig config, ObjectMapper objectMapper) {
        return new RestClient(config, objectMapper);
    }

    /**
     * Performs a GET request.
     *
     * @param <T> the response type
     * @param client the REST client
     * @param endpoint the API endpoint
     * @param responseType the response class
     * @return the response object
     * @throws ReadException if the request fails
     */
    public static <T> T get(RestClient client, String endpoint, Class<T> responseType)
            throws ReadException {
        return client.get(endpoint, responseType);
    }

    /**
     * Performs a POST request.
     *
     * @param <T> the response type
     * @param <R> the request body type
     * @param client the REST client
     * @param endpoint the API endpoint
     * @param body the request body
     * @param responseType the response class
     * @return the response object
     * @throws SendException if the request fails
     */
    public static <T, R> T post(RestClient client, String endpoint, R body, Class<T> responseType)
            throws SendException {
        return client.post(endpoint, body, responseType);
    }

    /**
     * Performs a PUT request.
     *
     * @param <T> the response type
     * @param <R> the request body type
     * @param client the REST client
     * @param endpoint the API endpoint
     * @param body the request body
     * @param responseType the response class
     * @return the response object
     * @throws SendException if the request fails
     */
    public static <T, R> T put(RestClient client, String endpoint, R body, Class<T> responseType)
            throws SendException {
        return client.put(endpoint, body, responseType);
    }

    /**
     * Performs a DELETE request.
     *
     * @param client the REST client
     * @param endpoint the API endpoint
     * @throws SendException if the request fails
     */
    public static void delete(RestClient client, String endpoint) throws SendException {
        client.delete(endpoint);
    }

    /**
     * Performs a PATCH request.
     *
     * @param <T> the response type
     * @param <R> the request body type
     * @param client the REST client
     * @param endpoint the API endpoint
     * @param body the request body
     * @param responseType the response class
     * @return the response object
     * @throws SendException if the request fails
     */
    public static <T, R> T patch(RestClient client, String endpoint, R body, Class<T> responseType)
            throws SendException {
        return client.patch(endpoint, body, responseType);
    }

    /**
     * Obtains an OAuth2 access token using client credentials flow.
     *
     * @param config the OAuth2 configuration
     * @return the access token
     * @throws ConnectionException if token retrieval fails
     */
    @SuppressWarnings("unchecked")
    public static String getOAuth2Token(OAuth2Config config) throws ConnectionException {
        try {
            StringBuilder body = new StringBuilder();
            body.append("grant_type=").append(config.getGrantType());
            body.append("&client_id=").append(config.getClientId());

            if (config.getClientSecret() != null) {
                body.append("&client_secret=").append(config.getClientSecret());
            }

            if (!config.getScopes().isEmpty()) {
                body.append("&scope=").append(config.getScopesAsString());
            }

            // Add username/password for resource owner password grant
            if (config.getUsername() != null) {
                body.append("&username=").append(config.getUsername());
            }
            if (config.getPassword() != null) {
                body.append("&password=").append(config.getPassword());
            }

            // Add additional parameters
            for (Map.Entry<String, String> param : config.getAdditionalParams().entrySet()) {
                body.append("&").append(param.getKey()).append("=").append(param.getValue());
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.getTokenUrl()))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new ConnectionException("OAuth2 token request failed with status " +
                        response.statusCode() + ": " + response.body());
            }

            Map<String, Object> tokenResponse = objectMapper.readValue(response.body(), Map.class);
            String token = (String) tokenResponse.get("access_token");

            if (token == null || token.isEmpty()) {
                throw new ConnectionException("No access_token in OAuth2 response");
            }

            log.debug("Successfully obtained OAuth2 token from {}", config.getTokenUrl());
            return token;

        } catch (ConnectionException e) {
            throw e;
        } catch (Exception e) {
            throw new ConnectionException("Failed to obtain OAuth2 token", e);
        }
    }

    /**
     * Creates a Basic Auth header value.
     *
     * @param username the username
     * @param password the password
     * @return the Basic Auth header value (including "Basic " prefix)
     */
    public static String createBasicAuthHeader(String username, String password) {
        String credentials = username + ":" + password;
        String encoded = Base64.getEncoder().encodeToString(
                credentials.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }

    /**
     * Performs a simple GET request without a client.
     *
     * @param <T> the response type
     * @param url the full URL
     * @param responseType the response class
     * @return the response object
     * @throws ReadException if the request fails
     */
    public static <T> T simpleGet(String url, Class<T> responseType) throws ReadException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                throw new ReadException("HTTP " + response.statusCode() + ": " + response.body());
            }

            return objectMapper.readValue(response.body(), responseType);

        } catch (ReadException e) {
            throw e;
        } catch (Exception e) {
            throw new ReadException("GET request failed: " + url, e);
        }
    }

    /**
     * Performs a simple POST request without a client.
     *
     * @param <T> the response type
     * @param <R> the request body type
     * @param url the full URL
     * @param body the request body
     * @param responseType the response class
     * @return the response object
     * @throws SendException if the request fails
     */
    public static <T, R> T simplePost(String url, R body, Class<T> responseType)
            throws SendException {
        try {
            String jsonBody = objectMapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                throw new SendException("HTTP " + response.statusCode() + ": " + response.body());
            }

            return objectMapper.readValue(response.body(), responseType);

        } catch (SendException e) {
            throw e;
        } catch (Exception e) {
            throw new SendException("POST request failed: " + url, e);
        }
    }
}
