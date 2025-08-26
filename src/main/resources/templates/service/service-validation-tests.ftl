    @Nested
    @DisplayName("Service Validation Tests")
    class ServiceValidationTests {

        @Test
        @DisplayName("Should validate entity before saving")
        void create${entity.name}ShouldValidateEntity() {
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
        @DisplayName("Should handle repository exceptions during validation")
        void create${entity.name}ShouldHandleRepositoryExceptions() {
            ${entity.name} test${entity.name} = create${entity.name}();

            when(${entity.variableName}Repository.save(test${entity.name}))
                    .thenThrow(new RuntimeException("Database connection error"));

            assertThatThrownBy(() -> ${entity.variableName}Service.create${entity.name}(test${entity.name}))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Database connection error");

            verify(${entity.variableName}Repository).save(test${entity.name});
        }

        @Test
        @DisplayName("Should validate entity state before update")
        void update${entity.name}ShouldValidateEntityState() {
<#if entity.primaryKeyAttribute.javaType == "String">
            String testId = "CREATED_ID";
<#elseif entity.primaryKeyAttribute.javaType == "Long">
            Long testId = 1L;
<#elseif entity.primaryKeyAttribute.javaType == "Integer">
            Integer testId = 1;
<#else>
            Object testId = 1L;
</#if>
            ${entity.name} existing${entity.name} = create${entity.name}();
            existing${entity.name}.${entity.primaryKeyAttribute.setterName}(testId);

            ${entity.name} updated${entity.name} = create${entity.name}();
            ${entity.name} saved${entity.name} = create${entity.name}();
            saved${entity.name}.${entity.primaryKeyAttribute.setterName}(testId);

            when(${entity.variableName}Repository.findById(testId)).thenReturn(Optional.of(existing${entity.name}));
            when(${entity.variableName}Repository.save(any(${entity.name}.class))).thenReturn(saved${entity.name});

            ${entity.name} result = ${entity.variableName}Service.update${entity.name}(testId, updated${entity.name});

            assertThat(result).isNotNull();
            assertThat(result.${entity.primaryKeyAttribute.getterName}()).isEqualTo(testId);
            verify(${entity.variableName}Repository).findById(testId);
            verify(${entity.variableName}Repository).save(any(${entity.name}.class));
        }
    }
