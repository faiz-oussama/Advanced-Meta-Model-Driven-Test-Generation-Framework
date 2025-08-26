package com.univade.TU.generator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseValidationRule {
    
    private String testName;
    private String testBody;
    private String attributeName;
    private String validationType;
    private String description;
    private Object constraintValue;
    private String expectedExceptionType;
    
    public String getTestNameCapitalized() {
        if (testName == null || testName.isEmpty()) {
            return "";
        }
        return Character.toUpperCase(testName.charAt(0)) + testName.substring(1);
    }
    
    public String getAttributeNameCapitalized() {
        if (attributeName == null || attributeName.isEmpty()) {
            return "";
        }
        return Character.toUpperCase(attributeName.charAt(0)) + attributeName.substring(1);
    }
    
    public boolean isNullableConstraint() {
        return "nullable".equals(validationType);
    }
    
    public boolean isUniqueConstraint() {
        return "unique".equals(validationType);
    }
    
    public boolean isMaxLengthConstraint() {
        return "maxLength".equals(validationType);
    }
    
    public boolean isMinLengthConstraint() {
        return "minLength".equals(validationType);
    }
    
    public boolean isMinValueConstraint() {
        return "minValue".equals(validationType);
    }
    
    public boolean isMaxValueConstraint() {
        return "maxValue".equals(validationType);
    }
    
    public boolean isPrimaryKeyConstraint() {
        return "primaryKey".equals(validationType);
    }

    public String getSetterName() {
        if (attributeName == null || attributeName.isEmpty()) {
            return "";
        }
        return "set" + Character.toUpperCase(attributeName.charAt(0)) + attributeName.substring(1);
    }

    public String getGetterName() {
        if (attributeName == null || attributeName.isEmpty()) {
            return "";
        }
        return "get" + Character.toUpperCase(attributeName.charAt(0)) + attributeName.substring(1);
    }
}
