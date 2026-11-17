package pl.sylwestergladki.split_payment_api.ai.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pl.sylwestergladki.split_payment_api.ai.application.model.ExpenseParsingContext;
import pl.sylwestergladki.split_payment_api.ai.application.model.ParsedExpense;
import pl.sylwestergladki.split_payment_api.ai.application.model.ParsingGroup;
import pl.sylwestergladki.split_payment_api.ai.application.model.ParsingGroupMember;
import pl.sylwestergladki.split_payment_api.ai.application.model.ResolvedExpense;
import pl.sylwestergladki.split_payment_api.ai.application.port.ExpenseParsingGroupProvider;
import pl.sylwestergladki.split_payment_api.ai.application.port.ExpenseTextParser;
import pl.sylwestergladki.split_payment_api.groups.domain.GroupId;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParseExpenseUseCaseTest {

    @Mock
    private ExpenseParsingGroupProvider groupProvider;

    @Mock
    private ExpenseTextParser expenseTextParser;

    private ParsedExpenseResolver resolver;

    private ParseExpenseUseCase useCase;

    @BeforeEach
    void setUp() {
        resolver = new ParsedExpenseResolver();

        useCase = new ParseExpenseUseCase(
                groupProvider,
                expenseTextParser,
                resolver
        );
    }

    @Test
    void shouldParseExpenseAndIdentifyGroup() {
        // given
        var requesterId = UserId.generate();
        var annaId = UserId.generate();
        var groupId = new GroupId(UUID.randomUUID());

        var members = List.of(
                new ParsingGroupMember(requesterId, "John"),
                new ParsingGroupMember(annaId, "Anna")
        );

        var group = new ParsingGroup(
                groupId,
                "Trip",
                members
        );

        when(groupProvider.findGroupsForUser(requesterId))
                .thenReturn(List.of(group));

        var parsedExpense = new ParsedExpense(
                groupId.value(),
                "Dinner",
                new BigDecimal("180.00"),
                "PLN",
                "me",
                List.of("me", "Anna")
        );

        when(expenseTextParser.parse(
                eq("Group Trip, I paid 180 pln for dinner for me and Anna"),
                any(ExpenseParsingContext.class)
        )).thenReturn(parsedExpense);

        // when
        ResolvedExpense result = useCase.execute(
                requesterId,
                "Group Trip, I paid 180 pln for dinner for me and Anna"
        );

        // then
        assertThat(result.description()).isEqualTo("Dinner");
        assertThat(result.amount()).isEqualByComparingTo("180.00");
        assertThat(result.currency()).isEqualTo("PLN");
        assertThat(result.paidByUserId()).isEqualTo(requesterId);
        assertThat(result.participantUserIds())
                .containsExactly(requesterId, annaId);

        verify(groupProvider).findGroupsForUser(requesterId);
        verify(expenseTextParser).parse(
                eq("Group Trip, I paid 180 pln for dinner for me and Anna"),
                any(ExpenseParsingContext.class)
        );
    }

    @Test
    void shouldRejectParsingWhenRequesterHasNoGroups() {
        // given
        var requesterId = UserId.generate();

        when(groupProvider.findGroupsForUser(requesterId))
                .thenReturn(List.of());

        // when / then
        assertThatThrownBy(() ->
                useCase.execute(
                        requesterId,
                        "Dinner 180 pln"
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Requester does not belong to any group");

        verifyNoInteractions(expenseTextParser);
    }

    @Test
    void shouldRejectGroupNotAvailableToRequester() {
        // given
        var requesterId = UserId.generate();

        var allowedGroupId = new GroupId(UUID.randomUUID());
        var forbiddenGroupId = new GroupId(UUID.randomUUID());

        var group = new ParsingGroup(
                allowedGroupId,
                "Trip",
                List.of(
                        new ParsingGroupMember(requesterId, "John")
                )
        );

        when(groupProvider.findGroupsForUser(requesterId))
                .thenReturn(List.of(group));

        var parsedExpense = new ParsedExpense(
                forbiddenGroupId.value(),
                "Dinner",
                new BigDecimal("180.00"),
                "PLN",
                "me",
                List.of("me")
        );

        when(expenseTextParser.parse(
                eq("Dinner 180 pln"),
                any(ExpenseParsingContext.class)
        )).thenReturn(parsedExpense);

        // when / then
        assertThatThrownBy(() ->
                useCase.execute(
                        requesterId,
                        "Dinner 180 pln"
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Parsed group is not available to requester");
    }

    @Test
    void shouldRejectParsingWhenGroupCannotBeIdentified() {
        // given
        var requesterId = UserId.generate();
        var groupId = new GroupId(UUID.randomUUID());

        var group = new ParsingGroup(
                groupId,
                "Trip",
                List.of(
                        new ParsingGroupMember(requesterId, "John")
                )
        );

        when(groupProvider.findGroupsForUser(requesterId))
                .thenReturn(List.of(group));

        var parsedExpense = new ParsedExpense(
                null,
                "Dinner",
                new BigDecimal("180.00"),
                "PLN",
                "me",
                List.of("me")
        );

        when(expenseTextParser.parse(
                eq("Dinner 180 pln"),
                any(ExpenseParsingContext.class)
        )).thenReturn(parsedExpense);

        // when / then
        assertThatThrownBy(() ->
                useCase.execute(
                        requesterId,
                        "Dinner 180 pln"
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Could not identify expense group");
    }

    @Test
    void shouldRejectBlankText() {
        // given
        var requesterId = UserId.generate();

        // when / then
        assertThatThrownBy(() ->
                useCase.execute(
                        requesterId,
                        "   "
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Expense text cannot be blank");

        verifyNoInteractions(
                groupProvider,
                expenseTextParser
        );
    }

    @Test
    void shouldRejectNullText() {
        // given
        var requesterId = UserId.generate();

        // when / then
        assertThatThrownBy(() ->
                useCase.execute(
                        requesterId,
                        null
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Expense text cannot be blank");

        verifyNoInteractions(
                groupProvider,
                expenseTextParser
        );
    }

    @Test
    void shouldRejectTextLongerThan1000Characters() {
        // given
        var requesterId = UserId.generate();
        var text = "a".repeat(1001);

        // when / then
        assertThatThrownBy(() ->
                useCase.execute(
                        requesterId,
                        text
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Expense text is too long");

        verifyNoInteractions(
                groupProvider,
                expenseTextParser
        );
    }
}