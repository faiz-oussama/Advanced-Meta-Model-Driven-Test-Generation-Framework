package com.univade.TU.generator.service;

import com.univade.TU.generator.exception.TestGenerationException;
import com.univade.TU.generator.model.*;
import com.univade.TU.generator.parser.EntityMetaModelParser;
import com.univade.TU.generator.util.TemplateUtils;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DtoGenerator {

    private final Configuration freemarkerConfig;
    private final EntityMetaModelParser parser;
    private final TemplateUtils templateUtils;

    @Autowired
    public DtoGenerator(@Qualifier("freemarkerConfiguration") Configuration freemarkerConfig,
                        EntityMetaModelParser parser) {
        this.freemarkerConfig = freemarkerConfig;
        this.parser = parser;
        this.templateUtils = new TemplateUtils();
    }

    public GeneratedTestResult generateDto(EntityMetaModel entity) {
        try {
            GeneratedTestResult result = GeneratedTestResult.builder()
                    .entityName(entity.getName())
                    .build();

            Map<String, Object> dataModel = createDtoDataModel(entity, "Dto");
            String dtoContent = processTemplate("dto/dto.ftl", dataModel);
            result.setDtoContent(dtoContent);
            result.addGeneratedFile(entity.getName() + "Dto.java");

            return result;
        } catch (Exception e) {
            throw new TestGenerationException("Failed to generate DTOs for " + entity.getName(), e);
        }
    }

    public GeneratedTestResult generateFromJson(String jsonMetaModel) {
        EntityMetaModel entity = parser.parseFromJson(jsonMetaModel);
        return generateDto(entity);
    }

    public GeneratedTestResult generateFromAnnotations(Class<?> entityClass) {
        EntityMetaModel entity = parser.parseFromAnnotations(entityClass);
        return generateDto(entity);
    }

    public List<DtoValidationRule> getValidationRulesForDtoType(EntityMetaModel entity, String dtoType) {
        return buildDtoValidationRules(entity, dtoType);
    }

    private Map<String, Object> createDtoDataModel(EntityMetaModel entity, String dtoType) {
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("entity", entity);
        dataModel.put("templateUtils", templateUtils);
        dataModel.put("dtoType", dtoType);

        List<DtoValidationRule> validationRules = buildDtoValidationRules(entity, dtoType);
        dataModel.put("validationRules", validationRules);

        Set<String> imports = buildImports(entity, validationRules, dtoType);
        dataModel.put("imports", imports);

        return dataModel;
    }

    private List<DtoValidationRule> buildDtoValidationRules(EntityMetaModel entity, String dtoType) {
        if ("Response".equals(dtoType)) {
            return new ArrayList<>();
        }

        Map<String, List<DtoValidationRule>> rulesByAttribute = new HashMap<>();

        for (ValidationMetaModel validation : entity.getValidations()) {
            DtoValidationRule rule = buildValidationRule(validation);
            if (rule != null) {
                rulesByAttribute.computeIfAbsent(rule.getAttributeName(), k -> new ArrayList<>()).add(rule);
            }
        }

        for (AttributeMetaModel attribute : entity.getAttributes()) {
            if ("Create".equals(dtoType) && attribute.isPrimaryKey() && attribute.isGeneratedValue()) {
                continue;
            }

            List<DtoValidationRule> existingRules = rulesByAttribute.get(attribute.getName());
            Set<String> existingValidationTypes = existingRules != null ?
                existingRules.stream().map(DtoValidationRule::getValidationType).collect(Collectors.toSet()) :
                new HashSet<>();

            List<DtoValidationRule> attributeRules = buildAttributeDtoValidationRules(attribute, existingValidationTypes, dtoType);
            if (!attributeRules.isEmpty()) {
                rulesByAttribute.computeIfAbsent(attribute.getName(), k -> new ArrayList<>()).addAll(attributeRules);
            }
        }

        return rulesByAttribute.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private List<DtoValidationRule> buildAttributeDtoValidationRules(AttributeMetaModel attribute, Set<String> existingValidationTypes, String dtoType) {
        List<DtoValidationRule> rules = new ArrayList<>();

        if (!attribute.isNullable() && !attribute.isPrimaryKey()) {
            if (isStringType(attribute) && attribute.isNotBlank() && !existingValidationTypes.contains("NotBlank")) {
                rules.add(DtoValidationRule.builder()
                        .attributeName(attribute.getName())
                        .validationType("NotBlank")
                        .message(attribute.getName() + " cannot be blank")
                        .required(true)
                        .build());
            } else if (!existingValidationTypes.contains("NotNull") && !existingValidationTypes.contains("NotBlank")) {
                rules.add(DtoValidationRule.builder()
                        .attributeName(attribute.getName())
                        .validationType("NotNull")
                        .message(attribute.getName() + " cannot be null")
                        .required(true)
                        .build());
            }
        }

        if (attribute.getMaxLength() != null && isStringType(attribute) && !existingValidationTypes.contains("Size")) {
            Integer minLength = attribute.getMinLength();
            rules.add(DtoValidationRule.builder()
                    .attributeName(attribute.getName())
                    .validationType("Size")
                    .minValue(minLength != null ? minLength : 0)
                    .maxValue(attribute.getMaxLength())
                    .message(buildSizeMessage(attribute))
                    .build());
        }

        if (attribute.getMinValue() != null && isNumericType(attribute) && !existingValidationTypes.contains("Min")) {
            rules.add(DtoValidationRule.builder()
                    .attributeName(attribute.getName())
                    .validationType("Min")
                    .constraintValue(attribute.getMinValue())
                    .message(attribute.getName() + " must be at least " + attribute.getMinValue())
                    .build());
        }

        if (attribute.getMaxValue() != null && isNumericType(attribute) && !existingValidationTypes.contains("Max")) {
            rules.add(DtoValidationRule.builder()
                    .attributeName(attribute.getName())
                    .validationType("Max")
                    .constraintValue(attribute.getMaxValue())
                    .message(attribute.getName() + " must not exceed " + attribute.getMaxValue())
                    .build());
        }

        if (attribute.isEmail() && !existingValidationTypes.contains("Email")) {
            rules.add(DtoValidationRule.builder()
                    .attributeName(attribute.getName())
                    .validationType("Email")
                    .message(attribute.getName() + " must be a valid email")
                    .build());
        }

        return rules;
    }

    private DtoValidationRule buildValidationRule(ValidationMetaModel validation) {
        DtoValidationRule.DtoValidationRuleBuilder builder = DtoValidationRule.builder()
                .attributeName(validation.getAttributeName())
                .validationType(validation.getValidationType())
                .message(validation.getMessage());

        if (validation.isSize()) {
            builder.minValue(validation.getMin())
                   .maxValue(validation.getMax());
        } else if (validation.isMin() || validation.isMax()) {
            builder.constraintValue(validation.getValue());
        } else if (validation.isPattern()) {
            builder.pattern(validation.getPattern());
        }

        return builder.build();
    }

    private Set<String> buildImports(EntityMetaModel entityMetaModel, List<DtoValidationRule> validationRules, String dtoType) {
        Set<String> imports = new HashSet<>();
        
        imports.add("lombok.AllArgsConstructor");
        imports.add("lombok.Builder");
        imports.add("lombok.Data");
        imports.add("lombok.NoArgsConstructor");

        if (!"Response".equals(dtoType)) {
            Set<String> validationTypes = validationRules.stream()
                    .map(DtoValidationRule::getValidationType)
                    .collect(Collectors.toSet());

            for (String validationType : validationTypes) {
                imports.add("jakarta.validation.constraints." + validationType);
            }
        }

        if ("Response".equals(dtoType) && entityMetaModel.hasRelationships()) {
            boolean hasCollectionRelationships = entityMetaModel.getRelationships().stream()
                    .anyMatch(rel -> "OneToMany".equals(rel.getType()) || "ManyToMany".equals(rel.getType()));
            if (hasCollectionRelationships) {
                imports.add("java.util.List");
            }
        }
        
        for (AttributeMetaModel attribute : entityMetaModel.getAttributes()) {
            if ("LocalDate".equals(attribute.getType())) {
                imports.add("java.time.LocalDate");
            } else if ("LocalDateTime".equals(attribute.getType())) {
                imports.add("java.time.LocalDateTime");
            } else if ("BigDecimal".equals(attribute.getType())) {
                imports.add("java.math.BigDecimal");
            }
        }
        
        return imports.stream().sorted().collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private boolean isStringType(AttributeMetaModel attribute) {
        return "String".equals(attribute.getType()) || "java.lang.String".equals(attribute.getType());
    }

    private boolean isNumericType(AttributeMetaModel attribute) {
        String type = attribute.getType();
        return "Integer".equals(type) || "Long".equals(type) || "Double".equals(type) || 
               "Float".equals(type) || "BigDecimal".equals(type) || "int".equals(type) || 
               "long".equals(type) || "double".equals(type) || "float".equals(type);
    }

    private String buildSizeMessage(AttributeMetaModel attribute) {
        Integer minLength = attribute.getMinLength();
        Integer maxLength = attribute.getMaxLength();
        
        if (minLength != null && maxLength != null) {
            return attribute.getName() + " must be between " + minLength + " and " + maxLength + " characters";
        } else if (maxLength != null) {
            return attribute.getName() + " must not exceed " + maxLength + " characters";
        } else if (minLength != null) {
            return attribute.getName() + " must be at least " + minLength + " characters";
        }
        
        return attribute.getName() + " size validation";
    }

    private String processTemplate(String templateName, Map<String, Object> dataModel) {
        try {
            Template template = freemarkerConfig.getTemplate(templateName);
            StringWriter writer = new StringWriter();
            template.process(dataModel, writer);
            return writer.toString();
        } catch (Exception e) {
            throw TestGenerationException.templateProcessingError(templateName, e);
        }
    }
}
