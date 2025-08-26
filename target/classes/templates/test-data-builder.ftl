package ${builderPackage};

import ${entity.packageName}.${entity.name};
<#if entity.hasRelationships()>
<#list entity.relationships as relationship>
import ${entity.packageName}.${relationship.targetEntity};
</#list>
</#if>
import java.time.LocalDate;
import java.util.*;
import java.util.UUID;

public class ${entity.builderClassName} {

<#list entity.attributes as attribute>
<#if attribute.primaryKey && !attribute.generatedValue>
    private ${attribute.javaType} ${attribute.name} = ${generator.generateUniqueValue(attribute)};
<#elseif !attribute.primaryKey && attribute.unique>
    private ${attribute.javaType} ${attribute.name} = ${generator.generateUniqueValue(attribute)};
<#elseif !attribute.primaryKey>
    private ${attribute.javaType} ${attribute.name} = ${generator.generateValidValue(attribute)};
</#if>
</#list>
<#list entity.relationships as relationship>
<#if relationship.collection>
    private ${relationship.javaType} ${relationship.name} = ${relationship.collectionInitializer};
<#else>
    private ${relationship.javaType} ${relationship.name};
</#if>
</#list>

    private ${entity.builderClassName}() {}

    private static String generateUniqueEmail() {
        String[] names = {"oussama", "hicham", "ilyass", "mohammed", "youssef", "hassan"};
        String[] domains = {"example.com", "test.com", "demo.org"};
        java.util.Random random = new java.util.Random();
        String name = names[random.nextInt(names.length)];
        String domain = domains[random.nextInt(domains.length)];
        return name + System.currentTimeMillis() + random.nextInt(1000) + "@" + domain;
    }

    public static ${entity.builderClassName} a${entity.name}() {
        return new ${entity.builderClassName}();
    }

    public static ${entity.builderClassName} aValid${entity.name}() {
        return new ${entity.builderClassName}()
<#list entity.nonPrimaryKeyAttributes as attribute>
<#if attribute.unique>
                .with${attribute.nameCapitalized}(${generator.generateUniqueValue(attribute)})
<#else>
                .with${attribute.nameCapitalized}(${generator.generateValidValue(attribute)})
</#if>
</#list><#list entity.requiredRelationships as relationship>
<#if !relationship.collection>
                .with${relationship.nameCapitalized}(${relationship.targetEntity}TestDataBuilder.aDefault${relationship.targetEntity}().build())
</#if>
</#list>;
    }

    public static ${entity.builderClassName} aDefault${entity.name}() {
        return new ${entity.builderClassName}()
<#list entity.requiredAttributes as attribute>
                .with${attribute.nameCapitalized}(${generator.generateValidValue(attribute)})
</#list><#list entity.uniqueAttributes as attribute>
<#if !attribute.primaryKey && attribute.nullable>
                .with${attribute.nameCapitalized}(${generator.generateUniqueValue(attribute)})
</#if>
</#list><#list entity.requiredRelationships as relationship>
<#if !relationship.collection>
                .with${relationship.nameCapitalized}(${relationship.targetEntity}TestDataBuilder.aDefault${relationship.targetEntity}().build())
</#if>
</#list>;
    }

    public static ${entity.builderClassName} aMinimal${entity.name}() {
        return new ${entity.builderClassName}()
<#list entity.requiredAttributes as attribute>
                .with${attribute.nameCapitalized}(${generator.generateValidValue(attribute)})
</#list><#list entity.requiredRelationships as relationship>
<#if !relationship.collection>
                .with${relationship.nameCapitalized}(${relationship.targetEntity}TestDataBuilder.aDefault${relationship.targetEntity}().build())
</#if>
</#list>;
    }

    public static ${entity.builderClassName} aComplete${entity.name}() {
        return new ${entity.builderClassName}()
<#list entity.attributes as attribute>
<#if !attribute.primaryKey>
                .with${attribute.nameCapitalized}(${generator.generateValidValue(attribute)})
</#if>
</#list>;
    }

<#list entity.attributes as attribute>
<#if attribute.primaryKey && !attribute.generatedValue>
    public ${entity.builderClassName} with${attribute.nameCapitalized}(${attribute.javaType} ${attribute.name}) {
        this.${attribute.name} = ${attribute.name};
        return this;
    }

<#elseif !attribute.primaryKey>
    public ${entity.builderClassName} with${attribute.nameCapitalized}(${attribute.javaType} ${attribute.name}) {
        this.${attribute.name} = ${attribute.name};
        return this;
    }

</#if>
</#list>
<#list entity.relationships as relationship>
    public ${entity.builderClassName} with${relationship.nameCapitalized}(${relationship.javaType} ${relationship.name}) {
        this.${relationship.name} = ${relationship.name};
        return this;
    }

