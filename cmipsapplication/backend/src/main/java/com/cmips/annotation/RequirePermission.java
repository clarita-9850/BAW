package com.cmips.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for method-level authorization using Keycloak policies
 * 
 * This annotation allows you to specify resource and scope requirements
 * for controller methods. The authorization will be evaluated by Keycloak's
 * policy engine, not by hardcoded logic in the backend.
 * 
 * Example usage:
 * @RequirePermission(resource = "Timesheet Resource", scope = "create")
 * public ResponseEntity<?> createTimesheet(...)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {
    
    /**
     * The resource name as defined in Keycloak
     * @return resource name
     */
    String resource();
    
    /**
     * The scope/action as defined in Keycloak
     * @return scope name
     */
    String scope();
    
    /**
     * Optional: Custom error message when access is denied
     * @return error message
     */
    String message() default "Access denied";
}
