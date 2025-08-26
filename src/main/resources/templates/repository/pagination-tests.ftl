    @Nested
    @DisplayName("Pagination Tests")
    class PaginationTests {

        @Test
        void findAllWithPaginationShouldReturnPagedResults() {
        ${entity.name} ${entity.variableName}1 = entityManager.persistAndFlush(create${entity.name}());
        ${entity.name} ${entity.variableName}2 = entityManager.persistAndFlush(create${entity.name}());
        ${entity.name} ${entity.variableName}3 = entityManager.persistAndFlush(create${entity.name}());

        Pageable pageable = PageRequest.of(0, 2);
        Page<${entity.name}> page = ${entity.variableName}Repository.findAll(pageable);

        assertThat(page).isNotNull();
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();
    }

    @Test
    void findAllWithPaginationSecondPageShouldReturnRemainingResults() {
        ${entity.name} ${entity.variableName}1 = entityManager.persistAndFlush(create${entity.name}());
        ${entity.name} ${entity.variableName}2 = entityManager.persistAndFlush(create${entity.name}());
        ${entity.name} ${entity.variableName}3 = entityManager.persistAndFlush(create${entity.name}());

        Pageable pageable = PageRequest.of(1, 2);
        Page<${entity.name}> page = ${entity.variableName}Repository.findAll(pageable);

        assertThat(page).isNotNull();
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.isLast()).isTrue();
        assertThat(page.hasPrevious()).isTrue();
    }

    @Test
    void findAllWithSortingShouldReturnSortedResults() {
<#list entity.attributes as attribute>
<#if attribute.stringType && !attribute.primaryKey>
        ${entity.name} ${entity.variableName}1 = entityManager.persistAndFlush(create${entity.name}());
        ${entity.name} ${entity.variableName}2 = entityManager.persistAndFlush(create${entity.name}());
        ${entity.name} ${entity.variableName}3 = entityManager.persistAndFlush(create${entity.name}());

        Sort sort = Sort.by(Sort.Direction.ASC, "${attribute.name}");
        List<${entity.name}> sorted${entity.name}s = ${entity.variableName}Repository.findAll(sort);

        assertThat(sorted${entity.name}s).hasSize(3);
        assertThat(sorted${entity.name}s).isSortedAccordingTo((a, b) -> a.${attribute.getterName}().compareTo(b.${attribute.getterName}()));
<#break>
</#if>
</#list>
    }

    @Test
    void findAllWithPaginationAndSortingShouldReturnPagedAndSortedResults() {
<#list entity.attributes as attribute>
<#if attribute.stringType && !attribute.primaryKey>
        ${entity.name} ${entity.variableName}1 = entityManager.persistAndFlush(create${entity.name}());
        ${entity.name} ${entity.variableName}2 = entityManager.persistAndFlush(create${entity.name}());
        ${entity.name} ${entity.variableName}3 = entityManager.persistAndFlush(create${entity.name}());

        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "${attribute.name}"));
        Page<${entity.name}> page = ${entity.variableName}Repository.findAll(pageable);

        assertThat(page).isNotNull();
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent()).isSortedAccordingTo((a, b) -> b.${attribute.getterName}().compareTo(a.${attribute.getterName}()));
<#break>
</#if>
</#list>
    }

    @Test
    void findAllWithEmptyPageShouldReturnEmptyResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<${entity.name}> page = ${entity.variableName}Repository.findAll(pageable);

        assertThat(page).isNotNull();
        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isZero();
        assertThat(page.getTotalPages()).isZero();
        assertThat(page.isFirst()).isTrue();
        assertThat(page.isLast()).isTrue();
    }

    @Test
    void findAllWithLargePageSizeShouldReturnAllResults() {
        ${entity.name} ${entity.variableName}1 = entityManager.persistAndFlush(create${entity.name}());
        ${entity.name} ${entity.variableName}2 = entityManager.persistAndFlush(create${entity.name}());

        Pageable pageable = PageRequest.of(0, 100);
        Page<${entity.name}> page = ${entity.variableName}Repository.findAll(pageable);

        assertThat(page).isNotNull();
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getTotalPages()).isEqualTo(1);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.isLast()).isTrue();
    }

    @Test
    void paginationWithMultipleSortFieldsShouldWork() {
<#assign firstStringAttr = "">
<#assign firstNumericAttr = "">
<#list entity.attributes as attr>
<#if attr.stringType && !attr.primaryKey && firstStringAttr == "">
<#assign firstStringAttr = attr.name>
</#if>
<#if attr.numericType && !attr.primaryKey && firstNumericAttr == "">
<#assign firstNumericAttr = attr.name>
</#if>
</#list>
<#if firstStringAttr != "" && firstNumericAttr != "">
        ${entity.name} ${entity.variableName}1 = entityManager.persistAndFlush(create${entity.name}());
        ${entity.name} ${entity.variableName}2 = entityManager.persistAndFlush(create${entity.name}());
        ${entity.name} ${entity.variableName}3 = entityManager.persistAndFlush(create${entity.name}());

        Sort multiSort = Sort.by(
            Sort.Order.asc("${firstStringAttr}"),
            Sort.Order.desc("${firstNumericAttr}")
        );
        Pageable pageable = PageRequest.of(0, 2, multiSort);
        Page<${entity.name}> page = ${entity.variableName}Repository.findAll(pageable);

        assertThat(page).isNotNull();
        assertThat(page.getContent()).hasSize(2);
<#else>
        ${entity.name} ${entity.variableName}1 = entityManager.persistAndFlush(create${entity.name}());
        ${entity.name} ${entity.variableName}2 = entityManager.persistAndFlush(create${entity.name}());

        Pageable pageable = PageRequest.of(0, 2);
        Page<${entity.name}> page = ${entity.variableName}Repository.findAll(pageable);

        assertThat(page).isNotNull();
        assertThat(page.getContent()).hasSize(2);
</#if>
    }

    
}
