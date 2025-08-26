    @Nested
    @DisplayName("Status Code Tests")
    class StatusCodeTests {

        @Test
        @DisplayName("Should return 201 Created with proper Location header on successful creation")
        void create${entity.name}ShouldReturn201WithLocationHeader() throws Exception {
            ${entity.dtoClassName} requestDto = create${entity.dtoClassName}();
            ${entity.dtoClassName} responseDto = create${entity.dtoClassName}();
<#if entity.primaryKeyAttribute.javaType == "String">
            responseDto.${entity.primaryKeyAttribute.setterName}("CREATED_ID");
<#else>
            responseDto.${entity.primaryKeyAttribute.setterName}(1L);
</#if>

            when(${entity.variableName}Service.create${entity.name}(any(${entity.name}.class))).thenReturn(createMockEntity());

            mockMvc.perform(post("/api/${entity.pluralName}")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated())
                    .andExpect(header().exists("Location"))
<#if entity.primaryKeyAttribute.javaType == "String">
                    .andExpect(header().string("Location", containsString("CREATED_ID")))
<#else>
                    .andExpect(header().string("Location", containsString("1")))
</#if>
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));

            verify(${entity.variableName}Service).create${entity.name}(any(${entity.name}.class));
        }

        @Test
        @DisplayName("Should return 200 OK on successful retrieval")
        void get${entity.name}ByIdShouldReturn200() throws Exception {
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
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));

            verify(${entity.variableName}Service).get${entity.name}ById(${entity.variableName}Id);
        }

        @Test
        @DisplayName("Should return 200 OK on successful update")
        void update${entity.name}ShouldReturn200() throws Exception {
            ${entity.dtoClassName} requestDto = create${entity.dtoClassName}();
            ${entity.dtoClassName} responseDto = create${entity.dtoClassName}();
<#if entity.primaryKeyAttribute.javaType == "String">
            String ${entity.variableName}Id = "CREATED_ID";
            responseDto.${entity.primaryKeyAttribute.setterName}(${entity.variableName}Id);
<#else>
            Long ${entity.variableName}Id = 1L;
            responseDto.${entity.primaryKeyAttribute.setterName}(${entity.variableName}Id);
</#if>

            when(${entity.variableName}Service.update${entity.name}(eq(${entity.variableName}Id), any(${entity.name}.class)))
                    .thenReturn(createMockEntity());

            mockMvc.perform(put("/api/${entity.pluralName}/{id}", ${entity.variableName}Id)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));

            verify(${entity.variableName}Service).update${entity.name}(eq(${entity.variableName}Id), any(${entity.name}.class));
        }

        @Test
        @DisplayName("Should return 204 No Content on successful deletion")
        void delete${entity.name}ShouldReturn204() throws Exception {
<#if entity.primaryKeyAttribute.javaType == "String">
            String ${entity.variableName}Id = "CREATED_ID";
<#else>
            Long ${entity.variableName}Id = 1L;
</#if>

            doNothing().when(${entity.variableName}Service).delete${entity.name}(${entity.variableName}Id);

            mockMvc.perform(delete("/api/${entity.pluralName}/{id}", ${entity.variableName}Id)
                    .with(csrf()))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            verify(${entity.variableName}Service).delete${entity.name}(${entity.variableName}Id);
        }

        @Test
        @DisplayName("Should return 404 Not Found when entity does not exist")
        void get${entity.name}ByIdShouldReturn404WhenNotFound() throws Exception {
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
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(${entity.variableName}Service).get${entity.name}ById(nonExistentId);
        }

<#if hasStrictValidation>
        @Test
        @DisplayName("Should return 400 Bad Request on validation failure")
        void create${entity.name}WithInvalidDataShouldReturn400() throws Exception {
            ${entity.dtoClassName} invalidDto = ${entity.dtoClassName}.builder().build();

            mockMvc.perform(post("/api/${entity.pluralName}")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.errors").isArray())
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(${entity.variableName}Service, never()).create${entity.name}(any());
        }
<#else>
        @Test
        @DisplayName("Should handle empty DTO when no validation rules exist")
        void create${entity.name}WithEmptyDataShouldSucceed() throws Exception {
            ${entity.dtoClassName} emptyDto = ${entity.dtoClassName}.builder().build();

            when(${entity.variableName}Service.create${entity.name}(any(${entity.name}.class))).thenReturn(createMockEntity());

            mockMvc.perform(post("/api/${entity.pluralName}")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(emptyDto)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));

            verify(${entity.variableName}Service).create${entity.name}(any(${entity.name}.class));
        }
</#if>

        @Test
        @DisplayName("Should return 415 Unsupported Media Type for wrong content type")
        void create${entity.name}WithWrongContentTypeShouldReturn415() throws Exception {
            ${entity.dtoClassName} requestDto = create${entity.dtoClassName}();

            mockMvc.perform(post("/api/${entity.pluralName}")
                    .with(csrf())
                    .contentType(MediaType.TEXT_PLAIN)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isUnsupportedMediaType())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(415))
                    .andExpect(jsonPath("$.error").value("Unsupported Media Type"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(${entity.variableName}Service, never()).create${entity.name}(any());
        }

        @Test
        @DisplayName("Should return 500 Internal Server Error on service exception")
        void get${entity.name}ByIdShouldReturn500OnServiceException() throws Exception {
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
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.error").value("Internal Server Error"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(${entity.variableName}Service).get${entity.name}ById(${entity.variableName}Id);
        }

        @Test
        @DisplayName("Should return 409 Conflict on duplicate resource creation")
        void create${entity.name}WithDuplicateDataShouldReturn409() throws Exception {
            ${entity.dtoClassName} requestDto = create${entity.dtoClassName}();

            when(${entity.variableName}Service.create${entity.name}(any(${entity.name}.class)))
                    .thenThrow(new BadRequestException("${entity.name} already exists"));

            mockMvc.perform(post("/api/${entity.pluralName}")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value("${entity.name} already exists"))
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(${entity.variableName}Service).create${entity.name}(any(${entity.name}.class));
        }


    }
