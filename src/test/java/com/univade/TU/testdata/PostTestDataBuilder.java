package com.univade.TU.testdata;

import com.univade.TU.entity.Post;
import com.univade.TU.entity.User;
import java.time.LocalDate;
import java.util.*;
import java.util.UUID;

public class PostTestDataBuilder {

    private String title = "emchtmfusv";
    private String content = "adnferxjvig";
    private User author;

    private PostTestDataBuilder() {}

    private static String generateUniqueEmail() {
        String[] names = {"oussama", "hicham", "ilyass", "mohammed", "youssef", "hassan"};
        String[] domains = {"example.com", "test.com", "demo.org"};
        java.util.Random random = new java.util.Random();
        String name = names[random.nextInt(names.length)];
        String domain = domains[random.nextInt(domains.length)];
        return name + System.currentTimeMillis() + random.nextInt(1000) + "@" + domain;
    }

    public static PostTestDataBuilder aPost() {
        return new PostTestDataBuilder();
    }

    public static PostTestDataBuilder aValidPost() {
        return new PostTestDataBuilder()
                .withTitle("kqwfjrxdpo")
                .withContent("eodnhucfiaj")
                .withAuthor(UserTestDataBuilder.aDefaultUser().build())
;
    }

    public static PostTestDataBuilder aDefaultPost() {
        return new PostTestDataBuilder()
                .withTitle("goajmpcjuv")
                .withContent("sleijfuwjlf")
                .withAuthor(UserTestDataBuilder.aDefaultUser().build())
;
    }

    public static PostTestDataBuilder aMinimalPost() {
        return new PostTestDataBuilder()
                .withTitle("wcbplrlklz")
                .withContent("mnrdayptcjz")
                .withAuthor(UserTestDataBuilder.aDefaultUser().build())
;
    }

    public static PostTestDataBuilder aCompletePost() {
        return new PostTestDataBuilder()
                .withTitle("pislbzwdyu")
                .withContent("knryytmnsdc")
;
    }

    public PostTestDataBuilder withTitle(String title) {
        this.title = title;
        return this;
    }

    public PostTestDataBuilder withContent(String content) {
        this.content = content;
        return this;
    }

    public PostTestDataBuilder withAuthor(User author) {
        this.author = author;
        return this;
    }

    public PostTestDataBuilder withNullAuthor() {
        this.author = null;
        return this;
    }

    public PostTestDataBuilder withEmptyTitle() {
        this.title = "";
        return this;
    }

    public PostTestDataBuilder withNullTitle() {
        this.title = null;
        return this;
    }

    public PostTestDataBuilder withTooLongTitle() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= 255; i++) {
            sb.append("a");
        }
        this.title = sb.toString();
        return this;
    }

    public PostTestDataBuilder withTooShortTitle() {
        this.title = "a";
        return this;
    }

    public PostTestDataBuilder withEmptyContent() {
        this.content = "";
        return this;
    }

    public PostTestDataBuilder withNullContent() {
        this.content = null;
        return this;
    }

    public PostTestDataBuilder withTooLongContent() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= 5000; i++) {
            sb.append("a");
        }
        this.content = sb.toString();
        return this;
    }

    public PostTestDataBuilder withTooShortContent() {
        this.content = "a";
        return this;
    }




    public PostTestDataBuilder copy() {
        PostTestDataBuilder copy = new PostTestDataBuilder();
        copy.title = this.title;
        copy.content = this.content;
        copy.author = this.author;
        return copy;
    }

    public Post build() {
        Post post = new Post();
        post.setTitle(title);
        post.setContent(content);

        if (author != null) {
            post.setAuthor(author);
        }

        return post;
    }

    public Post buildAndPersist() {
        Post entity = build();
        return entity;
    }
}
