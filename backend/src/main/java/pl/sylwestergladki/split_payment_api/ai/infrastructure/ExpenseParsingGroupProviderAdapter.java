package pl.sylwestergladki.split_payment_api.ai.infrastructure;

import org.springframework.stereotype.Component;

import pl.sylwestergladki.split_payment_api.ai.application.model.ParsingGroup;
import pl.sylwestergladki.split_payment_api.ai.application.model.ParsingGroupMember;
import pl.sylwestergladki.split_payment_api.ai.application.port.ExpenseParsingGroupProvider;
import pl.sylwestergladki.split_payment_api.ai.application.port.GroupMemberDisplayNameProvider;
import pl.sylwestergladki.split_payment_api.groups.domain.model.GroupMember;
import pl.sylwestergladki.split_payment_api.groups.domain.repository.GroupRepository;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ExpenseParsingGroupProviderAdapter
        implements ExpenseParsingGroupProvider {

    private final GroupRepository groupRepository;
    private final GroupMemberDisplayNameProvider memberProvider;

    public ExpenseParsingGroupProviderAdapter(
            GroupRepository groupRepository,
            GroupMemberDisplayNameProvider memberProvider
    ) {
        this.groupRepository = groupRepository;
        this.memberProvider = memberProvider;
    }

    @Override
    public List<ParsingGroup> findGroupsForUser(UserId requesterId) {

        var groups = groupRepository.findByMemberId(requesterId);

        return groups.stream()
                .filter(group -> group.hasMember(requesterId))
                .map(group -> {

                    List<UserId> memberIds = group.members()
                            .stream()
                            .map(GroupMember::userId)
                            .toList();

                    List<ParsingGroupMember> members =
                            memberProvider.findMembers(memberIds);

                    var resolvedIds = members.stream()
                            .map(ParsingGroupMember::userId)
                            .collect(Collectors.toSet());

                    if (members.size() != memberIds.size()
                            || !resolvedIds.equals(
                            new HashSet<>(memberIds)
                    )) {
                        throw new IllegalStateException(
                                "Could not resolve all group members"
                        );
                    }

                    return new ParsingGroup(
                            group.id(),
                            group.name(),
                            members
                    );
                })
                .toList();
    }
}