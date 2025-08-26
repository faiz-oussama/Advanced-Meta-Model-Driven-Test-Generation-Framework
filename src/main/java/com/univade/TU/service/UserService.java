package com.univade.TU.service;

import com.univade.TU.entity.Post;
import com.univade.TU.entity.User;
import com.univade.TU.exception.EntityNotFoundException;
import com.univade.TU.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public User updateUser(Long id, User user) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        
        User existingUser = getUserById(id);
        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());
        existingUser.setAge(user.getAge());
        existingUser.setAddress(user.getAddress());

        existingUser.getPosts().clear();
        if (user.getPosts() != null) {
            for (Post post : user.getPosts()) {
                post.setAuthor(existingUser);
                existingUser.getPosts().add(post);
            }
        }

        return userRepository.save(existingUser);
    }

    public void deleteUser(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean existsUserById(Long id) {
        if (id == null) {
            return false;
        }
        return userRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    public long countUsers() {
        return userRepository.count();
    }

    @Transactional(readOnly = true)
    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public List<User> findUsersByAge(Integer age) {
        return userRepository.findByAge(age);
    }

    @Transactional(readOnly = true)
    public List<User> findUsersByAgeRange(Integer minAge, Integer maxAge) {
        return userRepository.findByAgeBetween(minAge, maxAge);
    }


}
