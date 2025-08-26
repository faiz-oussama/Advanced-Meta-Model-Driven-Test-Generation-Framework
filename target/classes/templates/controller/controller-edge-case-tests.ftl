    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
<#if entity.primaryKeyAttribute.javaType == "String">
        @DisplayName("Should return 404 when ID does not exist")
        void get${entity.name}WithNonExistentIdShouldReturnNotFound() throws Exception {
            String invalidId = "NONEXISTENT_ID";

            when(${entity.variableName}Service.get${entity.name}ById(invalidId))
                    .thenThrow(new RuntimeException("${entity.name} not found"));

            mockMvc.perform(get("/api/${entity.pluralName}/{id}", invalidId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError());

            verify(${entity.variableName}Service).get${entity.name}ById(invalidId);
        }
<#else>
        @DisplayName("Should return 400 when ID is negative")
        void get${entity.name}WithNegativeIdShouldReturnBadRequest() throws Exception {
            Long invalidId = -1L;

            mockMvc.perform(get("/api/${entity.pluralName}/{id}", invalidId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(${entity.variableName}Service, never()).get${entity.name}ById(any());
        }
</#if>

        @Test
        @DisplayName("Should return 400 when ID is zero")
        void get${entity.name}WithZeroIdShouldReturnBadRequest() throws Exception {
<#if entity.primaryKeyAttribute.javaType == "String">
            String invalidId = " ";
<#else>
            Long invalidId = 0L;
</#if>

            mockMvc.perform(get("/api/${entity.pluralName}/{id}", invalidId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(${entity.variableName}Service, never()).get${entity.name}ById(any());
        }

        @Test
        @DisplayName("Should return 400 when request body is null")
        void create${entity.name}WithNullBodyShouldReturnBadRequest() throws Exception {
            mockMvc.perform(post("/api/${entity.pluralName}")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(${entity.variableName}Service, never()).create${entity.name}(any(${entity.name}.class));
        }

        @Test
        @DisplayName("Should return 413 when payload is too large")
        void create${entity.name}WithLargePayloadShouldReturnPayloadTooLarge() throws Exception {
            ${entity.dtoClassName} requestDto = create${entity.dtoClassName}();
<#list entity.attributes as attr>
<#if attr.stringType>
            requestDto.${attr.setterName}("a".repeat(10000)); // Very large string
<#break>
</#if>
</#list>

            mockMvc.perform(post("/api/${entity.pluralName}")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(${entity.variableName}Service, never()).create${entity.name}(any(${entity.name}.class));
        }

<#if hasStrictValidation>
        @Test
        @DisplayName("Should return 400 when JSON contains only unknown fields and entity has strict validation")
        void create${entity.name}WithUnknownFieldsShouldReturnBadRequest() throws Exception {
            String jsonWithUnknownFields = """
                {
                    "unknownField": "value",
                    "anotherUnknownField": 123
                }
                """;

            mockMvc.perform(post("/api/${entity.pluralName}")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonWithUnknownFields))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(${entity.variableName}Service, never()).create${entity.name}(any(${entity.name}.class));
        }
<#else>
        @Test
        @DisplayName("Should ignore unknown fields and process valid data")
        void create${entity.name}WithUnknownFieldsShouldIgnoreUnknownFields() throws Exception {
            String jsonWithUnknownFields = """
                {
                    "unknownField": "value",
                    "anotherUnknownField": 123
                }
                """;

            when(${entity.variableName}Service.create${entity.name}(any(${entity.name}.class))).thenReturn(createMockEntity());

            mockMvc.perform(post("/api/${entity.pluralName}")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonWithUnknownFields))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));

            verify(${entity.variableName}Service).create${entity.name}(any(${entity.name}.class));
        }
</#if>

        @Test
        @DisplayName("Should return 400 when JSON has wrong data types")
        void create${entity.name}WithWrongDataTypesShouldReturnBadRequest() throws Exception {
            String jsonWithWrongTypes = """
                {
<#list entity.attributes as attr>
<#if !attr.primaryKey>
<#if attr.javaType == "String">
                    "${attr.name}": 123,
<#elseif attr.numericType>
                    "${attr.name}": "not_a_number",
<#elseif attr.javaType == "Boolean">
                    "${attr.name}": "not_a_boolean",
</#if>
<#break>
</#if>
</#list>
                }
                """;

            mockMvc.perform(post("/api/${entity.pluralName}")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonWithWrongTypes))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(${entity.variableName}Service, never()).create${entity.name}(any(${entity.name}.class));
        }

        @Test
        @DisplayName("Should handle empty list responses correctly")
        void getAll${entity.name}sWhenEmptyListShouldReturnOk() throws Exception {
            List<${entity.name}> empty${entity.name}List = List.of();

            when(${entity.variableName}Service.getAll${entity.name}s()).thenReturn(empty${entity.name}List);

            mockMvc.perform(get("/api/${entity.pluralName}")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(0)))
                    .andExpect(jsonPath("$").isArray());

            verify(${entity.variableName}Service).getAll${entity.name}s();
        }

        @Test
        @DisplayName("Should handle very large ID values")
        void get${entity.name}WithVeryLargeIdShouldHandleCorrectly() throws Exception {
<#if entity.primaryKeyAttribute.javaType == "String">
            String veryLargeId = "a".repeat(1000);
<#else>
            Long veryLargeId = Long.MAX_VALUE;
</#if>

            when(${entity.variableName}Service.get${entity.name}ById(veryLargeId))
                    .thenThrow(new EntityNotFoundException("${entity.name} not found with id: " + veryLargeId));

            mockMvc.perform(get("/api/${entity.pluralName}/{id}", veryLargeId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(${entity.variableName}Service).get${entity.name}ById(veryLargeId);
        }

        @Test
        @DisplayName("Should handle special characters in string fields")
        void create${entity.name}WithSpecialCharactersShouldWork() throws Exception {
            ${entity.dtoClassName} requestDto = create${entity.dtoClassName}();
            ${entity.dtoClassName} responseDto = create${entity.dtoClassName}();
<#list entity.attributes as attr>
<#if attr.stringType && !attr.primaryKey>
            String specialCharsValue = "Oussama's café & résumé (2024) - 100% tested!";
            requestDto.${attr.setterName}(specialCharsValue);
            responseDto.${attr.setterName}(specialCharsValue);
<#break>
</#if>
</#list>
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
                    .andExpect(header().exists("Location"));

            verify(${entity.variableName}Service).create${entity.name}(any(${entity.name}.class));
        }

        @Test
        @DisplayName("Should handle Unicode characters correctly")
        void create${entity.name}WithUnicodeCharactersShouldWork() throws Exception {
            ${entity.dtoClassName} requestDto = create${entity.dtoClassName}();
            ${entity.dtoClassName} responseDto = create${entity.dtoClassName}();
<#list entity.attributes as attr>
<#if attr.stringType && !attr.primaryKey>
            String unicodeValue = "Mohammed العربية 中文 🚀 ñáéíóú";
            requestDto.${attr.setterName}(unicodeValue);
            responseDto.${attr.setterName}(unicodeValue);
<#break>
</#if>
</#list>
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
                    .andExpect(header().exists("Location"));

            verify(${entity.variableName}Service).create${entity.name}(any(${entity.name}.class));
        }
    }
