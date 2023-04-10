package com.ecore.roles.service;

import com.ecore.roles.client.TeamsClient;
import com.ecore.roles.client.model.Team;
import com.ecore.roles.exception.ResourceNotFoundException;
import com.ecore.roles.service.impl.TeamsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.List;

import static com.ecore.roles.utils.TestData.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeamsServiceTest {

    @InjectMocks
    private TeamsServiceImpl teamsService;
    @Mock
    private TeamsClient teamsClient;

    @Test
    public void shouldGetTeamWhenTeamIdExists() {
        Team ordinaryCoralLynxTeam = ORDINARY_CORAL_LYNX_TEAM();
        when(teamsClient.getTeam(ORDINARY_CORAL_LYNX_TEAM_UUID))
                .thenReturn(ResponseEntity
                        .status(HttpStatus.OK)
                        .body(ordinaryCoralLynxTeam));
        assertNotNull(teamsService.getTeam(ORDINARY_CORAL_LYNX_TEAM_UUID));
    }

    @Test
    public void shouldFailToGetTeamWhenTeamIdDoesNotExist() {
        when(teamsClient.getTeam(UUID_1))
                .thenReturn(ResponseEntity
                        .status(HttpStatus.OK)
                        .body(null));
        assertThrows(ResourceNotFoundException.class, () -> teamsService.getTeam(UUID_1));
    }

    @Test
    public void shouldGetAllTeams() {
        List<Team> expectedTeams = List.of(ORDINARY_CORAL_LYNX_TEAM());
        when(teamsClient.getTeams())
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(expectedTeams));
        assertNotNull(teamsService.getTeams());
    }
}
