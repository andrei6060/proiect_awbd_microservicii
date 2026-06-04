//package com.clinic.clinic.user;
//
//
//import com.clinic.clinic.Entity.User.User;
//import com.clinic.clinic.JpaRepo.UserJpaRepo;
//import com.clinic.clinic.role.Role;
//import com.clinic.clinic.role.RoleJpaRepo;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class UserServiceTest {
//
//    @Mock
//    private UserJpaRepo userJpaRepo;
//
//    @Mock
//    private RoleJpaRepo roleJpaRepo;
//
//    @Mock
//    private PasswordEncoder passwordEncoder;
//
//    @InjectMocks
//    private UserService userService;
//
//    private User user;
//    private Role userRole;
//
//    @BeforeEach
//    void setUp() {
//        userRole = Role.builder()
//                .id(1)
//                .name("USER")
//                .build();
//
//        user = User.builder()
//                .id(1)
//                .firstName("John")
//                .lastName("Doe")
//                .email("john@test.com")
//                .password("encodedPassword")
//                .specialization(null)
//                .enabled(true)
//                .accountLocked(false)
//                .roles(List.of(userRole))
//                .createdAt(LocalDateTime.now())
//                .build();
//    }
//
//    @Test
//    void getAllUsers_ShouldReturnUserResponseDtoList() {
//        when(userJpaRepo.findAll()).thenReturn(List.of(user));
//
//        List<UserResponseDto> result = userService.getAllUsers();
//
//        assertEquals(1, result.size());
//        assertEquals("John", result.getFirst().firstName());
//        assertEquals("Doe", result.get(0).lastName());
//        assertEquals("john@test.com", result.get(0).email());
//        assertTrue(result.get(0).enabled());
//        assertFalse(result.get(0).accountLocked());
//        assertEquals(List.of("USER"), result.get(0).roles());
//
//        verify(userJpaRepo).findAll();
//    }
//
//    @Test
//    void getUserById_WhenUserExists_ShouldReturnUserResponseDto() {
//        when(userJpaRepo.findById(1)).thenReturn(Optional.of(user));
//
//        UserResponseDto result = userService.getUserById(1);
//
//        assertEquals(1, result.id());
//        assertEquals("John", result.firstName());
//        assertEquals("Doe", result.lastName());
//        assertEquals("john@test.com", result.email());
//
//        verify(userJpaRepo).findById(1);
//    }
//
//    @Test
//    void getUserById_WhenUserDoesNotExist_ShouldThrowException() {
//        when(userJpaRepo.findById(99)).thenReturn(Optional.empty());
//
//        assertThrows(UserNotFoundException.class, () -> userService.getUserById(99));
//
//        verify(userJpaRepo).findById(99);
//    }
//
//    @Test
//    void createUser_WhenSpecializationIsNull_ShouldCreateUserWithUserRole() throws Exception {
//        RegistrationRequestDto request = new RegistrationRequestDto();
//        request.setFirstname("John");
//        request.setLastname("Doe");
//        request.setEmail("john@test.com");
//        request.setPassword("password");
//        request.setSpecialization(null);
//
//        when(roleJpaRepo.findByName("USER")).thenReturn(Optional.of(userRole));
//        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
//        when(userJpaRepo.save(any(User.class))).thenReturn(user);
//
//        UserResponseDto result = userService.createUser(request);
//
//        assertEquals("John", result.firstName());
//        assertEquals("john@test.com", result.email());
//        assertEquals(List.of("USER"), result.roles());
//
//        verify(roleJpaRepo).findByName("USER");
//        verify(passwordEncoder).encode("password");
//        verify(userJpaRepo).save(any(User.class));
//    }
//
//    @Test
//    void createUser_WhenRoleNotFound_ShouldThrowException() {
//        RegistrationRequestDto request = new RegistrationRequestDto();
//        request.setFirstname("John");
//        request.setLastname("Doe");
//        request.setEmail("john@test.com");
//        request.setPassword("password");
//        request.setSpecialization(null);
//
//        when(roleJpaRepo.findByName("USER")).thenReturn(Optional.empty());
//
//        assertThrows(IllegalArgumentException.class, () -> userService.createUser(request));
//
//        verify(roleJpaRepo).findByName("USER");
//        verify(userJpaRepo, never()).save(any());
//    }
//
//    @Test
//    void updateUser_WhenUserExists_ShouldUpdateUser() {
//        UpdateUserDto request = new UpdateUserDto();
//        request.setFirstname("Jane");
//        request.setLastname("Smith");
//        request.setEmail("jane@test.com");
//        request.setSpecialization(null);
//        request.setEnabled(false);
//        request.setAccountLocked(true);
//
//        when(userJpaRepo.findById(1)).thenReturn(Optional.of(user));
//        when(userJpaRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//        UserResponseDto result = userService.updateUser(1, request);
//
//        assertEquals("Jane", result.firstName());
//        assertEquals("Smith", result.lastName());
//        assertEquals("jane@test.com", result.email());
//        assertFalse(result.enabled());
//        assertTrue(result.accountLocked());
//
//        verify(userJpaRepo).findById(1);
//        verify(userJpaRepo).save(user);
//    }
//
//    @Test
//    void updateUser_WhenUserDoesNotExist_ShouldThrowException() {
//        UpdateUserDto request = new UpdateUserDto();
//
//        when(userJpaRepo.findById(99)).thenReturn(Optional.empty());
//
//        assertThrows(UserNotFoundException.class, () -> userService.updateUser(99, request));
//
//        verify(userJpaRepo).findById(99);
//        verify(userJpaRepo, never()).save(any());
//    }
//
//    @Test
//    void deleteUser_WhenUserExists_ShouldDeleteUser() {
//        when(userJpaRepo.existsById(1)).thenReturn(true);
//
//        userService.deleteUser(1);
//
//        verify(userJpaRepo).existsById(1);
//        verify(userJpaRepo).deleteById(1);
//    }
//
//    @Test
//    void deleteUser_WhenUserDoesNotExist_ShouldThrowException() {
//        when(userJpaRepo.existsById(99)).thenReturn(false);
//
//        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(99));
//
//        verify(userJpaRepo).existsById(99);
//        verify(userJpaRepo, never()).deleteById(anyInt());
//    }
//}
