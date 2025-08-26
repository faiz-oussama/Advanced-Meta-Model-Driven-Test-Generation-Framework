
    @Nested
    @DisplayName("CRUD Tests")
    class CrudTests {

        @Test
        void create${entity.name}ShouldPersistNewEntity() {
            ${entity.name} test${entity.name} = create${entity.name}();
            ${entity.name} created${entity.name} = ${entity.variableName}Repository.save(test${entity.name});

            assertThat(created${entity.name}).isNotNull();
            assertThat(created${entity.name}.${entity.primaryKeyAttribute.getterName}()).isNotNull();
<#if entity.primaryKeyAttribute.javaType == "Long" || entity.primaryKeyAttribute.javaType == "Integer">
            assertThat(created${entity.name}.${entity.primaryKeyAttribute.getterName}()).isPositive();
</#if>

            ${entity.name} found = entityManager.find(${entity.name}.class, created${entity.name}.${entity.primaryKeyAttribute.getterName}());
            assertThat(found).isNotNull();
<#list entity.nonPrimaryKeyAttributes as attribute>
            assertThat(found.${attribute.getterName}()).isEqualTo(test${entity.name}.${attribute.getterName}());
</#list>
        }

        @Test
        void create${entity.name}WithNullShouldThrowException() {
            assertThatThrownBy(() -> ${entity.variableName}Repository.save(null))
                    .isInstanceOf(InvalidDataAccessApiUsageException.class);
        }

        @Test
        void createMultiple${entity.name}sShouldPersistAll() {
            ${entity.name} ${entity.variableName}1 = create${entity.name}();
            ${entity.name} ${entity.variableName}2 = create${entity.name}();
            ${entity.name} ${entity.variableName}3 = create${entity.name}();

            List<${entity.name}> created${entity.name}s = ${entity.variableName}Repository.saveAll(
                    List.of(${entity.variableName}1, ${entity.variableName}2, ${entity.variableName}3));

            assertThat(created${entity.name}s).hasSize(3);
            assertThat(created${entity.name}s).allMatch(${entity.variableName} -> ${entity.variableName}.${entity.primaryKeyAttribute.getterName}() != null);

            long count = ${entity.variableName}Repository.count();
            assertThat(count).isEqualTo(3);
        }

        @Test
        void read${entity.name}ByIdShouldReturnCorrectEntity() {
            ${entity.name} test${entity.name} = create${entity.name}();
            ${entity.name} saved${entity.name} = entityManager.persistAndFlush(test${entity.name});

            Optional<${entity.name}> found = ${entity.variableName}Repository.findById(saved${entity.name}.${entity.primaryKeyAttribute.getterName}());

            assertThat(found).isPresent();
            ${entity.name} retrieved${entity.name} = found.get();
            assertThat(retrieved${entity.name}.${entity.primaryKeyAttribute.getterName}()).isEqualTo(saved${entity.name}.${entity.primaryKeyAttribute.getterName}());
<#list entity.nonPrimaryKeyAttributes as attribute>
            assertThat(retrieved${entity.name}.${attribute.getterName}()).isEqualTo(test${entity.name}.${attribute.getterName}());
</#list>
        }

        @Test
        void read${entity.name}ByNonExistentIdShouldReturnEmpty() {
<#if entity.primaryKeyAttribute.javaType == "String">
            Optional<${entity.name}> found = ${entity.variableName}Repository.findById("NON_EXISTENT_ID");
<#elseif entity.primaryKeyAttribute.javaType == "Long">
            Optional<${entity.name}> found = ${entity.variableName}Repository.findById(999L);
<#elseif entity.primaryKeyAttribute.javaType == "Integer">
            Optional<${entity.name}> found = ${entity.variableName}Repository.findById(999);
<#else>
            Optional<${entity.name}> found = ${entity.variableName}Repository.findById(null);
</#if>

            assertThat(found).isEmpty();
        }

    @Test
    void readAll${entity.name}sShouldReturnAllEntities() {
        ${entity.name} ${entity.variableName}1 = entityManager.persistAndFlush(create${entity.name}());
        ${entity.name} ${entity.variableName}2 = entityManager.persistAndFlush(create${entity.name}());

        List<${entity.name}> all${entity.name}s = ${entity.variableName}Repository.findAll();

            assertThat(all${entity.name}s).hasSize(2);
            assertThat(all${entity.name}s).extracting("${entity.primaryKeyAttribute.name}")
                    .contains(${entity.variableName}1.${entity.primaryKeyAttribute.getterName}(), ${entity.variableName}2.${entity.primaryKeyAttribute.getterName}());
    }

    @Test
    void readAll${entity.name}sWhenEmptyShouldReturnEmptyList() {
        List<${entity.name}> all${entity.name}s = ${entity.variableName}Repository.findAll();

        assertThat(all${entity.name}s).isEmpty();
    }

    @Test
    void update${entity.name}ShouldModifyExistingEntity() {
        ${entity.name} test${entity.name} = create${entity.name}();
        ${entity.name} saved${entity.name} = entityManager.persistAndFlush(test${entity.name});
        entityManager.detach(saved${entity.name});

<#list entity.stringAttributes as attribute>
<#if !attribute.primaryKey>
        saved${entity.name}.${attribute.setterName}("Updated ${attribute.nameCapitalized}");
<#break>
</#if>
</#list>

        ${entity.name} updated${entity.name} = ${entity.variableName}Repository.save(saved${entity.name});

        assertThat(updated${entity.name}).isNotNull();
        assertThat(updated${entity.name}.${entity.primaryKeyAttribute.getterName}()).isEqualTo(saved${entity.name}.${entity.primaryKeyAttribute.getterName}());
<#list entity.stringAttributes as attribute>
<#if !attribute.primaryKey>
        assertThat(updated${entity.name}.${attribute.getterName}()).isEqualTo("Updated ${attribute.nameCapitalized}");
<#break>
</#if>
</#list>

        ${entity.name} found = entityManager.find(${entity.name}.class, saved${entity.name}.${entity.primaryKeyAttribute.getterName}());
<#list entity.stringAttributes as attribute>
<#if !attribute.primaryKey>
        assertThat(found.${attribute.getterName}()).isEqualTo("Updated ${attribute.nameCapitalized}");
<#break>
</#if>
</#list>
    }

    @Test
    void saveNew${entity.name}ShouldCreateEntity() {
        long initialCount = ${entity.variableName}Repository.count();

        ${entity.name} new${entity.name} = create${entity.name}();

        ${entity.name} result = ${entity.variableName}Repository.save(new${entity.name});

        assertThat(result).isNotNull();
        assertThat(result.${entity.primaryKeyAttribute.getterName}()).isNotNull();

        long finalCount = ${entity.variableName}Repository.count();
        assertThat(finalCount).isEqualTo(initialCount + 1);
    }

<#list entity.stringAttributes as attribute>
<#if !attribute.primaryKey>
    @Test
    void updateAll${entity.name}AttributesShouldPersistChanges() {
        ${entity.name} test${entity.name} = create${entity.name}();
        ${entity.name} saved${entity.name} = entityManager.persistAndFlush(test${entity.name});
        entityManager.detach(saved${entity.name});

        saved${entity.name}.${attribute.setterName}("Updated ${attribute.nameCapitalized}");
<#list entity.numericAttributes as numAttr>
<#if !numAttr.primaryKey>
        saved${entity.name}.${numAttr.setterName}(999);
<#break>
</#if>
</#list>

        ${entity.name} updated${entity.name} = ${entity.variableName}Repository.save(saved${entity.name});

        assertThat(updated${entity.name}.${attribute.getterName}()).isEqualTo("Updated ${attribute.nameCapitalized}");
<#list entity.numericAttributes as numAttr>
<#if !numAttr.primaryKey>
        assertThat(updated${entity.name}.${numAttr.getterName}()).isEqualTo(999);
<#break>
</#if>
</#list>
    }
<#break>
</#if>
</#list>

    @Test
    void delete${entity.name}ByIdShouldRemoveEntity() {
        ${entity.name} test${entity.name} = create${entity.name}();
        ${entity.name} saved${entity.name} = entityManager.persistAndFlush(test${entity.name});

        ${entity.variableName}Repository.deleteById(saved${entity.name}.${entity.primaryKeyAttribute.getterName}());

        Optional<${entity.name}> found = ${entity.variableName}Repository.findById(saved${entity.name}.${entity.primaryKeyAttribute.getterName}());
        assertThat(found).isEmpty();

        ${entity.name} entityFound = entityManager.find(${entity.name}.class, saved${entity.name}.${entity.primaryKeyAttribute.getterName}());
        assertThat(entityFound).isNull();
    }

        @Test
        void delete${entity.name}ByNonExistentIdShouldNotThrowException() {
<#if entity.primaryKeyAttribute.javaType == "String">
            assertThatCode(() -> ${entity.variableName}Repository.deleteById("NON_EXISTENT_ID"))
<#elseif entity.primaryKeyAttribute.javaType == "Long">
            assertThatCode(() -> ${entity.variableName}Repository.deleteById(999L))
<#elseif entity.primaryKeyAttribute.javaType == "Integer">
            assertThatCode(() -> ${entity.variableName}Repository.deleteById(999))
<#else>
            assertThatCode(() -> ${entity.variableName}Repository.deleteById(null))
</#if>
                    .doesNotThrowAnyException();
        }

    @Test
    void delete${entity.name}EntityShouldRemoveFromDatabase() {
        ${entity.name} test${entity.name} = create${entity.name}();
        ${entity.name} saved${entity.name} = entityManager.persistAndFlush(test${entity.name});

        ${entity.variableName}Repository.delete(saved${entity.name});

        Optional<${entity.name}> found = ${entity.variableName}Repository.findById(saved${entity.name}.${entity.primaryKeyAttribute.getterName}());
        assertThat(found).isEmpty();
    }

    @Test
    void deleteAll${entity.name}sShouldRemoveAllEntities() {
        entityManager.persistAndFlush(create${entity.name}());
        entityManager.persistAndFlush(create${entity.name}());
        entityManager.persistAndFlush(create${entity.name}());

        ${entity.variableName}Repository.deleteAll();

        List<${entity.name}> all${entity.name}s = ${entity.variableName}Repository.findAll();
        assertThat(all${entity.name}s).isEmpty();

        long count = ${entity.variableName}Repository.count();
        assertThat(count).isZero();
    }

    @Test
    void deleteMultiple${entity.name}sByIdShouldRemoveSpecifiedEntities() {
        ${entity.name} ${entity.variableName}1 = entityManager.persistAndFlush(create${entity.name}());
        ${entity.name} ${entity.variableName}2 = entityManager.persistAndFlush(create${entity.name}());
        ${entity.name} ${entity.variableName}3 = entityManager.persistAndFlush(create${entity.name}());

        ${entity.variableName}Repository.deleteAllById(List.of(${entity.variableName}1.${entity.primaryKeyAttribute.getterName}(), ${entity.variableName}2.${entity.primaryKeyAttribute.getterName}()));

        List<${entity.name}> remaining${entity.name}s = ${entity.variableName}Repository.findAll();
        assertThat(remaining${entity.name}s).hasSize(1);
        assertThat(remaining${entity.name}s.get(0).${entity.primaryKeyAttribute.getterName}()).isEqualTo(${entity.variableName}3.${entity.primaryKeyAttribute.getterName}());
    }

    @Test
    void exists${entity.name}ByIdShouldReturnTrueForExistingEntity() {
        ${entity.name} test${entity.name} = create${entity.name}();
        ${entity.name} saved${entity.name} = entityManager.persistAndFlush(test${entity.name});

        boolean exists = ${entity.variableName}Repository.existsById(saved${entity.name}.${entity.primaryKeyAttribute.getterName}());

        assertThat(exists).isTrue();
    }

        @Test
        void exists${entity.name}ByIdShouldReturnFalseForNonExistingEntity() {
<#if entity.primaryKeyAttribute.javaType == "String">
            boolean exists = ${entity.variableName}Repository.existsById("NON_EXISTENT_ID");
<#elseif entity.primaryKeyAttribute.javaType == "Long">
            boolean exists = ${entity.variableName}Repository.existsById(999L);
<#elseif entity.primaryKeyAttribute.javaType == "Integer">
            boolean exists = ${entity.variableName}Repository.existsById(999);
<#else>
            boolean exists = ${entity.variableName}Repository.existsById(null);
</#if>

            assertThat(exists).isFalse();
        }

    @Test
    void count${entity.name}sShouldReturnCorrectNumber() {
        entityManager.persistAndFlush(create${entity.name}());
        entityManager.persistAndFlush(create${entity.name}());

        long count = ${entity.variableName}Repository.count();

        assertThat(count).isEqualTo(2);
    }

        @Test
        void count${entity.name}sWhenEmptyShouldReturnZero() {
            long count = ${entity.variableName}Repository.count();

            assertThat(count).isZero();
        }
    }
