package com.ecore.roles.service;

import com.ecore.roles.exception.ResourceExistsException;
import com.ecore.roles.exception.ResourceNotFoundException;
import com.ecore.roles.model.Membership;
import com.ecore.roles.model.Role;
import com.ecore.roles.repository.MembershipRepository;
import com.ecore.roles.repository.RoleRepository;
import com.ecore.roles.service.impl.RolesServiceImpl;
import com.ecore.roles.service.impl.TeamsServiceImpl;
import com.ecore.roles.service.impl.UsersServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.ecore.roles.utils.TestData.*;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RolesServiceTest {

    @InjectMocks
    private RolesServiceImpl rolesService;

    @Mock
    private RoleRepository roleRepository;
    @Mock
    private TeamsServiceImpl teamsServiceImpl;
    @Mock
    private UsersServiceImpl usersServiceImpl;
    @Mock
    private MembershipRepository membershipRepository;

    @Test
    public void shouldCreateRole() {
        Role developerRole = DEVELOPER_ROLE();
        when(roleRepository.findByName(developerRole.getName())).thenReturn(Optional.empty());
        when(roleRepository.save(developerRole)).thenReturn(developerRole);

        Role role = rolesService.createRole(developerRole);

        assertNotNull(role);
        assertEquals(developerRole, role);
    }

    @Test
    public void shouldFailToCreateRoleWhenRoleIsNull() {
        assertThrows(NullPointerException.class,
                () -> rolesService.createRole(null));
    }

    @Test
    public void shouldFailToCreateRoleWhenRoleNameExist() {
        Role developerRole = DEVELOPER_ROLE();
        when(roleRepository.findByName(developerRole.getName())).thenReturn(Optional.of(developerRole));

        ResourceExistsException exception = assertThrows(ResourceExistsException.class,
                () -> rolesService.createRole(developerRole));

        assertEquals("Role already exists", exception.getMessage());
        verify(roleRepository, times(0)).findById(any());
    }

    @Test
    public void shouldReturnRoleWhenRoleIdExists() {
        Role developerRole = DEVELOPER_ROLE();
        when(roleRepository.findById(developerRole.getId())).thenReturn(Optional.of(developerRole));

        Role role = rolesService.getRole(developerRole.getId());

        assertNotNull(role);
        assertEquals(developerRole, role);
    }

    @Test
    public void shouldFailToGetRoleWhenRoleIdDoesNotExist() {
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> rolesService.getRole(UUID_1));

        assertEquals(format("Role %s not found", UUID_1), exception.getMessage());
    }

    @Test
    public void shouldGetRoleByUserIdAndTeamId() {
        Role expectedRole = DEVELOPER_ROLE();
        Membership expectedMembership = DEFAULT_MEMBERSHIP();

        when(teamsServiceImpl.getTeam(expectedMembership.getTeamId()))
                .thenReturn(ORDINARY_CORAL_LYNX_TEAM());
        when(usersServiceImpl.getUser(expectedMembership.getUserId()))
                .thenReturn(GIANNI_USER());
        when(membershipRepository.findByUserIdAndTeamId(expectedMembership.getUserId(),
                expectedMembership.getTeamId()))
                        .thenReturn(Optional.of(expectedMembership));
        when(roleRepository.findById(expectedMembership.getRole().getId()))
                .thenReturn(Optional.of(expectedRole));

        Role actualRole = rolesService.getRoleByUserIdAndTeamId(expectedMembership.getUserId(),
                expectedMembership.getTeamId());

        assertNotNull(actualRole);
        assertEquals(expectedRole, actualRole);
        verify(roleRepository).findById(expectedMembership.getRole().getId());
    }

    @Test
    public void shouldFailToGetRoleByUserIdAndTeamIdWhenMembershipDoesNotExist() {
        Membership expectedMembership = DEFAULT_MEMBERSHIP();

        when(teamsServiceImpl.getTeam(expectedMembership.getTeamId()))
                .thenReturn(ORDINARY_CORAL_LYNX_TEAM());
        when(usersServiceImpl.getUser(expectedMembership.getUserId()))
                .thenReturn(GIANNI_USER());
        when(membershipRepository.findByUserIdAndTeamId(expectedMembership.getUserId(),
                expectedMembership.getTeamId()))
                        .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> rolesService.getRoleByUserIdAndTeamId(expectedMembership.getUserId(),
                        expectedMembership.getTeamId()));

        assertEquals("Membership not found", exception.getMessage());
    }

    @Test
    public void shouldFailToGetRoleByUserIdAndTeamIdWhenRoleDoesNotExist() {
        Membership expectedMembership = DEFAULT_MEMBERSHIP();
        expectedMembership.setRole(Role.builder().id(UUID_1).build());

        when(teamsServiceImpl.getTeam(expectedMembership.getTeamId()))
                .thenReturn(ORDINARY_CORAL_LYNX_TEAM());
        when(usersServiceImpl.getUser(expectedMembership.getUserId()))
                .thenReturn(GIANNI_USER());
        when(membershipRepository.findByUserIdAndTeamId(expectedMembership.getUserId(),
                expectedMembership.getTeamId()))
                        .thenReturn(Optional.of(expectedMembership));
        when(roleRepository.findById(expectedMembership.getRole().getId()))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> rolesService.getRoleByUserIdAndTeamId(expectedMembership.getUserId(),
                        expectedMembership.getTeamId()));

        assertEquals(format("Role %s not found", UUID_1), exception.getMessage());
    }

    @Test
    public void shouldFailToGetRoleByUserIdAndTeamIdWhenUserIdIsNull() {
        assertThrows(NullPointerException.class,
                () -> rolesService.getRoleByUserIdAndTeamId(null, ORDINARY_CORAL_LYNX_TEAM_UUID));
    }

    @Test
    public void shouldFailToGetRoleByUserIdAndTeamIdWhenTeamIdIsNull() {
        assertThrows(NullPointerException.class,
                () -> rolesService.getRoleByUserIdAndTeamId(GIANNI_USER_UUID, null));
    }

    @Test
    public void shouldFailToGetRoleByUserIdAndTeamIdWhenUserIdAndTeamIdAreNull() {
        assertThrows(NullPointerException.class,
                () -> rolesService.getRoleByUserIdAndTeamId(null, null));
    }
}
