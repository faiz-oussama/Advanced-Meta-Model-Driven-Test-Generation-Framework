    @Nested
    @DisplayName("JSON Response Structure Tests")
    class JsonResponseStructureTests {

<#if hasStrictValidation>
        @Test
        @DisplayName("Should return consistent error response structure for validation failures")
        void validationErrorShouldHaveConsistentStructure() throws Exception {
            ${entity.dtoClassName} invalidDto = ${entity.dtoClassName}.builder().build();

            mockMvc.perform(post("/api/${entity.pluralName}")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.path").value("/api/${entity.pluralName}"))
                    .andExpect(jsonPath("$.errors").isArray())
                    .andExpect(jsonPath("$.errors[0].field").exists())
                    .andExpect(jsonPath("$.errors[0].message").exists());

            verify(${entity.variableName}Service, never()).create${entity.name}(any(${entity.name}.class));
        }
<#else>
        @Test
        @DisplayName("Should handle successful creation when no validation rules exist")
        void successfulCreationShouldHaveProperStructure() throws Exception {
            ${entity.dtoClassName} requestDto = ${entity.dtoClassName}.builder().build();

            when(${entity.variableName}Service.create${entity.name}(any(${entity.name}.class))).thenReturn(createMockEntity());

            mockMvc.perform(post("/api/${entity.pluralName}")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));

            verify(${entity.variableName}Service).create${entity.name}(any(${entity.name}.class));
        }
</#if>

        @Test
        @DisplayName("Should return consistent error response structure for not found errors")
        void notFoundErrorShouldHaveConsistentStructure() throws Exception {
<#if entity.primaryKeyAttribute.javaType == "String">
            String nonExistentId = "NONEXISTENT_ID";
<#else>
            Long nonExistentId = 999L;
</#if>

            when(${entity.variableName}Service.get${entity.name}ById(nonExistentId))
                    .thenThrow(new EntityNotFoundException("${entity.name} not found with id: " + nonExistentId));

            mockMvc.perform(get("/api/${entity.pluralName}/{id}", nonExistentId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value("${entity.name} not found with id: " + nonExistentId))
                    .andExpect(jsonPath("$.path").value("/api/${entity.pluralName}/" + nonExistentId))
                    .andExpect(jsonPath("$.errors").doesNotExist());

            verify(${entity.variableName}Service).get${entity.name}ById(nonExistentId);
        }

        @Test
        @DisplayName("Should return consistent error response structure for internal server errors")
        void internalServerErrorShouldHaveConsistentStructure() throws Exception {
<#if entity.primaryKeyAttribute.javaType == "String">
            String ${entity.variableName}Id = "CREATED_ID";
<#else>
            Long ${entity.variableName}Id = 1L;
</#if>

            when(${entity.variableName}Service.get${entity.name}ById(${entity.variableName}Id))
                    .thenThrow(new RuntimeException("Database connection failed"));

            mockMvc.perform(get("/api/${entity.pluralName}/{id}", ${entity.variableName}Id)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.error").value("Internal Server Error"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.path").value("/api/${entity.pluralName}/" + ${entity.variableName}Id))
                    .andExpect(jsonPath("$.errors").doesNotExist());

            verify(${entity.variableName}Service).get${entity.name}ById(${entity.variableName}Id);
        }

        @Test
        @DisplayName("Should return proper JSON structure for successful creation")
        void successfulCreationShouldHaveProperJsonStructure() throws Exception {
            ${entity.dtoClassName} requestDto = create${entity.dtoClassName}();
            ${entity.dtoClassName} responseDto = create${entity.dtoClassName}();
<#if entity.primaryKeyAttribute.javaType == "String">
            responseDto.${entity.primaryKeyAttribute.setterName}("CREATED_ID");
<#else>
            responseDto.${entity.primaryKeyAttribute.setterName}(1L);
</#if>

            when(${entity.variableName}Service.create${entity.name}(any(${entity.name}.class))).thenReturn(createMockEntity());

            mockMvc.perform(post("/api/${entity.pluralName}")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
<#if entity.primaryKeyAttribute.javaType == "String">
                    .andExpect(jsonPath("$.${entity.primaryKeyAttribute.name}").value("CREATED_ID"))
<#else>
                    .andExpect(jsonPath("$.${entity.primaryKeyAttribute.name}").value(1))
</#if>
<#list entity.attributes as attr>
<#if !attr.primaryKey>
                    .andExpect(jsonPath("$.${attr.name}").exists())
</#if>
</#list>
                    .andExpect(jsonPath("$.timestamp").doesNotExist())
                    .andExpect(jsonPath("$.status").doesNotExist())
                    .andExpect(jsonPath("$.error").doesNotExist());

            verify(${entity.variableName}Service).create${entity.name}(any(${entity.name}.class));
        }

        @Test
        @DisplayName("Should return proper JSON structure for successful retrieval")
        void successfulRetrievalShouldHaveProperJsonStructure() throws Exception {
            ${entity.dtoClassName} responseDto = create${entity.dtoClassName}();
<#if entity.primaryKeyAttribute.javaType == "String">
            String ${entity.variableName}Id = "CREATED_ID";
            responseDto.${entity.primaryKeyAttribute.setterName}(${entity.variableName}Id);
<#else>
            Long ${entity.variableName}Id = 1L;
            responseDto.${entity.primaryKeyAttribute.setterName}(${entity.variableName}Id);
</#if>

            when(${entity.variableName}Service.get${entity.name}ById(${entity.variableName}Id)).thenReturn(createMockEntity());

            mockMvc.perform(get("/api/${entity.pluralName}/{id}", ${entity.variableName}Id)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
<#if entity.primaryKeyAttribute.javaType == "String">
                    .andExpect(jsonPath("$.${entity.primaryKeyAttribute.name}").value("CREATED_ID"))
<#else>
                    .andExpect(jsonPath("$.${entity.primaryKeyAttribute.name}").value(1))
</#if>
<#list entity.attributes as attr>
<#if !attr.primaryKey>
                    .andExpect(jsonPath("$.${attr.name}").exists())
</#if>
</#list>
                    .andExpect(jsonPath("$.timestamp").doesNotExist())
                    .andExpect(jsonPath("$.status").doesNotExist())
                    .andExpect(jsonPath("$.error").doesNotExist());

            verify(${entity.variableName}Service).get${entity.name}ById(${entity.variableName}Id);
        }

        @Test
        @DisplayName("Should return proper JSON array structure for list retrieval")
        void listRetrievalShouldHaveProperJsonStructure() throws Exception {
            ${entity.dtoClassName} ${entity.variableName}Dto1 = create${entity.dtoClassName}();
            ${entity.dtoClassName} ${entity.variableName}Dto2 = create${entity.dtoClassName}();
<#if entity.primaryKeyAttribute.javaType == "String">
            ${entity.variableName}Dto1.${entity.primaryKeyAttribute.setterName}("ID_1");
            ${entity.variableName}Dto2.${entity.primaryKeyAttribute.setterName}("ID_2");
<#else>
            ${entity.variableName}Dto1.${entity.primaryKeyAttribute.setterName}(1L);
            ${entity.variableName}Dto2.${entity.primaryKeyAttribute.setterName}(2L);
</#if>

            ${entity.name} entity1 = create${entity.name}();
            ${entity.name} entity2 = create${entity.name}();
<#if entity.primaryKeyAttribute.javaType == "String">
            entity1.${entity.primaryKeyAttribute.setterName}("ID_1");
            entity2.${entity.primaryKeyAttribute.setterName}("ID_2");
<#else>
            entity1.${entity.primaryKeyAttribute.setterName}(1L);
            entity2.${entity.primaryKeyAttribute.setterName}(2L);
</#if>
            List<${entity.name}> entityList = Arrays.asList(entity1, entity2);

            when(${entity.variableName}Service.getAll${entity.name}s()).thenReturn(entityList);

            mockMvc.perform(get("/api/${entity.pluralName}")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(2)))
<#if entity.primaryKeyAttribute.javaType == "String">
                    .andExpect(jsonPath("$[0].${entity.primaryKeyAttribute.name}").value("ID_1"))
                    .andExpect(jsonPath("$[1].${entity.primaryKeyAttribute.name}").value("ID_2"))
<#else>
                    .andExpect(jsonPath("$[0].${entity.primaryKeyAttribute.name}").value(1))
                    .andExpect(jsonPath("$[1].${entity.primaryKeyAttribute.name}").value(2))
</#if>
<#list entity.attributes as attr>
<#if !attr.primaryKey>
                    .andExpect(jsonPath("$[0].${attr.name}").exists())
                    .andExpect(jsonPath("$[1].${attr.name}").exists())
<#break>
</#if>
</#list>
                    .andExpect(jsonPath("$[0].timestamp").doesNotExist())
                    .andExpect(jsonPath("$[0].status").doesNotExist())
                    .andExpect(jsonPath("$[0].error").doesNotExist());

            verify(${entity.variableName}Service).getAll${entity.name}s();
        }

        @Test
        @DisplayName("Should return empty array with proper structure when no entities exist")
        void emptyListShouldHaveProperJsonStructure() throws Exception {
            List<${entity.name}> empty${entity.name}List = List.of();

            when(${entity.variableName}Service.getAll${entity.name}s()).thenReturn(empty${entity.name}List);

            mockMvc.perform(get("/api/${entity.pluralName}")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(0)))
                    .andExpect(content().json("[]"));

            verify(${entity.variableName}Service).getAll${entity.name}s();
        }

        @Test
        @DisplayName("Should return no content body for successful deletion")
        void successfulDeletionShouldHaveNoContent() throws Exception {
<#if entity.primaryKeyAttribute.javaType == "String">
            String ${entity.variableName}Id = "CREATED_ID";
<#else>
            Long ${entity.variableName}Id = 1L;
</#if>

            doNothing().when(${entity.variableName}Service).delete${entity.name}(${entity.variableName}Id);

            mockMvc.perform(delete("/api/${entity.pluralName}/{id}", ${entity.variableName}Id))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""))
                    .andExpect(header().doesNotExist("Content-Type"));

            verify(${entity.variableName}Service).delete${entity.name}(${entity.variableName}Id);
        }

        @Test
        @DisplayName("Should handle null values in JSON response correctly")
        void nullValuesInResponseShouldBeHandledCorrectly() throws Exception {
<#if entity.primaryKeyAttribute.javaType == "String">
            String ${entity.variableName}Id = "CREATED_ID";
<#else>
            Long ${entity.variableName}Id = 1L;
</#if>

            ${entity.name} mockEntity = createMockEntity();
<#list entity.attributes as attr>
<#if !attr.primaryKey && attr.nullable>
            mockEntity.${attr.setterName}(null);
<#break>
</#if>
</#list>

            when(${entity.variableName}Service.get${entity.name}ById(${entity.variableName}Id)).thenReturn(mockEntity);

            mockMvc.perform(get("/api/${entity.pluralName}/{id}", ${entity.variableName}Id)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
<#list entity.attributes as attr>
<#if !attr.primaryKey && attr.nullable>
                    .andExpect(jsonPath("$.${attr.name}").value(nullValue()))
<#break>
</#if>
</#list>
<#if entity.primaryKeyAttribute.javaType == "String">
                    .andExpect(jsonPath("$.${entity.primaryKeyAttribute.name}").value("CREATED_ID"));
<#else>
                    .andExpect(jsonPath("$.${entity.primaryKeyAttribute.name}").value(1));
</#if>

            verify(${entity.variableName}Service).get${entity.name}ById(${entity.variableName}Id);
        }
    }
