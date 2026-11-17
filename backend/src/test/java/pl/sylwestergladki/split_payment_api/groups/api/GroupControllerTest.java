
package pl.sylwestergladki.split_payment_api.groups.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import pl.sylwestergladki.split_payment_api.groups.application.CreateGroupUseCase;
import pl.sylwestergladki.split_payment_api.groups.application.FindUserIdByKeycloakSubjectUseCase;
import pl.sylwestergladki.split_payment_api.groups.application.GetGroupDetailsUseCase;
import pl.sylwestergladki.split_payment_api.groups.application.GetUserGroupsUseCase;
import pl.sylwestergladki.split_payment_api.groups.domain.Group;
import pl.sylwestergladki.split_payment_api.groups.domain.GroupId;
import pl.sylwestergladki.split_payment_api.identity.application.GetUsersByIdsUseCase;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;
import pl.sylwestergladki.split_payment_api.security.SecurityConfig;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GroupController.class)
@Import(SecurityConfig.class)
class GroupControllerTest {

    private static final String KEYCLOAK_SUBJECT = "keycloak-user-123";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateGroupUseCase createGroupUseCase;

    @MockitoBean
    private FindUserIdByKeycloakSubjectUseCase findUserIdUseCase;

    @MockitoBean
    private GetUserGroupsUseCase getUserGroupsUseCase;

    @MockitoBean
    private GetGroupDetailsUseCase getGroupDetailsUseCase;

    @MockitoBean
    private GetUsersByIdsUseCase getUsersByIdsUseCase;

    @Test
    void shouldCreateGroup() throws Exception {
        UserId userId = new UserId(UUID.randomUUID());
        Group group = Group.create("Mountain trip", userId);

        when(findUserIdUseCase.execute(KEYCLOAK_SUBJECT))
                .thenReturn(userId);

        when(createGroupUseCase.execute("Mountain trip", userId))
                .thenReturn(group);

        mockMvc.perform(post("/api/groups")
                        .with(jwt().jwt(jwt ->
                                jwt.subject(KEYCLOAK_SUBJECT)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Mountain trip"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id")
                        .value(group.id().value().toString()))
                .andExpect(jsonPath("$.name")
                        .value("Mountain trip"))
                .andExpect(jsonPath("$.createdBy")
                        .value(userId.value().toString()))
                .andExpect(jsonPath("$.members.length()")
                        .value(1))
                .andExpect(jsonPath("$.members[0].userId")
                        .value(userId.value().toString()));

        verify(findUserIdUseCase).execute(KEYCLOAK_SUBJECT);
        verify(createGroupUseCase)
                .execute("Mountain trip", userId);
    }

    @Test
    void shouldRejectEmptyGroupName() throws Exception {
        mockMvc.perform(post("/api/groups")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": ""
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(createGroupUseCase);
    }

    @Test
    void shouldRejectRequestWithoutAuthentication()
            throws Exception {

        mockMvc.perform(post("/api/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Mountain trip"
                                }
                                """))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(createGroupUseCase);
    }

    @Test
    void shouldReturnUserGroups() throws Exception {
        UserId userId = new UserId(UUID.randomUUID());
        Group group = Group.create("Mountain trip", userId);

        when(findUserIdUseCase.execute(KEYCLOAK_SUBJECT))
                .thenReturn(userId);

        when(getUserGroupsUseCase.execute(userId))
                .thenReturn(List.of(group));

        mockMvc.perform(get("/api/groups")
                        .with(jwt().jwt(jwt ->
                                jwt.subject(KEYCLOAK_SUBJECT))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id")
                        .value(group.id().value().toString()))
                .andExpect(jsonPath("$[0].name")
                        .value("Mountain trip"))
                .andExpect(jsonPath("$[0].createdBy")
                        .value(userId.value().toString()))
                .andExpect(jsonPath("$[0].members.length()")
                        .value(1))
                .andExpect(jsonPath("$[0].members[0].userId")
                        .value(userId.value().toString()));

        verify(findUserIdUseCase).execute(KEYCLOAK_SUBJECT);
        verify(getUserGroupsUseCase).execute(userId);
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoGroups()
            throws Exception {

        UserId userId = new UserId(UUID.randomUUID());

        when(findUserIdUseCase.execute(KEYCLOAK_SUBJECT))
                .thenReturn(userId);

        when(getUserGroupsUseCase.execute(userId))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/groups")
                        .with(jwt().jwt(jwt ->
                                jwt.subject(KEYCLOAK_SUBJECT))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(getUserGroupsUseCase).execute(userId);
    }

    @Test
    void shouldReturnGroupDetails() throws Exception {
        UserId userId = new UserId(UUID.randomUUID());
        Group group = Group.create("Mountain trip", userId);
        UUID groupId = group.id().value();

        when(findUserIdUseCase.execute(KEYCLOAK_SUBJECT))
                .thenReturn(userId);

        when(getGroupDetailsUseCase.execute(
                new GroupId(groupId),
                userId
        )).thenReturn(group);

        mockMvc.perform(get("/api/groups/{groupId}", groupId)
                        .with(jwt().jwt(jwt ->
                                jwt.subject(KEYCLOAK_SUBJECT))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(groupId.toString()))
                .andExpect(jsonPath("$.name")
                        .value("Mountain trip"))
                .andExpect(jsonPath("$.createdBy")
                        .value(userId.value().toString()))
                .andExpect(jsonPath("$.members.length()")
                        .value(1))
                .andExpect(jsonPath("$.members[0].userId")
                        .value(userId.value().toString()));

        verify(getGroupDetailsUseCase).execute(
                new GroupId(groupId),
                userId
        );
    }
}
