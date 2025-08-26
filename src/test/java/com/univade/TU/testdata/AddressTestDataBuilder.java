package com.univade.TU.testdata;

import com.univade.TU.entity.Address;
import java.time.LocalDate;
import java.util.*;
import java.util.UUID;

public class AddressTestDataBuilder {

    private String street = "fxjaexnhbr";
    private String city = "fbeclsuiib";
    private String zipCode = "vtjatownu";

    private AddressTestDataBuilder() {}

    private static String generateUniqueEmail() {
        String[] names = {"oussama", "hicham", "ilyass", "mohammed", "youssef", "hassan"};
        String[] domains = {"example.com", "test.com", "demo.org"};
        java.util.Random random = new java.util.Random();
        String name = names[random.nextInt(names.length)];
        String domain = domains[random.nextInt(domains.length)];
        return name + System.currentTimeMillis() + random.nextInt(1000) + "@" + domain;
    }

    public static AddressTestDataBuilder aAddress() {
        return new AddressTestDataBuilder();
    }

    public static AddressTestDataBuilder aValidAddress() {
        return new AddressTestDataBuilder()
                .withStreet("hezogpiixx")
                .withCity("lizcczaehv")
                .withZipCode("wiqsbcmfl")
;
    }

    public static AddressTestDataBuilder aDefaultAddress() {
        return new AddressTestDataBuilder()
                .withStreet("uugqnrpxok")
                .withCity("mmdccfvfpv")
                .withZipCode("uporkbdsy")
;
    }

    public static AddressTestDataBuilder aMinimalAddress() {
        return new AddressTestDataBuilder()
                .withStreet("rfkkjblgcb")
                .withCity("afpymavodd")
                .withZipCode("ojdpzvyht")
;
    }

    public static AddressTestDataBuilder aCompleteAddress() {
        return new AddressTestDataBuilder()
                .withStreet("lvbapzljbk")
                .withCity("ginrddlrqf")
                .withZipCode("eaxdocint")
;
    }

    public AddressTestDataBuilder withStreet(String street) {
        this.street = street;
        return this;
    }

    public AddressTestDataBuilder withCity(String city) {
        this.city = city;
        return this;
    }

    public AddressTestDataBuilder withZipCode(String zipCode) {
        this.zipCode = zipCode;
        return this;
    }

    public AddressTestDataBuilder withEmptyStreet() {
        this.street = "";
        return this;
    }

    public AddressTestDataBuilder withNullStreet() {
        this.street = null;
        return this;
    }

    public AddressTestDataBuilder withTooLongStreet() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= 150; i++) {
            sb.append("a");
        }
        this.street = sb.toString();
        return this;
    }

    public AddressTestDataBuilder withTooShortStreet() {
        this.street = "a";
        return this;
    }

    public AddressTestDataBuilder withEmptyCity() {
        this.city = "";
        return this;
    }

    public AddressTestDataBuilder withNullCity() {
        this.city = null;
        return this;
    }

    public AddressTestDataBuilder withTooLongCity() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= 100; i++) {
            sb.append("a");
        }
        this.city = sb.toString();
        return this;
    }

    public AddressTestDataBuilder withTooShortCity() {
        this.city = "a";
        return this;
    }

    public AddressTestDataBuilder withEmptyZipCode() {
        this.zipCode = "";
        return this;
    }

    public AddressTestDataBuilder withNullZipCode() {
        this.zipCode = null;
        return this;
    }

    public AddressTestDataBuilder withTooLongZipCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= 10; i++) {
            sb.append("a");
        }
        this.zipCode = sb.toString();
        return this;
    }

    public AddressTestDataBuilder withTooShortZipCode() {
        this.zipCode = "a";
        return this;
    }




    public AddressTestDataBuilder copy() {
        AddressTestDataBuilder copy = new AddressTestDataBuilder();
        copy.street = this.street;
        copy.city = this.city;
        copy.zipCode = this.zipCode;
        return copy;
    }

    public Address build() {
        Address address = new Address();
        address.setStreet(street);
        address.setCity(city);
        address.setZipCode(zipCode);


        return address;
    }

    public Address buildAndPersist() {
        Address entity = build();
        return entity;
    }
}
