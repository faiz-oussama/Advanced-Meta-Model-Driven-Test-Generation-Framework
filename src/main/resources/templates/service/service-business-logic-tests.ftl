    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Should throw exception when creating ${entity.name} with null entity")
        void create${entity.name}WithNullShouldThrowException() {
            assertThatThrownBy(() -> ${entity.variableName}Service.create${entity.name}(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("${entity.name} cannot be null");

            verifyNoInteractions(${entity.variableName}Repository);
        }

        @Test
        @DisplayName("Should throw exception when updating ${entity.name} with null entity")
        void update${entity.name}WithNullShouldThrowException() {
<#if entity.primaryKeyAttribute.javaType == "String">
            String testId = "CREATED_ID";
<#elseif entity.primaryKeyAttribute.javaType == "Long">
            Long testId = 1L;
<#elseif entity.primaryKeyAttribute.javaType == "Integer">
            Integer testId = 1;
<#else>
            Object testId = 1L;
</#if>

            assertThatThrownBy(() -> ${entity.variableName}Service.update${entity.name}(testId, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("${entity.name} cannot be null");

            verifyNoInteractions(${entity.variableName}Repository);
        }

        @Test
        @DisplayName("Should throw exception when updating ${entity.name} with null ID")
        void update${entity.name}WithNullIdShouldThrowException() {
            ${entity.name} test${entity.name} = create${entity.name}();

            assertThatThrownBy(() -> ${entity.variableName}Service.update${entity.name}(null, test${entity.name}))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("${entity.primaryKeyAttribute.name?upper_case} cannot be null");

            verifyNoInteractions(${entity.variableName}Repository);
        }

        @Test
        @DisplayName("Should throw exception when deleting ${entity.name} with null ID")
        void delete${entity.name}WithNullIdShouldThrowException() {
            assertThatThrownBy(() -> ${entity.variableName}Service.delete${entity.name}(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("${entity.primaryKeyAttribute.name?upper_case} cannot be null");

            verifyNoInteractions(${entity.variableName}Repository);
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent ${entity.name}")
        void update${entity.name}WithNonExistentIdShouldThrowException() {
<#if entity.primaryKeyAttribute.javaType == "String">
            String nonExistentId = "NON_EXISTENT_ID";
<#elseif entity.primaryKeyAttribute.javaType == "Long">
            Long nonExistentId = 999L;
<#elseif entity.primaryKeyAttribute.javaType == "Integer">
            Integer nonExistentId = 999;
<#else>
            Object nonExistentId = 999L;
</#if>
            ${entity.name} test${entity.name} = create${entity.name}();

            when(${entity.variableName}Repository.findById(nonExistentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ${entity.variableName}Service.update${entity.name}(nonExistentId, test${entity.name}))
                    .isInstanceOf(EntityNotFoundException.class)
<#if entity.primaryKeyAttribute.name == "id">
                    .hasMessageContaining("${entity.name} not found with id: " + nonExistentId);
<#else>
                    .hasMessageContaining("${entity.name} not found with ${entity.primaryKeyAttribute.name?upper_case}: " + nonExistentId);
</#if>

            verify(${entity.variableName}Repository).findById(nonExistentId);
            verify(${entity.variableName}Repository, never()).save(any(${entity.name}.class));
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent ${entity.name}")
        void delete${entity.name}WithNonExistentIdShouldThrowException() {
<#if entity.primaryKeyAttribute.javaType == "String">
            String nonExistentId = "NON_EXISTENT_ID";
<#elseif entity.primaryKeyAttribute.javaType == "Long">
            Long nonExistentId = 999L;
<#elseif entity.primaryKeyAttribute.javaType == "Integer">
            Integer nonExistentId = 999;
<#else>
            Object nonExistentId = 999L;
</#if>

            when(${entity.variableName}Repository.existsById(nonExistentId)).thenReturn(false);

            assertThatThrownBy(() -> ${entity.variableName}Service.delete${entity.name}(nonExistentId))
                    .isInstanceOf(EntityNotFoundException.class)
<#if entity.primaryKeyAttribute.name == "id">
                    .hasMessageContaining("${entity.name} not found with id: " + nonExistentId);
<#else>
                    .hasMessageContaining("${entity.name} not found with ${entity.primaryKeyAttribute.name?upper_case}: " + nonExistentId);
</#if>

            verify(${entity.variableName}Repository).existsById(nonExistentId);
            verify(${entity.variableName}Repository, never()).deleteById(any());
        }

        @Test
        @DisplayName("Should handle repository exception gracefully when finding ${entity.name}")
        void find${entity.name}ByIdShouldHandleRepositoryException() {
<#if entity.primaryKeyAttribute.javaType == "String">
            String testId = "CREATED_ID";
<#elseif entity.primaryKeyAttribute.javaType == "Long">
            Long testId = 1L;
<#elseif entity.primaryKeyAttribute.javaType == "Integer">
            Integer testId = 1;
<#else>
            Object testId = 1L;
</#if>

            when(${entity.variableName}Repository.findById(testId))
                    .thenThrow(new RuntimeException("Database connection error"));

            assertThatThrownBy(() -> ${entity.variableName}Service.get${entity.name}ById(testId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Database connection error");

            verify(${entity.variableName}Repository).findById(testId);
        }

        @Test
        @DisplayName("Should validate business rules before saving ${entity.name}")
        void create${entity.name}ShouldValidateBusinessRules() {
            ${entity.name} test${entity.name} = create${entity.name}();
            ${entity.name} saved${entity.name} = create${entity.name}();
<#if entity.primaryKeyAttribute.generatedValue>
            saved${entity.name}.${entity.primaryKeyAttribute.setterName}(1L);
</#if>

            when(${entity.variableName}Repository.save(test${entity.name})).thenReturn(saved${entity.name});

            ${entity.name} result = ${entity.variableName}Service.create${entity.name}(test${entity.name});

            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(saved${entity.name});
            verify(${entity.variableName}Repository).save(test${entity.name});
        }

        @Test
        @DisplayName("Should check existence before operations")
        void exists${entity.name}ByIdShouldReturnCorrectValue() {
<#if entity.primaryKeyAttribute.javaType == "String">
            String existingId = "EXISTING_ID";
            String nonExistentId = "NON_EXISTENT_ID";
<#elseif entity.primaryKeyAttribute.javaType == "Long">
            Long existingId = 1L;
            Long nonExistentId = 999L;
<#elseif entity.primaryKeyAttribute.javaType == "Integer">
            Integer existingId = 1;
            Integer nonExistentId = 999;
<#else>
            Object existingId = 1L;
            Object nonExistentId = 999L;
</#if>

            when(${entity.variableName}Repository.existsById(existingId)).thenReturn(true);
            when(${entity.variableName}Repository.existsById(nonExistentId)).thenReturn(false);

            boolean existsResult = ${entity.variableName}Service.exists${entity.name}ById(existingId);
            boolean notExistsResult = ${entity.variableName}Service.exists${entity.name}ById(nonExistentId);

            assertThat(existsResult).isTrue();
            assertThat(notExistsResult).isFalse();
            verify(${entity.variableName}Repository).existsById(existingId);
            verify(${entity.variableName}Repository).existsById(nonExistentId);
        }

        @Test
        @DisplayName("Should handle null ID gracefully in exists check")
        void exists${entity.name}ByIdWithNullShouldReturnFalse() {
            boolean result = ${entity.variableName}Service.exists${entity.name}ById(null);

            assertThat(result).isFalse();
            verifyNoInteractions(${entity.variableName}Repository);
        }

        @Test
        @DisplayName("Should handle validation errors from JPA/Hibernate")
        void create${entity.name}ShouldHandleConstraintViolationException() {
            ${entity.name} invalid${entity.name} = create${entity.name}();
<#list entity.attributes as attr>
<#if attr.nullable == false && !attr.primaryKey>
            invalid${entity.name}.${attr.setterName}(null);
<#break>
</#if>
</#list>

            when(${entity.variableName}Repository.save(invalid${entity.name}))
                    .thenThrow(new ConstraintViolationException("Validation failed", null));

            assertThatThrownBy(() -> ${entity.variableName}Service.create${entity.name}(invalid${entity.name}))
                    .isInstanceOf(ConstraintViolationException.class)
                    .hasMessageContaining("Validation failed");

            verify(${entity.variableName}Repository).save(invalid${entity.name});
        }

        @Test
        @DisplayName("Should handle data integrity violations")
        void create${entity.name}ShouldHandleDataIntegrityViolationException() {
            ${entity.name} duplicate${entity.name} = create${entity.name}();
<#list entity.attributes as attr>
<#if attr.unique == true>
            duplicate${entity.name}.${attr.setterName}("duplicate@example.com");
<#break>
</#if>
</#list>

            when(${entity.variableName}Repository.save(duplicate${entity.name}))
                    .thenThrow(new DataIntegrityViolationException("Duplicate constraint violation"));

            assertThatThrownBy(() -> ${entity.variableName}Service.create${entity.name}(duplicate${entity.name}))
                    .isInstanceOf(DataIntegrityViolationException.class)
                    .hasMessageContaining("Duplicate constraint violation");

            verify(${entity.variableName}Repository).save(duplicate${entity.name});
        }
    }
