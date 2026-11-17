package pl.sylwestergladki.split_payment_api.groups.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import pl.sylwestergladki.split_payment_api.groups.application.CreateGroupUseCase;
import pl.sylwestergladki.split_payment_api.groups.application.FindUserIdByKeycloakSubjectUseCase;
import pl.sylwestergladki.split_payment_api.groups.application.GetGroupDetailsUseCase;
import pl.sylwestergladki.split_payment_api.groups.application.GetUserGroupsUseCase;
import pl.sylwestergladki.split_payment_api.groups.domain.Group;
import pl.sylwestergladki.split_payment_api.groups.domain.GroupId;
import pl.sylwestergladki.split_payment_api.groups.domain.model.GroupMember;
import pl.sylwestergladki.split_payment_api.identity.application.GetUsersByIdsUseCase;
import pl.sylwestergladki.split_payment_api.identity.domain.AppUser;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final CreateGroupUseCase createGroupUseCase;
    private final FindUserIdByKeycloakSubjectUseCase findUserIdUseCase;
    private final GetUserGroupsUseCase getUserGroupsUseCase;
    private final GetGroupDetailsUseCase getGroupDetailsUseCase;
    private final GetUsersByIdsUseCase getUsersByIdsUseCase;

    public GroupController(
            CreateGroupUseCase createGroupUseCase,
            FindUserIdByKeycloakSubjectUseCase findUserIdUseCase, GetUserGroupsUseCase getUserGroupsUseCase, GetGroupDetailsUseCase getGroupDetailsUseCase, GetUsersByIdsUseCase getUsersByIdsUseCase
    ) {
        this.createGroupUseCase = createGroupUseCase;
        this.findUserIdUseCase = findUserIdUseCase;
        this.getUserGroupsUseCase = getUserGroupsUseCase;
        this.getGroupDetailsUseCase = getGroupDetailsUseCase;
        this.getUsersByIdsUseCase = getUsersByIdsUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GroupResponse createGroup(
            @Valid @RequestBody CreateGroupRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String keycloakSubject = jwt.getSubject();

        UserId creatorId = resolveUserId(keycloakSubject);

        Group group = createGroupUseCase.execute(
                request.name(),
                creatorId
        );

        return toResponse(group);
    }

    @GetMapping
    public List<GroupResponse> getMyGroups(
            @AuthenticationPrincipal Jwt jwt
    ) {
        UserId userId = findUserIdUseCase.execute(jwt.getSubject());

        return getUserGroupsUseCase.execute(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{groupId}")
    public GroupResponse getGroupDetails(
            @PathVariable UUID groupId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UserId userId = findUserIdUseCase.execute(jwt.getSubject());

        Group group = getGroupDetailsUseCase.execute(
                new GroupId(groupId),
                userId
        );

        return toResponse(group);
    }

    private UserId resolveUserId(String keycloakSubject) {
        return findUserIdUseCase.execute(keycloakSubject);
    }

    private GroupResponse toResponse(Group group) {

        List<UserId> memberIds = group.members().stream()
                .map(GroupMember::userId)
                .toList();

        Map<UserId, AppUser> usersById =
                getUsersByIdsUseCase.execute(memberIds)
                        .stream()
                        .collect(Collectors.toMap(
                                AppUser::id,
                                Function.identity()
                        ));

        return new GroupResponse(
                group.id().value(),
                group.name(),
                group.createdBy().value(),
                group.createdAt(),
                group.members().stream()
                        .map(member -> {
                            AppUser user = usersById.get(member.userId());

                            return new GroupResponse.GroupMemberResponse(
                                    member.userId().value(),
                                    user != null
                                            ? user.displayName()
                                            : "Unknown user",
                                    member.joinedAt()
                            );
                        })
                        .toList()
        );
    }
}
