    @Nested
    @DisplayName("CRUD Tests")
    class CrudTests {

        @Test
        void create${entity.name}ShouldSaveAndReturnEntity() {
            ${entity.name} test${entity.name} = create${entity.name}();
            ${entity.name} saved${entity.name} = create${entity.name}();
<#if entity.primaryKeyAttribute.generatedValue>
            saved${entity.name}.${entity.primaryKeyAttribute.setterName}(1L);
</#if>

            when(${entity.variableName}Repository.save(any(${entity.name}.class))).thenReturn(saved${entity.name});

            ${entity.name} result = ${entity.variableName}Service.create${entity.name}(test${entity.name});

            assertThat(result).isNotNull();
<#if entity.primaryKeyAttribute.generatedValue>
            assertThat(result.${entity.primaryKeyAttribute.getterName}()).isNotNull();
</#if>
            verify(${entity.variableName}Repository).save(test${entity.name});
        }

        @Test
        void create${entity.name}WithNullShouldThrowException() {
            assertThatThrownBy(() -> ${entity.variableName}Service.create${entity.name}(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void get${entity.name}ByIdShouldReturnEntityWhenExists() {
<#if entity.primaryKeyAttribute.javaType == "String">
            String testId = "CREATED_ID";
<#elseif entity.primaryKeyAttribute.javaType == "Long">
            Long testId = 1L;
<#elseif entity.primaryKeyAttribute.javaType == "Integer">
            Integer testId = 1;
<#else>
            Object testId = 1L;
</#if>
            ${entity.name} test${entity.name} = create${entity.name}();
            test${entity.name}.${entity.primaryKeyAttribute.setterName}(testId);

            when(${entity.variableName}Repository.findById(testId)).thenReturn(Optional.of(test${entity.name}));

            ${entity.name} result = ${entity.variableName}Service.get${entity.name}ById(testId);

            assertThat(result).isNotNull();
            assertThat(result.${entity.primaryKeyAttribute.getterName}()).isEqualTo(testId);
            verify(${entity.variableName}Repository).findById(testId);
        }

        @Test
        void get${entity.name}ByIdShouldThrowExceptionWhenNotFound() {
<#if entity.primaryKeyAttribute.javaType == "String">
            String testId = "NON_EXISTENT_ID";
<#elseif entity.primaryKeyAttribute.javaType == "Long">
            Long testId = 999L;
<#elseif entity.primaryKeyAttribute.javaType == "Integer">
            Integer testId = 999;
<#else>
            Object testId = 999L;
</#if>

            when(${entity.variableName}Repository.findById(testId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ${entity.variableName}Service.get${entity.name}ById(testId))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(${entity.variableName}Repository).findById(testId);
        }

        @Test
        void getAll${entity.name}sShouldReturnAllEntities() {
            ${entity.name} ${entity.variableName}1 = create${entity.name}();
            ${entity.name} ${entity.variableName}2 = create${entity.name}();
            List<${entity.name}> expected${entity.name}s = List.of(${entity.variableName}1, ${entity.variableName}2);

            when(${entity.variableName}Repository.findAll()).thenReturn(expected${entity.name}s);

            List<${entity.name}> result = ${entity.variableName}Service.getAll${entity.name}s();

            assertThat(result).hasSize(2);
            assertThat(result).containsExactlyElementsOf(expected${entity.name}s);
            verify(${entity.variableName}Repository).findAll();
        }

        @Test
        void getAll${entity.name}sWhenEmptyShouldReturnEmptyList() {
            when(${entity.variableName}Repository.findAll()).thenReturn(Collections.emptyList());

            List<${entity.name}> result = ${entity.variableName}Service.getAll${entity.name}s();

            assertThat(result).isEmpty();
            verify(${entity.variableName}Repository).findAll();
        }

        @Test
        void update${entity.name}ShouldUpdateAndReturnEntity() {
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
            updated${entity.name}.${entity.primaryKeyAttribute.setterName}(testId);
<#list entity.stringAttributes as attribute>
<#if !attribute.primaryKey>
            updated${entity.name}.${attribute.setterName}("Updated ${attribute.nameCapitalized}");
<#break>
</#if>
</#list>

            when(${entity.variableName}Repository.findById(testId)).thenReturn(Optional.of(existing${entity.name}));
            when(${entity.variableName}Repository.save(any(${entity.name}.class))).thenReturn(updated${entity.name});

            ${entity.name} result = ${entity.variableName}Service.update${entity.name}(testId, updated${entity.name});

            assertThat(result).isNotNull();
            assertThat(result.${entity.primaryKeyAttribute.getterName}()).isEqualTo(testId);
<#list entity.stringAttributes as attribute>
<#if !attribute.primaryKey>
            assertThat(result.${attribute.getterName}()).isEqualTo("Updated ${attribute.nameCapitalized}");
<#break>
</#if>
</#list>
            verify(${entity.variableName}Repository).findById(testId);
            verify(${entity.variableName}Repository).save(any(${entity.name}.class));
        }

        @Test
        void update${entity.name}WithNonExistentIdShouldThrowException() {
<#if entity.primaryKeyAttribute.javaType == "String">
            String testId = "NON_EXISTENT_ID";
<#elseif entity.primaryKeyAttribute.javaType == "Long">
            Long testId = 999L;
<#elseif entity.primaryKeyAttribute.javaType == "Integer">
            Integer testId = 999;
<#else>
            Object testId = 999L;
</#if>
            ${entity.name} updated${entity.name} = create${entity.name}();

            when(${entity.variableName}Repository.findById(testId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ${entity.variableName}Service.update${entity.name}(testId, updated${entity.name}))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(${entity.variableName}Repository).findById(testId);
            verify(${entity.variableName}Repository, never()).save(any(${entity.name}.class));
        }

        @Test
        void delete${entity.name}ByIdShouldDeleteWhenExists() {
<#if entity.primaryKeyAttribute.javaType == "String">
            String testId = "CREATED_ID";
<#elseif entity.primaryKeyAttribute.javaType == "Long">
            Long testId = 1L;
<#elseif entity.primaryKeyAttribute.javaType == "Integer">
            Integer testId = 1;
<#else>
            Object testId = 1L;
</#if>

            when(${entity.variableName}Repository.existsById(testId)).thenReturn(true);
            doNothing().when(${entity.variableName}Repository).deleteById(testId);

            assertThatCode(() -> ${entity.variableName}Service.delete${entity.name}(testId))
                    .doesNotThrowAnyException();

            verify(${entity.variableName}Repository).existsById(testId);
            verify(${entity.variableName}Repository).deleteById(testId);
        }

        @Test
        void delete${entity.name}ByIdShouldThrowExceptionWhenNotFound() {
<#if entity.primaryKeyAttribute.javaType == "String">
            String testId = "NON_EXISTENT_ID";
<#elseif entity.primaryKeyAttribute.javaType == "Long">
            Long testId = 999L;
<#elseif entity.primaryKeyAttribute.javaType == "Integer">
            Integer testId = 999;
<#else>
            Object testId = 999L;
</#if>

            when(${entity.variableName}Repository.existsById(testId)).thenReturn(false);

            assertThatThrownBy(() -> ${entity.variableName}Service.delete${entity.name}(testId))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(${entity.variableName}Repository).existsById(testId);
            verify(${entity.variableName}Repository, never()).deleteById(testId);
        }

        @Test
        void exists${entity.name}ByIdShouldReturnTrueWhenExists() {
<#if entity.primaryKeyAttribute.javaType == "String">
            String testId = "CREATED_ID";
<#elseif entity.primaryKeyAttribute.javaType == "Long">
            Long testId = 1L;
<#elseif entity.primaryKeyAttribute.javaType == "Integer">
            Integer testId = 1;
<#else>
            Object testId = 1L;
</#if>

            when(${entity.variableName}Repository.existsById(testId)).thenReturn(true);

            boolean result = ${entity.variableName}Service.exists${entity.name}ById(testId);

            assertThat(result).isTrue();
            verify(${entity.variableName}Repository).existsById(testId);
        }

        @Test
        void exists${entity.name}ByIdShouldReturnFalseWhenNotExists() {
<#if entity.primaryKeyAttribute.javaType == "String">
            String testId = "NON_EXISTENT_ID";
<#elseif entity.primaryKeyAttribute.javaType == "Long">
            Long testId = 999L;
<#elseif entity.primaryKeyAttribute.javaType == "Integer">
            Integer testId = 999;
<#else>
            Object testId = 999L;
</#if>

            when(${entity.variableName}Repository.existsById(testId)).thenReturn(false);

            boolean result = ${entity.variableName}Service.exists${entity.name}ById(testId);

            assertThat(result).isFalse();
            verify(${entity.variableName}Repository).existsById(testId);
        }

        @Test
        void count${entity.name}sShouldReturnCorrectCount() {
            long expectedCount = 5L;

            when(${entity.variableName}Repository.count()).thenReturn(expectedCount);

            long result = ${entity.variableName}Service.count${entity.name}s();

            assertThat(result).isEqualTo(expectedCount);
            verify(${entity.variableName}Repository).count();
        }
    }
