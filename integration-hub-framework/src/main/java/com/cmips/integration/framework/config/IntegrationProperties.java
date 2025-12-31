package com.cmips.integration.framework.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration properties for the Integration Hub Framework.
 *
 * <p>These properties can be configured in application.yml or application.properties:
 * <pre>
 * integration:
 *   enabled: true
 *   scan-packages:
 *     - com.example.flows
 *     - com.example.integrations
 *   retry:
 *     max-attempts: 3
 *     initial-delay: 1000
 *     multiplier: 2.0
 *   circuit-breaker:
 *     failure-threshold: 5
 *     reset-timeout: 30000
 *   async:
 *     core-pool-size: 10
 *     max-pool-size: 50
 *     queue-capacity: 100
 * </pre>
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "integration")
public class IntegrationProperties {

    /**
     * Whether the integration framework is enabled.
     */
    private boolean enabled = true;

    /**
     * Additional packages to scan for integration components.
     */
    private String[] scanPackages = {};

    /**
     * Retry configuration.
     */
    private RetryProperties retry = new RetryProperties();

    /**
     * Circuit breaker configuration.
     */
    private CircuitBreakerProperties circuitBreaker = new CircuitBreakerProperties();

    /**
     * Async execution configuration.
     */
    private AsyncProperties async = new AsyncProperties();

    /**
     * SFTP configuration defaults.
     */
    private SftpProperties sftp = new SftpProperties();

    /**
     * REST client configuration defaults.
     */
    private RestProperties rest = new RestProperties();

    /**
     * File processing configuration.
     */
    private FileProperties file = new FileProperties();

    /**
     * Custom properties for extension.
     */
    private Map<String, String> custom = new HashMap<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String[] getScanPackages() {
        return scanPackages;
    }

    public void setScanPackages(String[] scanPackages) {
        this.scanPackages = scanPackages;
    }

    public RetryProperties getRetry() {
        return retry;
    }

    public void setRetry(RetryProperties retry) {
        this.retry = retry;
    }

    public CircuitBreakerProperties getCircuitBreaker() {
        return circuitBreaker;
    }

    public void setCircuitBreaker(CircuitBreakerProperties circuitBreaker) {
        this.circuitBreaker = circuitBreaker;
    }

    public AsyncProperties getAsync() {
        return async;
    }

    public void setAsync(AsyncProperties async) {
        this.async = async;
    }

    public SftpProperties getSftp() {
        return sftp;
    }

    public void setSftp(SftpProperties sftp) {
        this.sftp = sftp;
    }

    public RestProperties getRest() {
        return rest;
    }

    public void setRest(RestProperties rest) {
        this.rest = rest;
    }

    public FileProperties getFile() {
        return file;
    }

    public void setFile(FileProperties file) {
        this.file = file;
    }

    public Map<String, String> getCustom() {
        return custom;
    }

    public void setCustom(Map<String, String> custom) {
        this.custom = custom;
    }

    /**
     * Retry configuration properties.
     */
    public static class RetryProperties {

        /**
         * Maximum number of retry attempts.
         */
        private int maxAttempts = 3;

        /**
         * Initial delay between retries in milliseconds.
         */
        private long initialDelay = 1000;

        /**
         * Maximum delay between retries in milliseconds.
         */
        private long maxDelay = 30000;

        /**
         * Multiplier for exponential backoff.
         */
        private double multiplier = 2.0;

        /**
         * Whether to add jitter to retry delays.
         */
        private boolean jitter = true;

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public long getInitialDelay() {
            return initialDelay;
        }

        public void setInitialDelay(long initialDelay) {
            this.initialDelay = initialDelay;
        }

        public long getMaxDelay() {
            return maxDelay;
        }

        public void setMaxDelay(long maxDelay) {
            this.maxDelay = maxDelay;
        }

        public double getMultiplier() {
            return multiplier;
        }

        public void setMultiplier(double multiplier) {
            this.multiplier = multiplier;
        }

        public boolean isJitter() {
            return jitter;
        }

        public void setJitter(boolean jitter) {
            this.jitter = jitter;
        }
    }

    /**
     * Circuit breaker configuration properties.
     */
    public static class CircuitBreakerProperties {

        /**
         * Number of failures before circuit opens.
         */
        private int failureThreshold = 5;

        /**
         * Time to wait before attempting reset in milliseconds.
         */
        private long resetTimeout = 30000;

        /**
         * Number of successful calls required to close circuit.
         */
        private int successThreshold = 3;

        /**
         * Whether circuit breaker is enabled.
         */
        private boolean enabled = true;

        public int getFailureThreshold() {
            return failureThreshold;
        }

        public void setFailureThreshold(int failureThreshold) {
            this.failureThreshold = failureThreshold;
        }

        public long getResetTimeout() {
            return resetTimeout;
        }

        public void setResetTimeout(long resetTimeout) {
            this.resetTimeout = resetTimeout;
        }

        public int getSuccessThreshold() {
            return successThreshold;
        }

