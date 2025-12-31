package com.cmips.integration.framework.baw.destination;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SSH credentials for SFTP connections.
 * Supports both password and private key authentication.
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SshCredentials {

    private String username;

    /**
     * Password for password-based authentication.
     */
    private String password;

    /**
     * Private key content for key-based authentication.
     */
    private String privateKey;

    /**
     * Private key file path (alternative to privateKey content).
     */
    private String privateKeyPath;

    /**
     * Passphrase for encrypted private keys.
     */
    private String passphrase;

    /**
     * Returns true if this uses key-based authentication.
     */
    public boolean isKeyBased() {
        return privateKey != null || privateKeyPath != null;
    }
}
