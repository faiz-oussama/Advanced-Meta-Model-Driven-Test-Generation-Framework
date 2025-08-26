    @Nested
    @DisplayName("Pagination Tests")
    class PaginationTests {

        @Test
        void getAll${entity.name}sWithPageableShouldReturnPagedResults() {
            ${entity.name} ${entity.variableName}1 = create${entity.name}();
            ${entity.name} ${entity.variableName}2 = create${entity.name}();
            ${entity.name} ${entity.variableName}3 = create${entity.name}();

            List<${entity.name}> ${entity.variableName}List = List.of(${entity.variableName}1, ${entity.variableName}2);
            Page<${entity.name}> expectedPage = new PageImpl<>(${entity.variableName}List, PageRequest.of(0, 2), 3);

            Pageable pageable = PageRequest.of(0, 2);
            when(${entity.variableName}Repository.findAll(pageable)).thenReturn(expectedPage);

            Page<${entity.name}> result = ${entity.variableName}Service.getAll${entity.name}s(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(3);
            assertThat(result.getTotalPages()).isEqualTo(2);
            assertThat(result.getNumber()).isEqualTo(0);
            assertThat(result.getSize()).isEqualTo(2);
            verify(${entity.variableName}Repository).findAll(pageable);
        }

        @Test
        void getAll${entity.name}sWithPageableAndSortShouldReturnSortedResults() {
<#list entity.stringAttributes as attribute>
<#if !attribute.primaryKey>
            ${entity.name} ${entity.variableName}1 = create${entity.name}();
            ${entity.variableName}1.${attribute.setterName}("Hicham");
            ${entity.name} ${entity.variableName}2 = create${entity.name}();
            ${entity.variableName}2.${attribute.setterName}("Oussama");

            List<${entity.name}> sorted${entity.name}List = List.of(${entity.variableName}1, ${entity.variableName}2);
            Pageable pageable = PageRequest.of(0, 10, Sort.by("${attribute.name}").ascending());
            Page<${entity.name}> expectedPage = new PageImpl<>(sorted${entity.name}List, pageable, 2);

            when(${entity.variableName}Repository.findAll(pageable)).thenReturn(expectedPage);

            Page<${entity.name}> result = ${entity.variableName}Service.getAll${entity.name}s(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).${attribute.getterName}()).isEqualTo("Hicham");
            assertThat(result.getContent().get(1).${attribute.getterName}()).isEqualTo("Oussama");
            verify(${entity.variableName}Repository).findAll(pageable);
<#break>
</#if>
</#list>
        }

        @Test
        void getAll${entity.name}sWithEmptyPageShouldReturnEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<${entity.name}> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(${entity.variableName}Repository.findAll(pageable)).thenReturn(emptyPage);

            Page<${entity.name}> result = ${entity.variableName}Service.getAll${entity.name}s(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
            assertThat(result.getTotalPages()).isEqualTo(0);
            verify(${entity.variableName}Repository).findAll(pageable);
        }

        @Test
        void getAll${entity.name}sWithLargePageSizeShouldReturnAllResults() {
            ${entity.name} ${entity.variableName}1 = create${entity.name}();
            ${entity.name} ${entity.variableName}2 = create${entity.name}();
            ${entity.name} ${entity.variableName}3 = create${entity.name}();

            List<${entity.name}> all${entity.name}s = List.of(${entity.variableName}1, ${entity.variableName}2, ${entity.variableName}3);
            Pageable pageable = PageRequest.of(0, 100);
            Page<${entity.name}> expectedPage = new PageImpl<>(all${entity.name}s, pageable, 3);

            when(${entity.variableName}Repository.findAll(pageable)).thenReturn(expectedPage);

            Page<${entity.name}> result = ${entity.variableName}Service.getAll${entity.name}s(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getTotalElements()).isEqualTo(3);
            assertThat(result.getTotalPages()).isEqualTo(1);
            verify(${entity.variableName}Repository).findAll(pageable);
        }

        @Test
        void getAll${entity.name}sWithSecondPageShouldReturnCorrectResults() {
            ${entity.name} ${entity.variableName}3 = create${entity.name}();
            ${entity.name} ${entity.variableName}4 = create${entity.name}();

            List<${entity.name}> secondPage${entity.name}s = List.of(${entity.variableName}3, ${entity.variableName}4);
            Pageable pageable = PageRequest.of(1, 2);
            Page<${entity.name}> expectedPage = new PageImpl<>(secondPage${entity.name}s, pageable, 4);

            when(${entity.variableName}Repository.findAll(pageable)).thenReturn(expectedPage);

            Page<${entity.name}> result = ${entity.variableName}Service.getAll${entity.name}s(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(4);
            assertThat(result.getTotalPages()).isEqualTo(2);
            assertThat(result.getNumber()).isEqualTo(1);
            assertThat(result.isFirst()).isFalse();
            assertThat(result.isLast()).isTrue();
            verify(${entity.variableName}Repository).findAll(pageable);
        }



        @Test
        void getAll${entity.name}sWithDescendingSortShouldReturnCorrectOrder() {
<#list entity.stringAttributes as attribute>
<#if !attribute.primaryKey>
            ${entity.name} ${entity.variableName}1 = create${entity.name}();
            ${entity.variableName}1.${attribute.setterName}("Ilyass");
            ${entity.name} ${entity.variableName}2 = create${entity.name}();
            ${entity.variableName}2.${attribute.setterName}("Oussama");

            List<${entity.name}> sorted${entity.name}List = List.of(${entity.variableName}2, ${entity.variableName}1);
            Pageable pageable = PageRequest.of(0, 10, Sort.by("${attribute.name}").descending());
            Page<${entity.name}> expectedPage = new PageImpl<>(sorted${entity.name}List, pageable, 2);

            when(${entity.variableName}Repository.findAll(pageable)).thenReturn(expectedPage);

            Page<${entity.name}> result = ${entity.variableName}Service.getAll${entity.name}s(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).${attribute.getterName}()).isEqualTo("Oussama");
            assertThat(result.getContent().get(1).${attribute.getterName}()).isEqualTo("Ilyass");
            verify(${entity.variableName}Repository).findAll(pageable);
<#break>
</#if>
</#list>
        }

        @Test
        void getAll${entity.name}sWithCustomPageSizeShouldRespectPageSize() {
            ${entity.name} ${entity.variableName}1 = create${entity.name}();
            ${entity.name} ${entity.variableName}2 = create${entity.name}();

            List<${entity.name}> ${entity.variableName}List = List.of(${entity.variableName}1, ${entity.variableName}2);
            Pageable pageable = PageRequest.of(0, 1);
            Page<${entity.name}> expectedPage = new PageImpl<>(List.of(${entity.variableName}1), pageable, 2);

            when(${entity.variableName}Repository.findAll(pageable)).thenReturn(expectedPage);

            Page<${entity.name}> result = ${entity.variableName}Service.getAll${entity.name}s(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getTotalPages()).isEqualTo(2);
            assertThat(result.getSize()).isEqualTo(1);
            verify(${entity.variableName}Repository).findAll(pageable);
        }
    }
