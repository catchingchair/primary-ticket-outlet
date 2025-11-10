package com.tickets.backend.service;

import com.tickets.backend.model.Role;
import com.tickets.backend.model.User;
import com.tickets.backend.model.UserRole;
import com.tickets.backend.model.Venue;
import com.tickets.backend.repository.RoleRepository;
import com.tickets.backend.repository.UserRepository;
import com.tickets.backend.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @InjectMocks
    private UserService userService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = User.builder()
            .id(UUID.randomUUID())
            .email("user@example.com")
            .displayName("Existing")
            .userRoles(new HashSet<>())
            .build();
    }

    @Test
    void findOrCreateUserCreatesWhenAbsent() {
        when(userRepository.findWithRolesByEmailIgnoreCase("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User created = userService.findOrCreateUser("new@example.com", "New User");

        assertThat(created.getId()).isNotNull();
        assertThat(created.getEmail()).isEqualTo("new@example.com");
        verify(userRepository).save(created);
    }

    @Test
    void ensureRolesAddsMissingRoles() {
        when(roleRepository.findByNameIgnoreCase("ROLE_MANAGER")).thenReturn(Optional.empty());
        when(roleRepository.findByNameIgnoreCase("ROLE_USER")).thenReturn(Optional.of(Role.builder().id(2L).name("ROLE_USER").build()));
        when(userRoleRepository.existsGlobalRole(existingUser.getId(), "ROLE_MANAGER")).thenReturn(false);
        when(userRoleRepository.existsGlobalRole(existingUser.getId(), "ROLE_USER")).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> {
            Role role = invocation.getArgument(0);
            return Role.builder().id(1L).name(role.getName()).build();
        });
        when(userRoleRepository.save(any(UserRole.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.ensureRoles(existingUser, List.of("ROLE_MANAGER", "user"));

        List<String> roleNames = existingUser.getUserRoles().stream()
            .map(UserRole::getRole)
            .map(Role::getName)
            .toList();

        assertThat(roleNames).containsExactlyInAnyOrder("ROLE_MANAGER", "ROLE_USER");
        verify(userRepository).save(existingUser);
    }

    @Test
    void assignManagerToVenueCreatesLinkWhenMissing() {
        Venue venue = Venue.builder()
            .id(UUID.randomUUID())
            .name("Main Hall")
            .build();
        when(roleRepository.findByNameIgnoreCase("ROLE_MANAGER"))
            .thenReturn(Optional.of(Role.builder().id(10L).name("ROLE_MANAGER").build()));
        when(userRoleRepository.findManagerAssignment(existingUser.getId(), "ROLE_MANAGER", venue.getId()))
            .thenReturn(Optional.empty());

        userService.assignManagerToVenue(existingUser, venue);

        verify(userRoleRepository).save(any(UserRole.class));
    }

    @Test
    void getRoleNamesReturnsDistinctNames() {
        Set<UserRole> roles = Set.of(
            UserRole.builder()
                .id(1L)
                .user(existingUser)
                .role(Role.builder().id(1L).name("ROLE_USER").build())
                .build(),
            UserRole.builder()
                .id(2L)
                .user(existingUser)
                .role(Role.builder().id(2L).name("ROLE_MANAGER").build())
                .build()
        );
        existingUser.setUserRoles(new HashSet<>(roles));

        List<String> names = userService.getRoleNames(existingUser);
        assertThat(names).containsExactlyInAnyOrder("ROLE_USER", "ROLE_MANAGER");
    }
}
