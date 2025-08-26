package com.univade.TU.controller;

import com.univade.TU.dto.PostDto;
import com.univade.TU.entity.Post;
import com.univade.TU.exception.BadRequestException;
import com.univade.TU.service.PostService;
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
@RequestMapping("/api/posts")
@Validated
public class PostController {

    private final PostService postService;

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<PostDto> createPost(@Valid @RequestBody PostDto postDto) {
        if (postDto == null) {
            throw new BadRequestException("Request body cannot be null");
        }
        Post post = convertToEntity(postDto);
        Post createdPost = postService.createPost(post);
        PostDto responseDto = convertToDto(createdPost);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdPost.getId())
                .toUri();

        return ResponseEntity.created(location).body(responseDto);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PostDto> getPostById(@PathVariable Long id) {
        if (id <= 0) {
            throw new BadRequestException("ID must be positive");
        }
        Post post = postService.getPostById(id);
        PostDto postDto = convertToDto(post);
        return ResponseEntity.ok(postDto);
    }

    @GetMapping(value = {"", "/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PostDto>> getAllPosts() {
        List<Post> posts = postService.getAllPosts();
        List<PostDto> postDtos = posts.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(postDtos);
    }

    @GetMapping(value = "/paginated", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<PostDto>> getAllPosts(Pageable pageable) {
        if (pageable.getPageNumber() < 0) {
            throw new BadRequestException("Page number must be non-negative");
        }
        if (pageable.getPageSize() <= 0) {
            throw new BadRequestException("Page size must be positive");
        }
        if (pageable.getPageSize() > 1000) {
            throw new BadRequestException("Page size cannot exceed 1000");
        }

        Page<Post> posts = postService.getAllPosts(pageable);
        Page<PostDto> postDtos = posts.map(this::convertToDto);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(postDtos.getTotalElements()))
                .body(postDtos);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostDto> updatePost(@PathVariable Long id, @Valid @RequestBody PostDto postDto) {
        Post post = convertToEntity(postDto);
        Post updatedPost = postService.updatePost(id, post);
        PostDto responseDto = convertToDto(updatedPost);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> existsPostById(@PathVariable Long id) {
        boolean exists = postService.existsPostById(id);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countPosts() {
        long count = postService.countPosts();
        return ResponseEntity.ok(count);
    }

    private PostDto convertToDto(Post post) {
        return PostDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .authorId(post.getAuthor() != null ? post.getAuthor().getId() : null)
                .build();
    }

    private Post convertToEntity(PostDto postDto) {
        Post post = new Post();
        post.setId(postDto.getId());
        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());
        return post;
    }
}
