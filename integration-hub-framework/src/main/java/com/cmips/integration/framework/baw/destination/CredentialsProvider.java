package com.cmips.integration.framework.baw.destination;

/**
 * Interface for providing credentials to destinations.
 *
 * <p>Implement this interface to provide secure credential management
 * for SFTP servers and HTTP APIs. Credentials are never stored in
 * annotations - they are retrieved at runtime through this interface.
 *
 * <p>Example usage:
 * <pre>
 * &#64;Component
 * public class VaultCredentialsProvider implements CredentialsProvider {
 *
 *     private final VaultClient vault;
 *
 *     &#64;Override
 *     public Credentials getCredentials(String name) {
 *         VaultSecret secret = vault.read("secret/cmips/" + name);
 *         return Credentials.builder()
 *             .username(secret.get("username"))
 *             .password(secret.get("password"))
 *             .build();
 *     }
 *
 *     &#64;Override
 *     public SshCredentials getSshCredentials(String name) {
 *         VaultSecret secret = vault.read("secret/cmips/ssh/" + name);
 *         return SshCredentials.builder()
 *             .username(secret.get("username"))
 *             .privateKey(secret.get("private_key"))
 *             .passphrase(secret.get("passphrase"))
 *             .build();
 *     }
 * }
 * </pre>
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public interface CredentialsProvider {

    /**
     * Gets credentials by reference name.
     *
     * @param name the credentials reference name (from @Sftp or @HttpApi annotation)
     * @return the credentials
     * @throws CredentialsNotFoundException if credentials not found
     */
    Credentials getCredentials(String name);

    /**
     * Gets SSH credentials for SFTP connections.
     *
     * @param name the credentials reference name
     * @return the SSH credentials
     * @throws CredentialsNotFoundException if credentials not found
     */
    default SshCredentials getSshCredentials(String name) {
        Credentials creds = getCredentials(name);
        return SshCredentials.builder()
                .username(creds.getUsername())
                .password(creds.getPassword())
                .build();
    }

    /**
     * Gets OAuth2 credentials for HTTP API authentication.
     *
     * @param name the authentication reference name
     * @return the OAuth2 credentials
     * @throws CredentialsNotFoundException if credentials not found
     */
    default OAuth2Credentials getOAuth2Credentials(String name) {
        throw new CredentialsNotFoundException("OAuth2 credentials not supported: " + name);
    }
}
