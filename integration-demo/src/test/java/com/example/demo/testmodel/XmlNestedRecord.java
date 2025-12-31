package com.example.demo.testmodel;

import com.cmips.integration.framework.baw.annotation.FileColumn;
import com.cmips.integration.framework.baw.annotation.FileId;
import com.cmips.integration.framework.baw.annotation.FileType;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Record for testing XML-specific features including nested elements.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FileType(name = "xml-nested-record", description = "XML nested structure test record", version = "1.0")
@JacksonXmlRootElement(localName = "order")
public class XmlNestedRecord {

    @FileId
    @FileColumn(order = 1, name = "orderId", nullable = false)
    @JacksonXmlProperty(localName = "orderId", isAttribute = true)
    private Long orderId;

    @FileColumn(order = 2, name = "customerName")
    @JacksonXmlProperty(localName = "customerName")
    private String customerName;

    @FileColumn(order = 3, name = "totalAmount", format = "#,##0.00")
    @JacksonXmlProperty(localName = "totalAmount")
    private BigDecimal totalAmount;

    @JacksonXmlElementWrapper(localName = "items")
    @JacksonXmlProperty(localName = "item")
    private List<OrderItem> items;

    @JacksonXmlProperty(localName = "shippingAddress")
    private Address shippingAddress;

    /**
     * Nested order item.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {
        @JacksonXmlProperty(localName = "sku", isAttribute = true)
        private String sku;

        @JacksonXmlProperty(localName = "name")
        private String name;

        @JacksonXmlProperty(localName = "quantity")
        private Integer quantity;

        @JacksonXmlProperty(localName = "price")
        private BigDecimal price;
    }

    /**
     * Nested address structure.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Address {
        @JacksonXmlProperty(localName = "street")
        private String street;

        @JacksonXmlProperty(localName = "city")
        private String city;

        @JacksonXmlProperty(localName = "state")
        private String state;

        @JacksonXmlProperty(localName = "zipCode")
        private String zipCode;
    }
}
