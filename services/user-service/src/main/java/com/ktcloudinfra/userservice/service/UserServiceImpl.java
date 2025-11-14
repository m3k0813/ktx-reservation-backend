package com.ktcloudinfra.userservice.service;

import com.ktcloudinfra.userservice.dto.request.CreateUserRequestDTO;
import com.ktcloudinfra.userservice.dto.request.LoginRequestDTO;
import com.ktcloudinfra.userservice.entity.User;
import com.ktcloudinfra.userservice.repository.UserRepository;
import com.ktcloudinfra.userservice.global.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    @Transactional
    public void signUp(CreateUserRequestDTO request) {
        if(userRepository.findByUsername(request.getUsername()).isPresent()){
            throw new ApiException("이미 존재하는 사용자입니다.");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(encoder.encode(request.getPassword()))
                .name(request.getName())
                .email(request.getEmail())
                .build();

        userRepository.save(user);
    }

    @Override
    @Transactional
    public Long login(LoginRequestDTO request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ApiException("사용자가 존재하지 않습니다."));

        if(!encoder.matches(request.getPassword(), user.getPassword())){
            throw new ApiException("비밀번호가 불일치합니다.");
        }

        return user.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public User getUser(Long userId) {

        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("해당 유저가 존재하지 않습니다."));
    }
}
