package com.univade.TU.generator.service;

import com.univade.TU.generator.exception.TestGenerationException;
import com.univade.TU.generator.model.ControllerValidationRule;
import com.univade.TU.generator.model.DtoValidationRule;
import com.univade.TU.generator.model.EntityMetaModel;
import com.univade.TU.generator.model.GeneratedTestResult;
import com.univade.TU.generator.model.SecurityRuleMetaModel;
import com.univade.TU.generator.model.ValidationMetaModel;
import com.univade.TU.generator.parser.EntityMetaModelParser;
import com.univade.TU.generator.util.TemplateUtils;
import com.univade.TU.generator.util.TestDataGenerator;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.util.*;
import java.util.Map;

@Service
public class ControllerTestGenerator {

    private final Configuration freemarkerConfig;
    private final EntityMetaModelParser parser;
    private final TemplateUtils templateUtils;
    private final TestDataGenerator testDataGenerator;
    private final DtoGenerator dtoGenerator;

    @Autowired
    public ControllerTestGenerator(@Qualifier("freemarkerConfiguration") Configuration freemarkerConfig,
                                   EntityMetaModelParser parser,
                                   DtoGenerator dtoGenerator) {
        this.freemarkerConfig = freemarkerConfig;
        this.parser = parser;
        this.templateUtils = new TemplateUtils();
        this.testDataGenerator = new TestDataGenerator();
        this.dtoGenerator = dtoGenerator;
    }

    public GeneratedTestResult generateControllerTest(EntityMetaModel entity) {
        try {
            GeneratedTestResult result = GeneratedTestResult.builder()
                    .entityName(entity.getName())
                    .build();

            Map<String, Object> dataModel = createControllerDataModel(entity);

            String testContent = processTemplate("controller/controller-test.ftl", dataModel);
            result.setControllerTestContent(testContent);
            result.addGeneratedFile(entity.getName() + "ControllerTest.java");

            String builderContent = processTemplate("test-data-builder.ftl", dataModel);
            result.setBuilderContent(builderContent);
            result.addGeneratedFile(entity.getBuilderClassName() + ".java");

            return result;
        } catch (Exception e) {
            throw new TestGenerationException("Failed to generate controller tests for " + entity.getName(), e);
        }
    }

    public GeneratedTestResult generateFromJson(String jsonMetaModel) {
        EntityMetaModel entity = parser.parseFromJson(jsonMetaModel);
        return generateControllerTest(entity);
    }

    public GeneratedTestResult generateFromAnnotations(Class<?> entityClass) {
        EntityMetaModel entity = parser.parseFromAnnotations(entityClass);
        return generateControllerTest(entity);
    }

    private Map<String, Object> createControllerDataModel(EntityMetaModel entity) {
        Map<String, Object> dataModel = new HashMap<>();

        dataModel.put("entity", entity);
        dataModel.put("validationRules", buildControllerValidationRules(entity));
        dataModel.put("securityRules", entity.getSecurityRules());
        dataModel.put("hasSecurityRules", entity.hasSecurityRules());
        dataModel.put("allSecurityRoles", entity.getAllSecurityRoles());
        dataModel.put("dynamicSecurityRoles", extractAllRolesFromSecurityRules(entity));
        dataModel.put("utils", templateUtils);
        dataModel.put("generator", testDataGenerator);

        String basePackage = entity.getPackageName().replace(".entity", "");
        dataModel.put("packageName", entity.getPackageName());
        dataModel.put("basePackage", basePackage);
        dataModel.put("repositoryPackage", basePackage + ".repository");
        dataModel.put("servicePackage", basePackage + ".service");
        dataModel.put("controllerPackage", basePackage + ".controller");
        dataModel.put("testPackage", basePackage + ".controller");
        dataModel.put("builderPackage", basePackage + ".testdata");

        return dataModel;
    }

