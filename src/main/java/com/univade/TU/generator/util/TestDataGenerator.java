package com.univade.TU.generator.util;

import com.univade.TU.generator.model.AttributeMetaModel;

import java.util.*;

public class TestDataGenerator {

    private final Random random = new Random();

    public String generateValidValue(AttributeMetaModel attribute) {
        String javaType = attribute.getJavaType();

        switch (javaType) {
            case "String":
                return generateStringValue(attribute);
            case "Integer":
                return String.valueOf(generateInteger(attribute));
            case "Long":
                return generateLongValue(attribute);
            case "Double":
                return String.valueOf(1.0 + random.nextDouble() * 100);
            case "Float":
                return String.valueOf(1.0f + random.nextFloat() * 100) + "f";
            case "Boolean":
                return "true";
            case "BigDecimal":
                return "new BigDecimal(\"" + String.format("%.2f", random.nextDouble() * 100) + "\")";
            case "LocalDate":
                return "LocalDate.now()";
            case "LocalDateTime":
                return "LocalDateTime.now()";
            case "UUID":
                return "UUID.randomUUID()";
            default:
                if (attribute.isEnum()) {
                    return attribute.getEnumValues().isEmpty()
                        ? "null"
                        : attribute.getEnumType() + "." + attribute.getEnumValues().get(0);
                }
                return "null";
        }
    }

    public String generateInvalidValue(AttributeMetaModel attribute) {
        String javaType = attribute.getJavaType();

        switch (javaType) {
            case "String":
                return generateInvalidStringValue(attribute);
            case "Integer":
                return String.valueOf(generateInvalidInteger(attribute));
            case "Long":
                return String.valueOf(generateInvalidLong(attribute)) + "L";
            case "Boolean":
                return "\"notBoolean\"";
            case "Double":
            case "Float":
            case "BigDecimal":
                return "\"NaN\"";
            default:
                return "null";
        }
    }

    private String generateStringValue(AttributeMetaModel attribute) {
        if (attribute.isEmail()) {
            return "generateUniqueEmail()";
        }

        int length = 10;
        int max = attribute.getMaxLength() != null ? attribute.getMaxLength() : 20;
        int min = attribute.getMinLength() != null ? attribute.getMinLength() : 1;

        length = Math.min(Math.max(length, min + 1), max - 1);
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            sb.append((char) ('a' + random.nextInt(26)));
        }

        return "\"" + sb.toString() + "\"";
    }

    private String generateInvalidStringValue(AttributeMetaModel attribute) {
        if (attribute.isNotBlank()) return "\"\"";

        if (attribute.isEmail()) return "\"invalid-email\"";

        if (attribute.getMaxLength() != null) {
            int tooLong = attribute.getMaxLength() + 1;
            return "\"" + "a".repeat(tooLong) + "\"";
        }

        return "\"invalid\"";
    }

    private int generateInteger(AttributeMetaModel attribute) {
        int min = attribute.getMinValue() != null ? attribute.getMinValue() : 1;
        int max = attribute.getMaxValue() != null ? attribute.getMaxValue() : 100;
        return min + random.nextInt(max - min + 1);
    }

    private int generateInvalidInteger(AttributeMetaModel attribute) {
        if (attribute.getMinValue() != null) return attribute.getMinValue() - 1;
        if (attribute.getMaxValue() != null) return attribute.getMaxValue() + 1;
        return -1;
    }

    private long generateInvalidLong(AttributeMetaModel attribute) {
        if (attribute.getMinValue() != null) return (long) attribute.getMinValue() - 1;
        return -1L;
    }

    private String generateLongValue(AttributeMetaModel attribute) {
        long min = attribute.getMinValue() != null ? attribute.getMinValue() : 1L;
        long max = attribute.getMaxValue() != null ? attribute.getMaxValue() : 1000L;
        return String.valueOf(min + (long) (random.nextDouble() * (max - min + 1))) + "L";
    }

    public String generateUniqueValue(AttributeMetaModel attribute) {
        String base = generateValidValue(attribute);
        if (isStringType(attribute)) {
            if (attribute.isEmail()) {
                return "generateUniqueEmail()";
            }
            int maxLength = attribute.getMaxLength() != null ? attribute.getMaxLength() : 255;
            int minLength = attribute.getMinLength() != null ? attribute.getMinLength() : 1;

            String baseValue = base.substring(1, base.length() - 1); 

            if (maxLength <= 20) {
                int uuidLength = Math.min(4, maxLength - minLength - 1); 
                if (uuidLength < 1) uuidLength = 1;

                int baseLength = Math.min(baseValue.length(), maxLength - uuidLength - 1); 
                if (baseLength < minLength - uuidLength - 1) {
                    baseLength = Math.max(1, minLength - uuidLength - 1);
                }

                baseValue = baseValue.substring(0, Math.max(1, baseLength));
                return "\"" + baseValue + "_\" + UUID.randomUUID().toString().substring(0, " + uuidLength + ") + \"\"";
            } else {
                if (baseValue.length() > maxLength - 9) {
                    baseValue = baseValue.substring(0, maxLength - 9);
                }
                return "\"" + baseValue + "_\" + UUID.randomUUID().toString().substring(0, 8) + \"\"";
            }
        }
        return base;
    }

    public String generateValidValueForParameter(String parameterType) {
        switch (parameterType) {
            case "String":
                return "\"test\"";
            case "Integer":
                return "1";
            case "Long":
                return "1L";
            case "Double":
                return "1.0";
            case "Float":
                return "1.0f";
            case "Boolean":
                return "true";
            case "BigDecimal":
                return "new BigDecimal(\"10.5\")";
            case "LocalDate":
                return "LocalDate.now()";
            case "LocalDateTime":
                return "LocalDateTime.now()";
            case "Pageable":
            case "org.springframework.data.domain.Pageable":
                return "PageRequest.of(0, 10)";
            default:
                return "null";
        }
    }

    public String generateBuilderMethodName(String attributeName) {
        return "with" + capitalize(attributeName);
    }

    public String generateTestVariantName(String baseName, String variant) {
        return "a" + capitalize(baseName) + capitalize(variant);
    }

    public List<String> generateEdgeCases(AttributeMetaModel attribute) {
        List<String> cases = new ArrayList<>();
        if (attribute.getMinValue() != null) {
            cases.add(String.valueOf(attribute.getMinValue()));
        }
        if (attribute.getMaxValue() != null) {
            cases.add(String.valueOf(attribute.getMaxValue()));
        }
        if (isStringType(attribute)) {
            if (attribute.getMinLength() != null) {
                cases.add("\"" + "a".repeat(attribute.getMinLength()) + "\"");
            }
            if (attribute.getMaxLength() != null) {
                cases.add("\"" + "a".repeat(attribute.getMaxLength()) + "\"");
            }
        }
        return cases;
    }

    private boolean isStringType(AttributeMetaModel attribute) {
        return "String".equals(attribute.getJavaType());
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}