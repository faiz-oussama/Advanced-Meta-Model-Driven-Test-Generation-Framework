package com.univade.TU.generator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestDataVariantMetaModel {
    
    private String name;
    private String description;
    private String methodName;
    
    @Builder.Default
    private Map<String, Object> attributeValues = new HashMap<>();
    
    @Builder.Default
    private Map<String, String> relationshipValues = new HashMap<>();

    public String getMethodNameCapitalized() {
        if (methodName == null || methodName.isEmpty()) {
            return "";
        }
        return Character.toUpperCase(methodName.charAt(0)) + methodName.substring(1);
    }

    public boolean hasAttributeValue(String attributeName) {
        return attributeValues != null && attributeValues.containsKey(attributeName);
    }

    public Object getAttributeValue(String attributeName) {
        return attributeValues != null ? attributeValues.get(attributeName) : null;
    }

    public void setAttributeValue(String attributeName, Object value) {
        if (attributeValues == null) {
            attributeValues = new HashMap<>();
        }
        attributeValues.put(attributeName, value);
    }

    public boolean hasRelationshipValue(String relationshipName) {
        return relationshipValues != null && relationshipValues.containsKey(relationshipName);
    }

    public String getRelationshipValue(String relationshipName) {
        return relationshipValues != null ? relationshipValues.get(relationshipName) : null;
    }

    public void setRelationshipValue(String relationshipName, String value) {
        if (relationshipValues == null) {
            relationshipValues = new HashMap<>();
        }
        relationshipValues.put(relationshipName, value);
    }

    public boolean hasCustomValues() {
        return (attributeValues != null && !attributeValues.isEmpty()) ||
               (relationshipValues != null && !relationshipValues.isEmpty());
    }
}
