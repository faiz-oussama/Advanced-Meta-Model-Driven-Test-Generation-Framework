package ${basePackage}.repository;

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

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@DisplayName("${entity.name} Repository Tests")
class ${entity.testClassName} {

    @Autowired
    private ${entity.repositoryName} ${entity.variableName}Repository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        ${entity.variableName}Repository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    private ${entity.name} create${entity.name}() {
        ${entity.name} ${entity.variableName} = ${entity.builderClassName}.aValid${entity.name}().build();
<#if entity.hasRelationships()>
<#list entity.relationships as relationship>
<#if !relationship.collection && !relationship.optional>
        ${relationship.targetEntity} ${relationship.targetEntity?uncap_first} = ${relationship.targetEntity}TestDataBuilder.aDefault${relationship.targetEntity}().build();
        ${relationship.targetEntity} persisted${relationship.targetEntity} = entityManager.persistAndFlush(${relationship.targetEntity?uncap_first});
        ${entity.variableName}.set${relationship.nameCapitalized}(persisted${relationship.targetEntity});
<#elseif !relationship.collection && relationship.optional>
        ${entity.variableName}.set${relationship.nameCapitalized}(create${relationship.targetEntity}());
</#if>
</#list>
</#if>
        return ${entity.variableName};
    }

<#if entity.hasRelationships()>
<#list entity.relationships as relationship>
    private ${relationship.targetEntity} create${relationship.targetEntity}() {
        ${relationship.targetEntity} ${relationship.targetEntity?uncap_first} = ${relationship.targetEntity}TestDataBuilder.aDefault${relationship.targetEntity}().build();
        return ${relationship.targetEntity?uncap_first};
    }

</#list>
</#if>
    <#include "crud-tests.ftl">

    <#if entity.hasValidations() || (!entity.primaryKeyAttribute.generatedValue)>
        <#include "validation-tests.ftl">
    </#if>

    <#if entity.hasRelationships()>
        <#include "relationship-tests.ftl">
    </#if>

    <#include "pagination-tests.ftl">

}
