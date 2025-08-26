package com.univade.TU.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "persons")
public class Person {

    @Id
    @NotBlank(message = "CIN cannot be blank")
    @Size(min = 8, max = 12, message = "CIN must be between 8 and 12 characters")
    @Column(name = "cin", nullable = false, unique = true, length = 12)
    private String cin;

    @NotBlank(message = "First name cannot be blank")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @NotBlank(message = "Last name cannot be blank")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @NotNull(message = "Date of birth cannot be null")
    @Column(name = "date_of_birth", nullable = true)
    private LocalDate dateOfBirth;

    @Size(min = 10, max = 15, message = "Phone number must be between 10 and 15 characters")
    @Column(name = "phone_number", unique = true, length = 15)
    private String phoneNumber;

    @Email(message = "Email must be valid")
    @Column(name = "email", unique = true, length = 100)
    private String email;

    @Override
    public String toString() {
        return "Person{" +
                "cin='" + cin + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
