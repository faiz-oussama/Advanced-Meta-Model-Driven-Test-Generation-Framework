package com.univade.TU.generator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DtoValidationRule {
    
    private String attributeName;
    private String validationType;
    private String message;
    private Object constraintValue;
    private Object minValue;
    private Object maxValue;
    private String pattern;
    private boolean required;
    
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
    
    public String getAnnotation() {
        StringBuilder annotation = new StringBuilder("@");
        annotation.append(validationType);
        
        if (isSize() && (minValue != null || maxValue != null)) {
            annotation.append("(");
            if (minValue != null) {
                annotation.append("min = ").append(minValue);
                if (maxValue != null) {
                    annotation.append(", ");
                }
            }
            if (maxValue != null) {
                annotation.append("max = ").append(maxValue);
            }
            if (message != null && !message.isEmpty()) {
                annotation.append(", message = \"").append(message).append("\"");
            }
            annotation.append(")");
        } else if (isMin() || isMax()) {
            annotation.append("(value = ").append(constraintValue);
            if (message != null && !message.isEmpty()) {
                annotation.append(", message = \"").append(message).append("\"");
            }
            annotation.append(")");
        } else if (isPattern()) {
            annotation.append("(regexp = \"").append(pattern).append("\"");
            if (message != null && !message.isEmpty()) {
                annotation.append(", message = \"").append(message).append("\"");
            }
            annotation.append(")");
        } else if (message != null && !message.isEmpty()) {
            annotation.append("(message = \"").append(message).append("\")");
        }
        
        return annotation.toString();
    }
}
