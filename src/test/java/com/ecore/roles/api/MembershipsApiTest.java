package com.ecore.roles.api;

import com.ecore.roles.model.Membership;
import com.ecore.roles.model.Role;
import com.ecore.roles.repository.MembershipRepository;
import com.ecore.roles.utils.RestAssuredHelper;
import com.ecore.roles.web.dto.MembershipDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static com.ecore.roles.utils.MockUtils.mockGetTeamById;
import static com.ecore.roles.utils.MockUtils.mockGetTeamByIdAndUserById;
import static com.ecore.roles.utils.RestAssuredHelper.createMembership;
import static com.ecore.roles.utils.RestAssuredHelper.getMemberships;
import static com.ecore.roles.utils.TestData.*;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MembershipsApiTest {

    private final MembershipRepository membershipRepository;
    private final RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    @LocalServerPort
    private int port;

    @Autowired
    public MembershipsApiTest(MembershipRepository membershipRepository, RestTemplate restTemplate) {
        this.membershipRepository = membershipRepository;
        this.restTemplate = restTemplate;
    }

    @BeforeEach
    public void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
        RestAssuredHelper.setUp(port);
        membershipRepository.deleteAll();
    }

    @Test
    public void shouldCreateRoleMembership() {
        Membership expectedMembership = DEFAULT_MEMBERSHIP();

        MembershipDto actualMembership = createDefaultMembership();

        assertThat(actualMembership.getId()).isNotNull();
        assertThat(actualMembership).isEqualTo(MembershipDto.fromModel(expectedMembership));
    }

    @Test
    public void shouldFailToCreateRoleMembershipWhenBodyIsNull() {
        createMembership(null)
                .validate(400, "Bad Request");
    }

    @Test
    public void shouldFailToCreateRoleMembershipWhenRoleIsNull() {
        Membership expectedMembership = DEFAULT_MEMBERSHIP();
        expectedMembership.setRole(null);

        createMembership(expectedMembership)
                .validate(400, "Bad Request");
    }

    @Test
    public void shouldFailToCreateRoleMembershipWhenRoleIdIsNull() {
        Membership expectedMembership = DEFAULT_MEMBERSHIP();
        expectedMembership.setRole(Role.builder().build());

        createMembership(expectedMembership)
                .validate(400, "Bad Request");
    }

    @Test
    public void shouldFailToCreateRoleMembershipWhenUserIdIsNull() {
        Membership expectedMembership = DEFAULT_MEMBERSHIP();
        expectedMembership.setUserId(null);

        createMembership(expectedMembership)
                .validate(400, "Bad Request");
    }

    @Test
    public void shouldFailToCreateRoleMembershipWhenTeamIdISNull() {
        Membership expectedMembership = DEFAULT_MEMBERSHIP();
        expectedMembership.setTeamId(null);

        createMembership(expectedMembership)
                .validate(400, "Bad Request");
    }

    @Test
    public void shouldFailToCreateRoleMembershipWhenMembershipAlreadyExists() {
        createDefaultMembership();

        createMembership(DEFAULT_MEMBERSHIP())
                .validate(400, "Membership already exists");
    }

    @Test
    public void shouldFailToCreateRoleMembershipWhenRoleDoesNotExist() {
        Membership expectedMembership = DEFAULT_MEMBERSHIP();
        expectedMembership.setRole(Role.builder().id(UUID_1).build());
        mockGetTeamByIdAndUserById(mockServer, expectedMembership.getTeamId(), ORDINARY_CORAL_LYNX_TEAM(),
                expectedMembership.getUserId(), GIANNI_USER());

        createMembership(expectedMembership)
                .validate(404, format("Role %s not found", UUID_1));
    }

    @Test
    public void shouldFailToCreateRoleMembershipWhenTeamDoesNotExist() {
        Membership expectedMembership = DEFAULT_MEMBERSHIP();
        mockGetTeamById(mockServer, expectedMembership.getTeamId(), null);

        createMembership(expectedMembership)
                .validate(404, format("Team %s not found", expectedMembership.getTeamId()));
    }

    @Test
    public void shouldFailToCreateRoleMembershipWhenUserDoesNotExist() {
        Membership expectedMembership = DEFAULT_MEMBERSHIP();
        mockGetTeamByIdAndUserById(mockServer, expectedMembership.getTeamId(), ORDINARY_CORAL_LYNX_TEAM(),
                expectedMembership.getUserId(), null);

        createMembership(expectedMembership)
                .validate(404, format("User %s not found", expectedMembership.getUserId()));
    }

    @Test
    public void shouldFailToCreateRoleWhenMembershipIsInvalid() {
        Membership expectedMembership = INVALID_MEMBERSHIP();
        mockGetTeamByIdAndUserById(mockServer, expectedMembership.getTeamId(), ORDINARY_CORAL_LYNX_TEAM(),
                expectedMembership.getUserId(), GENERIC_USER());

        createMembership(expectedMembership)
                .validate(400,
                        "Invalid 'Membership' object. The provided user doesn't belong to the provided team.");
    }

    @Test
    public void shouldGetAllMemberships() {
        createDefaultMembership();
        Membership expectedMembership = DEFAULT_MEMBERSHIP();

        MembershipDto[] actualMemberships = getMemberships(expectedMembership.getRole().getId())
                .statusCode(200)
                .extract().as(MembershipDto[].class);

        assertThat(actualMemberships.length).isEqualTo(1);
        assertThat(actualMemberships[0].getId()).isNotNull();
        assertThat(actualMemberships[0]).isEqualTo(MembershipDto.fromModel(expectedMembership));
    }

    @Test
    public void shouldGetAllMembershipsButReturnsEmptyList() {
        MembershipDto[] actualMemberships = getMemberships(DEVELOPER_ROLE_UUID)
                .statusCode(200)
                .extract().as(MembershipDto[].class);

        assertThat(actualMemberships.length).isEqualTo(0);
    }

    @Test
    public void shouldFailToGetAllMembershipsWhenRoleIdIsNull() {
        getMemberships(null)
                .validate(400, "Bad Request");
    }

    @Test
    public void shouldFailToGetAllMembershipsWhenRoleDoesNotExist() {
        getMemberships(UUID_1)
                .validate(404, format("Role %s not found", UUID_1));
    }

    private MembershipDto createDefaultMembership() {
        Membership expectedMembership = DEFAULT_MEMBERSHIP();
        mockGetTeamByIdAndUserById(mockServer, expectedMembership.getTeamId(), ORDINARY_CORAL_LYNX_TEAM(),
                expectedMembership.getUserId(), GIANNI_USER());

        return createMembership(expectedMembership)
                .statusCode(201)
                .extract().as(MembershipDto.class);
    }

}