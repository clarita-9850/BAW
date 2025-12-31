package com.cmips.integration.framework.base;

import com.cmips.integration.framework.exception.ConnectionException;
import com.cmips.integration.framework.exception.SendException;
import com.cmips.integration.framework.interfaces.IOutputDestination;
import com.cmips.integration.framework.model.SendResult;
import com.cmips.integration.framework.support.RestClient;
import com.cmips.integration.framework.support.RestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base implementation of IOutputDestination for REST API endpoints.
 *
 * <p>This class provides common REST client infrastructure. Subclasses only
 * need to implement the endpoint logic for their specific API.
 *
 * <p>Example implementation:
 * <pre>
 * &#64;OutputDestination(name = "paymentApiClient", description = "Sends payments to API")
 * public class PaymentApiClient extends AbstractRestClient&lt;Payment&gt; {
 *
 *     public PaymentApiClient(RestConfig config) {
 *         super(config);
 *     }
 *
 *     &#64;Override
 *     protected SendResult doSend(Payment payment) throws SendException {
 *         PaymentResponse response = getClient().post(
 *             "/api/payments",
 *             payment,
 *             PaymentResponse.class
 *         );
 *
 *         return SendResult.builder()
 *             .success(response.isSuccess())
 *             .message(response.getMessage())
 *             .metadata("paymentId", response.getPaymentId())
 *             .build();
 *     }
 * }
 * </pre>
 *
 * @param <T> the type of data this client sends
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class AbstractRestClient<T> implements IOutputDestination<T> {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private final RestConfig config;
    private RestClient client;
    private boolean connected;
    private long sendCount;
    private long errorCount;

    /**
     * Creates a new REST client with the given configuration.
     *
     * @param config the REST configuration
     */
    protected AbstractRestClient(RestConfig config) {
        this.config = config;
        this.connected = false;
        this.sendCount = 0;
        this.errorCount = 0;
    }

    @Override
    public void connect() throws ConnectionException {
        log.debug("Initializing REST client for: {}", config.getBaseUrl());
        client = new RestClient(config);
        connected = true;
        log.info("REST client initialized for: {}", config.getBaseUrl());
    }

    @Override
    public SendResult send(T data) throws SendException {
        if (!connected) {
            throw new SendException("Not connected. Call connect() first.");
        }

        if (data == null) {
            throw new SendException("Data cannot be null");
        }

        try {
            SendResult result = doSend(data);
            if (result.isSuccess()) {
                sendCount++;
            } else {
                errorCount++;
            }
            return result;
        } catch (SendException e) {
            errorCount++;
            throw e;
        } catch (Exception e) {
            errorCount++;
            throw new SendException("REST API call failed", e);
        }
    }

    /**
     * Performs the actual send operation.
     *
     * <p>Subclasses must implement this method to send data to the REST API.
     *
     * @param data the data to send
     * @return the send result
     * @throws SendException if the send operation fails
     */
    protected abstract SendResult doSend(T data) throws SendException;

    @Override
    public boolean verify(SendResult result) {
        // Default implementation just checks the result status
        return result != null && result.isSuccess();
    }

    @Override
    public void close() {
        if (client != null) {
            client.close();
            connected = false;
            log.info("REST client closed for: {}", config.getBaseUrl());
        }
    }

    @Override
    public boolean isConnected() {
        return connected && client != null;
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    /**
     * Returns the REST configuration.
     *
     * @return the config
     */
    protected RestConfig getConfig() {
        return config;
    }

    /**
     * Returns the REST client.
     *
     * @return the client, or null if not connected
     */
    protected RestClient getClient() {
        return client;
    }

    /**
     * Returns the number of successful sends.
     *
     * @return the send count
     */
    public long getSendCount() {
        return sendCount;
    }

    /**
     * Returns the number of send errors.
     *
     * @return the error count
     */
    public long getErrorCount() {
        return errorCount;
    }

    /**
     * Resets the statistics.
     */
    public void resetStats() {
        sendCount = 0;
        errorCount = 0;
    }
}
