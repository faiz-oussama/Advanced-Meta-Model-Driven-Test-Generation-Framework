package com.univade.TU.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Street cannot be blank")
    @Size(min = 5, max = 150, message = "Street must be between 5 and 150 characters")
    @Column(name = "street", length = 150)
    private String street;

    @NotBlank(message = "City cannot be blank")
    @Size(min = 2, max = 100, message = "City must be between 2 and 100 characters")
    @Column(name = "city", length = 100)
    private String city;

    @NotBlank(message = "Zip code cannot be blank")
    @Size(min = 3, max = 10, message = "Zip code must be between 3 and 10 characters")
    @Column(name = "zip_code", length = 10)
    private String zipCode;

    public Address(String street, String city, String zipCode) {
        this.street = street;
        this.city = city;
        this.zipCode = zipCode;
    }
}
