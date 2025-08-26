    @Nested
    @DisplayName("HTTP Method and Path Tests")
    class HttpMethodAndPathTests {


        @Test
        @DisplayName("Should return 405 for unsupported HTTP methods on existing paths")
        void unsupportedHttpMethodShouldReturn405() throws Exception {
            mockMvc.perform(patch("/api/${entity.pluralName}")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isMethodNotAllowed())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(405))
                    .andExpect(jsonPath("$.error").value("Method Not Allowed"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.path").value("/api/${entity.pluralName}"))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("Should return 405 when using PATCH on specific resource")
        void patchOnSpecificResourceShouldReturn405() throws Exception {
<#if entity.primaryKeyAttribute.javaType == "String">
            String ${entity.variableName}Id = "CREATED_ID";
<#else>
            Long ${entity.variableName}Id = 1L;
</#if>

            mockMvc.perform(patch("/api/${entity.pluralName}/{id}", ${entity.variableName}Id)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isMethodNotAllowed())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(405))
                    .andExpect(jsonPath("$.error").value("Method Not Allowed"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("Should return 200 when using HEAD on collection (Spring Boot allows HEAD for GET)")
        void headOnCollectionShouldReturn200() throws Exception {
            List<${entity.name}> mockEntities = Arrays.asList(createMockEntity());
            when(${entity.variableName}Service.getAll${entity.name}s()).thenReturn(mockEntities);

            mockMvc.perform(head("/api/${entity.pluralName}")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE));

            verify(${entity.variableName}Service).getAll${entity.name}s();
        }

        @Test
        @DisplayName("Should return 200 when using OPTIONS on specific resource (Spring Boot handles OPTIONS)")
        void optionsOnSpecificResourceShouldReturn200() throws Exception {
<#if entity.primaryKeyAttribute.javaType == "String">
            String ${entity.variableName}Id = "CREATED_ID";
<#else>
            Long ${entity.variableName}Id = 1L;
</#if>

            mockMvc.perform(options("/api/${entity.pluralName}/{id}", ${entity.variableName}Id))
                    .andExpect(status().isOk())
                    .andExpect(header().exists("Allow"));
        }

        @Test
        @DisplayName("Should handle trailing slashes correctly")
        void pathWithTrailingSlashShouldWork() throws Exception {
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

            mockMvc.perform(get("/api/${entity.pluralName}/")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)));

            verify(${entity.variableName}Service).getAll${entity.name}s();
        }

        @Test
        @DisplayName("Should handle case sensitivity in paths correctly")
        void pathCaseSensitivityShouldReturn404() throws Exception {
            mockMvc.perform(get("/api/${entity.nameCapitalized?upper_case}S")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.error").value("Internal Server Error"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("Should handle double slashes in paths gracefully")
        void pathWithDoubleSlashesShouldNormalizePath() throws Exception {
            when(${entity.variableName}Service.getAll${entity.name}s()).thenReturn(Arrays.asList(createMockEntity()));

            mockMvc.perform(get("/api//${entity.pluralName}")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));

            verify(${entity.variableName}Service).getAll${entity.name}s();
        }

        @Test
        @DisplayName("Should handle missing Accept header gracefully")
        void requestWithoutAcceptHeaderShouldWork() throws Exception {
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
        @DisplayName("Should return 406 for unsupported Accept header")
        void requestWithWrongAcceptHeaderShouldReturn406() throws Exception {
            mockMvc.perform(get("/api/${entity.pluralName}")
                    .accept(MediaType.TEXT_XML))
                    .andExpect(status().isNotAcceptable())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(406))
                    .andExpect(jsonPath("$.error").value("Not Acceptable"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(${entity.variableName}Service, never()).getAll${entity.name}s();
        }

        @Test
        @DisplayName("Should handle multiple Accept headers correctly")
        void requestWithMultipleAcceptHeadersShouldWork() throws Exception {
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
                    .accept(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)));

            verify(${entity.variableName}Service).getAll${entity.name}s();
        }
    }
