    @Nested
    @DisplayName("Dynamic Validation Tests")
    class DynamicValidationTests {

<#if hasStrictValidation>
        @Test
        @DisplayName("Should dynamically test all validation constraints from DTO")
        void testDynamicValidationConstraints() throws Exception {
            List<ControllerValidationRule> rules = createValidationRules();

            assertThat(rules).isNotEmpty();

            for (ControllerValidationRule rule : rules) {
                ${entity.name}Dto dto = create${entity.name}Dto();

                setInvalidValueOnDto(dto, rule);
                mockMvc.perform(post("/api/${entity.pluralName}")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                        .andExpect(status().isBadRequest())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.status").value(400))
                        .andExpect(jsonPath("$.message").exists())
                        .andExpect(jsonPath("$.timestamp").exists());
            }

            verify(${entity.variableName}Service, never()).create${entity.name}(any(${entity.name}.class));
        }
<#else>
        @Test
        @DisplayName("Should handle DTO with only non-strict validations")
        void testNonStrictValidationConstraints() throws Exception {
            List<ControllerValidationRule> rules = createValidationRules();

            ${entity.name}Dto dto = ${entity.name}Dto.builder().build();

            when(${entity.variableName}Service.create${entity.name}(any(${entity.name}.class))).thenReturn(createMockEntity());

            mockMvc.perform(post("/api/${entity.pluralName}")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));

            verify(${entity.variableName}Service).create${entity.name}(any(${entity.name}.class));
        }
</#if>

        private List<ControllerValidationRule> createValidationRules() {
            List<ControllerValidationRule> rules = new ArrayList<>();

<#if validationRules?has_content>
<#list validationRules as rule>
            // ${rule.description}
            rules.add(ControllerValidationRule.builder()
                    .testName("${rule.testName}")
                    .attributeName("${rule.attributeName}")
                    .validationType("${rule.validationType}")
                    .description("${rule.description}")
                    .expectedHttpStatus("${rule.expectedStatus}")
                    .message("${rule.message}")
<#if rule.minValue??>
                    .minValue(${rule.minValue?c})
</#if>
<#if rule.maxValue??>
                    .maxValue(${rule.maxValue?c})
</#if>
<#if rule.constraintValue??>
                    .constraintValue(${rule.constraintValue?c})
</#if>
<#if rule.pattern?? && rule.pattern?is_string>
                    .pattern("${rule.pattern}")
</#if>
<#if rule.invalidValue?? && rule.invalidValue?is_string>
                    .invalidValue(${rule.invalidValue})
</#if>
                    .build());

</#list>
</#if>
            return rules;
        }

        private void setInvalidValueOnDto(${entity.name}Dto dto, ControllerValidationRule rule) throws Exception {
            Field field = ${entity.name}Dto.class.getDeclaredField(rule.getAttributeName());
            field.setAccessible(true);

            if (rule.isNotNull()) {
                field.set(dto, null);
            } else if (rule.isNotBlank()) {
                field.set(dto, "");
            } else if (rule.isSize() && rule.getMaxValue() != null) {
                int maxLength = ((Number) rule.getMaxValue()).intValue();
                field.set(dto, "a".repeat(maxLength + 1));
            } else if (rule.isMin() && rule.getConstraintValue() != null) {
                Number minValue = (Number) rule.getConstraintValue();
                setNumericFieldValue(field, dto, minValue.intValue() - 1);
            } else if (rule.isMax() && rule.getConstraintValue() != null) {
                Number maxValue = (Number) rule.getConstraintValue();
                setNumericFieldValue(field, dto, maxValue.intValue() + 1);
            } else if (rule.isEmail()) {
                field.set(dto, "invalid-email");
            } else if (rule.isPattern()) {
                field.set(dto, "invalid-pattern-value");
            }
        }

        private void setNumericFieldValue(Field field, ${entity.name}Dto dto, int value) throws Exception {
            Class<?> fieldType = field.getType();
            if (fieldType == Integer.class || fieldType == int.class) {
                field.set(dto, value);
            } else if (fieldType == Long.class || fieldType == long.class) {
                field.set(dto, (long) value);
            } else if (fieldType == Short.class || fieldType == short.class) {
                field.set(dto, (short) value);
            } else if (fieldType == Byte.class || fieldType == byte.class) {
                field.set(dto, (byte) value);
            } else {
                field.set(dto, value);
            }
        }

<#if hasStrictValidation>
        @Test
        @DisplayName("Should return 400 when multiple validation errors occur")
        void create${entity.name}WithMultipleValidationErrorsShouldReturnBadRequest() throws Exception {
            ${entity.name}Dto requestDto = ${entity.name}Dto.builder().build();

            mockMvc.perform(post("/api/${entity.pluralName}")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(${entity.variableName}Service, never()).create${entity.name}(any(${entity.name}.class));
        }
<#else>
        @Test
        @DisplayName("Should handle empty DTO gracefully when no validation rules exist")
        void create${entity.name}WithEmptyDtoShouldHandleGracefully() throws Exception {
            ${entity.name}Dto requestDto = ${entity.name}Dto.builder().build();

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

    }
