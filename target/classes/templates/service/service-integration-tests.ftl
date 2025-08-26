    @Nested
    @SpringBootTest
    @Transactional
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Autowired
        private ${entity.name}Service ${entity.variableName}ServiceIntegration;

        @Autowired
        private ${entity.name}Repository ${entity.variableName}RepositoryIntegration;

        @Autowired
        private jakarta.persistence.EntityManager entityManager;

<#if entity.hasRelationships()>
<#list entity.relationships as relationship>
        @Autowired
        private ${relationship.targetEntity}Repository ${relationship.targetEntity?lower_case}RepositoryIntegration;

</#list>
</#if>
        private ${entity.name} test${entity.name}Integration;

        @BeforeEach
        void setUpIntegration() {
            test${entity.name}Integration = createTestEntity();
        }

        private ${entity.name} createTestEntity() {
            ${entity.name} entity = ${entity.name}TestDataBuilder.aDefault${entity.name}().build();
<#if entity.hasRelationships()>
<#list entity.relationships as relationship>
<#if !relationship.collection && !relationship.optional>
            if (entity.get${relationship.nameCapitalized}() != null) {
                ${relationship.targetEntity} persisted${relationship.targetEntity} = ${relationship.targetEntity?lower_case}RepositoryIntegration.save(entity.get${relationship.nameCapitalized}());
                entity.set${relationship.nameCapitalized}(persisted${relationship.targetEntity});
            }
</#if>
</#list>
</#if>
            return entity;
        }

        @Nested
        @DisplayName("Business Logic Integration Tests")
        class BusinessLogicIntegrationTests {

            @Test
            @DisplayName("Should create ${entity.name} and persist to database")
            void shouldCreate${entity.name}AndPersistToDatabase() {
                ${entity.name} testEntity = createTestEntity();
                ${entity.name} created = ${entity.variableName}ServiceIntegration.create${entity.name}(testEntity);

                assertThat(created).isNotNull();
<#if entity.primaryKeyAttribute.generatedValue>
                assertThat(created.${entity.primaryKeyAttribute.getterName}()).isNotNull();
</#if>
                
                Optional<${entity.name}> persisted = ${entity.variableName}RepositoryIntegration.findById(created.${entity.primaryKeyAttribute.getterName}());
                assertThat(persisted).isPresent();
                assertThat(persisted.get().${entity.primaryKeyAttribute.getterName}()).isEqualTo(created.${entity.primaryKeyAttribute.getterName}());
            }

            @Test
            @DisplayName("Should update ${entity.name} and persist changes")
            void shouldUpdate${entity.name}AndPersistChanges() {
                ${entity.name} testEntity = createTestEntity();
                ${entity.name} created = ${entity.variableName}ServiceIntegration.create${entity.name}(testEntity);
                
<#list entity.attributes as attr>
<#if !attr.primaryKey && !attr.generatedValue && attr.type == "String">
                created.${attr.setterName}("Updated ${attr.name}");
<#break>
</#if>
</#list>

                ${entity.name} updated = ${entity.variableName}ServiceIntegration.update${entity.name}(created.${entity.primaryKeyAttribute.getterName}(), created);

                assertThat(updated).isNotNull();
                ${entity.name} persisted = ${entity.variableName}RepositoryIntegration.findById(created.${entity.primaryKeyAttribute.getterName}()).orElseThrow();
<#list entity.attributes as attr>
<#if !attr.primaryKey && !attr.generatedValue && attr.type == "String">
                assertThat(persisted.${attr.getterName}()).isEqualTo("Updated ${attr.name}");
<#break>
</#if>
</#list>
            }

            @Test
            @DisplayName("Should delete ${entity.name} from database")
            void shouldDelete${entity.name}FromDatabase() {
                ${entity.name} testEntity = createTestEntity();
                ${entity.name} created = ${entity.variableName}ServiceIntegration.create${entity.name}(testEntity);
                ${entity.primaryKeyAttribute.javaType} id = created.${entity.primaryKeyAttribute.getterName}();

                ${entity.variableName}ServiceIntegration.delete${entity.name}(id);

                Optional<${entity.name}> deleted = ${entity.variableName}RepositoryIntegration.findById(id);
                assertThat(deleted).isEmpty();
            }
        }

        @Nested
        @DisplayName("Validation Constraint Integration Tests")
        class ValidationConstraintIntegrationTests {

<#assign hasNonNullableAttributes = false>
<#list entity.attributes as attr>
<#if attr.nullable == false && !attr.primaryKey>
<#assign hasNonNullableAttributes = true>
<#break>
</#if>
</#list>
<#if hasNonNullableAttributes>
            @Test
            @DisplayName("Should handle validation errors from JPA/Hibernate")
            void shouldHandleJpaValidationErrors() {
                ${entity.name} invalid${entity.name} = ${entity.name}TestDataBuilder.aDefault${entity.name}()
<#list entity.attributes as attr>
<#if attr.nullable == false && !attr.primaryKey>
                        .with${attr.name?cap_first}(null)
<#break>
</#if>
</#list>
                        .build();
<#if entity.hasRelationships()>
<#list entity.relationships as relationship>
<#if !relationship.collection && !relationship.optional>
                if (invalid${entity.name}.get${relationship.nameCapitalized}() != null) {
                    ${relationship.targetEntity} persisted${relationship.targetEntity} = ${relationship.targetEntity?lower_case}RepositoryIntegration.save(invalid${entity.name}.get${relationship.nameCapitalized}());
                    invalid${entity.name}.set${relationship.nameCapitalized}(persisted${relationship.targetEntity});
                }
</#if>
</#list>
</#if>

                try {
                    assertThatThrownBy(() -> {
                        ${entity.variableName}ServiceIntegration.create${entity.name}(invalid${entity.name});
                        entityManager.flush();
                    }).isInstanceOf(ConstraintViolationException.class);
                } finally {
                    entityManager.clear();
                }
            }
</#if>

<#list entity.attributes as attr>
<#if attr.unique == true>
            @Test
            @DisplayName("Should fail when duplicate ${attr.name} is saved")
            void shouldFailWhenDuplicate${attr.name?cap_first}IsSaved() {
                ${entity.name} firstEntity = ${entity.name}TestDataBuilder.aDefault${entity.name}()
<#if attr.email?? && attr.email == true>
                        .with${attr.name?cap_first}("test" + System.currentTimeMillis() + "@example.com")
<#elseif attr.name == "cin">
                        .with${attr.name?cap_first}("TEST" + String.format("%06d", (System.currentTimeMillis() % 1000000)))
<#elseif attr.name == "phoneNumber" || (attr.minLength?? && attr.minLength >= 10)>
                        .with${attr.name?cap_first}("0600" + String.format("%06d", (System.currentTimeMillis() % 1000000)))
<#elseif attr.type == "String" && attr.maxLength?? && (attr.maxLength < 20)>
                        .with${attr.name?cap_first}("test" + (System.currentTimeMillis() % 1000))
<#elseif attr.type == "String">
                        .with${attr.name?cap_first}("unique" + System.currentTimeMillis())
</#if>
                        .build();
                ${entity.name} first = ${entity.variableName}ServiceIntegration.create${entity.name}(firstEntity);

                entityManager.flush();
                entityManager.clear();

                ${entity.name} duplicate = ${entity.name}TestDataBuilder.aDefault${entity.name}().build();
<#list entity.attributes as uniqueAttr>
<#if uniqueAttr.unique && uniqueAttr.name != attr.name>
<#if uniqueAttr.email?? && uniqueAttr.email == true>
                duplicate.set${uniqueAttr.name?cap_first}("different" + System.currentTimeMillis() + "@test.com");
<#elseif uniqueAttr.name == "cin">
                duplicate.set${uniqueAttr.name?cap_first}("DUP" + String.format("%07d", (System.currentTimeMillis() % 10000000)));
<#elseif uniqueAttr.name == "phoneNumber" || (uniqueAttr.minLength?? && uniqueAttr.minLength >= 10)>
                duplicate.set${uniqueAttr.name?cap_first}("0600" + String.format("%06d", (System.currentTimeMillis() % 1000000)));
<#elseif uniqueAttr.type == "String" && uniqueAttr.maxLength?? && (uniqueAttr.maxLength < 20)>
                duplicate.set${uniqueAttr.name?cap_first}("dup" + (System.currentTimeMillis() % 1000));
<#elseif uniqueAttr.type == "String">
                duplicate.set${uniqueAttr.name?cap_first}("different" + (System.currentTimeMillis() % 10000));
</#if>
</#if>
</#list>
                duplicate.set${attr.name?cap_first}(first.get${attr.name?cap_first}());

                assertThatThrownBy(() -> {
<#if attr.primaryKey>
                    entityManager.persist(duplicate);
                    entityManager.flush();
<#else>
                    ${entity.variableName}ServiceIntegration.create${entity.name}(duplicate);
                    entityManager.flush();
</#if>
                }).satisfiesAnyOf(
                    throwable -> assertThat(throwable).isInstanceOf(DataIntegrityViolationException.class),
                    throwable -> assertThat(throwable).isInstanceOf(org.hibernate.exception.ConstraintViolationException.class),
                    throwable -> assertThat(throwable).isInstanceOf(jakarta.persistence.EntityExistsException.class)
                );
            }
</#if>
</#list>
        }

        @Nested
        @DisplayName("Transactional Behavior Integration Tests")
        class TransactionalBehaviorIntegrationTests {

<#if hasNonNullableAttributes>
            @Test
            @DisplayName("Should rollback transaction when error occurs during save")
            void shouldRollbackTransactionWhenErrorOccursDuringSave() {
                long initialCount = ${entity.variableName}RepositoryIntegration.count();

                ${entity.name} invalid${entity.name} = ${entity.name}TestDataBuilder.aDefault${entity.name}()
<#list entity.attributes as attr>
<#if attr.nullable == false && !attr.primaryKey>
                        .with${attr.name?cap_first}(null)
<#break>
</#if>
</#list>
                        .build();
<#if entity.hasRelationships()>
<#list entity.relationships as relationship>
<#if !relationship.collection && !relationship.optional>
                // Persist required relationship
                if (invalid${entity.name}.get${relationship.nameCapitalized}() != null) {
                    ${relationship.targetEntity} persisted${relationship.targetEntity} = ${relationship.targetEntity?lower_case}RepositoryIntegration.save(invalid${entity.name}.get${relationship.nameCapitalized}());
                    invalid${entity.name}.set${relationship.nameCapitalized}(persisted${relationship.targetEntity});
                }
</#if>
</#list>
</#if>

                try {
                    assertThatThrownBy(() -> {
                        ${entity.variableName}ServiceIntegration.create${entity.name}(invalid${entity.name});
                        entityManager.flush();
                    }).isInstanceOf(ConstraintViolationException.class);
                } catch (Exception e) {
                    // Clear entity manager after constraint violation to avoid inconsistent state
                    entityManager.clear();
                    throw e;
                }

                // Clear entity manager to avoid stale state
                entityManager.clear();
                long finalCount = ${entity.variableName}RepositoryIntegration.count();
                assertThat(finalCount).isEqualTo(initialCount);
            }
</#if>
        }

