package com.univade.TU.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name cannot be blank")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Email(message = "Email must be valid")
    @NotBlank(message = "Email cannot be blank")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Min(value = 0, message = "Age must be positive")
    @Max(value = 150, message = "Age must be realistic")
    @Column(name = "age")
    private Integer age;


    // complex attribute
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private Address address;

    // collection of a complex type
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Post> posts = new ArrayList<>();


    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", age=" + age +
                ", address=" + address +
                ", postsCount=" + posts.size() +
                '}';
    }
}
