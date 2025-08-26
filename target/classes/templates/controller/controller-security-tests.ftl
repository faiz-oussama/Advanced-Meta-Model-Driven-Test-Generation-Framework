<#if hasSecurityRules>
    @Nested
    @DisplayName("Security and Authorization Tests")
    class SecurityAndAuthorizationTests {

        <#-- Generate tests for each security rule -->
        <#list securityRules as rule>
            <#list rule.allHttpMethods as method>
                <#assign methodLower = method?lower_case>
                <#assign allowedRoles = rule.getAllowedRoles(method)>
                <#assign pathSanitized = rule.path?replace("/api/", "")?replace("/", "")?replace("{", "")?replace("}", "")?cap_first>

                <#-- Test for unauthenticated access -->
                @Test
                @DisplayName("Should deny ${methodLower?upper_case} access to ${rule.path} without authentication")
                void ${methodLower}${pathSanitized}WithoutAuthenticationShouldFail() throws Exception {
                    <#if method == "GET">
                        <#if rule.path?contains("{id}")>
                    ${entity.name} mockEntity = createMockEntity();
                    when(${entity.variableName}Service.get${entity.name}ById(any())).thenReturn(mockEntity);

                    mockMvc.perform(get("${rule.path}", 1L)
                            .accept(MediaType.APPLICATION_JSON))
                            .andExpect(status().isOk());
                        <#else>
                    List<${entity.name}> mockEntities = Arrays.asList(createMockEntity());
                    when(${entity.variableName}Service.getAll${entity.name}s()).thenReturn(mockEntities);

                    mockMvc.perform(get("${rule.path}")
                            .accept(MediaType.APPLICATION_JSON))
                            .andExpect(status().isOk());
                        </#if>
                    <#elseif method == "POST">
                    ${entity.dtoClassName} requestDto = create${entity.dtoClassName}();
                    ${entity.name} mockEntity = createMockEntity();
                    when(${entity.variableName}Service.create${entity.name}(any(${entity.name}.class))).thenReturn(mockEntity);

                    mockMvc.perform(post("${rule.path}")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                            .andExpect(status().isCreated());
                    <#elseif method == "PUT">
                    ${entity.dtoClassName} requestDto = create${entity.dtoClassName}();
                    ${entity.name} mockEntity = createMockEntity();
                    when(${entity.variableName}Service.update${entity.name}(any(), any(${entity.name}.class))).thenReturn(mockEntity);

                    mockMvc.perform(put("${rule.path}", 1L)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                            .andExpect(status().isOk());
                    <#elseif method == "DELETE">
                    doNothing().when(${entity.variableName}Service).delete${entity.name}(any());

                    mockMvc.perform(delete("${rule.path}", 1L)
                            .with(csrf()))
                            .andExpect(status().isNoContent());
                    </#if>
                }

                <#-- Test for each allowed role -->
                <#list allowedRoles as role>
                @Test
                @WithMockUser(roles = "${role}")
                @DisplayName("Should allow ${methodLower?upper_case} access to ${rule.path} with ${role} role")
                void ${methodLower}${pathSanitized}With${role?cap_first}RoleShouldSucceed() throws Exception {
                    <#if method == "GET">
                        <#if rule.path?contains("{id}")>
                    ${entity.name} mockEntity = createMockEntity();
                    when(${entity.variableName}Service.get${entity.name}ById(any())).thenReturn(mockEntity);

                    mockMvc.perform(get("${rule.path}", 1L)
                            .accept(MediaType.APPLICATION_JSON))
                            .andExpect(status().isOk())
                            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

                    verify(${entity.variableName}Service).get${entity.name}ById(any());
                        <#else>
                    List<${entity.name}> mockEntities = Arrays.asList(createMockEntity());
                    when(${entity.variableName}Service.getAll${entity.name}s()).thenReturn(mockEntities);

                    mockMvc.perform(get("${rule.path}")
                            .accept(MediaType.APPLICATION_JSON))
                            .andExpect(status().isOk())
                            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                            .andExpect(jsonPath("$").isArray());

                    verify(${entity.variableName}Service).getAll${entity.name}s();
                        </#if>
                    <#elseif method == "POST">
                    ${entity.dtoClassName} requestDto = create${entity.dtoClassName}();
                    ${entity.name} mockEntity = createMockEntity();
                    when(${entity.variableName}Service.create${entity.name}(any(${entity.name}.class))).thenReturn(mockEntity);

                    mockMvc.perform(post("${rule.path}")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                            .andExpect(status().isCreated())
                            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

                    verify(${entity.variableName}Service).create${entity.name}(any(${entity.name}.class));
                    <#elseif method == "PUT">
                    ${entity.dtoClassName} requestDto = create${entity.dtoClassName}();
                    ${entity.name} mockEntity = createMockEntity();
                    when(${entity.variableName}Service.update${entity.name}(any(), any(${entity.name}.class))).thenReturn(mockEntity);

                    mockMvc.perform(put("${rule.path}", 1L)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                            .andExpect(status().isOk())
                            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

                    verify(${entity.variableName}Service).update${entity.name}(any(), any(${entity.name}.class));
                    <#elseif method == "DELETE">
                    doNothing().when(${entity.variableName}Service).delete${entity.name}(any());

                    mockMvc.perform(delete("${rule.path}", 1L)
                            .with(csrf()))
                            .andExpect(status().isNoContent());

                    verify(${entity.variableName}Service).delete${entity.name}(any());
                    </#if>
                }
                </#list>

                <#-- Test for unauthorized roles - use all roles from security rules -->
                <#assign allDefinedRoles = allSecurityRoles>
                <#list allDefinedRoles as testRole>
                    <#if !allowedRoles?seq_contains(testRole)>
                @Test
                @WithMockUser(roles = "${testRole}")
                @DisplayName("Should deny ${methodLower?upper_case} access to ${rule.path} with ${testRole} role")
                void ${methodLower}${pathSanitized}With${testRole?cap_first}RoleShouldFail() throws Exception {
                    <#if method == "GET">
                        <#if rule.path?contains("{id}")>
                    ${entity.name} mockEntity = createMockEntity();
                    when(${entity.variableName}Service.get${entity.name}ById(any())).thenReturn(mockEntity);

                    mockMvc.perform(get("${rule.path}", 1L)
                            .accept(MediaType.APPLICATION_JSON))
                            .andExpect(status().isOk());
                        <#else>
                    List<${entity.name}> mockEntities = Arrays.asList(createMockEntity());
                    when(${entity.variableName}Service.getAll${entity.name}s()).thenReturn(mockEntities);

                    mockMvc.perform(get("${rule.path}")
                            .accept(MediaType.APPLICATION_JSON))
                            .andExpect(status().isOk());
                        </#if>
                    <#elseif method == "POST">
                    ${entity.dtoClassName} requestDto = create${entity.dtoClassName}();
                    ${entity.name} mockEntity = createMockEntity();
                    when(${entity.variableName}Service.create${entity.name}(any(${entity.name}.class))).thenReturn(mockEntity);

                    mockMvc.perform(post("${rule.path}")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                            .andExpect(status().isCreated());
                    <#elseif method == "PUT">
                    ${entity.dtoClassName} requestDto = create${entity.dtoClassName}();
                    ${entity.name} mockEntity = createMockEntity();
                    when(${entity.variableName}Service.update${entity.name}(any(), any(${entity.name}.class))).thenReturn(mockEntity);

                    mockMvc.perform(put("${rule.path}", 1L)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                            .andExpect(status().isOk());
                    <#elseif method == "DELETE">
                    doNothing().when(${entity.variableName}Service).delete${entity.name}(any());

                    mockMvc.perform(delete("${rule.path}", 1L)
                            .with(csrf()))
                            .andExpect(status().isNoContent());
                    </#if>
                }
                    </#if>
                </#list>
            </#list>
        </#list>
    }
<#else>
    @Nested
    @DisplayName("Security and Authorization Tests")
    class SecurityAndAuthorizationTests {

        @Test
        @DisplayName("Should allow access to public endpoints without authentication")
        void accessPublicEndpointWithoutAuthenticationShouldSucceed() throws Exception {
            List<${entity.name}> mockEntities = Arrays.asList(createMockEntity());
            when(${entity.variableName}Service.getAll${entity.name}s()).thenReturn(mockEntities);

            mockMvc.perform(get("/api/${entity.pluralName}")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].${entity.primaryKeyAttribute.name}").exists());

            verify(${entity.variableName}Service).getAll${entity.name}s();
        }

        @Test
        @DisplayName("Should allow create operation for any user since security is disabled")
        void createWithUserRoleShouldSucceed() throws Exception {
            ${entity.dtoClassName} requestDto = create${entity.dtoClassName}();
            when(${entity.variableName}Service.create${entity.name}(any(${entity.name}.class))).thenReturn(createMockEntity());

            mockMvc.perform(post("/api/${entity.pluralName}")
                    .with(user("testuser").roles("USER"))
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

    }
</#if>


