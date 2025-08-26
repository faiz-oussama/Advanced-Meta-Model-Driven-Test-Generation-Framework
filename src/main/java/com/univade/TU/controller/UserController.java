package com.univade.TU.controller;

import com.univade.TU.dto.UserDto;
import com.univade.TU.entity.User;
import com.univade.TU.exception.BadRequestException;
import com.univade.TU.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto userDto) {
        if (userDto == null) {
            throw new BadRequestException("Request body cannot be null");
        }
        User user = convertToEntity(userDto);
        User createdUser = userService.createUser(user);
        UserDto responseDto = convertToDto(createdUser);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdUser.getId())
                .toUri();

        return ResponseEntity.created(location).body(responseDto);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        if (id <= 0) {
            throw new BadRequestException("ID must be positive");
        }
        User user = userService.getUserById(id);
        UserDto userDto = convertToDto(user);
        return ResponseEntity.ok(userDto);
    }

    @GetMapping(value = {"", "/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserDto> userDtos = users.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDtos);
    }

    @GetMapping(value = "/paginated", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<UserDto>> getAllUsers(Pageable pageable) {
        if (pageable.getPageNumber() < 0) {
            throw new BadRequestException("Page number must be non-negative");
        }
        if (pageable.getPageSize() <= 0) {
            throw new BadRequestException("Page size must be positive");
        }
        if (pageable.getPageSize() > 1000) {
            throw new BadRequestException("Page size cannot exceed 1000");
        }

        Page<User> users = userService.getAllUsers(pageable);
        Page<UserDto> userDtos = users.map(this::convertToDto);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(userDtos.getTotalElements()))
                .body(userDtos);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @Valid @RequestBody UserDto userDto) {
        User user = convertToEntity(userDto);
        User updatedUser = userService.updateUser(id, user);
        UserDto responseDto = convertToDto(updatedUser);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> existsUserById(@PathVariable Long id) {
        boolean exists = userService.existsUserById(id);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countUsers() {
        long count = userService.countUsers();
        return ResponseEntity.ok(count);
    }

    private UserDto convertToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .age(user.getAge())
                .addressId(user.getAddress() != null ? user.getAddress().getId() : null)
                .build();
    }

    private User convertToEntity(UserDto userDto) {
        User user = new User();
        user.setId(userDto.getId());
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setAge(userDto.getAge());
        return user;
    }
}
