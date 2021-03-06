package com.sadadream.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.sadadream.domain.Role;
import com.sadadream.domain.RoleRepository;
import com.sadadream.domain.User;
import com.sadadream.domain.UserRepository;
import com.sadadream.errors.InvalidTokenException;
import com.sadadream.errors.LoginFailException;
import com.sadadream.utils.JwtUtil;

class AuthenticationServiceTest {
    private static final String SECRET = "12345678901234567890123456789012";

    private static final String VALID_TOKEN = "eyJhbGciOiJIUzI1NiJ9." +
        "eyJ1c2VySWQiOjF9.ZZ3CUl0jxeLGvQ1Js5nG2Ty5qGTlqai5ubDMXZOdaDk";
    private static final String INVALID_TOKEN = "eyJhbGciOiJIUzI1NiJ9." +
        "eyJ1c2VySWQiOjF9.ZZ3CUl0jxeLGvQ1Js5nG2Ty5qGTlqai5ubDMXZOdaD0";

    private AuthenticationService authenticationService;

    private final UserRepository userRepository = mock(UserRepository.class);
    private final RoleRepository roleRepository = mock(RoleRepository.class);

    @BeforeEach
    void setUp() {
        JwtUtil jwtUtil = new JwtUtil(SECRET);
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        authenticationService = new AuthenticationService(
            userRepository, roleRepository, jwtUtil, passwordEncoder);

        User user = User.builder()
            .id(1L)
            .build();

        user.changePassword("valid_password", passwordEncoder);

        given(userRepository.findByEmail("tester@example.com"))
            .willReturn(Optional.of(user));

        given(roleRepository.findAllByUserId(1L))
            .willReturn(Arrays.asList(new Role("USER")));
        given(roleRepository.findAllByUserId(1004L))
            .willReturn(Arrays.asList(new Role("USER"), new Role("ADMIN")));
    }

    @DisplayName("????????? ???????????? ??????????????? ???????????? ?????? ??? ????????????.")
    @Test
    void loginWithRightEmailAndPassword() {
        String accessToken = authenticationService.login(
                "tester@example.com", "valid_password");

        assertThat(accessToken).isEqualTo(VALID_TOKEN);

        verify(userRepository).findByEmail("tester@example.com");
    }

    @DisplayName("????????? ???????????? ???????????? ?????? ?????? ??? ????????? ????????????.")
    @Test
    void loginWithWrongEmail() {
        assertThatThrownBy(
                () -> authenticationService.login("wrong@example.com", "test")
        ).isInstanceOf(LoginFailException.class);

        verify(userRepository).findByEmail("wrong@example.com");
    }

    @DisplayName("????????? ??????????????? ???????????? ?????? ?????? ??? ????????? ????????????.")
    @Test
    void loginWithWrongPassword() {
        assertThatThrownBy(
                () -> authenticationService.login("tester@example.com", "xxx")
        ).isInstanceOf(LoginFailException.class);

        verify(userRepository).findByEmail("tester@example.com");
    }

    @DisplayName("?????? ????????? ????????? ????????? ????????? ????????????, ?????? ????????? ????????? ????????????.")
    @Test
    void parseTokenWithValidToken() {
        Long userId = authenticationService.parseToken(VALID_TOKEN);

        assertThat(userId).isEqualTo(1L);
    }

    @DisplayName("???????????? ?????? ????????? ????????? ????????????, ????????? ????????????.")
    @Test
    void parseTokenWithInvalidToken() {
        assertThatThrownBy(
                () -> authenticationService.parseToken(INVALID_TOKEN)
        ).isInstanceOf(InvalidTokenException.class);
    }

    @DisplayName("????????? ?????? ????????? ??????????????? ??? ????????? ?????? ????????? ????????????.")
    @Test
    void roles() {
        assertThat(authenticationService.roles(1L)
            .stream()
            .map(Role::getRole)
            .collect(Collectors.toList()))
            .isEqualTo(Arrays.asList("USER"));

        assertThat(authenticationService.roles(1004L)
            .stream()
            .map(Role::getRole)
            .collect(Collectors.toList()))
            .isEqualTo(Arrays.asList("USER", "ADMIN"));
    }
}
