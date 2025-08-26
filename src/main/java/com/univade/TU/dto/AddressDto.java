package com.univade.TU.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {

    private Long id;

    @NotBlank(message = "Street cannot be blank")
    @Size(max = 150, message = "Street must not exceed 150 characters")
    private String street;

    @NotBlank(message = "City cannot be blank")
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @NotBlank(message = "Zip code cannot be blank")
    @Size(max = 10, message = "Zip code must not exceed 10 characters")
    private String zipCode;
}
