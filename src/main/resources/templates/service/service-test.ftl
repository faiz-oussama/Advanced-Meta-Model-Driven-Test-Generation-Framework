package ${basePackage}.service;

import ${basePackage}.testdata.${entity.builderClassName};

<#if entity.hasRelationships()>
<#list entity.relationships as relationship>
import ${basePackage}.testdata.${relationship.targetEntity}TestDataBuilder;
</#list>
</#if>

import ${entity.packageName}.${entity.name};

<#if entity.hasRelationships()>
<#list entity.relationships as relationship>
import ${entity.packageName}.${relationship.targetEntity};
</#list>
</#if>

import ${basePackage}.repository.${entity.repositoryName};
<#if entity.hasRelationships()>
<#list entity.relationships as relationship>
import ${basePackage}.repository.${relationship.targetEntity}Repository;
</#list>
</#if>

import ${basePackage}.service.${entity.serviceName};
import ${basePackage}.exception.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("${entity.name} Service Tests")
class ${entity.serviceTestClassName} {

    @Mock
    private ${entity.repositoryName} ${entity.variableName}Repository;

    @InjectMocks
    private ${entity.serviceName} ${entity.variableName}Service;

    @BeforeEach
    void setUp() {
        reset(${entity.variableName}Repository);
    }

    private ${entity.name} create${entity.name}() {
        return ${entity.builderClassName}.aValid${entity.name}().build();
    }

<#if entity.hasRelationships()>
<#list entity.relationships as relationship>
    private ${relationship.targetEntity} create${relationship.targetEntity}() {
        return ${relationship.targetEntity}TestDataBuilder.aDefault${relationship.targetEntity}().build();
    }

</#list>
</#if>
    <#include "service-crud-tests.ftl">

    <#include "service-business-logic-tests.ftl">

    <#include "service-pagination-tests.ftl">

    <#include "service-validation-tests.ftl">

    <#if entity.hasRelationships()>
        <#include "service-relationship-tests.ftl">
    </#if>

    <#include "service-integration-tests.ftl">

}
