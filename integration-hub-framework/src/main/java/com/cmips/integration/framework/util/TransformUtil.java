package com.cmips.integration.framework.util;

import com.cmips.integration.framework.exception.TransformationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for data transformation operations.
 *
 * <p>This class provides static utility methods for converting data between
 * different formats including JSON, XML, and XSLT transformations.
 *
 * <p>Example usage:
 * <pre>
 * // Convert to JSON
 * String json = TransformUtil.toJson(payment);
 *
 * // Convert from JSON
 * Payment payment = TransformUtil.fromJson(jsonString, Payment.class);
 *
 * // Convert to XML
 * String xml = TransformUtil.toXml(payment);
 *
 * // Apply XSLT transformation
 * String transformed = TransformUtil.transformXml(xml, Paths.get("transform.xslt"));
 *
 * // Map fields between objects
 * PaymentDTO dto = TransformUtil.mapFields(payment, PaymentDTO.class);
 * </pre>
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public final class TransformUtil {

    private static final ObjectMapper jsonMapper;
    private static final XmlMapper xmlMapper;

    static {
        jsonMapper = new ObjectMapper();
        jsonMapper.registerModule(new JavaTimeModule());
        jsonMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        jsonMapper.enable(SerializationFeature.INDENT_OUTPUT);

        xmlMapper = new XmlMapper();
        xmlMapper.registerModule(new JavaTimeModule());
        xmlMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    private TransformUtil() {
        // Utility class - prevent instantiation
    }

    /**
     * Transforms XML using an XSLT stylesheet.
     *
     * @param xml the source XML
     * @param xsltFile the path to the XSLT file
     * @return the transformed XML
     * @throws TransformationException if transformation fails
     */
    public static String transformXml(String xml, Path xsltFile) throws TransformationException {
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            StreamSource xsltSource = new StreamSource(xsltFile.toFile());
            Transformer transformer = factory.newTransformer(xsltSource);

            StreamSource xmlSource = new StreamSource(new StringReader(xml));
            StringWriter writer = new StringWriter();
            transformer.transform(xmlSource, new StreamResult(writer));

            return writer.toString();
        } catch (Exception e) {
            throw new TransformationException("XSLT transformation failed", e);
        }
    }

    /**
     * Converts an object to XML.
     *
     * @param object the object to convert
     * @return the XML string
     * @throws TransformationException if conversion fails
     */
    public static String toXml(Object object) throws TransformationException {
        try {
            return xmlMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new TransformationException("Failed to convert to XML", e);
        }
    }

    /**
     * Converts XML to an object.
     *
     * @param <T> the target type
     * @param xml the XML string
     * @param type the target class
     * @return the deserialized object
     * @throws TransformationException if conversion fails
     */
    public static <T> T fromXml(String xml, Class<T> type) throws TransformationException {
        try {
            return xmlMapper.readValue(xml, type);
        } catch (JsonProcessingException e) {
            throw new TransformationException("Failed to parse XML", e);
        }
    }

    /**
     * Converts an object to JSON.
     *
     * @param object the object to convert
     * @return the JSON string
     * @throws TransformationException if conversion fails
     */
    public static String toJson(Object object) throws TransformationException {
        try {
            return jsonMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new TransformationException("Failed to convert to JSON", e);
        }
    }

    /**
     * Converts an object to JSON without indentation.
     *
     * @param object the object to convert
     * @return the compact JSON string
     * @throws TransformationException if conversion fails
     */
    public static String toJsonCompact(Object object) throws TransformationException {
        try {
            return jsonMapper.writer().withoutFeatures(SerializationFeature.INDENT_OUTPUT)
                    .writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new TransformationException("Failed to convert to JSON", e);
        }
    }

    /**
     * Converts JSON to an object.
     *
     * @param <T> the target type
     * @param json the JSON string
     * @param type the target class
     * @return the deserialized object
     * @throws TransformationException if conversion fails
     */
    public static <T> T fromJson(String json, Class<T> type) throws TransformationException {
        try {
            return jsonMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new TransformationException("Failed to parse JSON", e);
        }
    }

    /**
     * Applies a template file to an object.
     *
     * <p>The template uses ${fieldName} placeholders that are replaced
     * with field values from the object.
     *
     * @param object the source object
     * @param templateFile the template file path
     * @return the rendered template
     * @throws TransformationException if rendering fails
     */
    public static String applyTemplate(Object object, Path templateFile) throws TransformationException {
        try {
            String template = Files.readString(templateFile);
            return applyTemplateString(object, template);
        } catch (Exception e) {
            throw new TransformationException("Failed to apply template", e);
        }
    }

    /**
     * Applies a template string to an object.
     *
     * @param object the source object
     * @param template the template string
     * @return the rendered template
     * @throws TransformationException if rendering fails
     */
    public static String applyTemplateString(Object object, String template)
            throws TransformationException {
        try {
            String result = template;
            Map<String, Object> values = objectToMap(object);

            for (Map.Entry<String, Object> entry : values.entrySet()) {
                String placeholder = "${" + entry.getKey() + "}";
                String value = entry.getValue() != null ? entry.getValue().toString() : "";
                result = result.replace(placeholder, value);
            }

            return result;
        } catch (Exception e) {
            throw new TransformationException("Failed to apply template", e);
        }
    }

    /**
     * Converts an object to fixed-width text format.
     *
     * @param object the source object
     * @param fieldWidths map of field names to widths
     * @return the fixed-width string
     * @throws TransformationException if conversion fails
     */
    public static String toFixedWidth(Object object, Map<String, Integer> fieldWidths)
            throws TransformationException {
        try {
            StringBuilder sb = new StringBuilder();
            Map<String, Object> values = objectToMap(object);

            for (Map.Entry<String, Integer> entry : fieldWidths.entrySet()) {
                String fieldName = entry.getKey();
                int width = entry.getValue();
                Object value = values.get(fieldName);
                String strValue = value != null ? value.toString() : "";

                if (strValue.length() > width) {
                    strValue = strValue.substring(0, width);
                } else {
                    strValue = String.format("%-" + width + "s", strValue);
                }

                sb.append(strValue);
            }

            return sb.toString();
        } catch (Exception e) {
            throw new TransformationException("Failed to convert to fixed-width", e);
        }
    }

    /**
     * Maps fields from a source object to a target class.
     *
     * <p>Fields with matching names and compatible types are copied.
     *
     * @param <T> the target type
     * @param source the source object
     * @param targetClass the target class
     * @return a new instance of the target class with mapped fields
     * @throws TransformationException if mapping fails
     */
    public static <T> T mapFields(Object source, Class<T> targetClass)
            throws TransformationException {
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            Map<String, Object> sourceValues = objectToMap(source);

            for (Field targetField : targetClass.getDeclaredFields()) {
                targetField.setAccessible(true);
                String fieldName = targetField.getName();

                if (sourceValues.containsKey(fieldName)) {
                    Object value = sourceValues.get(fieldName);
                    if (value != null && targetField.getType().isAssignableFrom(value.getClass())) {
                        targetField.set(target, value);
                    }
                }
            }

            return target;
        } catch (Exception e) {
            throw new TransformationException("Failed to map fields", e);
        }
    }

    /**
     * Converts an object's fields to a map.
     *
     * @param object the source object
     * @return a map of field names to values
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> objectToMap(Object object) {
        if (object == null) {
            return new HashMap<>();
        }
        try {
            return jsonMapper.convertValue(object, Map.class);
        } catch (Exception e) {
            // Fallback to reflection
            Map<String, Object> map = new HashMap<>();
            for (Field field : object.getClass().getDeclaredFields()) {
                try {
                    field.setAccessible(true);
                    map.put(field.getName(), field.get(object));
                } catch (IllegalAccessException ignored) {
                }
            }
            return map;
        }
    }

    /**
     * Converts a map to an object.
     *
     * @param <T> the target type
     * @param map the source map
     * @param targetClass the target class
     * @return the converted object
     * @throws TransformationException if conversion fails
     */
    public static <T> T mapToObject(Map<String, Object> map, Class<T> targetClass)
            throws TransformationException {
        try {
            return jsonMapper.convertValue(map, targetClass);
        } catch (Exception e) {
            throw new TransformationException("Failed to convert map to object", e);
        }
    }
}
