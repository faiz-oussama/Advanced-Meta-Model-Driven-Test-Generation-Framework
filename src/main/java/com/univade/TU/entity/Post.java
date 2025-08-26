package com.univade.TU.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title cannot be blank")
    @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
    @Column(name = "title", length = 255)
    private String title;

    @NotBlank(message = "Content cannot be blank")
    @Size(min = 10, max = 5000, message = "Content must be between 10 and 5000 characters")
    @Lob
    @Column(name = "content")
    private String content;

    @NotNull(message = "Author cannot be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;
}
