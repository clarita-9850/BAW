package com.cmips.integration.framework.support;

import java.nio.file.Path;
import java.util.Properties;

/**
 * Configuration for SFTP connections.
 *
 * <p>This class provides a builder pattern for configuring SFTP client connections.
 * It supports both password and private key authentication.
 *
 * <p>Example usage:
 * <pre>
 * // Password authentication
 * SftpConfig config = SftpConfig.builder()
 *     .host("sftp.example.com")
 *     .port(22)
 *     .username("user")
 *     .password("secret")
 *     .strictHostKeyChecking(false)
 *     .build();
 *
 * // Private key authentication
 * SftpConfig keyConfig = SftpConfig.builder()
 *     .host("sftp.example.com")
 *     .username("user")
 *     .privateKeyFile(Path.of("/home/user/.ssh/id_rsa"))
 *     .privateKeyPassphrase("keypass")
 *     .knownHostsFile(Path.of("/home/user/.ssh/known_hosts"))
 *     .build();
 * </pre>
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public final class SftpConfig {

    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final Path privateKeyFile;
    private final String privateKeyPassphrase;
    private final Path knownHostsFile;
    private final boolean strictHostKeyChecking;
    private final int connectionTimeout;
    private final int sessionTimeout;
    private final int retryAttempts;
    private final long retryDelayMs;
    private final Properties sessionConfig;

    private SftpConfig(Builder builder) {
        this.host = builder.host;
        this.port = builder.port;
        this.username = builder.username;
        this.password = builder.password;
        this.privateKeyFile = builder.privateKeyFile;
        this.privateKeyPassphrase = builder.privateKeyPassphrase;
        this.knownHostsFile = builder.knownHostsFile;
        this.strictHostKeyChecking = builder.strictHostKeyChecking;
        this.connectionTimeout = builder.connectionTimeout;
        this.sessionTimeout = builder.sessionTimeout;
        this.retryAttempts = builder.retryAttempts;
        this.retryDelayMs = builder.retryDelayMs;
        this.sessionConfig = new Properties();
        if (builder.sessionConfig != null) {
            this.sessionConfig.putAll(builder.sessionConfig);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Path getPrivateKeyFile() {
        return privateKeyFile;
    }

    public String getPrivateKeyPassphrase() {
        return privateKeyPassphrase;
    }

    public Path getKnownHostsFile() {
        return knownHostsFile;
    }

    public boolean isStrictHostKeyChecking() {
        return strictHostKeyChecking;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public int getSessionTimeout() {
        return sessionTimeout;
    }

    public int getRetryAttempts() {
        return retryAttempts;
    }

    public long getRetryDelayMs() {
        return retryDelayMs;
    }

    public Properties getSessionConfig() {
        Properties copy = new Properties();
        copy.putAll(sessionConfig);
        return copy;
    }

    public boolean usesPrivateKey() {
        return privateKeyFile != null;
    }

    @Override
    public String toString() {
        return "SftpConfig{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", username='" + username + '\'' +
                ", usesPrivateKey=" + usesPrivateKey() +
                ", strictHostKeyChecking=" + strictHostKeyChecking +
                '}';
    }

    public static class Builder {
        private String host;
        private int port = 22;
        private String username;
        private String password;
        private Path privateKeyFile;
        private String privateKeyPassphrase;
        private Path knownHostsFile;
        private boolean strictHostKeyChecking = true;
        private int connectionTimeout = 30000;
        private int sessionTimeout = 0;
        private int retryAttempts = 3;
        private long retryDelayMs = 1000;
        private Properties sessionConfig;

        private Builder() {
        }

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
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

        public Builder privateKeyFile(Path privateKeyFile) {
            this.privateKeyFile = privateKeyFile;
            return this;
        }

        public Builder privateKeyPassphrase(String privateKeyPassphrase) {
            this.privateKeyPassphrase = privateKeyPassphrase;
            return this;
        }

        public Builder knownHostsFile(Path knownHostsFile) {
            this.knownHostsFile = knownHostsFile;
            return this;
        }

        public Builder strictHostKeyChecking(boolean strictHostKeyChecking) {
            this.strictHostKeyChecking = strictHostKeyChecking;
            return this;
        }

        public Builder connectionTimeout(int connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
            return this;
        }

        public Builder sessionTimeout(int sessionTimeout) {
            this.sessionTimeout = sessionTimeout;
            return this;
        }

        public Builder retryAttempts(int retryAttempts) {
            this.retryAttempts = retryAttempts;
            return this;
        }

        public Builder retryDelayMs(long retryDelayMs) {
            this.retryDelayMs = retryDelayMs;
            return this;
        }

        public Builder sessionConfig(Properties sessionConfig) {
            this.sessionConfig = sessionConfig;
            return this;
        }

        public Builder sessionProperty(String key, String value) {
            if (this.sessionConfig == null) {
                this.sessionConfig = new Properties();
            }
            this.sessionConfig.setProperty(key, value);
            return this;
        }

        public SftpConfig build() {
            if (host == null || host.isBlank()) {
                throw new IllegalArgumentException("Host is required");
            }
            if (username == null || username.isBlank()) {
                throw new IllegalArgumentException("Username is required");
            }
            return new SftpConfig(this);
        }
    }
}