    public ${entity.builderClassName} withNull${relationship.nameCapitalized}() {
        this.${relationship.name} = null;
        return this;
    }

<#if relationship.collection>
    public ${entity.builderClassName} addTo${relationship.nameCapitalized}(${relationship.targetEntity} ${relationship.targetEntity?uncap_first}) {
        if (this.${relationship.name} == null) {
            this.${relationship.name} = ${relationship.collectionInitializer};
        }
        this.${relationship.name}.add(${relationship.targetEntity?uncap_first});
        return this;
    }

</#if>
</#list>
<#list entity.stringAttributes as attribute>
    public ${entity.builderClassName} withEmpty${attribute.nameCapitalized}() {
        this.${attribute.name} = "";
        return this;
    }

    public ${entity.builderClassName} withNull${attribute.nameCapitalized}() {
        this.${attribute.name} = null;
        return this;
    }

<#if attribute.maxLength??>
    public ${entity.builderClassName} withTooLong${attribute.nameCapitalized}() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= ${attribute.maxLength?c}; i++) {
            sb.append("a");
        }
        this.${attribute.name} = sb.toString();
        return this;
    }

</#if>
<#if attribute.minLength??>
    public ${entity.builderClassName} withTooShort${attribute.nameCapitalized}() {
        this.${attribute.name} = "a";
        return this;
    }

</#if>
<#if attribute.unique>
    public ${entity.builderClassName} withUnique${attribute.nameCapitalized}() {
        this.${attribute.name} = ${generator.generateUniqueValue(attribute)};
        return this;
    }

</#if>
</#list>
<#list entity.numericAttributes as attribute>
<#if attribute.minValue??>
    public ${entity.builderClassName} withTooSmall${attribute.nameCapitalized}() {
        this.${attribute.name} = ${(attribute.minValue - 1)?c};
        return this;
    }

</#if>
<#if attribute.maxValue??>
    public ${entity.builderClassName} withTooLarge${attribute.nameCapitalized}() {
        this.${attribute.name} = ${(attribute.maxValue + 1)?c};
        return this;
    }

</#if>
</#list>

<#-- Add withNull methods for all nullable non-string attributes -->
<#list entity.attributes as attribute>
<#if attribute.nullable && !attribute.isStringType() && !attribute.primaryKey>
    public ${entity.builderClassName} withNull${attribute.nameCapitalized}() {
        this.${attribute.name} = null;
        return this;
    }

</#if>
</#list>

<#list entity.enumAttributes as attribute>
    public ${entity.builderClassName} withNull${attribute.nameCapitalized}() {
        this.${attribute.name} = null;
        return this;
    }

<#if attribute.enumValues?has_content>
<#list attribute.enumValues as enumValue>
    public ${entity.builderClassName} with${attribute.nameCapitalized}${enumValue}() {
        this.${attribute.name} = ${attribute.enumType}.${enumValue};
        return this;
    }

</#list>
    public ${entity.builderClassName} withInvalid${attribute.nameCapitalized}() {
        this.${attribute.name} = null;
        return this;
    }

</#if>
</#list>

    public ${entity.builderClassName} copy() {
        ${entity.builderClassName} copy = new ${entity.builderClassName}();
<#list entity.attributes as attribute>
<#if attribute.primaryKey && !attribute.generatedValue>
        copy.${attribute.name} = this.${attribute.name};
<#elseif !attribute.primaryKey>
        copy.${attribute.name} = this.${attribute.name};
</#if>
</#list>
<#list entity.relationships as relationship>
        copy.${relationship.name} = this.${relationship.name};
</#list>
        return copy;
    }

    public ${entity.name} build() {
        ${entity.name} ${entity.nameLowerCase} = new ${entity.name}();
<#list entity.attributes as attribute>
<#if attribute.primaryKey && !attribute.generatedValue>
        ${entity.nameLowerCase}.${attribute.setterName}(${attribute.name});
<#elseif !attribute.primaryKey>
        ${entity.nameLowerCase}.${attribute.setterName}(${attribute.name});
</#if>
</#list>

<#list entity.relationships as relationship>
        if (${relationship.name} != null) {
<#if relationship.collection>
            ${entity.nameLowerCase}.set${relationship.nameCapitalized}(new ArrayList<>(${relationship.name}));
<#if relationship.owner>
            for (${relationship.targetEntity} ${relationship.targetEntity?uncap_first} : ${entity.nameLowerCase}.get${relationship.nameCapitalized}()) {
                ${relationship.targetEntity?uncap_first}.set${entity.nameCapitalized}(${entity.nameLowerCase});
            }
</#if>
<#else>
            ${entity.nameLowerCase}.set${relationship.nameCapitalized}(${relationship.name});
</#if>
        }
</#list>

        return ${entity.nameLowerCase};
    }

    public ${entity.name} buildAndPersist() {
        ${entity.name} entity = build();
        return entity;
    }
}