    private List<String> extractAllRolesFromSecurityRules(EntityMetaModel entity) {
        Set<String> allRoles = new HashSet<>();

        if (entity.hasSecurityRules()) {
            for (SecurityRuleMetaModel rule : entity.getSecurityRules()) {
                allRoles.addAll(rule.getAllRoles());
            }
        }

        if (allRoles.isEmpty()) {
            allRoles.addAll(Arrays.asList("USER", "ADMIN"));
        }

        return new ArrayList<>(allRoles);
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

    public GeneratedTestResult generateAllTests(EntityMetaModel entity) {
        return generateControllerTest(entity);
    }

    private List<ControllerValidationRule> buildControllerValidationRules(EntityMetaModel entity) {
        List<DtoValidationRule> dtoRules = dtoGenerator.getValidationRulesForDtoType(entity, "Create");

        if (dtoRules.isEmpty()) {
            dtoRules = parseValidationRulesFromDtoClass(entity);
        }

        List<ControllerValidationRule> controllerRules = new ArrayList<>();
        for (DtoValidationRule dtoRule : dtoRules) {
            ControllerValidationRule controllerRule = convertDtoRuleToControllerRule(entity, dtoRule);
            if (controllerRule != null) {
                controllerRules.add(controllerRule);
            }
        }

        return controllerRules;
    }

    private List<DtoValidationRule> parseValidationRulesFromDtoClass(EntityMetaModel entity) {
        try {
            String dtoClassName = entity.getPackageName().replace(".entity", ".dto") + "." + entity.getName() + "Dto";
            Class<?> dtoClass = Class.forName(dtoClassName);

            List<ValidationMetaModel> validations = parser.parseValidationsFromDtoClass(dtoClass);

            List<DtoValidationRule> dtoRules = new ArrayList<>();
            for (ValidationMetaModel validation : validations) {
                DtoValidationRule rule = buildDtoValidationRule(validation);
                if (rule != null) {
                    dtoRules.add(rule);
                }
            }

            return dtoRules;
        } catch (ClassNotFoundException e) {
            return new ArrayList<>();
        }
    }

    private DtoValidationRule buildDtoValidationRule(ValidationMetaModel validation) {
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

    private ControllerValidationRule convertDtoRuleToControllerRule(EntityMetaModel entity, DtoValidationRule dtoRule) {
        String dtoAttributeName = mapToDtoAttributeName(entity, dtoRule.getAttributeName());

        if (dtoAttributeName == null) {
            return null;
        }

        String testName = "create" + entity.getName() + "With" + getValidationTestSuffix(dtoRule) +
                         capitalize(dtoAttributeName) + "ShouldReturnBadRequest";

        ControllerValidationRule.ControllerValidationRuleBuilder builder = ControllerValidationRule.builder()
                .testName(testName)
                .attributeName(dtoAttributeName)
                .validationType(dtoRule.getValidationType())
                .description("Should return 400 when " + dtoAttributeName + " " + getValidationDescription(dtoRule))
                .expectedHttpStatus("400")
                .message(dtoRule.getMessage());
        if (dtoRule.isNotNull()) {
            builder.invalidValue("null");
        } else if (dtoRule.isNotBlank()) {
            builder.invalidValue("\"\"");
        } else if (dtoRule.isSize()) {
            builder.minValue(dtoRule.getMinValue())
                   .maxValue(dtoRule.getMaxValue());
            if (dtoRule.getMaxValue() != null) {
                int maxLength = ((Number) dtoRule.getMaxValue()).intValue();
                builder.invalidValue("\"" + "a".repeat(maxLength + 1) + "\"");
            }
        } else if (dtoRule.isMin()) {
            builder.constraintValue(dtoRule.getConstraintValue());
            int minValue = ((Number) dtoRule.getConstraintValue()).intValue();
            builder.invalidValue("\"" + String.valueOf(minValue - 1) + "\"");
        } else if (dtoRule.isMax()) {
            builder.constraintValue(dtoRule.getConstraintValue());
            int maxValue = ((Number) dtoRule.getConstraintValue()).intValue();
            builder.invalidValue("\"" + String.valueOf(maxValue + 1) + "\"");
        } else if (dtoRule.isEmail()) {
            builder.invalidValue("\"invalid-email\"");
        } else if (dtoRule.isPattern()) {
            builder.pattern(dtoRule.getPattern())
                   .invalidValue("\"invalid-pattern\"");
        }

        return builder.build();
    }

    private String getValidationTestSuffix(DtoValidationRule dtoRule) {
        if (dtoRule.isNotNull()) return "Null";
        if (dtoRule.isNotBlank()) return "Blank";
        if (dtoRule.isNotEmpty()) return "Empty";
        if (dtoRule.isSize()) return "InvalidSize";
        if (dtoRule.isMin()) return "TooSmall";
        if (dtoRule.isMax()) return "TooLarge";
        if (dtoRule.isEmail()) return "InvalidEmail";
        if (dtoRule.isPattern()) return "InvalidPattern";
        return "Invalid";
    }

    private String getValidationDescription(DtoValidationRule dtoRule) {
        if (dtoRule.isNotNull()) return "is null";
        if (dtoRule.isNotBlank()) return "is blank";
        if (dtoRule.isNotEmpty()) return "is empty";
        if (dtoRule.isSize()) return "has invalid size";
        if (dtoRule.isMin()) return "is below minimum value";
        if (dtoRule.isMax()) return "exceeds maximum value";
        if (dtoRule.isEmail()) return "is not a valid email";
        if (dtoRule.isPattern()) return "doesn't match required pattern";
        return "is invalid";
    }

    private String mapToDtoAttributeName(EntityMetaModel entity, String attributeName) {
        boolean isRegularAttribute = entity.getAttributes().stream()
                .anyMatch(attr -> attr.getName().equals(attributeName));

        if (isRegularAttribute) {
            return attributeName;
        }

        boolean isRelationship = entity.getRelationships().stream()
                .anyMatch(rel -> rel.getName().equals(attributeName));

        if (isRelationship) {
            return attributeName + "Id";
        }

        return null;
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}
