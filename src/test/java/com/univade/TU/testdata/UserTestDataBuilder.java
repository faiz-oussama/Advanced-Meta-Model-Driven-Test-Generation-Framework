package com.univade.TU.testdata;

import com.univade.TU.entity.User;
import com.univade.TU.entity.Address;
import com.univade.TU.entity.Post;
import java.time.LocalDate;
import java.util.*;
import java.util.UUID;

public class UserTestDataBuilder {

    private String name = "dkxviuuhln";
    private String email = generateUniqueEmail();
    private Integer age = 42;
    private Address address;
    private java.util.List<Post> posts = new java.util.ArrayList<>();

    private UserTestDataBuilder() {}

    private static String generateUniqueEmail() {
        String[] names = {"oussama", "hicham", "ilyass", "mohammed", "youssef", "hassan"};
        String[] domains = {"example.com", "test.com", "demo.org"};
        java.util.Random random = new java.util.Random();
        String name = names[random.nextInt(names.length)];
        String domain = domains[random.nextInt(domains.length)];
        return name + System.currentTimeMillis() + random.nextInt(1000) + "@" + domain;
    }

    public static UserTestDataBuilder aUser() {
        return new UserTestDataBuilder();
    }

    public static UserTestDataBuilder aValidUser() {
        return new UserTestDataBuilder()
                .withName("fdoauttrxz")
                .withEmail(generateUniqueEmail())
                .withAge(63)
;
    }

    public static UserTestDataBuilder aDefaultUser() {
        return new UserTestDataBuilder()
                .withName("cjcybjdkhm")
                .withEmail(generateUniqueEmail())
;
    }

    public static UserTestDataBuilder aMinimalUser() {
        return new UserTestDataBuilder()
                .withName("rrcxuukfvg")
                .withEmail(generateUniqueEmail())
;
    }

    public static UserTestDataBuilder aCompleteUser() {
        return new UserTestDataBuilder()
                .withName("tglnvoxrjb")
                .withEmail(generateUniqueEmail())
                .withAge(35)
;
    }

    public UserTestDataBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public UserTestDataBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public UserTestDataBuilder withAge(Integer age) {
        this.age = age;
        return this;
    }

    public UserTestDataBuilder withAddress(Address address) {
        this.address = address;
        return this;
    }

    public UserTestDataBuilder withNullAddress() {
        this.address = null;
        return this;
    }

    public UserTestDataBuilder withPosts(java.util.List<Post> posts) {
        this.posts = posts;
        return this;
    }

    public UserTestDataBuilder withNullPosts() {
        this.posts = null;
        return this;
    }

    public UserTestDataBuilder addToPosts(Post post) {
        if (this.posts == null) {
            this.posts = new java.util.ArrayList<>();
        }
        this.posts.add(post);
        return this;
    }

    public UserTestDataBuilder withEmptyName() {
        this.name = "";
        return this;
    }

    public UserTestDataBuilder withNullName() {
        this.name = null;
        return this;
    }

    public UserTestDataBuilder withTooLongName() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= 100; i++) {
            sb.append("a");
        }
        this.name = sb.toString();
        return this;
    }

    public UserTestDataBuilder withTooShortName() {
        this.name = "a";
        return this;
    }

    public UserTestDataBuilder withEmptyEmail() {
        this.email = "";
        return this;
    }

    public UserTestDataBuilder withNullEmail() {
        this.email = null;
        return this;
    }

    public UserTestDataBuilder withTooLongEmail() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= 150; i++) {
            sb.append("a");
        }
        this.email = sb.toString();
        return this;
    }

    public UserTestDataBuilder withUniqueEmail() {
        this.email = generateUniqueEmail();
        return this;
    }

    public UserTestDataBuilder withTooSmallAge() {
        this.age = -1;
        return this;
    }

    public UserTestDataBuilder withTooLargeAge() {
        this.age = 151;
        return this;
    }


    public UserTestDataBuilder withNullAge() {
        this.age = null;
        return this;
    }



    public UserTestDataBuilder copy() {
        UserTestDataBuilder copy = new UserTestDataBuilder();
        copy.name = this.name;
        copy.email = this.email;
        copy.age = this.age;
        copy.address = this.address;
        copy.posts = this.posts;
        return copy;
    }

    public User build() {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setAge(age);

        if (address != null) {
            user.setAddress(address);
        }
        if (posts != null) {
            user.setPosts(new ArrayList<>(posts));
        }

        return user;
    }

    public User buildAndPersist() {
        User entity = build();
        return entity;
    }
}
