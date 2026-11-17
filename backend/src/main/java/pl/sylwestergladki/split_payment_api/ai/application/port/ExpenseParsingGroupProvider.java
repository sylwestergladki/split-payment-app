package pl.sylwestergladki.split_payment_api.ai.application.port;

import pl.sylwestergladki.split_payment_api.ai.application.model.ParsingGroup;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.util.List;

public interface ExpenseParsingGroupProvider {

    List<ParsingGroup> findGroupsForUser(UserId requesterId);
}