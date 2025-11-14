package com.ktcloudinfra.userservice.service;

import com.ktcloudinfra.userservice.dto.request.CreateUserRequestDTO;
import com.ktcloudinfra.userservice.dto.request.LoginRequestDTO;
import com.ktcloudinfra.userservice.entity.User;
import com.ktcloudinfra.userservice.global.exception.ApiException;
import com.ktcloudinfra.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private BCryptPasswordEncoder encoder;

    @BeforeEach
    void setUp() {
        encoder = new BCryptPasswordEncoder();
        ReflectionTestUtils.setField(userService, "encoder", encoder);
    }

    @Test
    @DisplayName("회원가입 성공")
    void signUp_Success() {
        // Given
        CreateUserRequestDTO request = new CreateUserRequestDTO();
        ReflectionTestUtils.setField(request, "username", "testuser");
        ReflectionTestUtils.setField(request, "password", "password123");
        ReflectionTestUtils.setField(request, "name", "홍길동");
        ReflectionTestUtils.setField(request, "email", "test@example.com");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        userService.signUp(request);

        // Then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getUsername()).isEqualTo("testuser");
        assertThat(savedUser.getName()).isEqualTo("홍길동");
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getPassword()).isNotEqualTo("password123"); // Encoded
        assertThat(encoder.matches("password123", savedUser.getPassword())).isTrue();
    }

    @Test
    @DisplayName("회원가입 실패 - 이미 존재하는 사용자")
    void signUp_Fail_UserAlreadyExists() {
        // Given
        CreateUserRequestDTO request = new CreateUserRequestDTO();
        ReflectionTestUtils.setField(request, "username", "existinguser");
        ReflectionTestUtils.setField(request, "password", "password123");
        ReflectionTestUtils.setField(request, "name", "홍길동");
        ReflectionTestUtils.setField(request, "email", "test@example.com");

        User existingUser = User.builder()
                .username("existinguser")
                .password("encodedPassword")
                .name("기존사용자")
                .email("existing@example.com")
                .build();
        when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(existingUser));

        // When & Then
        assertThatThrownBy(() -> userService.signUp(request))
                .isInstanceOf(ApiException.class)
                .hasMessage("이미 존재하는 사용자입니다.");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("로그인 성공")
    void login_Success() {
        // Given
        LoginRequestDTO request = new LoginRequestDTO();
        ReflectionTestUtils.setField(request, "username", "testuser");
        ReflectionTestUtils.setField(request, "password", "password123");

        String encodedPassword = encoder.encode("password123");
        User user = User.builder()
                .username("testuser")
                .password(encodedPassword)
                .name("홍길동")
                .email("test@example.com")
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // When
        Long userId = userService.login(request);

        // Then
        assertThat(userId).isEqualTo(1L);
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    @DisplayName("로그인 실패 - 사용자가 존재하지 않음")
    void login_Fail_UserNotFound() {
        // Given
        LoginRequestDTO request = new LoginRequestDTO();
        ReflectionTestUtils.setField(request, "username", "nonexistent");
        ReflectionTestUtils.setField(request, "password", "password123");

        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(ApiException.class)
                .hasMessage("사용자가 존재하지 않습니다.");
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_Fail_WrongPassword() {
        // Given
        LoginRequestDTO request = new LoginRequestDTO();
        ReflectionTestUtils.setField(request, "username", "testuser");
        ReflectionTestUtils.setField(request, "password", "wrongpassword");

        String encodedPassword = encoder.encode("correctpassword");
        User user = User.builder()
                .username("testuser")
                .password(encodedPassword)
                .name("홍길동")
                .email("test@example.com")
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // When & Then
        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(ApiException.class)
                .hasMessage("비밀번호가 불일치합니다.");
    }

    @Test
    @DisplayName("사용자 조회 성공")
    void getUser_Success() {
        // Given
        Long userId = 1L;
        User user = User.builder()
                .username("testuser")
                .password("encodedPassword")
                .name("홍길동")
                .email("test@example.com")
                .build();
        ReflectionTestUtils.setField(user, "id", userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        User foundUser = userService.getUser(userId);

        // Then
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getId()).isEqualTo(userId);
        assertThat(foundUser.getUsername()).isEqualTo("testuser");
        assertThat(foundUser.getName()).isEqualTo("홍길동");
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("사용자 조회 실패 - 사용자가 존재하지 않음")
    void getUser_Fail_UserNotFound() {
        // Given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUser(userId))
                .isInstanceOf(ApiException.class)
                .hasMessage("해당 유저가 존재하지 않습니다.");
    }
}
