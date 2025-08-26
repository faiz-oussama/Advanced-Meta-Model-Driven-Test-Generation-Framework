package com.univade.TU.generator.service;

import com.univade.TU.generator.exception.TestGenerationException;
import com.univade.TU.generator.model.EntityMetaModel;
import com.univade.TU.generator.model.GeneratedTestResult;
import com.univade.TU.generator.model.SecurityRuleMetaModel;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.util.*;

@Service
public class SecurityConfigGenerator {

    private final Configuration freemarkerConfig;

    @Autowired
    public SecurityConfigGenerator(@Qualifier("freemarkerConfiguration") Configuration freemarkerConfig) {
        this.freemarkerConfig = freemarkerConfig;
    }

    public GeneratedTestResult generateSecurityConfig(List<EntityMetaModel> entities, String basePackage) {
        try {
            GeneratedTestResult result = GeneratedTestResult.builder()
                    .entityName("TestSecurityConfig")
                    .build();

            Map<String, Object> dataModel = createSecurityConfigDataModel(entities, basePackage);
            String securityConfigContent = processTemplate("config/test-security-config.ftl", dataModel);
            result.setSecurityConfigContent(securityConfigContent);
            result.addGeneratedFile("TestSecurityConfig.java");


            return result;
        } catch (Exception e) {
            throw new TestGenerationException("Failed to generate TestSecurityConfig", e);
        }
    }

    private Map<String, Object> createSecurityConfigDataModel(List<EntityMetaModel> entities, String basePackage) {
        Map<String, Object> dataModel = new HashMap<>();
        
        List<SecurityRuleMetaModel> allSecurityRules = new ArrayList<>();
        Set<String> allRoles = new HashSet<>();
        boolean hasAnySecurityRules = false;
        
        for (EntityMetaModel entity : entities) {
            if (entity.hasSecurityRules()) {
                hasAnySecurityRules = true;
                allSecurityRules.addAll(entity.getSecurityRules());
                allRoles.addAll(entity.getAllSecurityRoles());
            }
        }
        
        dataModel.put("basePackage", basePackage);
        dataModel.put("securityRules", allSecurityRules);
        dataModel.put("hasSecurityRules", hasAnySecurityRules);
        dataModel.put("allSecurityRoles", new ArrayList<>(allRoles));
        
        return dataModel;
    }

    private String processTemplate(String templateName, Map<String, Object> dataModel) {
        try {
            Template template = freemarkerConfig.getTemplate(templateName);
            StringWriter writer = new StringWriter();
            template.process(dataModel, writer);
            return writer.toString();
        } catch (Exception e) {
            throw new TestGenerationException("Error processing template: " + templateName, e);
        }
    }
}
