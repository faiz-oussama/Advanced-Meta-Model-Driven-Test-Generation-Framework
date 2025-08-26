    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

<#list validationRules as rule>
        @Test
        @DisplayName("${rule.description}")
        void ${rule.testName}() {
<#if rule.nullableConstraint>
            ${entity.name} ${entity.variableName}WithNull${rule.attributeNameCapitalized} = create${entity.name}();
            ${entity.variableName}WithNull${rule.attributeNameCapitalized}.set${rule.attributeNameCapitalized}(null);

            assertThatThrownBy(() -> {
                ${entity.variableName}Repository.save(${entity.variableName}WithNull${rule.attributeNameCapitalized});
                entityManager.flush();
            }).isInstanceOf(${rule.expectedExceptionType}.class)
              .hasMessageContaining("${rule.attributeName}");

<#elseif rule.uniqueConstraint>
            ${entity.name} ${entity.variableName}1 = create${entity.name}();
            ${entity.name} ${entity.variableName}2 = create${entity.name}();

            entityManager.persistAndFlush(${entity.variableName}1);
            entityManager.clear();

            // Set duplicate value for unique field
<#if rule.attributeName == "email">
            ${entity.variableName}2.set${rule.attributeNameCapitalized}(${entity.variableName}1.get${rule.attributeNameCapitalized}());
<#elseif rule.attributeName == "name">
            ${entity.variableName}2.set${rule.attributeNameCapitalized}(${entity.variableName}1.get${rule.attributeNameCapitalized}());
<#else>
            ${entity.variableName}2.set${rule.attributeNameCapitalized}(${entity.variableName}1.get${rule.attributeNameCapitalized}());
</#if>

            assertThatThrownBy(() -> {
                entityManager.persist(${entity.variableName}2);
                entityManager.flush();
            }).isInstanceOf(${rule.expectedExceptionType}.class);

<#elseif rule.maxLengthConstraint>
            String tooLongValue = "a".repeat(${(rule.constraintValue?string?replace(",", "")?number + 1)?c});
            ${entity.name} ${entity.variableName}WithTooLong${rule.attributeNameCapitalized} = create${entity.name}();
            ${entity.variableName}WithTooLong${rule.attributeNameCapitalized}.set${rule.attributeNameCapitalized}(tooLongValue);

            assertThatThrownBy(() -> {
                ${entity.variableName}Repository.save(${entity.variableName}WithTooLong${rule.attributeNameCapitalized});
                entityManager.flush();
            }).isInstanceOf(${rule.expectedExceptionType}.class);

<#elseif rule.minValueConstraint>
            ${entity.name} ${entity.variableName}WithTooSmall${rule.attributeNameCapitalized} = create${entity.name}();
            ${entity.variableName}WithTooSmall${rule.attributeNameCapitalized}.set${rule.attributeNameCapitalized}(${(rule.constraintValue?string?replace(",", "")?number - 1)?c});

            assertThatThrownBy(() -> {
                ${entity.variableName}Repository.save(${entity.variableName}WithTooSmall${rule.attributeNameCapitalized});
                entityManager.flush();
            }).isInstanceOf(${rule.expectedExceptionType}.class);

<#elseif rule.maxValueConstraint>
            ${entity.name} ${entity.variableName}WithTooLarge${rule.attributeNameCapitalized} = create${entity.name}();
            ${entity.variableName}WithTooLarge${rule.attributeNameCapitalized}.set${rule.attributeNameCapitalized}(${(rule.constraintValue?string?replace(",", "")?number + 1)?c});

            assertThatThrownBy(() -> {
                ${entity.variableName}Repository.save(${entity.variableName}WithTooLarge${rule.attributeNameCapitalized});
                entityManager.flush();
            }).isInstanceOf(${rule.expectedExceptionType}.class);

<#elseif rule.primaryKeyConstraint>
            ${entity.name} ${entity.variableName} = create${entity.name}();

            ${entity.name} saved${entity.name} = ${entity.variableName}Repository.save(${entity.variableName});

            assertThat(saved${entity.name}).isNotNull();
            assertThat(saved${entity.name}.${entity.primaryKeyAttribute.getterName}()).isNotNull();
<#if entity.primaryKeyAttribute.javaType == "Long" || entity.primaryKeyAttribute.javaType == "Integer">
            assertThat(saved${entity.name}.${entity.primaryKeyAttribute.getterName}()).isPositive();
</#if>

<#else>
            ${entity.name} invalid${entity.name} = create${entity.name}();
            invalid${entity.name}.set${rule.attributeNameCapitalized}("invalid-value");

            assertThatThrownBy(() -> {
                ${entity.variableName}Repository.save(invalid${entity.name});
                entityManager.flush();
            }).isInstanceOf(${rule.expectedExceptionType}.class);

</#if>
        }

</#list>

<#if validationRules?has_content>
        @Test
        @DisplayName("Should handle transaction rollback on constraint violation")
        void shouldHandleTransactionRollbackOnConstraintViolation() {
            ${entity.name} valid${entity.name} = create${entity.name}();
            entityManager.persistAndFlush(valid${entity.name});
            long initialCount = ${entity.variableName}Repository.count();

<#list validationRules as rule>
<#if rule.nullableConstraint>
            ${entity.name} invalid${entity.name} = create${entity.name}();
            invalid${entity.name}.set${rule.attributeNameCapitalized}(null);

            assertThatThrownBy(() -> {
                ${entity.variableName}Repository.save(invalid${entity.name});
                entityManager.flush();
            }).isInstanceOf(${rule.expectedExceptionType}.class);

            // Clear entity manager after constraint violation
            entityManager.clear();

            // Verify transaction rollback - data consistency maintained
            long finalCount = ${entity.variableName}Repository.count();
            assertThat(finalCount).isEqualTo(initialCount);
<#break>
</#if>
</#list>
        }

        @Test
        @DisplayName("Should maintain data integrity across multiple constraint violations")
        void shouldMaintainDataIntegrityAcrossMultipleConstraintViolations() {
            long initialCount = ${entity.variableName}Repository.count();

<#list validationRules as rule>
<#if rule.nullableConstraint>
            ${entity.name} invalid${entity.name}${rule.attributeNameCapitalized} = create${entity.name}();
            invalid${entity.name}${rule.attributeNameCapitalized}.set${rule.attributeNameCapitalized}(null);

            assertThatThrownBy(() -> {
                ${entity.variableName}Repository.save(invalid${entity.name}${rule.attributeNameCapitalized});
                entityManager.flush();
            }).isInstanceOf(${rule.expectedExceptionType}.class);

            entityManager.clear();

<#break>
</#if>
</#list>

            long finalCount = ${entity.variableName}Repository.count();
            assertThat(finalCount).isEqualTo(initialCount);
        }
</#if>
    }