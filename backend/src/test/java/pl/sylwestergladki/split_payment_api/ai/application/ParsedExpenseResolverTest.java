
package pl.sylwestergladki.split_payment_api.ai.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pl.sylwestergladki.split_payment_api.ai.application.model.ExpenseResolutionContext;
import pl.sylwestergladki.split_payment_api.ai.application.model.ParsedExpense;
import pl.sylwestergladki.split_payment_api.ai.application.model.ParsingGroupMember;
import pl.sylwestergladki.split_payment_api.groups.domain.GroupId;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ParsedExpenseResolverTest {

    private ParsedExpenseResolver resolver;

    private GroupId groupId;
    private UserId requesterId;
    private UserId annaId;
    private ExpenseResolutionContext context;

    @BeforeEach
    void setUp() {
        resolver = new ParsedExpenseResolver();

        groupId = new GroupId(UUID.randomUUID());
        requesterId = UserId.generate();
        annaId = UserId.generate();

        context = new ExpenseResolutionContext(
                groupId,
                requesterId,
                List.of(
                        new ParsingGroupMember(requesterId, "John"),
                        new ParsingGroupMember(annaId, "Anna")
                )
        );
    }

    @Test
    void shouldResolveExpenseParticipants() {
        // given
        var parsed = new ParsedExpense(
                groupId.value(),
                "Dinner",
                new BigDecimal("180.00"),
                "PLN",
                "me",
                List.of("me", "Anna")
        );

        // when
        var result = resolver.resolve(parsed, context);

        // then
        assertThat(result.groupId()).isEqualTo(groupId);
        assertThat(result.description()).isEqualTo("Dinner");
        assertThat(result.amount())
                .isEqualByComparingTo("180.00");
        assertThat(result.currency()).isEqualTo("PLN");
        assertThat(result.paidByUserId())
                .isEqualTo(requesterId);
        assertThat(result.participantUserIds())
                .containsExactly(requesterId, annaId);
    }

    @Test
    void shouldRejectUnknownMember() {
        // given
        var parsed = new ParsedExpense(
                groupId.value(),
                "Dinner",
                new BigDecimal("180"),
                "PLN",
                "me",
                List.of("me", "Unknown")
        );

        // when / then
        assertThatThrownBy(() ->
                resolver.resolve(parsed, context)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown group member");
    }

    @Test
    void shouldRejectNegativeAmount() {
        // given
        var parsed = new ParsedExpense(
                groupId.value(),
                "Dinner",
                new BigDecimal("-10"),
                "PLN",
                "me",
                List.of("me", "Anna")
        );

        // when / then
        assertThatThrownBy(() ->
                resolver.resolve(parsed, context)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("amount must be positive");
    }

    @Test
    void shouldRejectAmbiguousMember() {
        // given
        var anotherAnnaId = UserId.generate();

        var ambiguousContext = new ExpenseResolutionContext(
                groupId,
                requesterId,
                List.of(
                        new ParsingGroupMember(requesterId, "John"),
                        new ParsingGroupMember(annaId, "Anna"),
                        new ParsingGroupMember(anotherAnnaId, "Anna")
                )
        );

        var parsed = new ParsedExpense(
                groupId.value(),
                "Dinner",
                new BigDecimal("180"),
                "PLN",
                "me",
                List.of("Anna")
        );

        // when / then
        assertThatThrownBy(() ->
                resolver.resolve(parsed, ambiguousContext)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ambiguous group member");
    }

    @Test
    void shouldRejectDuplicateParticipants() {
        // given
        var parsed = new ParsedExpense(
                groupId.value(),
                "Dinner",
                new BigDecimal("180"),
                "PLN",
                "me",
                List.of("Anna", "Anna")
        );

        // when / then
        assertThatThrownBy(() ->
                resolver.resolve(parsed, context)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(
                        "Duplicate expense participant"
                );
    }

    @Test
    void shouldRejectRequesterNotBelongingToGroup() {
        // given
        var outsiderId = UserId.generate();

        var invalidContext = new ExpenseResolutionContext(
                groupId,
                outsiderId,
                List.of(
                        new ParsingGroupMember(requesterId, "John"),
                        new ParsingGroupMember(annaId, "Anna")
                )
        );

        var parsed = new ParsedExpense(
                groupId.value(),
                "Dinner",
                new BigDecimal("180"),
                "PLN",
                "me",
                List.of("me", "Anna")
        );

        // when / then
        assertThatThrownBy(() ->
                resolver.resolve(parsed, invalidContext)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Requester is not a member of the group"
                );
    }

    @Test
    void shouldRejectDifferentGroupId() {
        // given
        var differentGroupId = new GroupId(UUID.randomUUID());

        var parsed = new ParsedExpense(
                differentGroupId.value(),
                "Dinner",
                new BigDecimal("180"),
                "PLN",
                "me",
                List.of("me", "Anna")
        );

        // when / then
        assertThatThrownBy(() ->
                resolver.resolve(parsed, context)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Parsed group does not match resolution context"
                );
    }

    @Test
    void shouldRejectNullParsedExpense() {
        assertThatThrownBy(() ->
                resolver.resolve(null, context)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("AI returned no expense");
    }

    @Test
    void shouldRejectNullContext() {
        // given
        var parsed = new ParsedExpense(
                groupId.value(),
                "Dinner",
                new BigDecimal("180"),
                "PLN",
                "me",
                List.of("me", "Anna")
        );

        // when / then
        assertThatThrownBy(() ->
                resolver.resolve(parsed, null)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Expense resolution context is required"
                );
    }
}
