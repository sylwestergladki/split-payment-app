package pl.sylwestergladki.split_payment_api.ai.application.model;

import pl.sylwestergladki.split_payment_api.groups.domain.GroupId;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.util.List;

public record ExpenseResolutionContext(
        GroupId groupId,
        UserId requesterId,
        List<ParsingGroupMember> members
) {
}