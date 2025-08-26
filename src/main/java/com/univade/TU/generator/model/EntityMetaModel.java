package com.univade.TU.generator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntityMetaModel {
    
    private String name;
    private String packageName;
    private String tableName;
    private boolean auditable;
    
    @Builder.Default
    private List<AttributeMetaModel> attributes = new ArrayList<>();
    
    @Builder.Default
    private List<RelationshipMetaModel> relationships = new ArrayList<>();
    
    @Builder.Default
    private List<ValidationMetaModel> validations = new ArrayList<>();

    @Builder.Default
    private List<SecurityRuleMetaModel> securityRules = new ArrayList<>();

    public String getNameCapitalized() {
        if (name == null || name.isEmpty()) {
            return "";
        }
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    public String getNameLowerCase() {
        if (name == null || name.isEmpty()) {
            return "";
        }
        return name.toLowerCase();
    }

    public String getVariableName() {
        if (name == null || name.isEmpty()) {
            return "";
        }
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    public String getRepositoryName() {
        return getNameCapitalized() + "Repository";
    }

    public String getTestClassName() {
        return getNameCapitalized() + "RepositoryTest";
    }

    public String getServiceName() {
        return getNameCapitalized() + "Service";
    }

    public String getServiceTestClassName() {
        return getNameCapitalized() + "ServiceTest";
    }

    public String getControllerName() {
        return getNameCapitalized() + "Controller";
    }

    public String getControllerTestClassName() {
        return getNameCapitalized() + "ControllerTest";
    }

    public String getDtoClassName() {
        return getNameCapitalized() + "Dto";
    }

    public String getBuilderClassName() {
        return getNameCapitalized() + "TestDataBuilder";
    }

    public String getFullyQualifiedName() {
        if (packageName == null || packageName.isEmpty()) {
            return name;
        }
        return packageName + "." + name;
    }

    public AttributeMetaModel getPrimaryKeyAttribute() {
        return attributes.stream()
                .filter(AttributeMetaModel::isPrimaryKey)
                .findFirst()
                .orElse(null);
    }

    public List<AttributeMetaModel> getNonPrimaryKeyAttributes() {
        return attributes.stream()
                .filter(attr -> !attr.isPrimaryKey())
                .collect(Collectors.toList());
    }

    public List<AttributeMetaModel> getRequiredAttributes() {
        return attributes.stream()
                .filter(attr -> !attr.isNullable() && !attr.isPrimaryKey())
                .collect(Collectors.toList());
    }

    public List<AttributeMetaModel> getUniqueAttributes() {
        return attributes.stream()
                .filter(AttributeMetaModel::isUnique)
                .collect(Collectors.toList());
    }

    public List<AttributeMetaModel> getStringAttributes() {
        return attributes.stream()
                .filter(AttributeMetaModel::isStringType)
                .collect(Collectors.toList());
    }

    public List<AttributeMetaModel> getNumericAttributes() {
        return attributes.stream()
                .filter(AttributeMetaModel::isNumericType)
                .collect(Collectors.toList());
    }

    public List<AttributeMetaModel> getEnumAttributes() {
        return attributes.stream()
                .filter(AttributeMetaModel::isEnumType)
                .collect(Collectors.toList());
    }

    public List<RelationshipMetaModel> getOwnerRelationships() {
        return relationships.stream()
                .filter(RelationshipMetaModel::isOwner)
                .collect(Collectors.toList());
    }

    public List<RelationshipMetaModel> getCollectionRelationships() {
        return relationships.stream()
                .filter(RelationshipMetaModel::isCollection)
                .collect(Collectors.toList());
    }

    public List<RelationshipMetaModel> getRequiredRelationships() {
        return relationships.stream()
                .filter(relationship -> {
                    if (!relationship.isOptional()) {
                        return true;
                    }
                    return validations.stream()
                            .anyMatch(validation ->
                                relationship.getName().equals(validation.getAttributeName()) &&
                                validation.isNotNull());
                })
                .collect(Collectors.toList());
    }

    public boolean hasRelationships() {
        return relationships != null && !relationships.isEmpty();
    }

    public boolean hasValidations() {
        return validations != null && !validations.isEmpty();
    }

    public String getTableNameOrDefault() {
        return tableName != null ? tableName : getPluralName();
    }

    public boolean hasSecurityRules() {
        return securityRules != null && !securityRules.isEmpty();
    }

    public List<String> getAllSecurityRoles() {
        List<String> allRoles = new ArrayList<>();
        if (securityRules != null) {
            securityRules.forEach(rule -> allRoles.addAll(rule.getAllRoles()));
        }
        return allRoles.stream().distinct().collect(Collectors.toList());
    }

    public SecurityRuleMetaModel getSecurityRuleForPath(String path) {
        if (securityRules == null) {
            return null;
        }
        return securityRules.stream()
                .filter(rule -> path.equals(rule.getPath()))
                .findFirst()
                .orElse(null);
    }

    public List<SecurityRuleMetaModel> getSecurityRulesForBasePath() {
        if (securityRules == null) {
            return new ArrayList<>();
        }
        String basePath = "/api/" + getPluralName();
        return securityRules.stream()
                .filter(rule -> rule.getPath().startsWith(basePath))
                .collect(Collectors.toList());
    }

    public String getPluralName() {
        return pluralize(getNameLowerCase());
    }

    public String getPluralNameCapitalized() {
        return pluralize(name);
    }

    private String pluralize(String word) {
        if (word == null || word.isEmpty()) {
            return word;
        }

        if (word.endsWith("s") || word.endsWith("ss") || word.endsWith("sh") ||
            word.endsWith("ch") || word.endsWith("x") || word.endsWith("z")) {
            return word + "es";
        }

        if (word.endsWith("y") && word.length() > 1) {
            char beforeY = word.charAt(word.length() - 2);
            if (!isVowel(beforeY)) {
                return word.substring(0, word.length() - 1) + "ies";
            }
        }

        if (word.endsWith("f")) {
            return word.substring(0, word.length() - 1) + "ves";
        }
        if (word.endsWith("fe")) {
            return word.substring(0, word.length() - 2) + "ves";
        }

        if (word.endsWith("o") && word.length() > 1) {
            char beforeO = word.charAt(word.length() - 2);
            if (!isVowel(beforeO)) {
                return word + "es";
            }
        }

        return word + "s";
    }

    private boolean isVowel(char c) {
        return "aeiouAEIOU".indexOf(c) != -1;
    }
}
