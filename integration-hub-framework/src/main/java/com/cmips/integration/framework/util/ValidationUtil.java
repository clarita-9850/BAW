package com.cmips.integration.framework.util;

import com.cmips.integration.framework.model.ValidationResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Utility class for data validation operations.
 *
 * <p>This class provides static utility methods for validating data against
 * various rules including JSON schema, XML schema, and field requirements.
 *
 * <p>Example usage:
 * <pre>
 * // Validate JSON against schema
 * ValidationResult result = ValidationUtil.validateJson(jsonString, Paths.get("schema.json"));
 *
 * // Validate XML against XSD
 * ValidationResult result = ValidationUtil.validateXml(xmlString, Paths.get("schema.xsd"));
 *
 * // Validate object
 * ValidationResult result = ValidationUtil.validate(payment);
 *
 * // Check required fields
 * ValidationResult result = ValidationUtil.checkRequiredFields(payment, "id", "amount", "date");
 * </pre>
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public final class ValidationUtil {

    private static final Logger log = LoggerFactory.getLogger(ValidationUtil.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private ValidationUtil() {
        // Utility class - prevent instantiation
    }

    /**
     * Validates JSON against a JSON schema file.
     *
     * <p>Note: This is a basic implementation that checks for required fields
     * and types. For full JSON Schema validation, consider using a dedicated
     * library like everit-org/json-schema or networknt/json-schema-validator.
     *
     * @param json the JSON string to validate
     * @param schemaPath the path to the JSON schema file
     * @return the validation result
     */
    public static ValidationResult validateJson(String json, Path schemaPath) {
        try {
            JsonNode jsonNode = objectMapper.readTree(json);
            JsonNode schemaNode = objectMapper.readTree(schemaPath.toFile());

            List<String> errors = new ArrayList<>();
            validateJsonNode(jsonNode, schemaNode, "", errors);

            return errors.isEmpty()
                    ? ValidationResult.valid()
                    : ValidationResult.invalid(errors);

        } catch (Exception e) {
            log.error("JSON validation failed", e);
            return ValidationResult.invalid("JSON validation error: " + e.getMessage());
        }
    }

    private static void validateJsonNode(JsonNode data, JsonNode schema, String path,
                                          List<String> errors) {
        // Check type
        if (schema.has("type")) {
            String expectedType = schema.get("type").asText();
            if (!matchesType(data, expectedType)) {
                errors.add("Invalid type at " + path + ": expected " + expectedType);
            }
        }

        // Check required fields
        if (schema.has("required") && data.isObject()) {
            for (JsonNode required : schema.get("required")) {
                String fieldName = required.asText();
                if (!data.has(fieldName) || data.get(fieldName).isNull()) {
                    errors.add("Missing required field: " + path + "." + fieldName);
                }
            }
        }

        // Check properties
        if (schema.has("properties") && data.isObject()) {
            JsonNode properties = schema.get("properties");
            Iterator<Map.Entry<String, JsonNode>> fields = properties.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String fieldName = entry.getKey();
                if (data.has(fieldName)) {
                    String fieldPath = path.isEmpty() ? fieldName : path + "." + fieldName;
                    validateJsonNode(data.get(fieldName), entry.getValue(), fieldPath, errors);
                }
            }
        }
    }

    private static boolean matchesType(JsonNode node, String expectedType) {
        return switch (expectedType) {
            case "string" -> node.isTextual();
            case "number" -> node.isNumber();
            case "integer" -> node.isInt() || node.isLong();
            case "boolean" -> node.isBoolean();
            case "array" -> node.isArray();
            case "object" -> node.isObject();
            case "null" -> node.isNull();
            default -> true;
        };
    }

    /**
     * Validates XML against an XSD schema file.
     *
     * @param xml the XML string to validate
     * @param xsdPath the path to the XSD schema file
     * @return the validation result
     */
    public static ValidationResult validateXml(String xml, Path xsdPath) {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(xsdPath.toFile());
            Validator validator = schema.newValidator();

            validator.validate(new StreamSource(new StringReader(xml)));
            return ValidationResult.valid();

        } catch (Exception e) {
            log.debug("XML validation failed: {}", e.getMessage());
            return ValidationResult.invalid("XML validation error: " + e.getMessage());
        }
    }

    /**
     * Validates XML against an XSD schema string.
     *
     * @param xml the XML string to validate
     * @param xsdContent the XSD schema content
     * @return the validation result
     */
    public static ValidationResult validateXmlWithSchema(String xml, String xsdContent) {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new StreamSource(new StringReader(xsdContent)));
            Validator validator = schema.newValidator();

            validator.validate(new StreamSource(new StringReader(xml)));
            return ValidationResult.valid();

        } catch (Exception e) {
            return ValidationResult.invalid("XML validation error: " + e.getMessage());
        }
    }

    /**
     * Validates an object using reflection to check for null fields.
     *
     * @param object the object to validate
     * @return the validation result
     */
    public static ValidationResult validate(Object object) {
        if (object == null) {
            return ValidationResult.invalid("Object cannot be null");
        }

        List<String> errors = new ArrayList<>();
        validateObject(object, errors, "");
        return errors.isEmpty()
                ? ValidationResult.valid()
                : ValidationResult.invalid(errors);
    }

    private static void validateObject(Object object, List<String> errors, String prefix) {
        for (Field field : object.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(object);
                String fieldPath = prefix.isEmpty() ? field.getName() : prefix + "." + field.getName();

                // Check for null on non-primitive fields
                if (value == null && !field.getType().isPrimitive()) {
                    // Only flag as error if field has @NotNull or similar annotation
                    // For now, we just log
                    log.trace("Null value for field: {}", fieldPath);
                }

                // Validate nested objects
                if (value != null && !isSimpleType(field.getType())) {
                    validateObject(value, errors, fieldPath);
                }
            } catch (IllegalAccessException e) {
                log.trace("Cannot access field: {}", field.getName());
            }
        }
    }

    private static boolean isSimpleType(Class<?> type) {
        return type.isPrimitive() ||
                type == String.class ||
                Number.class.isAssignableFrom(type) ||
                type == Boolean.class ||
                type.isEnum() ||
                java.util.Date.class.isAssignableFrom(type) ||
                java.time.temporal.Temporal.class.isAssignableFrom(type);
    }

    /**
     * Checks that required fields are not null or empty.
     *
     * @param object the object to check
     * @param fieldNames the names of required fields
     * @return the validation result
     */
    public static ValidationResult checkRequiredFields(Object object, String... fieldNames) {
        if (object == null) {
            return ValidationResult.invalid("Object cannot be null");
        }

        List<String> errors = new ArrayList<>();
        Set<String> requiredFields = Set.of(fieldNames);

        for (String fieldName : requiredFields) {
            try {
                Object value = getFieldValue(object, fieldName);
                if (value == null) {
                    errors.add("Required field '" + fieldName + "' is null");
                } else if (value instanceof String && ((String) value).isBlank()) {
                    errors.add("Required field '" + fieldName + "' is empty");
                }
            } catch (NoSuchFieldException e) {
                errors.add("Field '" + fieldName + "' does not exist");
            } catch (Exception e) {
                errors.add("Cannot check field '" + fieldName + "': " + e.getMessage());
            }
        }

        return errors.isEmpty()
                ? ValidationResult.valid()
                : ValidationResult.invalid(errors);
    }

    private static Object getFieldValue(Object object, String fieldName) throws Exception {
        Class<?> clazz = object.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(object);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }

    /**
     * Validates a string against a regex pattern.
     *
     * @param value the string to validate
     * @param pattern the regex pattern
     * @param fieldName the field name for error messages
     * @return the validation result
     */
    public static ValidationResult validatePattern(String value, String pattern, String fieldName) {
        if (value == null) {
            return ValidationResult.invalid(fieldName + " cannot be null");
        }
        if (Pattern.matches(pattern, value)) {
            return ValidationResult.valid();
        }
        return ValidationResult.invalid(fieldName + " does not match required pattern");
    }

    /**
     * Validates that a number is within a range.
     *
     * @param value the number to validate
     * @param min the minimum value (inclusive)
     * @param max the maximum value (inclusive)
     * @param fieldName the field name for error messages
     * @return the validation result
     */
    public static ValidationResult validateRange(Number value, Number min, Number max,
                                                   String fieldName) {
        if (value == null) {
            return ValidationResult.invalid(fieldName + " cannot be null");
        }
        double v = value.doubleValue();
        if (v < min.doubleValue()) {
            return ValidationResult.invalid(fieldName + " must be at least " + min);
        }
        if (v > max.doubleValue()) {
            return ValidationResult.invalid(fieldName + " must be at most " + max);
        }
        return ValidationResult.valid();
    }

    /**
     * Validates that a string is not empty.
     *
     * @param value the string to validate
     * @param fieldName the field name for error messages
     * @return the validation result
     */
    public static ValidationResult validateNotEmpty(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            return ValidationResult.invalid(fieldName + " cannot be empty");
        }
        return ValidationResult.valid();
    }

    /**
     * Validates that a collection is not empty.
     *
     * @param collection the collection to validate
     * @param fieldName the field name for error messages
     * @return the validation result
     */
    public static ValidationResult validateNotEmpty(java.util.Collection<?> collection,
                                                      String fieldName) {
        if (collection == null || collection.isEmpty()) {
            return ValidationResult.invalid(fieldName + " cannot be empty");
        }
        return ValidationResult.valid();
    }

    /**
     * Combines multiple validation results.
     *
     * @param results the results to combine
     * @return the combined result
     */
    public static ValidationResult combine(ValidationResult... results) {
        List<String> allErrors = new ArrayList<>();
        for (ValidationResult result : results) {
            if (!result.isValid()) {
                allErrors.addAll(result.getErrors());
            }
        }
        return allErrors.isEmpty()
                ? ValidationResult.valid()
                : ValidationResult.invalid(allErrors);
    }
}
