package com.univade.TU.generator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityRuleMetaModel {
    
    private String path;
    
    @Builder.Default
    private Map<String, List<String>> methods = Map.of();
    
    public boolean hasMethodSecurity(String httpMethod) {
        return methods != null && methods.containsKey(httpMethod);
    }
    
    public List<String> getAllowedRoles(String httpMethod) {
        if (methods == null || !methods.containsKey(httpMethod)) {
            return new ArrayList<>();
        }
        return methods.get(httpMethod);
    }
    
    public boolean isMethodAllowedForRole(String httpMethod, String role) {
        List<String> allowedRoles = getAllowedRoles(httpMethod);
        return allowedRoles.contains(role);
    }
    
    public boolean isPublicEndpoint(String httpMethod) {
        return !hasMethodSecurity(httpMethod) || getAllowedRoles(httpMethod).isEmpty();
    }
    
    public List<String> getAllHttpMethods() {
        return methods != null ? new ArrayList<>(methods.keySet()) : new ArrayList<>();
    }
    
    public List<String> getAllRoles() {
        List<String> allRoles = new ArrayList<>();
        if (methods != null) {
            methods.values().forEach(allRoles::addAll);
        }
        return allRoles.stream().distinct().toList();
    }
}
