package ${basePackage}.controller;

import ${basePackage}.testdata.${entity.builderClassName};
import ${basePackage}.dto.${entity.dtoClassName};

<#if entity.hasRelationships()>
<#list entity.relationships as relationship>
import ${basePackage}.testdata.${relationship.targetEntity}TestDataBuilder;
import ${basePackage}.dto.${relationship.targetEntity}Dto;
</#list>
</#if>

import ${entity.packageName}.${entity.name};

<#if entity.hasRelationships()>
<#list entity.relationships as relationship>
import ${entity.packageName}.${relationship.targetEntity};
</#list>
</#if>

import ${basePackage}.controller.${entity.controllerName};
import ${basePackage}.service.${entity.serviceName};
import ${basePackage}.exception.EntityNotFoundException;
import ${basePackage}.exception.BadRequestException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.lang.reflect.Field;
import java.util.ArrayList;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import ${basePackage}.generator.model.ControllerValidationRule;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.hamcrest.Matchers.*;
import static org.assertj.core.api.Assertions.*;

@WebMvcTest(${entity.controllerName}.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(${basePackage}.config.TestSecurityConfig.class)
<#if !hasSecurityRules>
@WithMockUser
</#if>
@DisplayName("${entity.name} Controller Tests")
class ${entity.controllerTestClassName} {

<#assign hasStrictValidation = false>
<#list validationRules as rule>
<#if rule.isNotNull() || rule.isNotBlank() || rule.isNotEmpty() || (rule.isSize() && rule.minValue?? && rule.minValue?number > 0)>
<#assign hasStrictValidation = true>
<#break>
</#if>
</#list>

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ${entity.serviceName} ${entity.variableName}Service;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        reset(${entity.variableName}Service);
    }

    private ${entity.name} create${entity.name}() {
<#if entity.hasRelationships()>
        ${entity.name}TestDataBuilder builder = ${entity.name}TestDataBuilder.aValid${entity.name}();
<#list entity.relationships as relationship>
<#if relationship.type == "ManyToOne" || relationship.type == "OneToOne">
        builder = builder.with${relationship.nameCapitalized}(create${relationship.targetEntity}());
</#if>
</#list>
        return builder.build();
<#else>
        return ${entity.builderClassName}.aValid${entity.name}().build();
</#if>
    }

    private ${entity.dtoClassName} create${entity.dtoClassName}() {
        ${entity.name} entity = create${entity.name}();
<#if entity.hasRelationships()>
<#list entity.relationships as relationship>
<#if relationship.type == "ManyToOne" || relationship.type == "OneToOne">
        if (entity.${relationship.getterName}() != null && entity.${relationship.getterName}().getId() == null) {
            entity.${relationship.getterName}().setId(1L);
        }
</#if>
</#list>
</#if>
        return ${entity.dtoClassName}.builder()
<#list entity.attributes as attr>
<#if !attr.primaryKey || !attr.generatedValue>
                .${attr.name}(entity.${attr.getterName}())
</#if>
</#list>
<#if entity.hasRelationships()>
<#list entity.relationships as relationship>
<#if relationship.type == "ManyToOne" || relationship.type == "OneToOne">
                .${relationship.name}Id(entity.${relationship.getterName}() != null ? entity.${relationship.getterName}().getId() : 1L)
</#if>
</#list>
</#if>
                .build();
    }



    private ${entity.name} createMockEntity() {
        ${entity.name} mockEntity = create${entity.name}();
<#if entity.primaryKeyAttribute.javaType == "String">
        mockEntity.${entity.primaryKeyAttribute.setterName}("CREATED_ID");
<#else>
        mockEntity.${entity.primaryKeyAttribute.setterName}(1L);
</#if>
<#-- Ensure related entities have IDs for proper DTO conversion -->
<#if entity.hasRelationships()>
<#list entity.relationships as relationship>
<#if relationship.type == "ManyToOne" || relationship.type == "OneToOne">
        if (mockEntity.${relationship.getterName}() != null && mockEntity.${relationship.getterName}().getId() == null) {
            mockEntity.${relationship.getterName}().setId(1L);
        }
</#if>
</#list>
</#if>
        return mockEntity;
    }

<#if entity.hasRelationships()>
<#list entity.relationships as relationship>
    private ${relationship.targetEntity} create${relationship.targetEntity}() {
        return ${relationship.targetEntity}TestDataBuilder.aDefault${relationship.targetEntity}().build();
    }

</#list>
</#if>
    <#include "controller-crud-tests.ftl">

    <#if validationRules?has_content>
        <#include "controller-dynamic-validation-tests.ftl">
    </#if>

    <#include "controller-exception-tests.ftl">

    <#include "controller-status-code-tests.ftl">

    <#include "controller-edge-case-tests.ftl">

    <#include "controller-http-method-tests.ftl">

    <#include "controller-json-structure-tests.ftl">

    <#include "controller-header-param-tests.ftl">

    <#include "controller-pagination-tests.ftl">

    <#include "controller-swagger-tests.ftl">

    <#include "controller-security-tests.ftl">

    <#if entity.hasRelationships()>
        <#include "controller-relationship-tests.ftl">
    </#if>

}
