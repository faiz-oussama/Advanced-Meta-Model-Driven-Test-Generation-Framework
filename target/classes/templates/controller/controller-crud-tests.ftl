    @Nested
    @DisplayName("CRUD Operations")
    class CrudOperations {

        @Test
        @DisplayName("Should create ${entity.name} successfully with 201 status")
        void create${entity.name}ShouldReturnCreated() throws Exception {
            ${entity.dtoClassName} requestDto = create${entity.dtoClassName}();
            ${entity.dtoClassName} responseDto = create${entity.dtoClassName}();
<#if entity.primaryKeyAttribute.javaType == "String">
            responseDto.${entity.primaryKeyAttribute.setterName}("CREATED_ID");
<#elseif entity.primaryKeyAttribute.javaType == "Long">
            responseDto.${entity.primaryKeyAttribute.setterName}(1L);
<#elseif entity.primaryKeyAttribute.javaType == "Integer">
            responseDto.${entity.primaryKeyAttribute.setterName}(1);
<#else>
            responseDto.${entity.primaryKeyAttribute.setterName}(1L);
</#if>

            ${entity.name} mockEntity = create${entity.name}();
<#if entity.primaryKeyAttribute.javaType == "String">
            mockEntity.${entity.primaryKeyAttribute.setterName}("CREATED_ID");
<#elseif entity.primaryKeyAttribute.javaType == "Long">
            mockEntity.${entity.primaryKeyAttribute.setterName}(1L);
<#elseif entity.primaryKeyAttribute.javaType == "Integer">
            mockEntity.${entity.primaryKeyAttribute.setterName}(1);
<#else>
            mockEntity.${entity.primaryKeyAttribute.setterName}(1L);
</#if>
            when(${entity.variableName}Service.create${entity.name}(any(${entity.name}.class))).thenReturn(createMockEntity());

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
        @DisplayName("Should get ${entity.name} by ID successfully with 200 status")
        void get${entity.name}ByIdShouldReturnOk() throws Exception {
            ${entity.dtoClassName} responseDto = create${entity.dtoClassName}();
<#if entity.primaryKeyAttribute.javaType == "String">
            String ${entity.variableName}Id = "CREATED_ID";
            responseDto.${entity.primaryKeyAttribute.setterName}(${entity.variableName}Id);
<#elseif entity.primaryKeyAttribute.javaType == "Long">
            Long ${entity.variableName}Id = 1L;
            responseDto.${entity.primaryKeyAttribute.setterName}(${entity.variableName}Id);
<#elseif entity.primaryKeyAttribute.javaType == "Integer">
            Integer ${entity.variableName}Id = 1;
            responseDto.${entity.primaryKeyAttribute.setterName}(${entity.variableName}Id);
<#else>
            Object ${entity.variableName}Id = 1L;
            responseDto.${entity.primaryKeyAttribute.setterName}(${entity.variableName}Id);
</#if>

            ${entity.name} mockEntity = create${entity.name}();
<#if entity.primaryKeyAttribute.javaType == "String">
            mockEntity.${entity.primaryKeyAttribute.setterName}(${entity.variableName}Id);
<#else>
            mockEntity.${entity.primaryKeyAttribute.setterName}(${entity.variableName}Id);
</#if>
            when(${entity.variableName}Service.get${entity.name}ById(${entity.variableName}Id)).thenReturn(createMockEntity());

            mockMvc.perform(get("/api/${entity.pluralName}/{id}", ${entity.variableName}Id)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE))
<#if entity.primaryKeyAttribute.javaType == "String">
                    .andExpect(jsonPath("$.${entity.primaryKeyAttribute.name}").value("CREATED_ID"));
<#else>
                    .andExpect(jsonPath("$.${entity.primaryKeyAttribute.name}").value(1));
</#if>

            verify(${entity.variableName}Service).get${entity.name}ById(${entity.variableName}Id);
        }

        @Test
        @DisplayName("Should return 404 when ${entity.name} not found")
        void get${entity.name}ByIdShouldReturn404WhenNotFound() throws Exception {
<#if entity.primaryKeyAttribute.javaType == "String">
            String ${entity.variableName}Id = "NONEXISTENT_ID";
<#elseif entity.primaryKeyAttribute.javaType == "Long">
            Long ${entity.variableName}Id = 999L;
<#elseif entity.primaryKeyAttribute.javaType == "Integer">
            Integer ${entity.variableName}Id = 999;
<#else>
            Object ${entity.variableName}Id = 999L;
</#if>

            when(${entity.variableName}Service.get${entity.name}ById(${entity.variableName}Id))
                    .thenThrow(new EntityNotFoundException("${entity.name} not found with id: " + ${entity.variableName}Id));

            mockMvc.perform(get("/api/${entity.pluralName}/{id}", ${entity.variableName}Id)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message").value("${entity.name} not found with id: " + ${entity.variableName}Id))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(${entity.variableName}Service).get${entity.name}ById(${entity.variableName}Id);
        }

        @Test
        @DisplayName("Should update ${entity.name} successfully with 200 status")
        void update${entity.name}ShouldReturnOk() throws Exception {
            ${entity.dtoClassName} requestDto = create${entity.dtoClassName}();
            ${entity.dtoClassName} responseDto = create${entity.dtoClassName}();
<#if entity.primaryKeyAttribute.javaType == "String">
            String ${entity.variableName}Id = "CREATED_ID";
            responseDto.${entity.primaryKeyAttribute.setterName}(${entity.variableName}Id);
<#elseif entity.primaryKeyAttribute.javaType == "Long">
            Long ${entity.variableName}Id = 1L;
            responseDto.${entity.primaryKeyAttribute.setterName}(${entity.variableName}Id);
<#elseif entity.primaryKeyAttribute.javaType == "Integer">
            Integer ${entity.variableName}Id = 1;
            responseDto.${entity.primaryKeyAttribute.setterName}(${entity.variableName}Id);
<#else>
            Object ${entity.variableName}Id = 1L;
            responseDto.${entity.primaryKeyAttribute.setterName}(${entity.variableName}Id);
</#if>

            ${entity.name} mockEntity = create${entity.name}();
<#if entity.primaryKeyAttribute.javaType == "String">
            mockEntity.${entity.primaryKeyAttribute.setterName}(${entity.variableName}Id);
<#else>
            mockEntity.${entity.primaryKeyAttribute.setterName}(${entity.variableName}Id);
</#if>
            when(${entity.variableName}Service.update${entity.name}(eq(${entity.variableName}Id), any(${entity.name}.class)))
                    .thenReturn(createMockEntity());

            mockMvc.perform(put("/api/${entity.pluralName}/{id}", ${entity.variableName}Id)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE))
<#if entity.primaryKeyAttribute.javaType == "String">
                    .andExpect(jsonPath("$.${entity.primaryKeyAttribute.name}").value("CREATED_ID"));
<#else>
                    .andExpect(jsonPath("$.${entity.primaryKeyAttribute.name}").value(1));
</#if>

            verify(${entity.variableName}Service).update${entity.name}(eq(${entity.variableName}Id), any(${entity.name}.class));
        }

        @Test
        @DisplayName("Should delete ${entity.name} successfully with 204 status")
        void delete${entity.name}ShouldReturnNoContent() throws Exception {
<#if entity.primaryKeyAttribute.javaType == "String">
            String ${entity.variableName}Id = "CREATED_ID";
<#elseif entity.primaryKeyAttribute.javaType == "Long">
            Long ${entity.variableName}Id = 1L;
<#elseif entity.primaryKeyAttribute.javaType == "Integer">
            Integer ${entity.variableName}Id = 1;
<#else>
            Object ${entity.variableName}Id = 1L;
</#if>

            doNothing().when(${entity.variableName}Service).delete${entity.name}(${entity.variableName}Id);

            mockMvc.perform(delete("/api/${entity.pluralName}/{id}", ${entity.variableName}Id)
                    .with(csrf()))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            verify(${entity.variableName}Service).delete${entity.name}(${entity.variableName}Id);
        }

        @Test
        @DisplayName("Should get all ${entity.name}s successfully with 200 status")
        void getAll${entity.name}sShouldReturnOk() throws Exception {
            ${entity.dtoClassName} ${entity.variableName}Dto1 = create${entity.dtoClassName}();
            ${entity.dtoClassName} ${entity.variableName}Dto2 = create${entity.dtoClassName}();
<#if entity.primaryKeyAttribute.javaType == "String">
            ${entity.variableName}Dto1.${entity.primaryKeyAttribute.setterName}("ID_1");
            ${entity.variableName}Dto2.${entity.primaryKeyAttribute.setterName}("ID_2");
<#else>
            ${entity.variableName}Dto1.${entity.primaryKeyAttribute.setterName}(1L);
            ${entity.variableName}Dto2.${entity.primaryKeyAttribute.setterName}(2L);
</#if>

            List<${entity.dtoClassName}> ${entity.variableName}DtoList = Arrays.asList(${entity.variableName}Dto1, ${entity.variableName}Dto2);

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
                    .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(jsonPath("$", hasSize(2)))
<#if entity.primaryKeyAttribute.javaType == "String">
                    .andExpect(jsonPath("$[0].${entity.primaryKeyAttribute.name}").value("ID_1"))
                    .andExpect(jsonPath("$[1].${entity.primaryKeyAttribute.name}").value("ID_2"));
<#else>
                    .andExpect(jsonPath("$[0].${entity.primaryKeyAttribute.name}").value(1))
                    .andExpect(jsonPath("$[1].${entity.primaryKeyAttribute.name}").value(2));
</#if>

            verify(${entity.variableName}Service).getAll${entity.name}s();
        }
    }
