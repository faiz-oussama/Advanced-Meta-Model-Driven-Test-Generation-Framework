package com.univade.TU.generator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ControllerValidationRule {
    
    private String testName;
    private String attributeName;
    private String validationType;
    private String description;
    private Object constraintValue;
    private Object minValue;
    private Object maxValue;
    private String pattern;
    private String message;
    private String expectedHttpStatus;
    private String invalidValue;
    private String validValue;
    
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
    
    public boolean isNotNull() {
        return "NotNull".equals(validationType);
    }
    
    public boolean isNotBlank() {
        return "NotBlank".equals(validationType);
    }
    
    public boolean isNotEmpty() {
        return "NotEmpty".equals(validationType);
    }
    
    public boolean isSize() {
        return "Size".equals(validationType);
    }
    
    public boolean isMin() {
        return "Min".equals(validationType);
    }
    
    public boolean isMax() {
        return "Max".equals(validationType);
    }
    
    public boolean isEmail() {
        return "Email".equals(validationType);
    }
    
    public boolean isPattern() {
        return "Pattern".equals(validationType);
    }
    
    public boolean isUnique() {
        return "Unique".equals(validationType);
    }
    
    public boolean isMaxLength() {
        return "MaxLength".equals(validationType);
    }
    
    public boolean isMinLength() {
        return "MinLength".equals(validationType);
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
    
    public String getExpectedStatus() {
        return expectedHttpStatus != null ? expectedHttpStatus : "400";
    }
}
