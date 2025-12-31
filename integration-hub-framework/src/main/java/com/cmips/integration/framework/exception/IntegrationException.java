package com.cmips.integration.framework.exception;

/**
 * Base exception for all integration framework exceptions.
 *
 * <p>This is the root exception class for the integration framework.
 * All custom framework exceptions extend this class, enabling catch-all
 * handling of framework-specific errors.
 *
 * <p>Example usage:
 * <pre>
 * try {
 *     integrationEngine.executeFlow("paymentFlow");
 * } catch (IntegrationException e) {
 *     logger.error("Integration flow failed: {}", e.getMessage(), e);
 *     // Handle any framework exception
 * }
 * </pre>
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public class IntegrationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * The component that threw this exception.
     */
    private final String componentName;

    /**
     * The flow that was being executed when the exception occurred.
     */
    private final String flowName;

    /**
     * Creates a new IntegrationException with the specified message.
     *
     * @param message the detail message
     */
    public IntegrationException(String message) {
        super(message);
        this.componentName = null;
        this.flowName = null;
    }

    /**
     * Creates a new IntegrationException with the specified message and cause.
     *
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public IntegrationException(String message, Throwable cause) {
        super(message, cause);
        this.componentName = null;
        this.flowName = null;
    }

    /**
     * Creates a new IntegrationException with component context.
     *
     * @param message the detail message
     * @param componentName the name of the component that threw the exception
     */
    public IntegrationException(String message, String componentName) {
        super(message);
        this.componentName = componentName;
        this.flowName = null;
    }

    /**
     * Creates a new IntegrationException with full context.
     *
     * @param message the detail message
     * @param cause the cause of this exception
     * @param componentName the name of the component that threw the exception
     * @param flowName the name of the flow being executed
     */
    public IntegrationException(String message, Throwable cause,
                                  String componentName, String flowName) {
        super(message, cause);
        this.componentName = componentName;
        this.flowName = flowName;
    }

    /**
     * Returns the name of the component that threw this exception.
     *
     * @return the component name, or {@code null} if not set
     */
    public String getComponentName() {
        return componentName;
    }

    /**
     * Returns the name of the flow that was being executed.
     *
     * @return the flow name, or {@code null} if not set
     */
    public String getFlowName() {
        return flowName;
    }

    /**
     * Returns a detailed message including context information.
     *
     * @return the detailed message
     */
    public String getDetailedMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append(getMessage());
        if (componentName != null) {
            sb.append(" [Component: ").append(componentName).append("]");
        }
        if (flowName != null) {
            sb.append(" [Flow: ").append(flowName).append("]");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + getDetailedMessage();
    }
}
