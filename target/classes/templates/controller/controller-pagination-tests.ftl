    @Nested
    @DisplayName("Pagination Tests")
    class PaginationTests {

        @Test
        @DisplayName("Should return paginated ${entity.name}s with default parameters and proper headers")
        void get${entity.name}sWithDefaultPaginationShouldReturnOk() throws Exception {
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
            List<${entity.name}> ${entity.variableName}List = Arrays.asList(entity1, entity2);
            Page<${entity.name}> ${entity.variableName}Page = new PageImpl<>(${entity.variableName}List, PageRequest.of(0, 20), 2);

            when(${entity.variableName}Service.getAll${entity.name}s(any(Pageable.class))).thenReturn(${entity.variableName}Page);

            mockMvc.perform(get("/api/${entity.pluralName}/paginated")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(header().string("X-Total-Count", "2"))
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.totalElements").value(2))
                    .andExpect(jsonPath("$.totalPages").value(1))
                    .andExpect(jsonPath("$.size").value(20))
                    .andExpect(jsonPath("$.number").value(0))
                    .andExpect(jsonPath("$.first").value(true))
                    .andExpect(jsonPath("$.last").value(true))
<#if entity.primaryKeyAttribute.javaType == "String">
                    .andExpect(jsonPath("$.content[0].${entity.primaryKeyAttribute.name}").value("ID_1"))
                    .andExpect(jsonPath("$.content[1].${entity.primaryKeyAttribute.name}").value("ID_2"));
<#else>
                    .andExpect(jsonPath("$.content[0].${entity.primaryKeyAttribute.name}").value(1))
                    .andExpect(jsonPath("$.content[1].${entity.primaryKeyAttribute.name}").value(2));
</#if>

            verify(${entity.variableName}Service).getAll${entity.name}s(any(Pageable.class));
        }

        @Test
        @DisplayName("Should return paginated ${entity.name}s with custom page and size")
        void get${entity.name}sWithCustomPaginationShouldReturnOk() throws Exception {
            ${entity.name} test${entity.name} = create${entity.name}();
<#if entity.primaryKeyAttribute.javaType == "String">
            test${entity.name}.${entity.primaryKeyAttribute.setterName}("CREATED_ID");
<#else>
            test${entity.name}.${entity.primaryKeyAttribute.setterName}(1L);
</#if>

            List<${entity.name}> ${entity.variableName}List = Arrays.asList(test${entity.name});
            Page<${entity.name}> ${entity.variableName}Page = new PageImpl<>(${entity.variableName}List, PageRequest.of(1, 5), 10);

            when(${entity.variableName}Service.getAll${entity.name}s(any(Pageable.class))).thenReturn(${entity.variableName}Page);

            mockMvc.perform(get("/api/${entity.pluralName}/paginated")
                    .param("page", "1")
                    .param("size", "5"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.totalElements").value(10))
                    .andExpect(jsonPath("$.totalPages").value(2))
                    .andExpect(jsonPath("$.size").value(5))
                    .andExpect(jsonPath("$.number").value(1))
<#if entity.primaryKeyAttribute.javaType == "String">
                    .andExpect(jsonPath("$.content[0].${entity.primaryKeyAttribute.name}").value("CREATED_ID"));
<#else>
                    .andExpect(jsonPath("$.content[0].${entity.primaryKeyAttribute.name}").value(1));
</#if>

            verify(${entity.variableName}Service).getAll${entity.name}s(any(Pageable.class));
        }

        @Test
        @DisplayName("Should return paginated ${entity.name}s with sorting")
        void get${entity.name}sWithSortingShouldReturnOk() throws Exception {
            ${entity.name} ${entity.variableName}1 = create${entity.name}();
            ${entity.name} ${entity.variableName}2 = create${entity.name}();
<#if entity.primaryKeyAttribute.javaType == "String">
            ${entity.variableName}1.${entity.primaryKeyAttribute.setterName}("ID_A");
            ${entity.variableName}2.${entity.primaryKeyAttribute.setterName}("ID_B");
<#else>
            ${entity.variableName}1.${entity.primaryKeyAttribute.setterName}(1L);
            ${entity.variableName}2.${entity.primaryKeyAttribute.setterName}(2L);
</#if>

            List<${entity.name}> ${entity.variableName}List = Arrays.asList(${entity.variableName}2, ${entity.variableName}1);
            Page<${entity.name}> ${entity.variableName}Page = new PageImpl<>(${entity.variableName}List, PageRequest.of(0, 20), 2);

            when(${entity.variableName}Service.getAll${entity.name}s(any(Pageable.class))).thenReturn(${entity.variableName}Page);

            mockMvc.perform(get("/api/${entity.pluralName}/paginated")
                    .param("sort", "${entity.primaryKeyAttribute.name},desc"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.totalElements").value(2))
<#if entity.primaryKeyAttribute.javaType == "String">
                    .andExpect(jsonPath("$.content[0].${entity.primaryKeyAttribute.name}").value("ID_B"))
                    .andExpect(jsonPath("$.content[1].${entity.primaryKeyAttribute.name}").value("ID_A"));
<#else>
                    .andExpect(jsonPath("$.content[0].${entity.primaryKeyAttribute.name}").value(2))
                    .andExpect(jsonPath("$.content[1].${entity.primaryKeyAttribute.name}").value(1));
</#if>

            verify(${entity.variableName}Service).getAll${entity.name}s(any(Pageable.class));
        }

        @Test
        @DisplayName("Should return empty page when no ${entity.name}s exist")
        void get${entity.name}sWhenEmptyShouldReturnEmptyPage() throws Exception {
            Page<${entity.name}> empty${entity.name}Page = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 20), 0);

            when(${entity.variableName}Service.getAll${entity.name}s(any(Pageable.class))).thenReturn(empty${entity.name}Page);

            mockMvc.perform(get("/api/${entity.pluralName}/paginated"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content", hasSize(0)))
                    .andExpect(jsonPath("$.totalElements").value(0))
                    .andExpect(jsonPath("$.totalPages").value(0))
                    .andExpect(jsonPath("$.size").value(20))
                    .andExpect(jsonPath("$.number").value(0));

            verify(${entity.variableName}Service).getAll${entity.name}s(any(Pageable.class));
        }



        @Test
        @DisplayName("Should handle large page size requests")
        void get${entity.name}sWithLargePageSizeShouldReturnOk() throws Exception {
            List<${entity.name}> ${entity.variableName}List = Arrays.asList();
            Page<${entity.name}> ${entity.variableName}Page = new PageImpl<>(${entity.variableName}List, PageRequest.of(0, 1000), 0);

            when(${entity.variableName}Service.getAll${entity.name}s(any(Pageable.class))).thenReturn(${entity.variableName}Page);

            mockMvc.perform(get("/api/${entity.pluralName}/paginated")
                    .param("size", "1000"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.size").value(1000));

            verify(${entity.variableName}Service).getAll${entity.name}s(any(Pageable.class));
        }
    }
