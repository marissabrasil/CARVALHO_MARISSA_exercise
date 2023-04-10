package com.ecore.roles.service.impl;

import com.ecore.roles.exception.ResourceExistsException;
import com.ecore.roles.exception.ResourceNotFoundException;
import com.ecore.roles.model.Membership;
import com.ecore.roles.model.Role;
import com.ecore.roles.repository.MembershipRepository;
import com.ecore.roles.repository.RoleRepository;
import com.ecore.roles.service.RolesService;
import com.ecore.roles.service.TeamsService;
import com.ecore.roles.service.UsersService;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Log4j2
@Service
public class RolesServiceImpl implements RolesService {

    private final RoleRepository roleRepository;
    private final MembershipRepository membershipRepository;
    private final TeamsService teamsService;
    private final UsersService usersService;

    @Autowired
    public RolesServiceImpl(
            RoleRepository roleRepository,
            MembershipRepository membershipRepository,
            TeamsService teamsService,
            UsersService usersService) {
        this.roleRepository = roleRepository;
        this.membershipRepository = membershipRepository;
        this.teamsService = teamsService;
        this.usersService = usersService;
    }

    @Override
    public Role createRole(@NonNull Role role) {
        if (roleRepository.findByName(role.getName()).isPresent()) {
            throw new ResourceExistsException(Role.class);
        }

        return roleRepository.save(role);
    }

    @Override
    public Role getRole(@NonNull UUID roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException(Role.class, roleId));
    }

    @Override
    public List<Role> getRoles() {
        return roleRepository.findAll();
    }

    @Override
    public Role getRoleByUserIdAndTeamId(@NonNull UUID userId, @NonNull UUID teamId) {
        teamsService.getTeam(teamId);
        usersService.getUser(userId);

        Membership membership = membershipRepository.findByUserIdAndTeamId(userId, teamId)
                .orElseThrow(() -> new ResourceNotFoundException(Membership.class));

        UUID roleId = membership.getRole().getId();
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException(Role.class, roleId));
    }

}
