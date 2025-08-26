package com.univade.TU.generator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.ArrayList;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttributeMetaModel {
    
    private String name;
    private String type;
    private boolean nullable;
    private boolean unique;
    private Integer maxLength;
    private Integer minLength;
    private String validationPattern;
    private Object defaultValue;
    private String columnName;
    private boolean primaryKey;
    private boolean generatedValue;
    private String generationType;
    private Integer minValue;
    private Integer maxValue;
    private boolean email;
    private boolean notBlank;
    private boolean lob;
    private boolean enumType;
    private String enumClassName;
    @Builder.Default
    private List<String> enumValues = new ArrayList<>();

    public String getNameCapitalized() {
        if (name == null || name.isEmpty()) {
            return "";
        }
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    public String getGetterName() {
        return "get" + getNameCapitalized();
    }

    public String getSetterName() {
        return "set" + getNameCapitalized();
    }

    public String getJavaType() {
        if (type == null) {
            return "Object";
        }

        if (isEnum() && enumClassName != null) {
            return enumClassName;
        }

        switch (type.toLowerCase()) {
            case "string":
                return "String";
            case "integer":
            case "int":
                return "Integer";
            case "long":
                return "Long";
            case "double":
                return "Double";
            case "float":
                return "Float";
            case "boolean":
                return "Boolean";
            case "date":
                return "java.time.LocalDate";
            case "datetime":
            case "timestamp":
                return "java.time.LocalDateTime";
            case "bigdecimal":
                return "java.math.BigDecimal";
            default:
                return type;
        }
    }

    public boolean isNumericType() {
        String javaType = getJavaType();
        return javaType.equals("Integer") || javaType.equals("Long") || 
               javaType.equals("Double") || javaType.equals("Float") ||
               javaType.equals("java.math.BigDecimal");
    }

    public boolean isStringType() {
        return "String".equals(getJavaType());
    }

    public boolean isDateType() {
        String javaType = getJavaType();
        return javaType.contains("LocalDate") || javaType.contains("LocalDateTime");
    }

    public boolean isEnumType() {
        return isEnum();
    }

    public boolean isEnum() {
        return enumType;
    }

    public boolean isEmail() {
        return email;
    }

    public String getEnumType() {
        return enumClassName;
    }

    public List<String> getEnumValues() {
        return enumValues != null ? enumValues : new ArrayList<>();
    }
}
