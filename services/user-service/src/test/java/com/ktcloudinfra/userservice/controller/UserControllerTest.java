package com.ktcloudinfra.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ktcloudinfra.userservice.dto.request.CreateUserRequestDTO;
import com.ktcloudinfra.userservice.dto.request.LoginRequestDTO;
import com.ktcloudinfra.userservice.entity.User;
import com.ktcloudinfra.userservice.global.config.SecurityConfig;
import com.ktcloudinfra.userservice.global.exception.ApiException;
import com.ktcloudinfra.userservice.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("POST /api/v1/users/sign-up - 회원가입 성공")
    void signUp_Success() throws Exception {
        // Given
        CreateUserRequestDTO request = new CreateUserRequestDTO();
        ReflectionTestUtils.setField(request, "username", "testuser");
        ReflectionTestUtils.setField(request, "password", "password123");
        ReflectionTestUtils.setField(request, "name", "홍길동");
        ReflectionTestUtils.setField(request, "email", "test@example.com");

        doNothing().when(userService).signUp(any(CreateUserRequestDTO.class));

        // When & Then
        mockMvc.perform(post("/api/v1/users/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string("회원가입 완료"));

        verify(userService).signUp(any(CreateUserRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/sign-up - 회원가입 실패 (중복 사용자)")
    void signUp_Fail_DuplicateUser() throws Exception {
        // Given
        CreateUserRequestDTO request = new CreateUserRequestDTO();
        ReflectionTestUtils.setField(request, "username", "existinguser");
        ReflectionTestUtils.setField(request, "password", "password123");
        ReflectionTestUtils.setField(request, "name", "홍길동");
        ReflectionTestUtils.setField(request, "email", "test@example.com");

        doThrow(new ApiException("이미 존재하는 사용자입니다."))
                .when(userService).signUp(any(CreateUserRequestDTO.class));

        // When & Then
        mockMvc.perform(post("/api/v1/users/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userService).signUp(any(CreateUserRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/login - 로그인 성공")
    void login_Success() throws Exception {
        // Given
        LoginRequestDTO request = new LoginRequestDTO();
        ReflectionTestUtils.setField(request, "username", "testuser");
        ReflectionTestUtils.setField(request, "password", "password123");

        when(userService.login(any(LoginRequestDTO.class))).thenReturn(1L);

        // When & Then
        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        verify(userService).login(any(LoginRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/login - 로그인 실패 (사용자 없음)")
    void login_Fail_UserNotFound() throws Exception {
        // Given
        LoginRequestDTO request = new LoginRequestDTO();
        ReflectionTestUtils.setField(request, "username", "nonexistent");
        ReflectionTestUtils.setField(request, "password", "password123");

        when(userService.login(any(LoginRequestDTO.class)))
                .thenThrow(new ApiException("사용자가 존재하지 않습니다."));

        // When & Then
        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userService).login(any(LoginRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/login - 로그인 실패 (비밀번호 불일치)")
    void login_Fail_WrongPassword() throws Exception {
        // Given
        LoginRequestDTO request = new LoginRequestDTO();
        ReflectionTestUtils.setField(request, "username", "testuser");
        ReflectionTestUtils.setField(request, "password", "wrongpassword");

        when(userService.login(any(LoginRequestDTO.class)))
                .thenThrow(new ApiException("비밀번호가 불일치합니다."));

        // When & Then
        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userService).login(any(LoginRequestDTO.class));
    }

    @Test
    @DisplayName("GET /api/v1/users/{userId} - 사용자 조회 성공")
    void getUser_Success() throws Exception {
        // Given
        Long userId = 1L;
        User user = User.builder()
                .username("testuser")
                .password("encodedPassword")
                .name("홍길동")
                .email("test@example.com")
                .build();
        ReflectionTestUtils.setField(user, "id", userId);

        when(userService.getUser(userId)).thenReturn(user);

        // When & Then
        mockMvc.perform(get("/api/v1/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.name").value("홍길동"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService).getUser(userId);
    }

    @Test
    @DisplayName("GET /api/v1/users/{userId} - 사용자 조회 실패 (사용자 없음)")
    void getUser_Fail_UserNotFound() throws Exception {
        // Given
        Long userId = 999L;
        when(userService.getUser(userId))
                .thenThrow(new ApiException("해당 유저가 존재하지 않습니다."));

        // When & Then
        mockMvc.perform(get("/api/v1/users/{userId}", userId))
                .andExpect(status().isBadRequest());

        verify(userService).getUser(userId);
    }
}
