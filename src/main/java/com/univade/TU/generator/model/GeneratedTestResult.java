package com.univade.TU.generator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedTestResult {
    
    private String entityName;
    private String repositoryTestContent;
    private String serviceTestContent;
    private String controllerTestContent;
    private String serviceIntegrationTestContent;
    private String builderContent;
    private String validationTestContent;
    private String crudTestContent;
    private String relationshipTestContent;
    private String dtoContent;
    private String createDtoContent;
    private String updateDtoContent;
    private String responseDtoContent;
    private String securityConfigContent;
    
    @Builder.Default
    private List<String> generatedFiles = new ArrayList<>();
    
    @Builder.Default
    private List<String> errors = new ArrayList<>();
    
    @Builder.Default
    private List<String> warnings = new ArrayList<>();

    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }

    public boolean hasWarnings() {
        return warnings != null && !warnings.isEmpty();
    }

    public boolean isSuccessful() {
        return !hasErrors();
    }

    public void addError(String error) {
        if (errors == null) {
            errors = new ArrayList<>();
        }
        errors.add(error);
    }

    public void addWarning(String warning) {
        if (warnings == null) {
            warnings = new ArrayList<>();
        }
        warnings.add(warning);
    }

    public void addGeneratedFile(String fileName) {
        if (generatedFiles == null) {
            generatedFiles = new ArrayList<>();
        }
        generatedFiles.add(fileName);
    }

    public int getGeneratedFileCount() {
        return generatedFiles != null ? generatedFiles.size() : 0;
    }

    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Generated ").append(getGeneratedFileCount()).append(" files for entity: ").append(entityName);
        
        if (hasErrors()) {
            sb.append(" (").append(errors.size()).append(" errors)");
        }
        
        if (hasWarnings()) {
            sb.append(" (").append(warnings.size()).append(" warnings)");
        }
        
        return sb.toString();
    }
}
