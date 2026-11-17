package pl.sylwestergladki.split_payment_api.ai.application.model;

import pl.sylwestergladki.split_payment_api.groups.domain.GroupId;

import java.util.List;

public record ParsingGroup(
        GroupId groupId,
        String name,
        List<ParsingGroupMember> members
) {
}