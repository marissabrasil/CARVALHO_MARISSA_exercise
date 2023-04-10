package com.ecore.roles.service;

import com.ecore.roles.client.UsersClient;
import com.ecore.roles.client.model.User;
import com.ecore.roles.exception.ResourceNotFoundException;
import com.ecore.roles.service.impl.UsersServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static com.ecore.roles.utils.TestData.GIANNI_USER;
import static com.ecore.roles.utils.TestData.UUID_1;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UsersServiceTest {

    @InjectMocks
    private UsersServiceImpl usersService;
    @Mock
    private UsersClient usersClient;

    @Test
    public void shouldGetUserWhenUserIdExists() {
        User gianniUser = GIANNI_USER();
        when(usersClient.getUser(UUID_1))
                .thenReturn(ResponseEntity
                        .status(HttpStatus.OK)
                        .body(gianniUser));

        assertNotNull(usersService.getUser(UUID_1));
    }

    @Test
    public void shouldFailToGetUserWhenUserIdDoesNotExists() {
        when(usersClient.getUser(UUID_1))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(null));
        assertThrows(ResourceNotFoundException.class, () -> usersService.getUser(UUID_1));
    }

    @Test
    public void shouldGetAllUsers() {
        List<User> expectedUsers = List.of(GIANNI_USER());
        when(usersClient.getUsers())
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(expectedUsers));
        assertNotNull(usersService.getUsers());
    }
}
