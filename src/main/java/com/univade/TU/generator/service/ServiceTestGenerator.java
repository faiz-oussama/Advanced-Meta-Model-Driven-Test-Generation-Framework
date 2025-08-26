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
public class ServiceTestGenerator {

    private final Configuration freemarkerConfig;
    private final EntityMetaModelParser parser;
    private final TemplateUtils templateUtils;
    private final TestDataGenerator testDataGenerator;

    @Autowired
    public ServiceTestGenerator(@Qualifier("freemarkerConfiguration") Configuration freemarkerConfig,
                            EntityMetaModelParser parser) {
        this.freemarkerConfig = freemarkerConfig;
        this.parser = parser;
        this.templateUtils = new TemplateUtils();
        this.testDataGenerator = new TestDataGenerator();
    }

    public GeneratedTestResult generateServiceTest(EntityMetaModel entity) {
        try {
            GeneratedTestResult result = GeneratedTestResult.builder()
                    .entityName(entity.getName())
                    .build();

            Map<String, Object> dataModel = createServiceDataModel(entity);

            String testContent = processTemplate("service/service-test.ftl", dataModel);
            result.setServiceTestContent(testContent);
            result.addGeneratedFile(entity.getName() + "ServiceTest.java");

            String builderContent = processTemplate("test-data-builder.ftl", dataModel);
            result.setBuilderContent(builderContent);
            result.addGeneratedFile(entity.getBuilderClassName() + ".java");

            return result;
        } catch (Exception e) {
            throw new TestGenerationException("Failed to generate service tests for " + entity.getName(), e);
        }
    }

    public GeneratedTestResult generateFromJson(String jsonMetaModel) {
        EntityMetaModel entity = parser.parseFromJson(jsonMetaModel);
        return generateServiceTest(entity);
    }

    public GeneratedTestResult generateFromAnnotations(Class<?> entityClass) {
        EntityMetaModel entity = parser.parseFromAnnotations(entityClass);
        return generateServiceTest(entity);
    }

    private Map<String, Object> createServiceDataModel(EntityMetaModel entity) {
        Map<String, Object> dataModel = new HashMap<>();

        dataModel.put("entity", entity);
        dataModel.put("utils", templateUtils);
        dataModel.put("generator", testDataGenerator);

        String basePackage = entity.getPackageName().replace(".entity", "");
        dataModel.put("packageName", entity.getPackageName());
        dataModel.put("basePackage", basePackage);
        dataModel.put("repositoryPackage", basePackage + ".repository");
        dataModel.put("servicePackage", basePackage + ".service");
        dataModel.put("testPackage", basePackage + ".service");
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
}