        public void setSuccessThreshold(int successThreshold) {
            this.successThreshold = successThreshold;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    /**
     * Async execution configuration properties.
     */
    public static class AsyncProperties {

        /**
         * Core pool size for async executor.
         */
        private int corePoolSize = 10;

        /**
         * Maximum pool size for async executor.
         */
        private int maxPoolSize = 50;

        /**
         * Queue capacity for async executor.
         */
        private int queueCapacity = 100;

        /**
         * Keep-alive time for idle threads in seconds.
         */
        private int keepAliveSeconds = 60;

        /**
         * Thread name prefix.
         */
        private String threadNamePrefix = "integration-async-";

        public int getCorePoolSize() {
            return corePoolSize;
        }

        public void setCorePoolSize(int corePoolSize) {
            this.corePoolSize = corePoolSize;
        }

        public int getMaxPoolSize() {
            return maxPoolSize;
        }

        public void setMaxPoolSize(int maxPoolSize) {
            this.maxPoolSize = maxPoolSize;
        }

        public int getQueueCapacity() {
            return queueCapacity;
        }

        public void setQueueCapacity(int queueCapacity) {
            this.queueCapacity = queueCapacity;
        }

        public int getKeepAliveSeconds() {
            return keepAliveSeconds;
        }

        public void setKeepAliveSeconds(int keepAliveSeconds) {
            this.keepAliveSeconds = keepAliveSeconds;
        }

        public String getThreadNamePrefix() {
            return threadNamePrefix;
        }

        public void setThreadNamePrefix(String threadNamePrefix) {
            this.threadNamePrefix = threadNamePrefix;
        }
    }

    /**
     * SFTP configuration properties.
     */
    public static class SftpProperties {

        /**
         * Default connection timeout in milliseconds.
         */
        private int connectionTimeout = 30000;

        /**
         * Default read timeout in milliseconds.
         */
        private int readTimeout = 60000;

        /**
         * Whether strict host key checking is enabled.
         */
        private boolean strictHostKeyChecking = false;

        /**
         * Default port for SFTP connections.
         */
        private int defaultPort = 22;

        /**
         * Directory for temporary files during transfer.
         */
        private String tempDirectory = System.getProperty("java.io.tmpdir");

        public int getConnectionTimeout() {
            return connectionTimeout;
        }

        public void setConnectionTimeout(int connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
        }

        public int getReadTimeout() {
            return readTimeout;
        }

        public void setReadTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
        }

        public boolean isStrictHostKeyChecking() {
            return strictHostKeyChecking;
        }

        public void setStrictHostKeyChecking(boolean strictHostKeyChecking) {
            this.strictHostKeyChecking = strictHostKeyChecking;
        }

        public int getDefaultPort() {
            return defaultPort;
        }

        public void setDefaultPort(int defaultPort) {
            this.defaultPort = defaultPort;
        }

        public String getTempDirectory() {
            return tempDirectory;
        }

        public void setTempDirectory(String tempDirectory) {
            this.tempDirectory = tempDirectory;
        }
    }

    /**
     * REST client configuration properties.
     */
    public static class RestProperties {

        /**
         * Default connection timeout in milliseconds.
         */
        private int connectionTimeout = 10000;

        /**
         * Default read timeout in milliseconds.
         */
        private int readTimeout = 30000;

        /**
         * Maximum connections per route.
         */
        private int maxConnectionsPerRoute = 20;

        /**
         * Maximum total connections.
         */
        private int maxTotalConnections = 100;

        /**
         * Whether to follow redirects.
         */
        private boolean followRedirects = true;

        /**
         * User agent string.
         */
        private String userAgent = "IntegrationHub/1.0";

        public int getConnectionTimeout() {
            return connectionTimeout;
        }

        public void setConnectionTimeout(int connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
        }

        public int getReadTimeout() {
            return readTimeout;
        }

        public void setReadTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
        }

        public int getMaxConnectionsPerRoute() {
            return maxConnectionsPerRoute;
        }

        public void setMaxConnectionsPerRoute(int maxConnectionsPerRoute) {
            this.maxConnectionsPerRoute = maxConnectionsPerRoute;
        }

        public int getMaxTotalConnections() {
            return maxTotalConnections;
        }

        public void setMaxTotalConnections(int maxTotalConnections) {
            this.maxTotalConnections = maxTotalConnections;
        }

        public boolean isFollowRedirects() {
            return followRedirects;
        }

        public void setFollowRedirects(boolean followRedirects) {
            this.followRedirects = followRedirects;
        }

        public String getUserAgent() {
            return userAgent;
        }

        public void setUserAgent(String userAgent) {
            this.userAgent = userAgent;
        }
    }

    /**
     * File processing configuration properties.
     */
    public static class FileProperties {

        /**
         * Default encoding for file reading.
         */
        private String encoding = "UTF-8";

        /**
         * Buffer size for file operations in bytes.
         */
        private int bufferSize = 8192;

        /**
         * Directory for archive files.
         */
        private String archiveDirectory = "archive";

        /**
         * Directory for error files.
         */
        private String errorDirectory = "error";

        /**
         * Whether to delete source files after processing.
         */
        private boolean deleteAfterProcess = false;

        /**
         * File watch poll interval.
         */
        private Duration watchPollInterval = Duration.ofSeconds(10);

        public String getEncoding() {
            return encoding;
        }

        public void setEncoding(String encoding) {
            this.encoding = encoding;
        }

        public int getBufferSize() {
            return bufferSize;
        }

        public void setBufferSize(int bufferSize) {
            this.bufferSize = bufferSize;
        }

        public String getArchiveDirectory() {
            return archiveDirectory;
        }

        public void setArchiveDirectory(String archiveDirectory) {
            this.archiveDirectory = archiveDirectory;
        }

        public String getErrorDirectory() {
            return errorDirectory;
        }

        public void setErrorDirectory(String errorDirectory) {
            this.errorDirectory = errorDirectory;
        }

        public boolean isDeleteAfterProcess() {
            return deleteAfterProcess;
        }

        public void setDeleteAfterProcess(boolean deleteAfterProcess) {
            this.deleteAfterProcess = deleteAfterProcess;
        }

        public Duration getWatchPollInterval() {
            return watchPollInterval;
        }

        public void setWatchPollInterval(Duration watchPollInterval) {
            this.watchPollInterval = watchPollInterval;
        }
    }
}
