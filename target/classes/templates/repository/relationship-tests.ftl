    @Nested
    @DisplayName("Relationship Tests")
    class RelationshipTests {

<#list entity.relationships as relationship>
<#if relationship.oneToOne>
        @Test
        @DisplayName("Should persist OneToOne relationship with ${relationship.nameCapitalized}")
        void shouldPersistOneToOneRelationshipWith${relationship.nameCapitalized}() {
            // Given
            ${entity.name} test${entity.name} = ${entity.builderClassName}.aValid${entity.name}().build();
            ${relationship.targetEntity} ${relationship.name} = create${relationship.targetEntity}();
            test${entity.name}.set${relationship.nameCapitalized}(${relationship.name});

            // When
            ${entity.name} saved${entity.name} = ${entity.variableName}Repository.save(test${entity.name});

            // Then
            assertThat(saved${entity.name}).isNotNull();
            assertThat(saved${entity.name}.get${relationship.nameCapitalized}()).isNotNull();
            assertThat(saved${entity.name}.get${relationship.nameCapitalized}().getId()).isNotNull();
        }

        @Test
        @DisplayName("Should persist without relationship when ${relationship.nameCapitalized} is null")
        void shouldPersistWithoutRelationshipWhen${relationship.nameCapitalized}IsNull() {
            // Given
            ${entity.name} test${entity.name} = ${entity.builderClassName}.aValid${entity.name}().build();
            test${entity.name}.set${relationship.nameCapitalized}(null);

            // When
            ${entity.name} saved${entity.name} = ${entity.variableName}Repository.save(test${entity.name});

            // Then
            assertThat(saved${entity.name}).isNotNull();
            assertThat(saved${entity.name}.get${relationship.nameCapitalized}()).isNull();
        }

<#if relationship.cascadeType?contains("ALL") || relationship.cascadeType?contains("REMOVE")>
    @Test
    void delete${entity.name}ShouldCascadeDelete${relationship.nameCapitalized}() {
        ${entity.name} test${entity.name} = ${entity.builderClassName}.aValid${entity.name}().build();
        ${relationship.targetEntity} ${relationship.name} = create${relationship.targetEntity}();
        test${entity.name}.set${relationship.nameCapitalized}(${relationship.name});
        ${entity.name} saved${entity.name} = entityManager.persistAndFlush(test${entity.name});

        ${entity.nameLowerCase}Repository.delete(saved${entity.name});
        entityManager.flush();

        ${relationship.targetEntity} found${relationship.targetEntity} = entityManager.find(${relationship.targetEntity}.class, ${relationship.name}.getId());
        assertThat(found${relationship.targetEntity}).isNull();
    }
</#if>

</#if>
<#if relationship.oneToMany>
    @Test
    void save${entity.name}With${relationship.nameCapitalized}ShouldPersistOneToManyRelationship() {
        ${entity.name} test${entity.name} = ${entity.builderClassName}.aValid${entity.name}().build();
        ${relationship.targetEntity} ${relationship.targetEntity?uncap_first}1 = create${relationship.targetEntity}();
        ${relationship.targetEntity} ${relationship.targetEntity?uncap_first}2 = create${relationship.targetEntity}();

        test${entity.name}.get${relationship.nameCapitalized}().add(${relationship.targetEntity?uncap_first}1);
        test${entity.name}.get${relationship.nameCapitalized}().add(${relationship.targetEntity?uncap_first}2);

<#if relationship.mappedBy?? && relationship.mappedBy?has_content>
        ${relationship.targetEntity?uncap_first}1.set${relationship.mappedBy?cap_first}(test${entity.name});
        ${relationship.targetEntity?uncap_first}2.set${relationship.mappedBy?cap_first}(test${entity.name});
<#elseif relationship.owner>
        ${relationship.targetEntity?uncap_first}1.set${entity.nameCapitalized}(test${entity.name});
        ${relationship.targetEntity?uncap_first}2.set${entity.nameCapitalized}(test${entity.name});
</#if>

        ${entity.name} saved${entity.name} = ${entity.nameLowerCase}Repository.save(test${entity.name});

        assertThat(saved${entity.name}).isNotNull();
        assertThat(saved${entity.name}.get${relationship.nameCapitalized}()).hasSize(2);
        assertThat(saved${entity.name}.get${relationship.nameCapitalized}()).allMatch(item -> item.getId() != null);
    }

    @Test
    void save${entity.name}WithEmpty${relationship.nameCapitalized}ShouldPersistWithoutRelationships() {
        ${entity.name} test${entity.name} = ${entity.builderClassName}.aValid${entity.name}().build();
        test${entity.name}.get${relationship.nameCapitalized}().clear();

        ${entity.name} saved${entity.name} = ${entity.nameLowerCase}Repository.save(test${entity.name});

        assertThat(saved${entity.name}).isNotNull();
        assertThat(saved${entity.name}.get${relationship.nameCapitalized}()).isEmpty();
    }

    @Test
    void add${relationship.targetEntity}To${entity.name}ShouldUpdateRelationship() {
        ${entity.name} test${entity.name} = ${entity.builderClassName}.aValid${entity.name}().build();
        ${entity.name} saved${entity.name} = entityManager.persistAndFlush(test${entity.name});

        ${relationship.targetEntity} new${relationship.targetEntity} = create${relationship.targetEntity}();
        saved${entity.name}.get${relationship.nameCapitalized}().add(new${relationship.targetEntity});
<#if relationship.mappedBy?? && relationship.mappedBy?has_content>
        new${relationship.targetEntity}.set${relationship.mappedBy?cap_first}(saved${entity.name});
<#elseif relationship.owner>
        new${relationship.targetEntity}.set${entity.nameCapitalized}(saved${entity.name});
</#if>

        ${entity.name} updated${entity.name} = ${entity.nameLowerCase}Repository.save(saved${entity.name});

        assertThat(updated${entity.name}.get${relationship.nameCapitalized}()).hasSize(1);
        assertThat(updated${entity.name}.get${relationship.nameCapitalized}().get(0).getId()).isNotNull();
    }

    @Test
    void remove${relationship.targetEntity}From${entity.name}ShouldUpdateRelationship() {
        ${entity.name} test${entity.name} = ${entity.builderClassName}.aValid${entity.name}().build();
        ${relationship.targetEntity} ${relationship.targetEntity?uncap_first} = create${relationship.targetEntity}();
        test${entity.name}.get${relationship.nameCapitalized}().add(${relationship.targetEntity?uncap_first});
<#if relationship.mappedBy?? && relationship.mappedBy?has_content>
        ${relationship.targetEntity?uncap_first}.set${relationship.mappedBy?cap_first}(test${entity.name});
<#elseif relationship.owner>
        ${relationship.targetEntity?uncap_first}.set${entity.nameCapitalized}(test${entity.name});
</#if>
        ${entity.name} saved${entity.name} = entityManager.persistAndFlush(test${entity.name});

        saved${entity.name}.get${relationship.nameCapitalized}().clear();

        ${entity.name} updated${entity.name} = ${entity.nameLowerCase}Repository.save(saved${entity.name});

        assertThat(updated${entity.name}.get${relationship.nameCapitalized}()).isEmpty();
    }

<#if relationship.orphanRemoval>
    @Test
    void remove${relationship.targetEntity}From${entity.name}ShouldDeleteOrphan() {
        ${entity.name} test${entity.name} = ${entity.builderClassName}.aValid${entity.name}().build();
        ${relationship.targetEntity} ${relationship.targetEntity?uncap_first} = create${relationship.targetEntity}();
        test${entity.name}.get${relationship.nameCapitalized}().add(${relationship.targetEntity?uncap_first});
<#if relationship.mappedBy?? && relationship.mappedBy?has_content>
        ${relationship.targetEntity?uncap_first}.set${relationship.mappedBy?cap_first}(test${entity.name});
<#elseif relationship.owner>
        ${relationship.targetEntity?uncap_first}.set${entity.nameCapitalized}(test${entity.name});
</#if>
        ${entity.name} saved${entity.name} = entityManager.persistAndFlush(test${entity.name});
        Long ${relationship.targetEntity?uncap_first}Id = ${relationship.targetEntity?uncap_first}.getId();

        saved${entity.name}.get${relationship.nameCapitalized}().clear();
        ${entity.nameLowerCase}Repository.save(saved${entity.name});
        entityManager.flush();

        ${relationship.targetEntity} found${relationship.targetEntity} = entityManager.find(${relationship.targetEntity}.class, ${relationship.targetEntity?uncap_first}Id);
        assertThat(found${relationship.targetEntity}).isNull();
    }
</#if>

<#if relationship.cascadeType?contains("ALL") || relationship.cascadeType?contains("REMOVE")>
    @Test
    void delete${entity.name}ShouldCascadeDelete${relationship.nameCapitalized}() {
        ${entity.name} test${entity.name} = ${entity.builderClassName}.aValid${entity.name}().build();
        ${relationship.targetEntity} ${relationship.targetEntity?uncap_first} = create${relationship.targetEntity}();
        test${entity.name}.get${relationship.nameCapitalized}().add(${relationship.targetEntity?uncap_first});
<#if relationship.mappedBy?? && relationship.mappedBy?has_content>
        ${relationship.targetEntity?uncap_first}.set${relationship.mappedBy?cap_first}(test${entity.name});
<#elseif relationship.owner>
        ${relationship.targetEntity?uncap_first}.set${entity.nameCapitalized}(test${entity.name});
</#if>
        ${entity.name} saved${entity.name} = entityManager.persistAndFlush(test${entity.name});
        Long ${relationship.targetEntity?uncap_first}Id = ${relationship.targetEntity?uncap_first}.getId();

        ${entity.nameLowerCase}Repository.delete(saved${entity.name});
        entityManager.flush();

        ${relationship.targetEntity} found${relationship.targetEntity} = entityManager.find(${relationship.targetEntity}.class, ${relationship.targetEntity?uncap_first}Id);
        assertThat(found${relationship.targetEntity}).isNull();
    }
</#if>

</#if>
<#if relationship.manyToOne>
    @Test
    void save${entity.name}With${relationship.nameCapitalized}ShouldPersistManyToOneRelationship() {
        // Create and persist the target entity first
        ${relationship.targetEntity} ${relationship.name} = ${relationship.targetEntity}TestDataBuilder.aDefault${relationship.targetEntity}().build();
        ${relationship.targetEntity} persisted${relationship.targetEntity} = entityManager.persistAndFlush(${relationship.name});

        // Create the main entity and set the relationship
        ${entity.name} test${entity.name} = ${entity.builderClassName}.aValid${entity.name}().build();
        test${entity.name}.set${relationship.nameCapitalized}(persisted${relationship.targetEntity});

        ${entity.name} saved${entity.name} = ${entity.nameLowerCase}Repository.save(test${entity.name});

        assertThat(saved${entity.name}).isNotNull();
        assertThat(saved${entity.name}.get${relationship.nameCapitalized}()).isNotNull();
        assertThat(saved${entity.name}.get${relationship.nameCapitalized}().getId()).isNotNull();
    }

    @Test
    void saveMultiple${entity.name}sWithSame${relationship.nameCapitalized}ShouldWork() {
        // Create and persist the shared target entity
        ${relationship.targetEntity} shared${relationship.targetEntity} = ${relationship.targetEntity}TestDataBuilder.aDefault${relationship.targetEntity}().build();
        ${relationship.targetEntity} persistedShared${relationship.targetEntity} = entityManager.persistAndFlush(shared${relationship.targetEntity});

        // Create main entities and set the shared relationship
        ${entity.name} ${entity.nameLowerCase}1 = ${entity.builderClassName}.aValid${entity.name}().build();
        ${entity.name} ${entity.nameLowerCase}2 = ${entity.builderClassName}.aValid${entity.name}().build();

        ${entity.nameLowerCase}1.set${relationship.nameCapitalized}(persistedShared${relationship.targetEntity});
        ${entity.nameLowerCase}2.set${relationship.nameCapitalized}(persistedShared${relationship.targetEntity});

        ${entity.name} saved${entity.name}1 = ${entity.nameLowerCase}Repository.save(${entity.nameLowerCase}1);
        ${entity.name} saved${entity.name}2 = ${entity.nameLowerCase}Repository.save(${entity.nameLowerCase}2);

        assertThat(saved${entity.name}1.get${relationship.nameCapitalized}().getId()).isEqualTo(persistedShared${relationship.targetEntity}.getId());
        assertThat(saved${entity.name}2.get${relationship.nameCapitalized}().getId()).isEqualTo(persistedShared${relationship.targetEntity}.getId());
    }

</#if>
<#if relationship.manyToMany>
    @Test
    void save${entity.name}With${relationship.nameCapitalized}ShouldPersistManyToManyRelationship() {
        ${entity.name} test${entity.name} = ${entity.builderClassName}.aValid${entity.name}().build();
        ${relationship.targetEntity} ${relationship.targetEntity?uncap_first}1 = entityManager.persistAndFlush(create${relationship.targetEntity}());
        ${relationship.targetEntity} ${relationship.targetEntity?uncap_first}2 = entityManager.persistAndFlush(create${relationship.targetEntity}());

        test${entity.name}.get${relationship.nameCapitalized}().add(${relationship.targetEntity?uncap_first}1);
        test${entity.name}.get${relationship.nameCapitalized}().add(${relationship.targetEntity?uncap_first}2);

        ${entity.name} saved${entity.name} = ${entity.nameLowerCase}Repository.save(test${entity.name});

        assertThat(saved${entity.name}).isNotNull();
        assertThat(saved${entity.name}.get${relationship.nameCapitalized}()).hasSize(2);
    }

    @Test
    void addAndRemove${relationship.targetEntity}InManyToManyRelationshipShouldWork() {
        ${entity.name} test${entity.name} = ${entity.builderClassName}.aValid${entity.name}().build();
        ${relationship.targetEntity} ${relationship.targetEntity?uncap_first}1 = entityManager.persistAndFlush(create${relationship.targetEntity}());
        ${relationship.targetEntity} ${relationship.targetEntity?uncap_first}2 = entityManager.persistAndFlush(create${relationship.targetEntity}());

        test${entity.name}.get${relationship.nameCapitalized}().add(${relationship.targetEntity?uncap_first}1);
        ${entity.name} saved${entity.name} = entityManager.persistAndFlush(test${entity.name});

        saved${entity.name}.get${relationship.nameCapitalized}().add(${relationship.targetEntity?uncap_first}2);
        saved${entity.name}.get${relationship.nameCapitalized}().remove(${relationship.targetEntity?uncap_first}1);
        
        ${entity.name} updated${entity.name} = ${entity.nameLowerCase}Repository.save(saved${entity.name});

        assertThat(updated${entity.name}.get${relationship.nameCapitalized}()).hasSize(1);
        assertThat(updated${entity.name}.get${relationship.nameCapitalized}()).contains(${relationship.targetEntity?uncap_first}2);
        assertThat(updated${entity.name}.get${relationship.nameCapitalized}()).doesNotContain(${relationship.targetEntity?uncap_first}1);
    }

</#if>
</#list>

    @Test
    void load${entity.name}WithRelationshipsShouldNotCauseNPlusOneQueries() {
        ${entity.name} test${entity.name} = create${entity.name}();
        ${entity.name} saved${entity.name} = entityManager.persistAndFlush(test${entity.name});
        entityManager.clear();

        Optional<${entity.name}> found = ${entity.nameLowerCase}Repository.findById(saved${entity.name}.${entity.primaryKeyAttribute.getterName}());

        assertThat(found).isPresent();
        ${entity.name} loaded${entity.name} = found.get();
        assertThat(loaded${entity.name}).isNotNull();
<#list entity.relationships as relationship>
<#if relationship.fetchType == "EAGER">
        assertThat(loaded${entity.name}.get${relationship.nameCapitalized}()).isNotNull();
</#if>
</#list>
    }


}
