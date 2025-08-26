package com.univade.TU.generator.service;

import com.univade.TU.generator.exception.TestGenerationException;
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
import java.util.HashMap;
import java.util.Map;

@Service
public class TestGeneratorService {

    private final Configuration freemarkerConfig;
    private final EntityMetaModelParser parser;
    private final FileWriterService fileWriterService;
    private final TemplateUtils templateUtils;
    private final TestDataGenerator testDataGenerator;

    @Autowired
    public TestGeneratorService(@Qualifier("freemarkerConfiguration") Configuration freemarkerConfig, EntityMetaModelParser parser, FileWriterService fileWriterService) {
        this.freemarkerConfig = freemarkerConfig;
        this.parser = parser;
        this.fileWriterService = fileWriterService;
        this.templateUtils = new TemplateUtils();
        this.testDataGenerator = new TestDataGenerator();
    }

    public GeneratedTestResult generateRepositoryTest(EntityMetaModel entityMetaModel) {
        try {
            GeneratedTestResult result = GeneratedTestResult.builder()
                    .entityName(entityMetaModel.getName())
                    .build();

            Map<String, Object> dataModel = createDataModel(entityMetaModel);

            String repositoryTestContent = processTemplate("repository-test.ftl", dataModel);
            result.setRepositoryTestContent(repositoryTestContent);
            result.addGeneratedFile(entityMetaModel.getTestClassName() + ".java");

            String builderContent = processTemplate("test-data-builder.ftl", dataModel);
            result.setBuilderContent(builderContent);
            result.addGeneratedFile(entityMetaModel.getBuilderClassName() + ".java");

            if (entityMetaModel.hasValidations()) {
                String validationTestContent = processTemplate("validation-tests.ftl", dataModel);
                result.setValidationTestContent(validationTestContent);
                result.addGeneratedFile(entityMetaModel.getName() + "ValidationTest.java");
            }

            String crudTestContent = processTemplate("crud-tests.ftl", dataModel);
            result.setCrudTestContent(crudTestContent);
            result.addGeneratedFile(entityMetaModel.getName() + "CrudTest.java");

            return result;

        } catch (Exception e) {
            throw new TestGenerationException("Error while generating repository tests for " + entityMetaModel.getName(), e);
        }
    }

    public GeneratedTestResult generateAndSaveTests(EntityMetaModel entityMetaModel) {
        GeneratedTestResult result = generateRepositoryTest(entityMetaModel);
        fileWriterService.writeGeneratedFiles(entityMetaModel, result);
        return result;
    }

    public GeneratedTestResult generateFromJson(String jsonMetaModel) {
        EntityMetaModel entityMetaModel = parser.parseFromJson(jsonMetaModel);
        return generateRepositoryTest(entityMetaModel);
    }

    public GeneratedTestResult generateFromAnnotations(Class<?> entityClass) {
        EntityMetaModel entityMetaModel = parser.parseFromAnnotations(entityClass);
        return generateRepositoryTest(entityMetaModel);
    }

    private Map<String, Object> createDataModel(EntityMetaModel entityMetaModel) {
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("entity", entityMetaModel);
        dataModel.put("utils", templateUtils);
        dataModel.put("generator", testDataGenerator);
        dataModel.put("packageName", entityMetaModel.getPackageName());
        dataModel.put("repositoryPackage", entityMetaModel.getPackageName().replace(".entity", ".repository"));
        dataModel.put("testPackage", entityMetaModel.getPackageName().replace(".entity", ".repository"));
        dataModel.put("builderPackage", entityMetaModel.getPackageName().replace(".entity", ".builder"));
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

    public GeneratedTestResult generateAllTests(EntityMetaModel entityMetaModel) {
        return generateRepositoryTest(entityMetaModel);
    }

    public boolean validateMetaModel(EntityMetaModel entityMetaModel) {
        if (entityMetaModel == null) {
            throw TestGenerationException.invalidMetaModel("EntityMetaModel cannot be null");
        }
        if (entityMetaModel.getName() == null || entityMetaModel.getName().isEmpty()) {
            throw TestGenerationException.invalidMetaModel("Entity name cannot be null or empty");
        }
        if (entityMetaModel.getPackageName() == null || entityMetaModel.getPackageName().isEmpty()) {
            throw TestGenerationException.invalidMetaModel("Package name cannot be null or empty");
        }
        if (entityMetaModel.getAttributes() == null || entityMetaModel.getAttributes().isEmpty()) {
            throw TestGenerationException.invalidMetaModel("Entity must have at least one attribute");
        }
        if (entityMetaModel.getPrimaryKeyAttribute() == null) {
            throw TestGenerationException.invalidMetaModel("Entity must have a primary key attribute");
        }
        return true;
    }
}
