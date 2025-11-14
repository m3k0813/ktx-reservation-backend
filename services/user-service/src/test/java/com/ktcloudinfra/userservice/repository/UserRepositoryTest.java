package com.ktcloudinfra.userservice.repository;

import com.ktcloudinfra.userservice.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("사용자명으로 사용자 조회 - 성공")
    void findByUsername_Success() {
        // Given
        User user = User.builder()
                .username("testuser")
                .password("encodedPassword")
                .name("홍길동")
                .email("test@example.com")
                .build();
        entityManager.persist(user);
        entityManager.flush();

        // When
        Optional<User> found = userRepository.findByUsername("testuser");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
        assertThat(found.get().getName()).isEqualTo("홍길동");
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("사용자명으로 사용자 조회 - 존재하지 않음")
    void findByUsername_NotFound() {
        // When
        Optional<User> found = userRepository.findByUsername("nonexistent");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("사용자 저장 및 ID로 조회")
    void saveAndFindById() {
        // Given
        User user = User.builder()
                .username("newuser")
                .password("password123")
                .name("김철수")
                .email("newuser@example.com")
                .build();

        // When
        User saved = userRepository.save(user);
        Optional<User> found = userRepository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
        assertThat(found.get().getUsername()).isEqualTo("newuser");
    }

    @Test
    @DisplayName("모든 사용자 조회")
    void findAll() {
        // Given
        User user1 = User.builder()
                .username("user1")
                .password("pass1")
                .name("사용자1")
                .email("user1@example.com")
                .build();
        User user2 = User.builder()
                .username("user2")
                .password("pass2")
                .name("사용자2")
                .email("user2@example.com")
                .build();
        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.flush();

        // When
        var users = userRepository.findAll();

        // Then
        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getUsername)
                .containsExactlyInAnyOrder("user1", "user2");
    }
}
