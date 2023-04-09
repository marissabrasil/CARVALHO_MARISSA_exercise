package com.ecore.roles.service;

import com.ecore.roles.client.model.User;
import com.ecore.roles.exception.InvalidArgumentException;
import com.ecore.roles.exception.InvalidMembershipException;
import com.ecore.roles.exception.ResourceExistsException;
import com.ecore.roles.exception.ResourceNotFoundException;
import com.ecore.roles.model.Membership;
import com.ecore.roles.repository.MembershipRepository;
import com.ecore.roles.repository.RoleRepository;
import com.ecore.roles.service.impl.MembershipsServiceImpl;
import com.ecore.roles.service.impl.TeamsServiceImpl;
import com.ecore.roles.service.impl.UsersServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.ecore.roles.client.model.Team;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.ecore.roles.utils.TestData.*;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class MembershipsServiceTest {

    @InjectMocks
    private MembershipsServiceImpl membershipsService;
    @Mock
    private MembershipRepository membershipRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private TeamsServiceImpl teamsServiceImpl;
    @Mock
    private UsersServiceImpl usersServiceImpl;

    @Test
    public void shouldCreateMembership() {
        Membership expectedMembership = DEFAULT_MEMBERSHIP();
        Team expectedTeam = ORDINARY_CORAL_LYNX_TEAM();
        User expectedUser = GIANNI_USER();
        when(teamsServiceImpl.getTeam(expectedMembership.getTeamId()))
                .thenReturn(expectedTeam);
        when(usersServiceImpl.getUser(expectedMembership.getUserId()))
                .thenReturn(expectedUser);
        when(membershipRepository.findByUserIdAndTeamId(expectedMembership.getUserId(),
                expectedMembership.getTeamId()))
                        .thenReturn(Optional.empty());
        when(roleRepository.findById(expectedMembership.getRole().getId()))
                .thenReturn(Optional.ofNullable(DEVELOPER_ROLE()));
        when(membershipRepository
                .save(expectedMembership))
                        .thenReturn(expectedMembership);

        Membership actualMembership = membershipsService.assignRoleToMembership(expectedMembership);

        assertNotNull(actualMembership);
        assertEquals(actualMembership, expectedMembership);
        verify(roleRepository).findById(expectedMembership.getRole().getId());
    }

    @Test
    public void shouldFailToCreateMembershipWhenMembershipsIsNull() {
        assertThrows(NullPointerException.class,
                () -> membershipsService.assignRoleToMembership(null));
    }

    @Test
    public void shouldFailToCreateMembershipWhenItHasInvalidRole() {
        Membership expectedMembership = DEFAULT_MEMBERSHIP();
        expectedMembership.setRole(null);

        InvalidArgumentException exception = assertThrows(InvalidArgumentException.class,
                () -> membershipsService.assignRoleToMembership(expectedMembership));

        assertEquals("Invalid 'Role' object", exception.getMessage());
        verify(membershipRepository, times(0)).findByUserIdAndTeamId(any(), any());
        verify(roleRepository, times(0)).findById(any());
        verify(usersServiceImpl, times(0)).getUser(any());
        verify(teamsServiceImpl, times(0)).getTeam(any());
    }

    @Test
    public void shouldFailToCreateMembershipWhenMemberNotInTeam() {
        Membership expectedMembership = INVALID_MEMBERSHIP();
        Team expectedTeam = ORDINARY_CORAL_LYNX_TEAM();
        User expectedUser = GENERIC_USER();
        when(teamsServiceImpl.getTeam(expectedMembership.getTeamId()))
                .thenReturn(expectedTeam);
        when(usersServiceImpl.getUser(expectedMembership.getUserId()))
                .thenReturn(expectedUser);

        InvalidMembershipException exception = assertThrows(InvalidMembershipException.class,
                () -> membershipsService.assignRoleToMembership(expectedMembership));

        assertEquals("Invalid 'Membership' object. The provided user doesn't " +
                "belong to the provided team.", exception.getMessage());
        verify(membershipRepository, times(0)).findByUserIdAndTeamId(any(), any());
        verify(roleRepository, times(0)).findById(any());
    }

    @Test
    public void shouldFailToCreateMembershipWhenItExist() {
        Membership expectedMembership = DEFAULT_MEMBERSHIP();
        Team expectedTeam = ORDINARY_CORAL_LYNX_TEAM();
        User expectedUser = GIANNI_USER();
        when(teamsServiceImpl.getTeam(expectedMembership.getTeamId()))
                .thenReturn(expectedTeam);
        when(usersServiceImpl.getUser(expectedMembership.getUserId()))
                .thenReturn(expectedUser);
        when(membershipRepository.findByUserIdAndTeamId(expectedMembership.getUserId(),
                expectedMembership.getTeamId()))
                        .thenReturn(Optional.of(expectedMembership));

        ResourceExistsException exception = assertThrows(ResourceExistsException.class,
                () -> membershipsService.assignRoleToMembership(expectedMembership));

        assertEquals("Membership already exists", exception.getMessage());
        verify(roleRepository, times(0)).findById(any());
    }

    @Test
    public void shouldFailToCreateMembershipWhenRoleDoesNotExist() {
        Membership expectedMembership = DEFAULT_MEMBERSHIP();
        Team expectedTeam = ORDINARY_CORAL_LYNX_TEAM();
        User expectedUser = GIANNI_USER();
        when(teamsServiceImpl.getTeam(expectedMembership.getTeamId()))
                .thenReturn(expectedTeam);
        when(usersServiceImpl.getUser(expectedMembership.getUserId()))
                .thenReturn(expectedUser);
        when(membershipRepository.findByUserIdAndTeamId(expectedMembership.getUserId(),
                expectedMembership.getTeamId()))
                        .thenReturn(Optional.empty());
        when(roleRepository.findById(expectedMembership.getRole().getId()))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> membershipsService.assignRoleToMembership(expectedMembership));

        assertEquals(format("Role %s not found", expectedMembership.getRole().getId()),
                exception.getMessage());
    }

    @Test
    public void shouldGetMemberships() {
        Membership expectedMemberships = DEFAULT_MEMBERSHIP();
        UUID roleId = expectedMemberships.getRole().getId();

        when(roleRepository.findById(roleId))
                .thenReturn(Optional.of(DEVELOPER_ROLE()));
        when(membershipRepository.findByRoleId(roleId))
                .thenReturn(List.of(expectedMemberships));

        List<Membership> actualMemberships = membershipsService.getMemberships(roleId);

        assertNotNull(actualMemberships);
        assertEquals(actualMemberships, List.of(expectedMemberships));
        verify(roleRepository).findById(roleId);
        verify(membershipRepository).findByRoleId(roleId);
    }

    @Test
    public void shouldFailToGetMembershipsWhenRoleDoesNotExist() {
        when(roleRepository.findById(UUID_1))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> membershipsService.getMemberships(UUID_1));

        assertEquals(format("Role %s not found", UUID_1), exception.getMessage());
        verify(membershipRepository, times(0)).findByRoleId(any());
    }

    @Test
    public void shouldFailToGetMembershipsWhenRoleIdIsNull() {
        assertThrows(NullPointerException.class,
                () -> membershipsService.getMemberships(null));
    }

}
