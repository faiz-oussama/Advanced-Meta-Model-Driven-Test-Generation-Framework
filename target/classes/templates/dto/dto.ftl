package ${entity.packageName}.dto;

<#list imports as import>
import ${import};
</#list>

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ${entity.name}Dto {

<#list entity.attributes as attribute>
<#if !attribute.primaryKey || !attribute.generatedValue>
<#-- Add validation annotations for this attribute -->
<#list validationRules as rule>
<#if rule.attributeName == attribute.name>
    ${rule.annotation}
</#if>
</#list>
    private ${attribute.type} ${attribute.name};

</#if>
</#list>
<#-- Handle relationships -->
<#if entity.hasRelationships()>
<#list entity.relationships as relationship>
<#if relationship.type == "ManyToOne" || relationship.type == "OneToOne">
    private ${relationship.targetEntity}Dto ${relationship.name};

<#elseif relationship.type == "OneToMany" || relationship.type == "ManyToMany">
    private List<${relationship.targetEntity}Dto> ${relationship.name};

</#if>
</#list>
</#if>
}
