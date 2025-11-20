package com.cmips.aspect;

import com.cmips.annotation.RequirePermission;
import com.cmips.service.KeycloakPolicyEvaluationService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Authorization Aspect for method-level security using Keycloak policies
 * 
 * This aspect intercepts methods annotated with @RequirePermission and
 * evaluates authorization using Keycloak's policy engine before allowing
 * the method to execute.
 */
@Aspect
@Component
public class AuthorizationAspect {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationAspect.class);

    @Autowired
    private KeycloakPolicyEvaluationService keycloakPolicyEvaluationService;

    /**
     * Around advice for @RequirePermission annotation
     * Evaluates authorization using Keycloak before method execution
     */
    @Around("@annotation(requirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, RequirePermission requirePermission) throws Throwable {
        
        String resource = requirePermission.resource();
        String scope = requirePermission.scope();
        String methodName = joinPoint.getSignature().getName();
        
        logger.info("Checking authorization for method: {} - Resource: {}, Scope: {}", methodName, resource, scope);
        
        // Query Keycloak for authorization decision
        boolean hasPermission = keycloakPolicyEvaluationService.evaluatePermission(resource, scope);
        
        if (hasPermission) {
            logger.info("Authorization granted for method: {} - {}:{}", methodName, resource, scope);
            // Proceed with method execution
            return joinPoint.proceed();
        } else {
            logger.warn("Authorization denied for method: {} - {}:{}", methodName, resource, scope);
            // Return 403 Forbidden response
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("{\"error\":\"Access denied\",\"message\":\"" + requirePermission.message() + "\"}");
        }
    }
}