<#if entity.hasRelationships()>
        @Nested
        @DisplayName("Entity Relationship Integration Tests")
        class EntityRelationshipIntegrationTests {

<#list entity.relationships as relationship>
<#if relationship.type == "OneToMany" || relationship.type == "ManyToMany">
            @Test
            @DisplayName("Should cascade save ${relationship.targetEntity} when saving ${entity.name}")
            void shouldCascadeSave${relationship.targetEntity}WhenSaving${entity.name}() {
                ${entity.name} testEntity = createTestEntity();
                ${relationship.targetEntity} related = ${relationship.targetEntity}TestDataBuilder.aDefault${relationship.targetEntity}().build();
<#if relationship.collection>
                // Set the relationship properly for bidirectional mapping
                testEntity.get${relationship.nameCapitalized}().add(related);
<#if relationship.mappedBy?? && relationship.mappedBy != "">
                related.set${relationship.mappedBy?cap_first}(testEntity);
</#if>

                ${entity.name} saved = ${entity.variableName}ServiceIntegration.create${entity.name}(testEntity);

                assertThat(saved.get${relationship.nameCapitalized}()).hasSize(1);
                assertThat(saved.get${relationship.nameCapitalized}().get(0).getId()).isNotNull();

                Optional<${relationship.targetEntity}> persistedRelated = ${relationship.targetEntity?lower_case}RepositoryIntegration
                        .findById(saved.get${relationship.nameCapitalized}().get(0).getId());
<#else>
                testEntity.set${relationship.nameCapitalized}(related);

                ${entity.name} saved = ${entity.variableName}ServiceIntegration.create${entity.name}(testEntity);

                assertThat(saved.get${relationship.nameCapitalized}()).isNotNull();
                assertThat(saved.get${relationship.nameCapitalized}().getId()).isNotNull();

                Optional<${relationship.targetEntity}> persistedRelated = ${relationship.targetEntity?lower_case}RepositoryIntegration
                        .findById(saved.get${relationship.nameCapitalized}().getId());
</#if>
                assertThat(persistedRelated).isPresent();
            }

<#if relationship.orphanRemoval>
            @Test
            @DisplayName("Should remove orphaned ${relationship.targetEntity} when removing from ${entity.name}")
            void shouldRemoveOrphaned${relationship.targetEntity}WhenRemovingFrom${entity.name}() {
                ${entity.name} testEntity = createTestEntity();
                ${relationship.targetEntity} related = ${relationship.targetEntity}TestDataBuilder.aDefault${relationship.targetEntity}().build();
<#if relationship.collection>
                testEntity.get${relationship.nameCapitalized}().add(related);
<#if relationship.mappedBy?? && relationship.mappedBy != "">
                related.set${relationship.mappedBy?cap_first}(testEntity);
</#if>
                ${entity.name} saved = ${entity.variableName}ServiceIntegration.create${entity.name}(testEntity);

                Long relatedId = saved.get${relationship.nameCapitalized}().get(0).getId();
                saved.get${relationship.nameCapitalized}().clear();
<#else>
                testEntity.set${relationship.nameCapitalized}(related);
                ${entity.name} saved = ${entity.variableName}ServiceIntegration.create${entity.name}(testEntity);

                Long relatedId = saved.get${relationship.nameCapitalized}().getId();
                saved.set${relationship.nameCapitalized}(null);
</#if>

                ${entity.variableName}ServiceIntegration.update${entity.name}(saved.${entity.primaryKeyAttribute.getterName}(), saved);

                // Flush to ensure orphan removal is processed
                entityManager.flush();

                Optional<${relationship.targetEntity}> orphaned = ${relationship.targetEntity?lower_case}RepositoryIntegration.findById(relatedId);
                assertThat(orphaned).isEmpty();
            }

</#if>
<#elseif relationship.type == "ManyToOne" || relationship.type == "OneToOne">
            @Test
            @DisplayName("Should save ${entity.name} with ${relationship.targetEntity} reference")
            void shouldSave${entity.name}With${relationship.targetEntity}Reference() {
                ${entity.name} testEntity = createTestEntity();
                ${relationship.targetEntity} related = ${relationship.targetEntity}TestDataBuilder.aDefault${relationship.targetEntity}().build();
                ${relationship.targetEntity} savedRelated = ${relationship.targetEntity?lower_case}RepositoryIntegration.save(related);

                testEntity.set${relationship.nameCapitalized}(savedRelated);

                ${entity.name} saved = ${entity.variableName}ServiceIntegration.create${entity.name}(testEntity);

                assertThat(saved.get${relationship.nameCapitalized}()).isNotNull();
                assertThat(saved.get${relationship.nameCapitalized}().getId())
                        .isEqualTo(savedRelated.getId());
            }

            @Test
            @DisplayName("Should update ${entity.name} with new ${relationship.targetEntity} relationship")
            void shouldUpdate${entity.name}WithNew${relationship.targetEntity}() {
                ${entity.name} testEntity = createTestEntity();
                ${entity.name} created = ${entity.variableName}ServiceIntegration.create${entity.name}(testEntity);

                ${relationship.targetEntity} new${relationship.targetEntity} = ${relationship.targetEntity}TestDataBuilder.aDefault${relationship.targetEntity}().build();
                ${relationship.targetEntity} saved${relationship.targetEntity} = ${relationship.targetEntity?lower_case}RepositoryIntegration.save(new${relationship.targetEntity});

                created.set${relationship.nameCapitalized}(saved${relationship.targetEntity});
                ${entity.name} updated = ${entity.variableName}ServiceIntegration.update${entity.name}(created.${entity.primaryKeyAttribute.getterName}(), created);

                assertThat(updated.get${relationship.nameCapitalized}()).isNotNull();
                assertThat(updated.get${relationship.nameCapitalized}().getId()).isEqualTo(saved${relationship.targetEntity}.getId());
            }

</#if>
</#list>
        }

