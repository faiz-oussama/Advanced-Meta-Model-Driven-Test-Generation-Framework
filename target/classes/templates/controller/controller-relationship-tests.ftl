    @Nested
    @DisplayName("Relationship Tests")
    class RelationshipTests {

<#list entity.relationships as relationship>
<#if !relationship.collection>
        @Test
        @DisplayName("Should create ${entity.name} with valid ${relationship.targetEntity}")
        void create${entity.name}WithValid${relationship.targetEntity}ShouldReturnCreated() throws Exception {
            ${entity.name}Dto requestDto = create${entity.name}Dto();
            ${entity.name} mockEntity = createMockEntity();

            when(${entity.variableName}Service.create${entity.name}(any(${entity.name}.class))).thenReturn(mockEntity);

            mockMvc.perform(post("/api/${entity.pluralName}")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
<#if entity.primaryKeyAttribute.javaType == "String">
                    .andExpect(jsonPath("$.${entity.primaryKeyAttribute.name}").value("CREATED_ID"))
<#else>
                    .andExpect(jsonPath("$.${entity.primaryKeyAttribute.name}").value(1))
</#if>
                    .andExpect(jsonPath("$.${relationship.name}Id").value(1));

            verify(${entity.variableName}Service).create${entity.name}(any(${entity.name}.class));
        }

<#if !relationship.optional>
        @Test
        @DisplayName("Should return 400 when ${relationship.targetEntity} is null for required relationship")
        void create${entity.name}WithNull${relationship.targetEntity}ShouldReturnBadRequest() throws Exception {
            ${entity.name}Dto requestDto = create${entity.name}Dto();
            requestDto.set${relationship.nameCapitalized}Id(null);

            mockMvc.perform(post("/api/${entity.pluralName}")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest());

            verify(${entity.variableName}Service, never()).create${entity.name}(any(${entity.name}.class));
        }

</#if>
        @Test
        @DisplayName("Should get ${entity.name} with ${relationship.targetEntity} details")
        void get${entity.name}WithRelationshipDetailsShouldReturnOk() throws Exception {
<#if entity.primaryKeyAttribute.javaType == "String">
            String ${entity.variableName}Id = "CREATED_ID";
<#else>
            Long ${entity.variableName}Id = 1L;
</#if>
            ${entity.name} mockEntity = createMockEntity();

            when(${entity.variableName}Service.get${entity.name}ById(${entity.variableName}Id)).thenReturn(mockEntity);

            mockMvc.perform(get("/api/${entity.pluralName}/{id}", ${entity.variableName}Id)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
<#if entity.primaryKeyAttribute.javaType == "String">
                    .andExpect(jsonPath("$.${entity.primaryKeyAttribute.name}").value("CREATED_ID"))
<#else>
                    .andExpect(jsonPath("$.${entity.primaryKeyAttribute.name}").value(1))
</#if>
                    .andExpect(jsonPath("$.${relationship.name}Id").value(1));

            verify(${entity.variableName}Service).get${entity.name}ById(${entity.variableName}Id);
        }

        @Test
        @DisplayName("Should update ${entity.name} with new ${relationship.targetEntity}")
        void update${entity.name}WithNew${relationship.targetEntity}ShouldReturnOk() throws Exception {
<#if entity.primaryKeyAttribute.javaType == "String">
            String ${entity.variableName}Id = "CREATED_ID";
<#else>
            Long ${entity.variableName}Id = 1L;
</#if>
            ${entity.name}Dto updateDto = create${entity.name}Dto();
            updateDto.set${relationship.nameCapitalized}Id(2L);

            ${entity.name} mockEntity = createMockEntity();

            when(${entity.variableName}Service.update${entity.name}(eq(${entity.variableName}Id), any(${entity.name}.class)))
                    .thenReturn(mockEntity);

            mockMvc.perform(put("/api/${entity.pluralName}/{id}", ${entity.variableName}Id)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
<#if entity.primaryKeyAttribute.javaType == "String">
                    .andExpect(jsonPath("$.${entity.primaryKeyAttribute.name}").value("CREATED_ID"))
<#else>
                    .andExpect(jsonPath("$.${entity.primaryKeyAttribute.name}").value(1))
</#if>
                    .andExpect(jsonPath("$.${relationship.name}Id").value(1));

            verify(${entity.variableName}Service).update${entity.name}(eq(${entity.variableName}Id), any(${entity.name}.class));
        }

</#if>
<#if relationship.collection>
        @Test
        @DisplayName("Should create ${entity.name} successfully (collection relationships managed separately)")
        void create${entity.name}With${relationship.targetEntity}CollectionShouldReturnCreated() throws Exception {
            ${entity.name}Dto requestDto = create${entity.name}Dto();
            ${entity.name} mockEntity = createMockEntity();

            when(${entity.variableName}Service.create${entity.name}(any(${entity.name}.class))).thenReturn(mockEntity);

            mockMvc.perform(post("/api/${entity.pluralName}")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
<#if entity.primaryKeyAttribute.javaType == "String">
                    .andExpect(jsonPath("$.${entity.primaryKeyAttribute.name}").value("CREATED_ID"));
<#else>
                    .andExpect(jsonPath("$.${entity.primaryKeyAttribute.name}").value(1));
</#if>

            verify(${entity.variableName}Service).create${entity.name}(any(${entity.name}.class));
        }

        @Test
        @DisplayName("Should get ${entity.name} successfully (collection relationships managed separately)")
        void get${entity.name}With${relationship.targetEntity}CollectionShouldReturnOk() throws Exception {
<#if entity.primaryKeyAttribute.javaType == "String">
            String ${entity.variableName}Id = "CREATED_ID";
<#else>
            Long ${entity.variableName}Id = 1L;
</#if>
            ${entity.name} mockEntity = createMockEntity();

            when(${entity.variableName}Service.get${entity.name}ById(${entity.variableName}Id)).thenReturn(mockEntity);

            mockMvc.perform(get("/api/${entity.pluralName}/{id}", ${entity.variableName}Id)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
<#if entity.primaryKeyAttribute.javaType == "String">
                    .andExpect(jsonPath("$.${entity.primaryKeyAttribute.name}").value("CREATED_ID"));
<#else>
                    .andExpect(jsonPath("$.${entity.primaryKeyAttribute.name}").value(1));
</#if>

            verify(${entity.variableName}Service).get${entity.name}ById(${entity.variableName}Id);
        }

</#if>
</#list>
    }
