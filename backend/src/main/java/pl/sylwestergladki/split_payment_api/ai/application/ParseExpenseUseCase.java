
package pl.sylwestergladki.split_payment_api.ai.application;

import org.springframework.stereotype.Service;
import pl.sylwestergladki.split_payment_api.ai.application.model.ExpenseParsingContext;
import pl.sylwestergladki.split_payment_api.ai.application.model.ExpenseResolutionContext;
import pl.sylwestergladki.split_payment_api.ai.application.model.ResolvedExpense;
import pl.sylwestergladki.split_payment_api.ai.application.port.ExpenseParsingGroupProvider;
import pl.sylwestergladki.split_payment_api.ai.application.port.ExpenseTextParser;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.util.List;

@Service
public class ParseExpenseUseCase {

    private final ExpenseParsingGroupProvider groupProvider;
    private final ExpenseTextParser expenseTextParser;
    private final ParsedExpenseResolver resolver;

    public ParseExpenseUseCase(
            ExpenseParsingGroupProvider groupProvider,
            ExpenseTextParser expenseTextParser,
            ParsedExpenseResolver resolver
    ) {
        this.groupProvider = groupProvider;
        this.expenseTextParser = expenseTextParser;
        this.resolver = resolver;
    }

    public ResolvedExpense execute(
            UserId requesterId,
            String text
    ) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                    "Expense text cannot be blank"
            );
        }

        if (text.length() > 1000) {
            throw new IllegalArgumentException(
                    "Expense text is too long"
            );
        }

        var groups = groupProvider.findGroupsForUser(requesterId);

        if (groups.isEmpty()) {
            throw new IllegalArgumentException(
                    "Requester does not belong to any group"
            );
        }

        var parsingContext = new ExpenseParsingContext(
                requesterId,
                groups
        );

        var parsed = expenseTextParser.parse(text, parsingContext);

        if (parsed.groupId() == null) {
            throw new IllegalArgumentException(
                    "Could not identify expense group"
            );
        }

        boolean groupAllowed = groups.stream()
                .anyMatch(group ->
                        group.groupId().value()
                                .equals(parsed.groupId())
                );

        if (!groupAllowed) {
            throw new IllegalArgumentException(
                    "Parsed group is not available to requester"
            );
        }

        var selectedGroup = groups.stream()
                .filter(group ->
                        group.groupId().value()
                                .equals(parsed.groupId())
                )
                .findFirst()
                .orElseThrow();

        var resolutionContext = new ExpenseResolutionContext(
                selectedGroup.groupId(),
                requesterId,
                selectedGroup.members()
        );

        return resolver.resolve(parsed, resolutionContext);
    }
}