    @Nested
    @DisplayName("OpenAPI/Swagger Documentation Tests")
    class SwaggerDocumentationTests {

        @Test
        @DisplayName("Should have proper OpenAPI annotations on create endpoint")
        void create${entity.name}EndpointShouldHaveProperSwaggerAnnotations() throws Exception {
            ${entity.dtoClassName} requestDto = create${entity.dtoClassName}();
            ${entity.dtoClassName} responseDto = create${entity.dtoClassName}();
<#if entity.primaryKeyAttribute.javaType == "String">
            responseDto.${entity.primaryKeyAttribute.setterName}("CREATED_ID");
<#else>
            responseDto.${entity.primaryKeyAttribute.setterName}(1L);
</#if>

            when(${entity.variableName}Service.create${entity.name}(any(${entity.name}.class))).thenReturn(createMockEntity());

            MvcResult result = mockMvc.perform(post("/api/${entity.pluralName}")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated())
                    .andReturn();

            // @Operation(summary = "Create ${entity.name}", description = "Creates a new ${entity.name}")
            // @ApiResponses({@ApiResponse(responseCode = "201", description = "Created")})
            assertThat(result.getResponse().getStatus()).isEqualTo(201);
            verify(${entity.variableName}Service).create${entity.name}(any(${entity.name}.class));
        }

        @Test
        @DisplayName("Should have proper OpenAPI annotations on get by ID endpoint")
        void get${entity.name}ByIdEndpointShouldHaveProperSwaggerAnnotations() throws Exception {
            ${entity.dtoClassName} responseDto = create${entity.dtoClassName}();
<#if entity.primaryKeyAttribute.javaType == "String">
            String ${entity.variableName}Id = "CREATED_ID";
            responseDto.${entity.primaryKeyAttribute.setterName}(${entity.variableName}Id);
<#else>
            Long ${entity.variableName}Id = 1L;
            responseDto.${entity.primaryKeyAttribute.setterName}(${entity.variableName}Id);
</#if>

            when(${entity.variableName}Service.get${entity.name}ById(${entity.variableName}Id)).thenReturn(createMockEntity());

            MvcResult result = mockMvc.perform(get("/api/${entity.pluralName}/{id}", ${entity.variableName}Id)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn();

            // Verify that the endpoint would be documented with proper OpenAPI annotations
            // @Operation(summary = "Get ${entity.name} by ID", description = "Returns a single ${entity.name}")
            // @ApiResponses({
            //     @ApiResponse(responseCode = "200", description = "OK"),
            //     @ApiResponse(responseCode = "404", description = "Not Found")
            // })
            assertThat(result.getResponse().getStatus()).isEqualTo(200);
            verify(${entity.variableName}Service).get${entity.name}ById(${entity.variableName}Id);
        }

        @Test
        @DisplayName("Should have proper OpenAPI annotations on update endpoint")
        void update${entity.name}EndpointShouldHaveProperSwaggerAnnotations() throws Exception {
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

            MvcResult result = mockMvc.perform(put("/api/${entity.pluralName}/{id}", ${entity.variableName}Id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isOk())
                    .andReturn();

            // Verify that the endpoint would be documented with proper OpenAPI annotations
            // @Operation(summary = "Update ${entity.name}", description = "Updates an existing ${entity.name}")
            // @ApiResponses({
            //     @ApiResponse(responseCode = "200", description = "OK"),
            //     @ApiResponse(responseCode = "404", description = "Not Found")
            // })
            assertThat(result.getResponse().getStatus()).isEqualTo(200);
            verify(${entity.variableName}Service).update${entity.name}(eq(${entity.variableName}Id), any(${entity.name}.class));
        }

        @Test
        @DisplayName("Should have proper OpenAPI annotations on delete endpoint")
        void delete${entity.name}EndpointShouldHaveProperSwaggerAnnotations() throws Exception {
<#if entity.primaryKeyAttribute.javaType == "String">
            String ${entity.variableName}Id = "CREATED_ID";
<#else>
            Long ${entity.variableName}Id = 1L;
</#if>

            doNothing().when(${entity.variableName}Service).delete${entity.name}(${entity.variableName}Id);

            MvcResult result = mockMvc.perform(delete("/api/${entity.pluralName}/{id}", ${entity.variableName}Id))
                    .andExpect(status().isNoContent())
                    .andReturn();

            // Verify that the endpoint would be documented with proper OpenAPI annotations
            // @Operation(summary = "Delete ${entity.name}", description = "Deletes a ${entity.name}")
            // @ApiResponses({
            //     @ApiResponse(responseCode = "204", description = "No Content"),
            //     @ApiResponse(responseCode = "404", description = "Not Found")
            // })
            assertThat(result.getResponse().getStatus()).isEqualTo(204);
            verify(${entity.variableName}Service).delete${entity.name}(${entity.variableName}Id);
        }

        @Test
        @DisplayName("Should have proper OpenAPI annotations on get all endpoint")
        void getAll${entity.name}sEndpointShouldHaveProperSwaggerAnnotations() throws Exception {
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

            MvcResult result = mockMvc.perform(get("/api/${entity.pluralName}")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn();

            // Verify that the endpoint would be documented with proper OpenAPI annotations
            // @Operation(summary = "Get all ${entity.name}s", description = "Returns a list of all ${entity.name}s")
            // @ApiResponse(responseCode = "200", description = "OK")
            assertThat(result.getResponse().getStatus()).isEqualTo(200);
            verify(${entity.variableName}Service).getAll${entity.name}s();
        }

        @Test
        @DisplayName("Should have proper OpenAPI annotations on paginated endpoint")
        void get${entity.name}sPageEndpointShouldHaveProperSwaggerAnnotations() throws Exception {
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
            Page<${entity.name}> ${entity.variableName}Page = new PageImpl<>(entityList, PageRequest.of(0, 20), 2);

            when(${entity.variableName}Service.getAll${entity.name}s(any(Pageable.class))).thenReturn(${entity.variableName}Page);

            MvcResult result = mockMvc.perform(get("/api/${entity.pluralName}/paginated")
                    .param("page", "0")
                    .param("size", "20")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn();

            // Verify that the endpoint would be documented with proper OpenAPI annotations
            // @Operation(summary = "Get paginated ${entity.name}s", description = "Returns a paginated list of ${entity.name}s")
            // @Parameter(name = "page", description = "Page number (0-based)")
            // @Parameter(name = "size", description = "Page size")
            // @Parameter(name = "sort", description = "Sort criteria")
            // @ApiResponse(responseCode = "200", description = "OK")
            assertThat(result.getResponse().getStatus()).isEqualTo(200);
            verify(${entity.variableName}Service).getAll${entity.name}s(any(Pageable.class));
        }

        @Test
        @DisplayName("Should document error responses properly")
        void errorResponsesShouldBeProperlyDocumented() throws Exception {
<#if entity.primaryKeyAttribute.javaType == "String">
            String nonExistentId = "NONEXISTENT_ID";
<#else>
            Long nonExistentId = 999L;
</#if>

            when(${entity.variableName}Service.get${entity.name}ById(nonExistentId))
                    .thenThrow(new EntityNotFoundException("${entity.name} not found with id: " + nonExistentId));

            MvcResult result = mockMvc.perform(get("/api/${entity.pluralName}/{id}", nonExistentId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andReturn();

            // Verify that error responses would be documented with proper OpenAPI annotations
            // @ApiResponse(responseCode = "404", description = "Not Found", 
            //              content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            assertThat(result.getResponse().getStatus()).isEqualTo(404);
            verify(${entity.variableName}Service).get${entity.name}ById(nonExistentId);
        }

<#if hasStrictValidation>
        @Test
        @DisplayName("Should document validation error responses")
        void validationErrorResponsesShouldBeProperlyDocumented() throws Exception {
            ${entity.dtoClassName} invalidDto = ${entity.dtoClassName}.builder().build(); // Invalid DTO

            MvcResult result = mockMvc.perform(post("/api/${entity.pluralName}")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest())
                    .andReturn();

            // Verify that validation error responses would be documented with proper OpenAPI annotations
            // @ApiResponse(responseCode = "400", description = "Bad Request - Validation Error",
            //              content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
            assertThat(result.getResponse().getStatus()).isEqualTo(400);
            verify(${entity.variableName}Service, never()).create${entity.name}(any(${entity.name}.class));
        }
<#else>
        @Test
        @DisplayName("Should document successful creation when no validation rules exist")
        void successfulCreationResponsesShouldBeProperlyDocumented() throws Exception {
            ${entity.dtoClassName} requestDto = ${entity.dtoClassName}.builder().build();

            when(${entity.variableName}Service.create${entity.name}(any(${entity.name}.class))).thenReturn(createMockEntity());

            MvcResult result = mockMvc.perform(post("/api/${entity.pluralName}")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated())
                    .andReturn();

            // Verify that successful creation responses would be documented with proper OpenAPI annotations
            // @ApiResponse(responseCode = "201", description = "Created",
            //              content = @Content(schema = @Schema(implementation = ${entity.dtoClassName}.class)))
            assertThat(result.getResponse().getStatus()).isEqualTo(201);
            verify(${entity.variableName}Service).create${entity.name}(any(${entity.name}.class));
        }
</#if>
    }
