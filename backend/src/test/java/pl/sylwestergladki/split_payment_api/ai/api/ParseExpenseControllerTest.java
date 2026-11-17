
package pl.sylwestergladki.split_payment_api.ai.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import pl.sylwestergladki.split_payment_api.ai.application.ParseExpenseUseCase;
import pl.sylwestergladki.split_payment_api.ai.application.model.ResolvedExpense;
import pl.sylwestergladki.split_payment_api.groups.application.FindUserIdByKeycloakSubjectUseCase;
import pl.sylwestergladki.split_payment_api.groups.domain.GroupId;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;
import pl.sylwestergladki.split_payment_api.security.SecurityConfig;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ParseExpenseController.class)
@Import(SecurityConfig.class)
class ParseExpenseControllerTest {

    private static final String KEYCLOAK_SUBJECT =
            "keycloak-user-123";

    private static final String EXPENSE_TEXT =
            "I paid 180 pln for dinner for me and Anna";

    private static final String PARSE_ENDPOINT =
            "/api/expenses/parse";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ParseExpenseUseCase parseExpenseUseCase;

    @MockitoBean
    private FindUserIdByKeycloakSubjectUseCase findUserIdUseCase;

    @Test
    void shouldParseExpense() throws Exception {
        // given
        var groupId = new GroupId(UUID.randomUUID());
        var requesterId = UserId.generate();
        var annaId = UserId.generate();

        var resolvedExpense = new ResolvedExpense(
                groupId,
                "Dinner",
                new BigDecimal("180.00"),
                "PLN",
                requesterId,
                List.of(requesterId, annaId)
        );

        when(findUserIdUseCase.execute(KEYCLOAK_SUBJECT))
                .thenReturn(requesterId);

        when(parseExpenseUseCase.execute(
                eq(requesterId),
                eq(EXPENSE_TEXT)
        )).thenReturn(resolvedExpense);

        // when / then
        mockMvc.perform(post(PARSE_ENDPOINT)
                        .with(jwt().jwt(jwt ->
                                jwt.subject(KEYCLOAK_SUBJECT)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "%s"
                                }
                                """.formatted(EXPENSE_TEXT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupId")
                        .value(groupId.value().toString()))
                .andExpect(jsonPath("$.description")
                        .value("Dinner"))
                .andExpect(jsonPath("$.amount")
                        .value(180.0))
                .andExpect(jsonPath("$.currency")
                        .value("PLN"))
                .andExpect(jsonPath("$.paidBy")
                        .value(requesterId.value().toString()))
                .andExpect(jsonPath("$.participants.length()")
                        .value(2))
                .andExpect(jsonPath("$.participants[0]")
                        .value(requesterId.value().toString()))
                .andExpect(jsonPath("$.participants[1]")
                        .value(annaId.value().toString()));

        verify(findUserIdUseCase)
                .execute(KEYCLOAK_SUBJECT);

        verify(parseExpenseUseCase).execute(
                requesterId,
                EXPENSE_TEXT
        );
    }

    @Test
    void shouldRejectBlankText() throws Exception {
        // when / then
        mockMvc.perform(post(PARSE_ENDPOINT)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "   "
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(parseExpenseUseCase);
        verifyNoInteractions(findUserIdUseCase);
    }

    @Test
    void shouldRejectUnauthenticatedRequest() throws Exception {
        // when / then
        mockMvc.perform(post(PARSE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "Dinner 180 pln"
                                }
                                """))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(parseExpenseUseCase);
        verifyNoInteractions(findUserIdUseCase);
    }

    @Test
    void shouldRejectTextLongerThan1000Characters()
            throws Exception {

        // given
        var text = "a".repeat(1001);

        // when / then
        mockMvc.perform(post(PARSE_ENDPOINT)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "%s"
                                }
                                """.formatted(text)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(parseExpenseUseCase);
        verifyNoInteractions(findUserIdUseCase);
    }

    @Test
    void shouldRejectMissingText() throws Exception {
        // when / then
        mockMvc.perform(post(PARSE_ENDPOINT)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(parseExpenseUseCase);
        verifyNoInteractions(findUserIdUseCase);
    }
}
