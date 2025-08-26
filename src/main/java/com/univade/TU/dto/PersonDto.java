package com.univade.TU.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonDto {

    @NotBlank(message = "CIN cannot be blank")
    @Size(max = 12, message = "CIN must not exceed 12 characters")
    private String cin;

    @NotBlank(message = "First name cannot be blank")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    @NotBlank(message = "Last name cannot be blank")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Size(max = 15, message = "Phone number must not exceed 15 characters")
    private String phoneNumber;

    @NotNull(message = "Date of birth cannot be null")
    private LocalDate dateOfBirth;
}
