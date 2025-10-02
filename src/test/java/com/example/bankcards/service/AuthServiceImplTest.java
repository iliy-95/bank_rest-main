package com.example.bankcards.service;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.example.bankcards.dto.request.LoginRequest;
import com.example.bankcards.dto.request.RegisterUserRequest;
import com.example.bankcards.dto.response.MessageResponse;
import com.example.bankcards.dto.response.RegistrationResponse;
import com.example.bankcards.entity.BankUser;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.exception.UserAlreadyExistsException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.repository.UserRoleRepository;
import com.example.bankcards.security.JwtToken;
import com.example.bankcards.security.UserDetailsServiceImpl;
import com.example.bankcards.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    private static final String USERNAME = "testuser";

    private static final String PASSWORD = "password123";

    private static final String FULL_NAME = "Тестовый Пользователь";

    private static final String TOKEN = "jwt.token.value";

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private JwtToken jwtToken;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    private UserRole userRole;

    @BeforeEach
    void setUp() {
        userRole = new UserRole();
        userRole.setName("USER");
    }

    @Test
    @DisplayName("Успешная регистрация нового пользователя")
    void testRegisterUser_Success() {
        RegisterUserRequest request = new RegisterUserRequest(USERNAME, PASSWORD, FULL_NAME);

        when(userRepository.existsByUsername(USERNAME)).thenReturn(false);
        when(userRoleRepository.findByName("USER")).thenReturn(java.util.Optional.of(userRole));
        when(bCryptPasswordEncoder.encode(PASSWORD)).thenReturn("encodedPassword");
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername(USERNAME)).thenReturn(userDetails);
        when(jwtToken.generatorToken(userDetails)).thenReturn(TOKEN);

        RegistrationResponse response = authService.registerUser(request);

        assertThat(response).isNotNull();
        assertThat(response.message()).isEqualTo("Вы успешно зарегистрировались.");
        assertThat(response.token()).isEqualTo(TOKEN);

        ArgumentCaptor<BankUser> userCaptor = ArgumentCaptor.forClass(BankUser.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getUsername()).isEqualTo(USERNAME);
        assertThat(userCaptor.getValue().getPassword()).isEqualTo("encodedPassword");
    }

    @Test
    @DisplayName("Попытка регистрации существующего пользователя")
    void testRegisterUser_UserAlreadyExists() {
        RegisterUserRequest request = new RegisterUserRequest(USERNAME, PASSWORD, FULL_NAME);

        when(userRepository.existsByUsername(USERNAME)).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.registerUser(request));
        verify(userRepository, never()).save(any(BankUser.class));
    }

    @Test
    @DisplayName("Регистрация неудачна, если роль USER не найдена")
    void testRegisterUser_RoleNotFound() {
        RegisterUserRequest request = new RegisterUserRequest(USERNAME, PASSWORD, FULL_NAME);

        when(userRepository.existsByUsername(USERNAME)).thenReturn(false);
        when(userRoleRepository.findByName("USER")).thenReturn(java.util.Optional.empty());

        assertThrows(NotFoundException.class, () -> authService.registerUser(request));
        verify(userRepository, never()).save(any(BankUser.class));
    }

    @Test
    @DisplayName("Успешный вход пользователя")
    void testLoginUser_Success() {
        LoginRequest request = new LoginRequest(USERNAME, PASSWORD);
        Authentication auth = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);

        when(authenticationManager.authenticate(
            any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(jwtToken.generatorToken(userDetails)).thenReturn(TOKEN);

        MessageResponse response = authService.loginUser(request);

        assertThat(response).isNotNull();
        assertThat(response.message()).isEqualTo(TOKEN);
    }

    @Test
    @DisplayName("Неудачная попытка входа с неправильными данными")
    void testLoginUser_BadCredentials() {
        LoginRequest request = new LoginRequest(USERNAME, PASSWORD);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(BadCredentialsException.class);

        assertThrows(BadCredentialsException.class, () -> authService.loginUser(request));
    }
}