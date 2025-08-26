package com.univade.TU.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDto {

    private Long id;

    @NotBlank(message = "Title cannot be blank")
    @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
    private String title;

    @NotBlank(message = "Content cannot be blank")
    @Size(min = 10, max = 5000, message = "Content must be between 10 and 5000 characters")
    private String content;

    @NotNull(message = "Author cannot be null")
    private Long authorId;
}
