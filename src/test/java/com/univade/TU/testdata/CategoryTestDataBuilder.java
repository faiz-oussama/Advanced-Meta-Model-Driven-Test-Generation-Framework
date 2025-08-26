package com.univade.TU.testdata;

import com.univade.TU.entity.Category;
import java.time.LocalDate;
import java.util.*;
import java.util.UUID;

public class CategoryTestDataBuilder {

    private String name = "hdigakkruu";
    private String description = "kjbzgwoxgg";
    private Boolean active = true;

    private CategoryTestDataBuilder() {}

    private static String generateUniqueEmail() {
        String[] names = {"oussama", "hicham", "ilyass", "mohammed", "youssef", "hassan"};
        String[] domains = {"example.com", "test.com", "demo.org"};
        java.util.Random random = new java.util.Random();
        String name = names[random.nextInt(names.length)];
        String domain = domains[random.nextInt(domains.length)];
        return name + System.currentTimeMillis() + random.nextInt(1000) + "@" + domain;
    }

    public static CategoryTestDataBuilder aCategory() {
        return new CategoryTestDataBuilder();
    }

    public static CategoryTestDataBuilder aValidCategory() {
        return new CategoryTestDataBuilder()
                .withName("gaytlymurl")
                .withDescription("cftoyzzoyw")
                .withActive(true)
;
    }

    public static CategoryTestDataBuilder aDefaultCategory() {
        return new CategoryTestDataBuilder()
;
    }

    public static CategoryTestDataBuilder aMinimalCategory() {
        return new CategoryTestDataBuilder()
;
    }

    public static CategoryTestDataBuilder aCompleteCategory() {
        return new CategoryTestDataBuilder()
                .withName("eimhryxwrd")
                .withDescription("ktfmtvrqxn")
                .withActive(true)
;
    }

    public CategoryTestDataBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public CategoryTestDataBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public CategoryTestDataBuilder withActive(Boolean active) {
        this.active = active;
        return this;
    }

    public CategoryTestDataBuilder withEmptyName() {
        this.name = "";
        return this;
    }

    public CategoryTestDataBuilder withNullName() {
        this.name = null;
        return this;
    }

    public CategoryTestDataBuilder withTooLongName() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= 100; i++) {
            sb.append("a");
        }
        this.name = sb.toString();
        return this;
    }

    public CategoryTestDataBuilder withEmptyDescription() {
        this.description = "";
        return this;
    }

    public CategoryTestDataBuilder withNullDescription() {
        this.description = null;
        return this;
    }

    public CategoryTestDataBuilder withTooLongDescription() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= 500; i++) {
            sb.append("a");
        }
        this.description = sb.toString();
        return this;
    }


    public CategoryTestDataBuilder withNullActive() {
        this.active = null;
        return this;
    }



    public CategoryTestDataBuilder copy() {
        CategoryTestDataBuilder copy = new CategoryTestDataBuilder();
        copy.name = this.name;
        copy.description = this.description;
        copy.active = this.active;
        return copy;
    }

    public Category build() {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setActive(active);


        return category;
    }

    public Category buildAndPersist() {
        Category entity = build();
        return entity;
    }
}
