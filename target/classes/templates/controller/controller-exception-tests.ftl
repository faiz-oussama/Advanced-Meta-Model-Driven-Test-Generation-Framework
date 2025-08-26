    @Nested
    @DisplayName("Exception Handling Tests")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("Should return 404 when entity not found on GET")
        void get${entity.name}ByIdShouldReturn404WhenEntityNotFound() throws Exception {
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
                    .andExpect(jsonPath("$.message").value("${entity.name} not found with id: " + nonExistentId))
                    .andExpect(jsonPath("$.path").value("/api/${entity.pluralName}/" + nonExistentId))
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(${entity.variableName}Service).get${entity.name}ById(nonExistentId);
        }

        @Test
        @DisplayName("Should return 404 when entity not found on UPDATE")
        void update${entity.name}ShouldReturn404WhenEntityNotFound() throws Exception {
<#if entity.primaryKeyAttribute.javaType == "String">
            String nonExistentId = "NONEXISTENT_ID";
<#else>
            Long nonExistentId = 999L;
</#if>
            ${entity.dtoClassName} requestDto = create${entity.dtoClassName}();

            when(${entity.variableName}Service.update${entity.name}(eq(nonExistentId), any(${entity.name}.class)))
                    .thenThrow(new EntityNotFoundException("${entity.name} not found with id: " + nonExistentId));

            mockMvc.perform(put("/api/${entity.pluralName}/{id}", nonExistentId)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value("${entity.name} not found with id: " + nonExistentId))
                    .andExpect(jsonPath("$.path").value("/api/${entity.pluralName}/" + nonExistentId))
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(${entity.variableName}Service).update${entity.name}(eq(nonExistentId), any(${entity.name}.class));
        }

        @Test
        @DisplayName("Should return 404 when entity not found on DELETE")
        void delete${entity.name}ShouldReturn404WhenEntityNotFound() throws Exception {
<#if entity.primaryKeyAttribute.javaType == "String">
            String nonExistentId = "NONEXISTENT_ID";
<#else>
            Long nonExistentId = 999L;
</#if>

            doThrow(new EntityNotFoundException("${entity.name} not found with id: " + nonExistentId))
                    .when(${entity.variableName}Service).delete${entity.name}(nonExistentId);

            mockMvc.perform(delete("/api/${entity.pluralName}/{id}", nonExistentId)
                    .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value("${entity.name} not found with id: " + nonExistentId))
                    .andExpect(jsonPath("$.path").value("/api/${entity.pluralName}/" + nonExistentId))
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(${entity.variableName}Service).delete${entity.name}(nonExistentId);
        }

        @Test
        @DisplayName("Should return 500 when service throws unexpected exception")
        void create${entity.name}ShouldReturn500OnUnexpectedException() throws Exception {
            ${entity.dtoClassName} requestDto = create${entity.dtoClassName}();

            when(${entity.variableName}Service.create${entity.name}(any(${entity.name}.class)))
                    .thenThrow(new RuntimeException("Unexpected database error"));

            mockMvc.perform(post("/api/${entity.pluralName}")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.error").value("Internal Server Error"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.path").value("/api/${entity.pluralName}"))
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(${entity.variableName}Service).create${entity.name}(any(${entity.name}.class));
        }

        @Test
        @DisplayName("Should return 400 when service throws BadRequestException")
        void create${entity.name}ShouldReturn400OnBadRequestException() throws Exception {
            ${entity.dtoClassName} requestDto = create${entity.dtoClassName}();

            when(${entity.variableName}Service.create${entity.name}(any(${entity.name}.class)))
                    .thenThrow(new BadRequestException("Invalid data provided"));

            mockMvc.perform(post("/api/${entity.pluralName}")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").value("Invalid data provided"))
                    .andExpect(jsonPath("$.path").value("/api/${entity.pluralName}"))
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(${entity.variableName}Service).create${entity.name}(any(${entity.name}.class));
        }

        @Test
        @DisplayName("Should return 400 when bad request exception occurs")
        void create${entity.name}ShouldReturn400WhenBadRequestException() throws Exception {
            ${entity.dtoClassName} requestDto = create${entity.dtoClassName}();
            String errorMessage = "Invalid data provided";

            when(${entity.variableName}Service.create${entity.name}(any(${entity.name}.class)))
                    .thenThrow(new BadRequestException(errorMessage));

            mockMvc.perform(post("/api/${entity.pluralName}")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").value(errorMessage))
                    .andExpect(jsonPath("$.path").value("/api/${entity.pluralName}"))
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(${entity.variableName}Service).create${entity.name}(any(${entity.name}.class));
        }

        @Test
        @DisplayName("Should return 500 when internal server error occurs")
        void get${entity.name}ShouldReturn500WhenInternalServerError() throws Exception {
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
                    .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
                    .andExpect(jsonPath("$.path").value("/api/${entity.pluralName}/" + ${entity.variableName}Id))
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(${entity.variableName}Service).get${entity.name}ById(${entity.variableName}Id);
        }

        @Test
        @DisplayName("Should return 405 when HTTP method not allowed")
        void shouldReturn405WhenMethodNotAllowed() throws Exception {
            mockMvc.perform(patch("/api/${entity.pluralName}")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isMethodNotAllowed())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(405))
                    .andExpect(jsonPath("$.error").value("Method Not Allowed"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.path").value("/api/${entity.pluralName}"))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("Should return 406 when accept header is not supported")
        void shouldReturn406WhenAcceptHeaderNotSupported() throws Exception {
<#if entity.primaryKeyAttribute.javaType == "String">
            String ${entity.variableName}Id = "CREATED_ID";
<#else>
            Long ${entity.variableName}Id = 1L;
</#if>
            ${entity.dtoClassName} responseDto = create${entity.dtoClassName}();
            responseDto.${entity.primaryKeyAttribute.setterName}(${entity.variableName}Id);

            when(${entity.variableName}Service.get${entity.name}ById(${entity.variableName}Id)).thenReturn(createMockEntity());

            mockMvc.perform(get("/api/${entity.pluralName}/{id}", ${entity.variableName}Id)
                    .accept(MediaType.TEXT_XML))
                    .andExpect(status().isNotAcceptable())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(406))
                    .andExpect(jsonPath("$.error").value("Not Acceptable"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.path").value("/api/${entity.pluralName}/" + ${entity.variableName}Id))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("Should handle constraint violation exceptions properly")
        void create${entity.name}ShouldHandleConstraintViolationException() throws Exception {
            ${entity.dtoClassName} requestDto = create${entity.dtoClassName}();

            when(${entity.variableName}Service.create${entity.name}(any(${entity.name}.class)))
                    .thenThrow(new ConstraintViolationException("Constraint violation", null));

            mockMvc.perform(post("/api/${entity.pluralName}")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.path").value("/api/${entity.pluralName}"))
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(${entity.variableName}Service).create${entity.name}(any(${entity.name}.class));
        }
    }
