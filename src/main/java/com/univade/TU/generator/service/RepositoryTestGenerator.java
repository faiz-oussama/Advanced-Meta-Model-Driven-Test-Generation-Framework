package com.univade.TU.generator.service;

import com.univade.TU.generator.exception.TestGenerationException;
import com.univade.TU.generator.model.AttributeMetaModel;
import com.univade.TU.generator.model.DatabaseValidationRule;
import com.univade.TU.generator.model.EntityMetaModel;
import com.univade.TU.generator.model.GeneratedTestResult;

import com.univade.TU.generator.parser.EntityMetaModelParser;
import com.univade.TU.generator.util.TemplateUtils;
import com.univade.TU.generator.util.TestDataGenerator;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;


import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RepositoryTestGenerator {

    private final Configuration freemarkerConfig;
    private final EntityMetaModelParser parser;
    private final TemplateUtils templateUtils;
    private final TestDataGenerator testDataGenerator;

    @Autowired
    public RepositoryTestGenerator(@Qualifier("freemarkerConfiguration") Configuration freemarkerConfig,
                                   EntityMetaModelParser parser) {
        this.freemarkerConfig = freemarkerConfig;
        this.parser = parser;
        this.templateUtils = new TemplateUtils();
        this.testDataGenerator = new TestDataGenerator();
    }

    public GeneratedTestResult generateRepositoryTest(EntityMetaModel entity) {
        try {
            GeneratedTestResult result = GeneratedTestResult.builder()
                    .entityName(entity.getName())
                    .build();

            Map<String, Object> dataModel = createDataModel(entity);

            String testContent = processTemplate("repository/repository-test.ftl", dataModel);
            result.setRepositoryTestContent(testContent);
            result.addGeneratedFile(entity.getName() + "RepositoryTest.java");

            String builderContent = processTemplate("test-data-builder.ftl", dataModel);
            result.setBuilderContent(builderContent);
            result.addGeneratedFile(entity.getBuilderClassName() + ".java");

            return result;

        } catch (Exception e) {
            throw new TestGenerationException("Failed to generate repository tests for " + entity.getName(), e);
        }
    }

    public GeneratedTestResult generateFromJson(String jsonMetaModel) {
        EntityMetaModel entity = parser.parseFromJson(jsonMetaModel);
        return generateRepositoryTest(entity);
    }

    public GeneratedTestResult generateFromAnnotations(Class<?> entityClass) {
        EntityMetaModel entity = parser.parseFromAnnotations(entityClass);
        return generateRepositoryTest(entity);
    }

    private Map<String, Object> createDataModel(EntityMetaModel entity) {
        Map<String, Object> dataModel = new HashMap<>();

        dataModel.put("entity", entity);
        dataModel.put("validationRules", buildValidationRules(entity));
        dataModel.put("utils", templateUtils);
        dataModel.put("generator", testDataGenerator);

        String basePackage = entity.getPackageName().replace(".entity", "");
        dataModel.put("packageName", entity.getPackageName());
        dataModel.put("basePackage", basePackage);
        dataModel.put("repositoryPackage", basePackage + ".repository");
        dataModel.put("testPackage", basePackage + ".repository");
        dataModel.put("builderPackage", basePackage + ".testdata");

        return dataModel;
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
        return generateRepositoryTest(entity);
    }

    public boolean validateMetaModel(EntityMetaModel entity) {
        if (entity == null) {
            throw TestGenerationException.invalidMetaModel("EntityMetaModel cannot be null");
        }
        if (entity.getName() == null || entity.getName().isEmpty()) {
            throw TestGenerationException.invalidMetaModel("Entity name cannot be null or empty");
        }
        if (entity.getPackageName() == null || entity.getPackageName().isEmpty()) {
            throw TestGenerationException.invalidMetaModel("Package name cannot be null or empty");
        }
        if (entity.getAttributes() == null || entity.getAttributes().isEmpty()) {
            throw TestGenerationException.invalidMetaModel("Entity must have at least one attribute");
        }
        if (entity.getPrimaryKeyAttribute() == null) {
            throw TestGenerationException.invalidMetaModel("Entity must have a primary key attribute");
        }
        return true;
    }

    private List<DatabaseValidationRule> buildValidationRules(EntityMetaModel entity) {
        List<DatabaseValidationRule> rules = new ArrayList<>();

        for (AttributeMetaModel attribute : entity.getAttributes()) {
            rules.addAll(buildAttributeValidationRules(entity, attribute));
        }

        return rules;
    }



    private List<DatabaseValidationRule> buildAttributeValidationRules(EntityMetaModel entity, AttributeMetaModel attribute) {
        List<DatabaseValidationRule> rules = new ArrayList<>();

        if (!attribute.isNullable() && !attribute.isPrimaryKey()) {
            rules.add(buildNullableConstraintRule(entity, attribute));
        }

        if (attribute.isUnique()) {
            rules.add(buildUniqueConstraintRule(entity, attribute));
        }

        if (attribute.getMaxLength() != null && isStringType(attribute)) {
            rules.add(buildMaxLengthConstraintRule(entity, attribute));
        }

        if (attribute.getMinValue() != null && isNumericType(attribute)) {
            rules.add(buildMinValueConstraintRule(entity, attribute));
        }

        if (attribute.getMaxValue() != null && isNumericType(attribute)) {
            rules.add(buildMaxValueConstraintRule(entity, attribute));
        }

        if (attribute.isPrimaryKey() && attribute.isGeneratedValue()) {
            rules.add(buildPrimaryKeyGenerationRule(entity, attribute));
        }

        return rules;
    }

    private boolean isStringType(AttributeMetaModel attribute) {
        return "String".equals(attribute.getType()) || "java.lang.String".equals(attribute.getType());
    }

    private boolean isNumericType(AttributeMetaModel attribute) {
        String type = attribute.getType();
        return "Integer".equals(type) || "Long".equals(type) || "Double".equals(type) ||
               "Float".equals(type) || "BigDecimal".equals(type) || "BigInteger".equals(type) ||
               "int".equals(type) || "long".equals(type) || "double".equals(type) || "float".equals(type);
    }

    private DatabaseValidationRule buildNullableConstraintRule(EntityMetaModel entity, AttributeMetaModel attribute) {
        String testName = "save" + entity.getName() + "WithNull" + attribute.getNameCapitalized() + "ShouldThrowException";
        String testBody = generateNullConstraintTestBody(entity, attribute);

        return DatabaseValidationRule.builder()
                .testName(testName)
                .testBody(testBody)
                .attributeName(attribute.getName())
                .validationType("nullable")
                .description("Test that null values are rejected for non-nullable field")
                .expectedExceptionType("ConstraintViolationException")
                .build();
    }

    private DatabaseValidationRule buildUniqueConstraintRule(EntityMetaModel entity, AttributeMetaModel attribute) {
        String testName = "save" + entity.getName() + "WithDuplicate" + attribute.getNameCapitalized() + "ShouldThrowException";
        String testBody = generateUniqueConstraintTestBody(entity, attribute);

        return DatabaseValidationRule.builder()
                .testName(testName)
                .testBody(testBody)
                .attributeName(attribute.getName())
                .validationType("unique")
                .description("Test that duplicate values are rejected for unique field")
                .expectedExceptionType("org.hibernate.exception.ConstraintViolationException")
                .build();
    }

    private DatabaseValidationRule buildMaxLengthConstraintRule(EntityMetaModel entity, AttributeMetaModel attribute) {
        String testName = "save" + entity.getName() + "WithTooLong" + attribute.getNameCapitalized() + "ShouldThrowException";
        String testBody = generateMaxLengthConstraintTestBody(entity, attribute);

        return DatabaseValidationRule.builder()
                .testName(testName)
                .testBody(testBody)
                .attributeName(attribute.getName())
                .validationType("maxLength")
                .description("Test that values exceeding max length are rejected")
                .constraintValue(attribute.getMaxLength())
                .expectedExceptionType("ConstraintViolationException")
                .build();
    }

    private DatabaseValidationRule buildMinValueConstraintRule(EntityMetaModel entity, AttributeMetaModel attribute) {
        String testName = "save" + entity.getName() + "WithTooSmall" + attribute.getNameCapitalized() + "ShouldThrowException";
        String testBody = generateMinValueConstraintTestBody(entity, attribute);

        return DatabaseValidationRule.builder()
                .testName(testName)
                .testBody(testBody)
                .attributeName(attribute.getName())
                .validationType("minValue")
                .description("Test that values below minimum are rejected")
                .constraintValue(attribute.getMinValue())
                .expectedExceptionType("ConstraintViolationException")
                .build();
    }

    private DatabaseValidationRule buildMaxValueConstraintRule(EntityMetaModel entity, AttributeMetaModel attribute) {
        String testName = "save" + entity.getName() + "WithTooLarge" + attribute.getNameCapitalized() + "ShouldThrowException";
        String testBody = generateMaxValueConstraintTestBody(entity, attribute);

        return DatabaseValidationRule.builder()
                .testName(testName)
                .testBody(testBody)
                .attributeName(attribute.getName())
                .validationType("maxValue")
                .description("Test that values above maximum are rejected")
                .constraintValue(attribute.getMaxValue())
                .expectedExceptionType("ConstraintViolationException")
                .build();
    }

    private DatabaseValidationRule buildPrimaryKeyGenerationRule(EntityMetaModel entity, AttributeMetaModel attribute) {
        String testName = "save" + entity.getName() + "ShouldGenerateId";
        String testBody = generatePrimaryKeyGenerationTestBody(entity, attribute);

        return DatabaseValidationRule.builder()
                .testName(testName)
                .testBody(testBody)
                .attributeName(attribute.getName())
                .validationType("primaryKey")
                .description("Test that primary key is automatically generated")
                .build();
    }

    private String generateNullConstraintTestBody(EntityMetaModel entity, AttributeMetaModel attribute) {
        return String.format(
            "        %s %s = create%s();\n" +
            "        %s.%s(null);\n\n" +
            "        assertThatThrownBy(() -> {\n" +
            "            %sRepository.save(%s);\n" +
            "            entityManager.flush();\n" +
            "        }).isInstanceOf(DataIntegrityViolationException.class);",
            entity.getName(),
            entity.getNameLowerCase(),
            entity.getName(),
            entity.getNameLowerCase(),
            attribute.getSetterName(),
            entity.getNameLowerCase(),
            entity.getNameLowerCase()
        );
    }

    private String generateUniqueConstraintTestBody(EntityMetaModel entity, AttributeMetaModel attribute) {
        return String.format(
            "        %s %s1 = entityManager.persistAndFlush(create%s());\n" +
            "        entityManager.clear(); // Clear session to avoid entity identity issues\n" +
            "        %s %s2 = create%s();\n" +
            "        %s2.%s(%s1.%s());\n\n" +
            "        assertThatThrownBy(() -> {\n" +
            "            entityManager.persist(%s2);\n" +
            "            entityManager.flush();\n" +
            "        }).isInstanceOf(org.hibernate.exception.ConstraintViolationException.class);",
            entity.getName(),
            entity.getNameLowerCase(),
            entity.getName(),
            entity.getName(),
            entity.getNameLowerCase(),
            entity.getName(),
            entity.getNameLowerCase(),
            attribute.getSetterName(),
            entity.getNameLowerCase(),
            attribute.getGetterName(),
            entity.getNameLowerCase()
        );
    }

    private String generateMaxLengthConstraintTestBody(EntityMetaModel entity, AttributeMetaModel attribute) {
        String longValue = "\"" + "a".repeat(attribute.getMaxLength() + 10) + "\"";
        return String.format(
            "        %s %s = create%s();\n" +
            "        %s.%s(%s);\n\n" +
            "        assertThatThrownBy(() -> {\n" +
            "            %sRepository.save(%s);\n" +
            "            entityManager.flush();\n" +
            "        }).isInstanceOf(DataIntegrityViolationException.class);",
            entity.getName(),
            entity.getNameLowerCase(),
            entity.getName(),
            entity.getNameLowerCase(),
            attribute.getSetterName(),
            longValue,
            entity.getNameLowerCase(),
            entity.getNameLowerCase()
        );
    }

    private String generateMinValueConstraintTestBody(EntityMetaModel entity, AttributeMetaModel attribute) {
        String invalidValue = String.valueOf(attribute.getMinValue() - 1);
        return String.format(
            "        %s %s = create%s();\n" +
            "        %s.%s(%s);\n\n" +
            "        assertThatThrownBy(() -> {\n" +
            "            %sRepository.save(%s);\n" +
            "            entityManager.flush();\n" +
            "        }).isInstanceOf(DataIntegrityViolationException.class);",
            entity.getName(),
            entity.getNameLowerCase(),
            entity.getName(),
            entity.getNameLowerCase(),
            attribute.getSetterName(),
            invalidValue,
            entity.getNameLowerCase(),
            entity.getNameLowerCase()
        );
    }

    private String generateMaxValueConstraintTestBody(EntityMetaModel entity, AttributeMetaModel attribute) {
        String invalidValue = String.valueOf(attribute.getMaxValue() + 1);
        return String.format(
            "        %s %s = create%s();\n" +
            "        %s.%s(%s);\n\n" +
            "        assertThatThrownBy(() -> {\n" +
            "            %sRepository.save(%s);\n" +
            "            entityManager.flush();\n" +
            "        }).isInstanceOf(DataIntegrityViolationException.class);",
            entity.getName(),
            entity.getNameLowerCase(),
            entity.getName(),
            entity.getNameLowerCase(),
            attribute.getSetterName(),
            invalidValue,
            entity.getNameLowerCase(),
            entity.getNameLowerCase()
        );
    }

    private String generatePrimaryKeyGenerationTestBody(EntityMetaModel entity, AttributeMetaModel attribute) {
        return String.format(
            "        %s %s = create%s();\n" +
            "        assertThat(%s.%s()).isNull();\n\n" +
            "        %s saved%s = %sRepository.save(%s);\n\n" +
            "        assertThat(saved%s.%s()).isNotNull();\n" +
            "        assertThat(saved%s.%s()).isPositive();",
            entity.getName(),
            entity.getNameLowerCase(),
            entity.getName(),
            entity.getNameLowerCase(),
            attribute.getGetterName(),
            entity.getName(),
            entity.getName(),
            entity.getNameLowerCase(),
            entity.getNameLowerCase(),
            entity.getName(),
            attribute.getGetterName(),
            entity.getName(),
            attribute.getGetterName()
        );
    }
}
