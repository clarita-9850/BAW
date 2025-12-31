package com.cmips.integration.framework.baw.exception;

/**
 * Exception thrown when contradictory split rules are combined.
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public class SplitRuleConflictException extends BawException {

    private final String rule1Description;
    private final String rule2Description;

    public SplitRuleConflictException(String message) {
        super(message);
        this.rule1Description = null;
        this.rule2Description = null;
    }

    public SplitRuleConflictException(String rule1Description, String rule2Description, String reason) {
        super("Conflicting split rules: [" + rule1Description + "] and [" +
              rule2Description + "] - " + reason);
        this.rule1Description = rule1Description;
        this.rule2Description = rule2Description;
    }

    public String getRule1Description() {
        return rule1Description;
    }

    public String getRule2Description() {
        return rule2Description;
    }
}
