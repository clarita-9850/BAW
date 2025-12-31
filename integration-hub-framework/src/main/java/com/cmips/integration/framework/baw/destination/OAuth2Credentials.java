package com.cmips.integration.framework.baw.destination;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OAuth2 credentials for HTTP API authentication.
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2Credentials {

    private String clientId;
    private String clientSecret;
    private String tokenUrl;
    private String scope;

    /**
     * Grant type (default: client_credentials).
     */
    @Builder.Default
    private String grantType = "client_credentials";

    /**
     * For password grant type.
     */
    private String username;
    private String password;
}
