package com.univade.TU.service;

import com.univade.TU.entity.Post;
import com.univade.TU.exception.EntityNotFoundException;
import com.univade.TU.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PostService {

    private final PostRepository postRepository;

    @Autowired
    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public Post createPost(Post post) {
        if (post == null) {
            throw new IllegalArgumentException("Post cannot be null");
        }
        return postRepository.save(post);
    }

    @Transactional(readOnly = true)
    public Post getPostById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        return postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Post> getAllPosts(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    public Post updatePost(Long id, Post post) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        if (post == null) {
            throw new IllegalArgumentException("Post cannot be null");
        }
        
        Post existingPost = getPostById(id);
        existingPost.setTitle(post.getTitle());
        existingPost.setContent(post.getContent());
        existingPost.setAuthor(post.getAuthor());
        
        return postRepository.save(existingPost);
    }

    public void deletePost(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        if (!postRepository.existsById(id)) {
            throw new EntityNotFoundException("Post not found with id: " + id);
        }
        postRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean existsPostById(Long id) {
        if (id == null) {
            return false;
        }
        return postRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    public long countPosts() {
        return postRepository.count();
    }

    @Transactional(readOnly = true)
    public Optional<Post> findPostByTitle(String title) {
        return postRepository.findByTitle(title);
    }

    @Transactional(readOnly = true)
    public List<Post> findPostsByAuthorId(Long authorId) {
        return postRepository.findByAuthorId(authorId);
    }

    @Transactional(readOnly = true)
    public List<Post> searchPostsByTitleOrContent(String searchTerm) {
        return postRepository.searchByTitleOrContent(searchTerm);
    }

    @Transactional(readOnly = true)
    public Page<Post> searchPostsByTitleOrContent(String searchTerm, Pageable pageable) {
        return postRepository.searchByTitleOrContent(searchTerm, pageable);
    }

    @Transactional(readOnly = true)
    public List<Post> findPostsByAuthorName(String authorName) {
        return postRepository.findByAuthorName(authorName);
    }
}