</#if>

        @Nested
        @DisplayName("Finder Method Integration Tests")
        class FinderMethodIntegrationTests {

            @Test
            @DisplayName("Should find ${entity.name} by ID correctly")
            void shouldFind${entity.name}ByIdCorrectly() {
                ${entity.name} testEntity = createTestEntity();
                ${entity.name} created = ${entity.variableName}ServiceIntegration.create${entity.name}(testEntity);

                ${entity.name} found = ${entity.variableName}ServiceIntegration.get${entity.name}ById(created.${entity.primaryKeyAttribute.getterName}());

                assertThat(found).isNotNull();
                assertThat(found.${entity.primaryKeyAttribute.getterName}()).isEqualTo(created.${entity.primaryKeyAttribute.getterName}());
            }

            @Test
            @DisplayName("Should return empty when ${entity.name} not found by ID")
            void shouldReturnEmptyWhen${entity.name}NotFoundById() {
<#if entity.primaryKeyAttribute.javaType == "String">
                String nonExistentId = "NON_EXISTENT_ID";
<#elseif entity.primaryKeyAttribute.javaType == "Long">
                Long nonExistentId = 999L;
<#elseif entity.primaryKeyAttribute.javaType == "Integer">
                Integer nonExistentId = 999;
<#else>
                Object nonExistentId = 999L;
</#if>

                assertThatThrownBy(() -> ${entity.variableName}ServiceIntegration.get${entity.name}ById(nonExistentId))
                        .isInstanceOf(EntityNotFoundException.class);
            }

            @Test
            @DisplayName("Should check existence correctly")
            void shouldCheckExistenceCorrectly() {
                ${entity.name} testEntity = createTestEntity();
                ${entity.name} created = ${entity.variableName}ServiceIntegration.create${entity.name}(testEntity);
<#if entity.primaryKeyAttribute.javaType == "String">
                String nonExistentId = "NON_EXISTENT_ID";
<#elseif entity.primaryKeyAttribute.javaType == "Long">
                Long nonExistentId = 999L;
<#elseif entity.primaryKeyAttribute.javaType == "Integer">
                Integer nonExistentId = 999;
<#else>
                Object nonExistentId = 999L;
</#if>

                boolean exists = ${entity.variableName}ServiceIntegration.exists${entity.name}ById(created.${entity.primaryKeyAttribute.getterName}());
                boolean notExists = ${entity.variableName}ServiceIntegration.exists${entity.name}ById(nonExistentId);

                assertThat(exists).isTrue();
                assertThat(notExists).isFalse();
            }

            @Test
            @DisplayName("Should find all ${entity.name}s from database")
            void shouldFindAll${entity.name}sFromDatabase() {
                ${entity.name} testEntity1 = createTestEntity();
                ${entity.name} entity1 = ${entity.variableName}ServiceIntegration.create${entity.name}(testEntity1);
                ${entity.name} testEntity2 = createTestEntity();
                ${entity.name} entity2 = ${entity.variableName}ServiceIntegration.create${entity.name}(testEntity2);

                List<${entity.name}> all = ${entity.variableName}ServiceIntegration.getAll${entity.name}s();

                assertThat(all).hasSizeGreaterThanOrEqualTo(2);
                assertThat(all).extracting(${entity.name}::${entity.primaryKeyAttribute.getterName})
                        .contains(entity1.${entity.primaryKeyAttribute.getterName}(), entity2.${entity.primaryKeyAttribute.getterName}());
            }
        }

        @Nested
        @DisplayName("Deletion Logic Integration Tests")
        class DeletionLogicIntegrationTests {

            @Test
            @DisplayName("Should delete ${entity.name} and verify removal")
            void shouldDelete${entity.name}AndVerifyRemoval() {
                ${entity.name} testEntity = createTestEntity();
                ${entity.name} created = ${entity.variableName}ServiceIntegration.create${entity.name}(testEntity);
                ${entity.primaryKeyAttribute.javaType} id = created.${entity.primaryKeyAttribute.getterName}();

                ${entity.variableName}ServiceIntegration.delete${entity.name}(id);

                boolean exists = ${entity.variableName}RepositoryIntegration.existsById(id);
                assertThat(exists).isFalse();
            }

<#if entity.hasRelationships()>
<#list entity.relationships as relationship>
<#if relationship.cascadeType?contains("ALL") || relationship.cascadeType?contains("REMOVE")>
            @Test
            @DisplayName("Should cascade delete ${relationship.targetEntity} when deleting ${entity.name}")
            void shouldCascadeDelete${relationship.targetEntity}WhenDeleting${entity.name}() {
                ${entity.name} testEntity = createTestEntity();
                ${relationship.targetEntity} related = ${relationship.targetEntity}TestDataBuilder.aDefault${relationship.targetEntity}().build();
<#if relationship.collection>
                testEntity.get${relationship.nameCapitalized}().add(related);
<#if relationship.mappedBy?? && relationship.mappedBy != "">
                related.set${relationship.mappedBy?cap_first}(testEntity);
</#if>
                ${entity.name} saved = ${entity.variableName}ServiceIntegration.create${entity.name}(testEntity);

                Long relatedId = saved.get${relationship.nameCapitalized}().get(0).getId();
<#else>
                testEntity.set${relationship.nameCapitalized}(related);
                ${entity.name} saved = ${entity.variableName}ServiceIntegration.create${entity.name}(testEntity);

                Long relatedId = saved.get${relationship.nameCapitalized}().getId();
</#if>

                ${entity.variableName}ServiceIntegration.delete${entity.name}(saved.${entity.primaryKeyAttribute.getterName}());

                boolean relatedExists = ${relationship.targetEntity?lower_case}RepositoryIntegration.existsById(relatedId);
                assertThat(relatedExists).isFalse();
            }

</#if>
</#list>
</#if>
        }
    }



