package com.ktcloudinfra.userservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ktcloudinfra.userservice.dto.request.CreateUserRequestDTO;
import com.ktcloudinfra.userservice.dto.request.LoginRequestDTO;
import com.ktcloudinfra.userservice.entity.User;
import com.ktcloudinfra.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("전체 플로우: 회원가입 -> 로그인 -> 사용자 조회")
    void completeUserFlow() throws Exception {
        // 1. 회원가입
        CreateUserRequestDTO signUpRequest = new CreateUserRequestDTO();
        ReflectionTestUtils.setField(signUpRequest, "username", "testuser");
        ReflectionTestUtils.setField(signUpRequest, "password", "password123");
        ReflectionTestUtils.setField(signUpRequest, "name", "홍길동");
        ReflectionTestUtils.setField(signUpRequest, "email", "test@example.com");

        mockMvc.perform(post("/api/v1/users/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().string("회원가입 완료"));

        // DB 검증
        User savedUser = userRepository.findByUsername("testuser").orElseThrow();
        assertThat(savedUser.getUsername()).isEqualTo("testuser");
        assertThat(savedUser.getName()).isEqualTo("홍길동");
        assertThat(encoder.matches("password123", savedUser.getPassword())).isTrue();

        // 2. 로그인
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        ReflectionTestUtils.setField(loginRequest, "username", "testuser");
        ReflectionTestUtils.setField(loginRequest, "password", "password123");

        String response = mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long userId = Long.parseLong(response);

        // 3. 사용자 조회
        mockMvc.perform(get("/api/v1/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.name").value("홍길동"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @DisplayName("중복 사용자 회원가입 실패")
    void signUp_Fail_DuplicateUser() throws Exception {
        // Given - 기존 사용자 생성
        User existingUser = User.builder()
                .username("existinguser")
                .password(encoder.encode("password123"))
                .name("기존사용자")
                .email("existing@example.com")
                .build();
        userRepository.save(existingUser);

        // When - 동일한 사용자명으로 회원가입 시도
        CreateUserRequestDTO request = new CreateUserRequestDTO();
        ReflectionTestUtils.setField(request, "username", "existinguser");
        ReflectionTestUtils.setField(request, "password", "newpassword");
        ReflectionTestUtils.setField(request, "name", "새사용자");
        ReflectionTestUtils.setField(request, "email", "new@example.com");

        // Then
        mockMvc.perform(post("/api/v1/users/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("존재하지 않는 사용자 로그인 실패")
    void login_Fail_UserNotFound() throws Exception {
        // Given
        LoginRequestDTO request = new LoginRequestDTO();
        ReflectionTestUtils.setField(request, "username", "nonexistent");
        ReflectionTestUtils.setField(request, "password", "password123");

        // When & Then
        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("비밀번호 불일치 로그인 실패")
    void login_Fail_WrongPassword() throws Exception {
        // Given - 사용자 생성
        User user = User.builder()
                .username("testuser")
                .password(encoder.encode("correctpassword"))
                .name("테스트")
                .email("test@example.com")
                .build();
        userRepository.save(user);

        LoginRequestDTO request = new LoginRequestDTO();
        ReflectionTestUtils.setField(request, "username", "testuser");
        ReflectionTestUtils.setField(request, "password", "wrongpassword");

        // When & Then
        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("존재하지 않는 사용자 조회 실패")
    void getUser_Fail_UserNotFound() throws Exception {
        // Given
        Long nonExistentUserId = 999L;

        // When & Then
        mockMvc.perform(get("/api/v1/users/{userId}", nonExistentUserId))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("여러 사용자 생성 및 조회")
    void multipleUsers() throws Exception {
        // Given - 3명의 사용자 생성
        for (int i = 1; i <= 3; i++) {
            CreateUserRequestDTO request = new CreateUserRequestDTO();
            ReflectionTestUtils.setField(request, "username", "user" + i);
            ReflectionTestUtils.setField(request, "password", "password" + i);
            ReflectionTestUtils.setField(request, "name", "사용자" + i);
            ReflectionTestUtils.setField(request, "email", "user" + i + "@example.com");

            mockMvc.perform(post("/api/v1/users/sign-up")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        // When & Then - DB에서 검증
        assertThat(userRepository.findAll()).hasSize(3);
        assertThat(userRepository.findByUsername("user1")).isPresent();
        assertThat(userRepository.findByUsername("user2")).isPresent();
        assertThat(userRepository.findByUsername("user3")).isPresent();
    }
}
