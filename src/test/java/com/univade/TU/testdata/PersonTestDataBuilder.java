package com.univade.TU.testdata;

import com.univade.TU.entity.Person;
import java.time.LocalDate;
import java.util.*;
import java.util.UUID;

public class PersonTestDataBuilder {

    private String cin = "mvdtvrti_" + UUID.randomUUID().toString().substring(0, 3) + "";
    private String firstName = "nbngohwxjx";
    private String lastName = "uyxqekrhjy";
    private LocalDate dateOfBirth = LocalDate.now();
    private String phoneNumber = "manblcjldn_" + UUID.randomUUID().toString().substring(0, 4) + "";
    private String email = generateUniqueEmail();

    private PersonTestDataBuilder() {}

    private static String generateUniqueEmail() {
        String[] names = {"oussama", "hicham", "ilyass", "mohammed", "youssef", "hassan"};
        String[] domains = {"example.com", "test.com", "demo.org"};
        java.util.Random random = new java.util.Random();
        String name = names[random.nextInt(names.length)];
        String domain = domains[random.nextInt(domains.length)];
        return name + System.currentTimeMillis() + random.nextInt(1000) + "@" + domain;
    }

    public static PersonTestDataBuilder aPerson() {
        return new PersonTestDataBuilder();
    }

    public static PersonTestDataBuilder aValidPerson() {
        return new PersonTestDataBuilder()
                .withFirstName("ojslxhigzp")
                .withLastName("cvmzisexkd")
                .withDateOfBirth(LocalDate.now())
                .withPhoneNumber("abvzvphezi_" + UUID.randomUUID().toString().substring(0, 4) + "")
                .withEmail(generateUniqueEmail())
;
    }

    public static PersonTestDataBuilder aDefaultPerson() {
        return new PersonTestDataBuilder()
                .withFirstName("bjdtkslzur")
                .withLastName("iukkofbpgu")
                .withPhoneNumber("krlmqwaebo_" + UUID.randomUUID().toString().substring(0, 4) + "")
                .withEmail(generateUniqueEmail())
;
    }

    public static PersonTestDataBuilder aMinimalPerson() {
        return new PersonTestDataBuilder()
                .withFirstName("jqfsatczei")
                .withLastName("blxdewmstu")
;
    }

    public static PersonTestDataBuilder aCompletePerson() {
        return new PersonTestDataBuilder()
                .withFirstName("hcrcykxavw")
                .withLastName("wwbhibfjob")
                .withDateOfBirth(LocalDate.now())
                .withPhoneNumber("ntipoujkdrj")
                .withEmail(generateUniqueEmail())
;
    }

    public PersonTestDataBuilder withCin(String cin) {
        this.cin = cin;
        return this;
    }

    public PersonTestDataBuilder withFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public PersonTestDataBuilder withLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public PersonTestDataBuilder withDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        return this;
    }

    public PersonTestDataBuilder withPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public PersonTestDataBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public PersonTestDataBuilder withEmptyCin() {
        this.cin = "";
        return this;
    }

    public PersonTestDataBuilder withNullCin() {
        this.cin = null;
        return this;
    }

    public PersonTestDataBuilder withTooLongCin() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= 12; i++) {
            sb.append("a");
        }
        this.cin = sb.toString();
        return this;
    }

    public PersonTestDataBuilder withTooShortCin() {
        this.cin = "a";
        return this;
    }

    public PersonTestDataBuilder withUniqueCin() {
        this.cin = "kcozqqmo_" + UUID.randomUUID().toString().substring(0, 3) + "";
        return this;
    }

    public PersonTestDataBuilder withEmptyFirstName() {
        this.firstName = "";
        return this;
    }

    public PersonTestDataBuilder withNullFirstName() {
        this.firstName = null;
        return this;
    }

    public PersonTestDataBuilder withTooLongFirstName() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= 50; i++) {
            sb.append("a");
        }
        this.firstName = sb.toString();
        return this;
    }

    public PersonTestDataBuilder withTooShortFirstName() {
        this.firstName = "a";
        return this;
    }

    public PersonTestDataBuilder withEmptyLastName() {
        this.lastName = "";
        return this;
    }

    public PersonTestDataBuilder withNullLastName() {
        this.lastName = null;
        return this;
    }

    public PersonTestDataBuilder withTooLongLastName() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= 50; i++) {
            sb.append("a");
        }
        this.lastName = sb.toString();
        return this;
    }

    public PersonTestDataBuilder withTooShortLastName() {
        this.lastName = "a";
        return this;
    }

    public PersonTestDataBuilder withEmptyPhoneNumber() {
        this.phoneNumber = "";
        return this;
    }

    public PersonTestDataBuilder withNullPhoneNumber() {
        this.phoneNumber = null;
        return this;
    }

    public PersonTestDataBuilder withTooLongPhoneNumber() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= 15; i++) {
            sb.append("a");
        }
        this.phoneNumber = sb.toString();
        return this;
    }

    public PersonTestDataBuilder withTooShortPhoneNumber() {
        this.phoneNumber = "a";
        return this;
    }

    public PersonTestDataBuilder withUniquePhoneNumber() {
        this.phoneNumber = "yfijinncvb_" + UUID.randomUUID().toString().substring(0, 4) + "";
        return this;
    }

    public PersonTestDataBuilder withEmptyEmail() {
        this.email = "";
        return this;
    }

    public PersonTestDataBuilder withNullEmail() {
        this.email = null;
        return this;
    }

    public PersonTestDataBuilder withTooLongEmail() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= 100; i++) {
            sb.append("a");
        }
        this.email = sb.toString();
        return this;
    }

    public PersonTestDataBuilder withUniqueEmail() {
        this.email = generateUniqueEmail();
        return this;
    }


    public PersonTestDataBuilder withNullDateOfBirth() {
        this.dateOfBirth = null;
        return this;
    }



    public PersonTestDataBuilder copy() {
        PersonTestDataBuilder copy = new PersonTestDataBuilder();
        copy.cin = this.cin;
        copy.firstName = this.firstName;
        copy.lastName = this.lastName;
        copy.dateOfBirth = this.dateOfBirth;
        copy.phoneNumber = this.phoneNumber;
        copy.email = this.email;
        return copy;
    }

    public Person build() {
        Person person = new Person();
        person.setCin(cin);
        person.setFirstName(firstName);
        person.setLastName(lastName);
        person.setDateOfBirth(dateOfBirth);
        person.setPhoneNumber(phoneNumber);
        person.setEmail(email);


        return person;
    }

    public Person buildAndPersist() {
        Person entity = build();
        return entity;
    }
}
