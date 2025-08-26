    @Nested
    @DisplayName("Relationship Tests")
    class RelationshipTests {

<#list entity.relationships as relationship>
<#if !relationship.collection && !relationship.optional>
        @Test
        void create${entity.name}WithValid${relationship.nameCapitalized}ShouldSucceed() {
            ${entity.name} test${entity.name} = create${entity.name}();
            ${relationship.targetEntity} test${relationship.targetEntity} = create${relationship.targetEntity}();
<#if relationship.targetEntity == "User">
            test${relationship.targetEntity}.setId(1L);
<#elseif relationship.targetEntity == "Post">
            test${relationship.targetEntity}.setId(1L);
<#else>
            test${relationship.targetEntity}.setId(1L);
</#if>
            test${entity.name}.set${relationship.nameCapitalized}(test${relationship.targetEntity});

            ${entity.name} saved${entity.name} = create${entity.name}();
            saved${entity.name}.set${relationship.nameCapitalized}(test${relationship.targetEntity});
<#if entity.primaryKeyAttribute.generatedValue>
            saved${entity.name}.${entity.primaryKeyAttribute.setterName}(1L);
</#if>

            when(${entity.variableName}Repository.save(any(${entity.name}.class))).thenReturn(saved${entity.name});

            ${entity.name} result = ${entity.variableName}Service.create${entity.name}(test${entity.name});

            assertThat(result).isNotNull();
            assertThat(result.get${relationship.nameCapitalized}()).isNotNull();
            verify(${entity.variableName}Repository).save(test${entity.name});
        }



        @Test
        void create${entity.name}WithNull${relationship.nameCapitalized}ShouldThrowException() {
            ${entity.name} test${entity.name} = create${entity.name}();
            test${entity.name}.set${relationship.nameCapitalized}(null);

            when(${entity.variableName}Repository.save(any(${entity.name}.class)))
                    .thenThrow(new ConstraintViolationException("${relationship.name} cannot be null", null));

            assertThatThrownBy(() -> ${entity.variableName}Service.create${entity.name}(test${entity.name}))
                    .isInstanceOf(ConstraintViolationException.class);

            verify(${entity.variableName}Repository).save(test${entity.name});
        }



<#elseif !relationship.collection && relationship.optional>
        @Test
        void create${entity.name}WithOptional${relationship.nameCapitalized}ShouldSucceed() {
            ${entity.name} test${entity.name} = create${entity.name}();
            ${relationship.targetEntity} test${relationship.targetEntity} = create${relationship.targetEntity}();
            test${relationship.targetEntity}.setId(1L);
            test${entity.name}.set${relationship.nameCapitalized}(test${relationship.targetEntity});

            ${entity.name} saved${entity.name} = create${entity.name}();
            saved${entity.name}.set${relationship.nameCapitalized}(test${relationship.targetEntity});
<#if entity.primaryKeyAttribute.generatedValue>
            saved${entity.name}.${entity.primaryKeyAttribute.setterName}(1L);
</#if>

            when(${entity.variableName}Repository.save(any(${entity.name}.class))).thenReturn(saved${entity.name});

            ${entity.name} result = ${entity.variableName}Service.create${entity.name}(test${entity.name});

            assertThat(result).isNotNull();
            assertThat(result.get${relationship.nameCapitalized}()).isNotNull();
            verify(${entity.variableName}Repository).save(test${entity.name});
        }

        @Test
        void create${entity.name}WithoutOptional${relationship.nameCapitalized}ShouldSucceed() {
            ${entity.name} test${entity.name} = create${entity.name}();
            test${entity.name}.set${relationship.nameCapitalized}(null);

            ${entity.name} saved${entity.name} = create${entity.name}();
            saved${entity.name}.set${relationship.nameCapitalized}(null);
<#if entity.primaryKeyAttribute.generatedValue>
            saved${entity.name}.${entity.primaryKeyAttribute.setterName}(1L);
</#if>

            when(${entity.variableName}Repository.save(any(${entity.name}.class))).thenReturn(saved${entity.name});

            ${entity.name} result = ${entity.variableName}Service.create${entity.name}(test${entity.name});

            assertThat(result).isNotNull();
            assertThat(result.get${relationship.nameCapitalized}()).isNull();
            verify(${entity.variableName}Repository).save(test${entity.name});
        }

<#elseif relationship.collection>
        @Test
        void create${entity.name}WithMultiple${relationship.nameCapitalized}ShouldSucceed() {
            ${entity.name} test${entity.name} = create${entity.name}();
            ${relationship.targetEntity} ${relationship.targetEntity?uncap_first}1 = create${relationship.targetEntity}();
            ${relationship.targetEntity} ${relationship.targetEntity?uncap_first}2 = create${relationship.targetEntity}();
            ${relationship.targetEntity?uncap_first}1.setId(1L);
            ${relationship.targetEntity?uncap_first}2.setId(2L);

            List<${relationship.targetEntity}> ${relationship.name}List = List.of(${relationship.targetEntity?uncap_first}1, ${relationship.targetEntity?uncap_first}2);
            test${entity.name}.set${relationship.nameCapitalized}(${relationship.name}List);

            ${entity.name} saved${entity.name} = create${entity.name}();
            saved${entity.name}.set${relationship.nameCapitalized}(${relationship.name}List);
<#if entity.primaryKeyAttribute.generatedValue>
            saved${entity.name}.${entity.primaryKeyAttribute.setterName}(1L);
</#if>

            when(${entity.variableName}Repository.save(any(${entity.name}.class))).thenReturn(saved${entity.name});

            ${entity.name} result = ${entity.variableName}Service.create${entity.name}(test${entity.name});

            assertThat(result).isNotNull();
            assertThat(result.get${relationship.nameCapitalized}()).hasSize(2);
            verify(${entity.variableName}Repository).save(test${entity.name});
        }

        @Test
        void create${entity.name}WithEmpty${relationship.nameCapitalized}ShouldSucceed() {
            ${entity.name} test${entity.name} = create${entity.name}();
            test${entity.name}.set${relationship.nameCapitalized}(Collections.emptyList());

            ${entity.name} saved${entity.name} = create${entity.name}();
            saved${entity.name}.set${relationship.nameCapitalized}(Collections.emptyList());
<#if entity.primaryKeyAttribute.generatedValue>
            saved${entity.name}.${entity.primaryKeyAttribute.setterName}(1L);
</#if>

            when(${entity.variableName}Repository.save(any(${entity.name}.class))).thenReturn(saved${entity.name});

            ${entity.name} result = ${entity.variableName}Service.create${entity.name}(test${entity.name});

            assertThat(result).isNotNull();
            assertThat(result.get${relationship.nameCapitalized}()).isEmpty();
            verify(${entity.variableName}Repository).save(test${entity.name});
        }

</#if>
</#list>
    }

    @Nested
    @DisplayName("Relationship Update Tests")
    class RelationshipUpdateTests {

<#list entity.relationships as relationship>
<#if !relationship.collection>
        @Test
        @DisplayName("Should update ${entity.name} with new ${relationship.targetEntity}")
        void update${entity.name}WithNew${relationship.targetEntity}ShouldWork() {
<#if entity.primaryKeyAttribute.javaType == "String">
            String ${entity.variableName}Id = "CREATED_ID";
<#elseif entity.primaryKeyAttribute.javaType == "Long">
            Long ${entity.variableName}Id = 1L;
<#elseif entity.primaryKeyAttribute.javaType == "Integer">
            Integer ${entity.variableName}Id = 1;
<#else>
            Object ${entity.variableName}Id = 1L;
</#if>
            ${entity.name} existing${entity.name} = create${entity.name}();
            existing${entity.name}.${entity.primaryKeyAttribute.setterName}(${entity.variableName}Id);

            ${relationship.targetEntity} new${relationship.targetEntity} = create${relationship.targetEntity}();
            ${entity.name} updated${entity.name} = create${entity.name}();
            updated${entity.name}.${entity.primaryKeyAttribute.setterName}(${entity.variableName}Id);
            updated${entity.name}.set${relationship.nameCapitalized}(new${relationship.targetEntity});

            when(${entity.variableName}Repository.findById(${entity.variableName}Id)).thenReturn(Optional.of(existing${entity.name}));
            when(${entity.variableName}Repository.save(any(${entity.name}.class))).thenReturn(updated${entity.name});

            ${entity.name} result = ${entity.variableName}Service.update${entity.name}(${entity.variableName}Id, updated${entity.name});

            assertThat(result).isNotNull();
            assertThat(result.get${relationship.nameCapitalized}()).isEqualTo(new${relationship.targetEntity});
            verify(${entity.variableName}Repository).findById(${entity.variableName}Id);
            verify(${entity.variableName}Repository).save(any(${entity.name}.class));
        }

        @Test
        @DisplayName("Should handle null ${relationship.targetEntity} update")
        void update${entity.name}WithNull${relationship.targetEntity}ShouldWork() {
<#if entity.primaryKeyAttribute.javaType == "String">
            String ${entity.variableName}Id = "CREATED_ID";
<#elseif entity.primaryKeyAttribute.javaType == "Long">
            Long ${entity.variableName}Id = 1L;
<#elseif entity.primaryKeyAttribute.javaType == "Integer">
            Integer ${entity.variableName}Id = 1;
<#else>
            Object ${entity.variableName}Id = 1L;
</#if>
            ${entity.name} existing${entity.name} = create${entity.name}();
            existing${entity.name}.${entity.primaryKeyAttribute.setterName}(${entity.variableName}Id);
            existing${entity.name}.set${relationship.nameCapitalized}(create${relationship.targetEntity}());

            ${entity.name} updated${entity.name} = create${entity.name}();
            updated${entity.name}.${entity.primaryKeyAttribute.setterName}(${entity.variableName}Id);
            updated${entity.name}.set${relationship.nameCapitalized}(null);

            when(${entity.variableName}Repository.findById(${entity.variableName}Id)).thenReturn(Optional.of(existing${entity.name}));
            when(${entity.variableName}Repository.save(any(${entity.name}.class))).thenReturn(updated${entity.name});

            ${entity.name} result = ${entity.variableName}Service.update${entity.name}(${entity.variableName}Id, updated${entity.name});

            assertThat(result).isNotNull();
            assertThat(result.get${relationship.nameCapitalized}()).isNull();
            verify(${entity.variableName}Repository).findById(${entity.variableName}Id);
            verify(${entity.variableName}Repository).save(any(${entity.name}.class));
        }

<#else>
        @Test
        @DisplayName("Should update ${entity.name} with new ${relationship.name}")
        void update${entity.name}WithNew${relationship.nameCapitalized}ShouldWork() {
<#if entity.primaryKeyAttribute.javaType == "String">
            String ${entity.variableName}Id = "CREATED_ID";
<#elseif entity.primaryKeyAttribute.javaType == "Long">
            Long ${entity.variableName}Id = 1L;
<#elseif entity.primaryKeyAttribute.javaType == "Integer">
            Integer ${entity.variableName}Id = 1;
<#else>
            Object ${entity.variableName}Id = 1L;
</#if>
            ${entity.name} existing${entity.name} = create${entity.name}();
            existing${entity.name}.${entity.primaryKeyAttribute.setterName}(${entity.variableName}Id);

            ${relationship.targetEntity} new${relationship.targetEntity} = create${relationship.targetEntity}();
            ${entity.name} updated${entity.name} = create${entity.name}();
            updated${entity.name}.${entity.primaryKeyAttribute.setterName}(${entity.variableName}Id);
            updated${entity.name}.get${relationship.nameCapitalized}().add(new${relationship.targetEntity});

            when(${entity.variableName}Repository.findById(${entity.variableName}Id)).thenReturn(Optional.of(existing${entity.name}));
            when(${entity.variableName}Repository.save(any(${entity.name}.class))).thenReturn(updated${entity.name});

            ${entity.name} result = ${entity.variableName}Service.update${entity.name}(${entity.variableName}Id, updated${entity.name});

            assertThat(result).isNotNull();
            assertThat(result.get${relationship.nameCapitalized}()).hasSize(1);
            assertThat(result.get${relationship.nameCapitalized}().get(0)).isEqualTo(new${relationship.targetEntity});
            verify(${entity.variableName}Repository).findById(${entity.variableName}Id);
            verify(${entity.variableName}Repository).save(any(${entity.name}.class));
        }

        @Test
        @DisplayName("Should handle clearing ${relationship.name} collection")
        void update${entity.name}WithCleared${relationship.nameCapitalized}ShouldWork() {
<#if entity.primaryKeyAttribute.javaType == "String">
            String ${entity.variableName}Id = "CREATED_ID";
<#elseif entity.primaryKeyAttribute.javaType == "Long">
            Long ${entity.variableName}Id = 1L;
<#elseif entity.primaryKeyAttribute.javaType == "Integer">
            Integer ${entity.variableName}Id = 1;
<#else>
            Object ${entity.variableName}Id = 1L;
</#if>
            ${entity.name} existing${entity.name} = create${entity.name}();
            existing${entity.name}.${entity.primaryKeyAttribute.setterName}(${entity.variableName}Id);
            existing${entity.name}.get${relationship.nameCapitalized}().add(create${relationship.targetEntity}());

            ${entity.name} updated${entity.name} = create${entity.name}();
            updated${entity.name}.${entity.primaryKeyAttribute.setterName}(${entity.variableName}Id);
            updated${entity.name}.get${relationship.nameCapitalized}().clear();

            when(${entity.variableName}Repository.findById(${entity.variableName}Id)).thenReturn(Optional.of(existing${entity.name}));
            when(${entity.variableName}Repository.save(any(${entity.name}.class))).thenReturn(updated${entity.name});

            ${entity.name} result = ${entity.variableName}Service.update${entity.name}(${entity.variableName}Id, updated${entity.name});

            assertThat(result).isNotNull();
            assertThat(result.get${relationship.nameCapitalized}()).isEmpty();
            verify(${entity.variableName}Repository).findById(${entity.variableName}Id);
            verify(${entity.variableName}Repository).save(any(${entity.name}.class));
        }

</#if>
</#list>
    }
