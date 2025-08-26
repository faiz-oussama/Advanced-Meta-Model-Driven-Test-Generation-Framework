    @Nested
    @DisplayName("Header and Parameter Tests")
    class HeaderAndParameterTests {

        @Test
        @DisplayName("Should handle custom headers correctly")
        void requestWithCustomHeadersShouldWork() throws Exception {
            ${entity.dtoClassName} ${entity.variableName}Dto = create${entity.dtoClassName}();
<#if entity.primaryKeyAttribute.javaType == "String">
            ${entity.variableName}Dto.${entity.primaryKeyAttribute.setterName}("CREATED_ID");
<#else>
            ${entity.variableName}Dto.${entity.primaryKeyAttribute.setterName}(1L);
</#if>

            ${entity.name} entity = create${entity.name}();
<#if entity.primaryKeyAttribute.javaType == "String">
            entity.${entity.primaryKeyAttribute.setterName}("CREATED_ID");
<#else>
            entity.${entity.primaryKeyAttribute.setterName}(1L);
</#if>
            List<${entity.name}> entityList = Arrays.asList(entity);

            when(${entity.variableName}Service.getAll${entity.name}s()).thenReturn(entityList);

            mockMvc.perform(get("/api/${entity.pluralName}")
                    .header("X-Request-ID", "test-request-123")
                    .header("X-Client-Version", "1.0.0")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)));

            verify(${entity.variableName}Service).getAll${entity.name}s();
        }

        @Test
        @DisplayName("Should handle query parameters correctly")
        void requestWithQueryParametersShouldWork() throws Exception {
            ${entity.dtoClassName} ${entity.variableName}Dto = create${entity.dtoClassName}();
<#if entity.primaryKeyAttribute.javaType == "String">
            ${entity.variableName}Dto.${entity.primaryKeyAttribute.setterName}("CREATED_ID");
<#else>
            ${entity.variableName}Dto.${entity.primaryKeyAttribute.setterName}(1L);
</#if>

            ${entity.name} entity = create${entity.name}();
<#if entity.primaryKeyAttribute.javaType == "String">
            entity.${entity.primaryKeyAttribute.setterName}("CREATED_ID");
<#else>
            entity.${entity.primaryKeyAttribute.setterName}(1L);
</#if>
            List<${entity.name}> entityList = Arrays.asList(entity);

            when(${entity.variableName}Service.getAll${entity.name}s()).thenReturn(entityList);

            mockMvc.perform(get("/api/${entity.pluralName}")
                    .param("sort", "id")
                    .param("order", "asc")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)));

            verify(${entity.variableName}Service).getAll${entity.name}s();
        }

        @Test
        @DisplayName("Should handle pagination parameters correctly")
        void requestWithPaginationParametersShouldWork() throws Exception {
            ${entity.dtoClassName} ${entity.variableName}Dto = create${entity.dtoClassName}();
<#if entity.primaryKeyAttribute.javaType == "String">
            ${entity.variableName}Dto.${entity.primaryKeyAttribute.setterName}("CREATED_ID");
<#else>
            ${entity.variableName}Dto.${entity.primaryKeyAttribute.setterName}(1L);
</#if>

            ${entity.name} entity = create${entity.name}();
<#if entity.primaryKeyAttribute.javaType == "String">
            entity.${entity.primaryKeyAttribute.setterName}("CREATED_ID");
<#else>
            entity.${entity.primaryKeyAttribute.setterName}(1L);
</#if>
            List<${entity.name}> entityList = Arrays.asList(entity);
            Page<${entity.name}> ${entity.variableName}Page = new PageImpl<>(entityList, PageRequest.of(0, 10), 1);

            when(${entity.variableName}Service.getAll${entity.name}s(any(Pageable.class))).thenReturn(${entity.variableName}Page);

            mockMvc.perform(get("/api/${entity.pluralName}/paginated")
                    .param("page", "0")
                    .param("size", "10")
                    .param("sort", "id,asc")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.size").value(10))
                    .andExpect(jsonPath("$.number").value(0));

            verify(${entity.variableName}Service).getAll${entity.name}s(any(Pageable.class));
        }



        @Test
        @DisplayName("Should handle very large page size parameters")
        void requestWithLargePageSizeShouldReturnBadRequest() throws Exception {
            mockMvc.perform(get("/api/${entity.pluralName}/paginated")
                    .param("page", "0")
                    .param("size", "10000")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(${entity.variableName}Service, never()).getAll${entity.name}s(any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle missing required headers gracefully")
        void requestWithoutRequiredHeadersShouldWork() throws Exception {
            ${entity.dtoClassName} ${entity.variableName}Dto = create${entity.dtoClassName}();
<#if entity.primaryKeyAttribute.javaType == "String">
            ${entity.variableName}Dto.${entity.primaryKeyAttribute.setterName}("CREATED_ID");
<#else>
            ${entity.variableName}Dto.${entity.primaryKeyAttribute.setterName}(1L);
</#if>

            ${entity.name} entity = create${entity.name}();
<#if entity.primaryKeyAttribute.javaType == "String">
            entity.${entity.primaryKeyAttribute.setterName}("CREATED_ID");
<#else>
            entity.${entity.primaryKeyAttribute.setterName}(1L);
</#if>
            List<${entity.name}> entityList = Arrays.asList(entity);

            when(${entity.variableName}Service.getAll${entity.name}s()).thenReturn(entityList);

            mockMvc.perform(get("/api/${entity.pluralName}"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)));

            verify(${entity.variableName}Service).getAll${entity.name}s();
        }

        @Test
        @DisplayName("Should handle malformed query parameters")
        void requestWithMalformedQueryParametersShouldWork() throws Exception {
            ${entity.dtoClassName} ${entity.variableName}Dto = create${entity.dtoClassName}();
<#if entity.primaryKeyAttribute.javaType == "String">
            ${entity.variableName}Dto.${entity.primaryKeyAttribute.setterName}("CREATED_ID");
<#else>
            ${entity.variableName}Dto.${entity.primaryKeyAttribute.setterName}(1L);
</#if>

            ${entity.name} entity = create${entity.name}();
<#if entity.primaryKeyAttribute.javaType == "String">
            entity.${entity.primaryKeyAttribute.setterName}("CREATED_ID");
<#else>
            entity.${entity.primaryKeyAttribute.setterName}(1L);
</#if>
            List<${entity.name}> entityList = Arrays.asList(entity);

            when(${entity.variableName}Service.getAll${entity.name}s()).thenReturn(entityList);

            mockMvc.perform(get("/api/${entity.pluralName}")
                    .param("invalid_param", "value")
                    .param("malformed_param", "")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)));

            verify(${entity.variableName}Service).getAll${entity.name}s();
        }

        @Test
        @DisplayName("Should handle special characters in query parameters")
        void requestWithSpecialCharactersInParametersShouldWork() throws Exception {
            ${entity.dtoClassName} ${entity.variableName}Dto = create${entity.dtoClassName}();
<#if entity.primaryKeyAttribute.javaType == "String">
            ${entity.variableName}Dto.${entity.primaryKeyAttribute.setterName}("CREATED_ID");
<#else>
            ${entity.variableName}Dto.${entity.primaryKeyAttribute.setterName}(1L);
</#if>

            ${entity.name} entity = create${entity.name}();
<#if entity.primaryKeyAttribute.javaType == "String">
            entity.${entity.primaryKeyAttribute.setterName}("CREATED_ID");
<#else>
            entity.${entity.primaryKeyAttribute.setterName}(1L);
</#if>
            List<${entity.name}> entityList = Arrays.asList(entity);

            when(${entity.variableName}Service.getAll${entity.name}s()).thenReturn(entityList);

            mockMvc.perform(get("/api/${entity.pluralName}")
                    .param("search", "Oussama & Hicham's café")
                    .param("filter", "name=Mohammed,age>25")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)));

            verify(${entity.variableName}Service).getAll${entity.name}s();
        }

        @Test
        @DisplayName("Should return proper Content-Type header")
        void responseShouldHaveProperContentTypeHeader() throws Exception {
            ${entity.dtoClassName} ${entity.variableName}Dto = create${entity.dtoClassName}();
<#if entity.primaryKeyAttribute.javaType == "String">
            ${entity.variableName}Dto.${entity.primaryKeyAttribute.setterName}("CREATED_ID");
<#else>
            ${entity.variableName}Dto.${entity.primaryKeyAttribute.setterName}(1L);
</#if>

            ${entity.name} entity = create${entity.name}();
<#if entity.primaryKeyAttribute.javaType == "String">
            entity.${entity.primaryKeyAttribute.setterName}("CREATED_ID");
<#else>
            entity.${entity.primaryKeyAttribute.setterName}(1L);
</#if>
            List<${entity.name}> entityList = Arrays.asList(entity);

            when(${entity.variableName}Service.getAll${entity.name}s()).thenReturn(entityList);

            mockMvc.perform(get("/api/${entity.pluralName}")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)));

            verify(${entity.variableName}Service).getAll${entity.name}s();
        }



        @Test
        @DisplayName("Should handle multiple values for same query parameter")
        void requestWithMultipleParameterValuesShouldWork() throws Exception {
            ${entity.dtoClassName} ${entity.variableName}Dto = create${entity.dtoClassName}();
<#if entity.primaryKeyAttribute.javaType == "String">
            ${entity.variableName}Dto.${entity.primaryKeyAttribute.setterName}("CREATED_ID");
<#else>
            ${entity.variableName}Dto.${entity.primaryKeyAttribute.setterName}(1L);
</#if>

            ${entity.name} entity = create${entity.name}();
<#if entity.primaryKeyAttribute.javaType == "String">
            entity.${entity.primaryKeyAttribute.setterName}("CREATED_ID");
<#else>
            entity.${entity.primaryKeyAttribute.setterName}(1L);
</#if>
            List<${entity.name}> entityList = Arrays.asList(entity);

            when(${entity.variableName}Service.getAll${entity.name}s()).thenReturn(entityList);

            mockMvc.perform(get("/api/${entity.pluralName}")
                    .param("tags", "tag1", "tag2", "tag3")
                    .param("categories", "cat1", "cat2")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)));

            verify(${entity.variableName}Service).getAll${entity.name}s();
        }
    }
